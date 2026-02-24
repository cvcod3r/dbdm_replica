package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.UserGroupEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.UserGroupService;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.StringUtils;
import com.dbms.annotation.SysLogAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/user-group")
public class UserGroupController extends BaseController {

    @Autowired
    private UserGroupService userGroupService;

    @GetMapping("/listGroup")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "查询", procedureInfo = "查询用户组")
    public TableDataInfo listGroup(UserGroupEntity userGroupEntity)
    {
        startPage();
        QueryWrapper<UserGroupEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(userGroupEntity.getGroupName())) {
            queryWrapper.like(UserGroupEntity.GROUP_NAME,userGroupEntity.getGroupName());
        }
        if(StringUtils.isNotNull(userGroupEntity.getStatus())){
            queryWrapper.like(UserGroupEntity.STATUS,userGroupEntity.getStatus());
        }
        queryWrapper.eq(UserGroupEntity.IS_DELETE,0);
        List<UserGroupEntity> list = userGroupService.list(queryWrapper);
        return getDataTable(list);
    }

    @GetMapping("/allGroup")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "获取", procedureInfo = "获取用户组列表")
    public AjaxResult allGroup()
    {
        QueryWrapper<UserGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserGroupEntity.IS_DELETE,0);
        List<UserGroupEntity> res = userGroupService.list(queryWrapper);
        return AjaxResult.success(res);
    }

    @GetMapping(value = "/selectById/{groupId}")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "查询", procedureInfo = "根据Id查询用户组")
    public AjaxResult selectById(@PathVariable Integer groupId)
    {
        return AjaxResult.success(userGroupService.getById(groupId));
    }

    /**
     * 校验用户组名是否重复
     * @param groupId
     * @param groupName
     * @return
     */
    @GetMapping(value = "/checkGroupName/{groupId}/{groupName}")
    public AjaxResult checkGroupName(@PathVariable Integer groupId, @PathVariable String groupName)
    {
        QueryWrapper<UserGroupEntity> queryWrapper = new QueryWrapper<>();
        if (groupId != -1){
            queryWrapper.ne(UserGroupEntity.GROUP_ID,groupId);
        }
        queryWrapper.eq(UserGroupEntity.GROUP_NAME, groupName);
        queryWrapper.ne(UserGroupEntity.IS_DELETE, 1);
        List<UserGroupEntity> userGroupEntities = userGroupService.list(queryWrapper);
        if (userGroupEntities != null && userGroupEntities.size() != 0){
            return AjaxResult.success(GlobalMessageUtil.statusSuccess);
        }
        return AjaxResult.success(GlobalMessageUtil.statusFailure);
    }


    /**
     * 新增用户组
     */
    @PostMapping(value = "/addGroup")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "新增", procedureInfo = "新增用户组")
    public AjaxResult add(@Validated @RequestBody UserGroupEntity userGroupEntity)
    {
        //存储创建的用户信息
        userGroupEntity.setIsDelete(0);
        userGroupEntity.setCreatetime(LocalDateTime.now());
        return toAjax(userGroupService.save(userGroupEntity));
    }

    /**
     * 修改用户组
     */
    @PutMapping(value = "/updateGroup")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "修改", procedureInfo = "修改用户组")
    public AjaxResult edit(@Validated @RequestBody UserGroupEntity userGroupEntity)
    {
        //存储更改的用户信息
        userGroupEntity.setUpdatetime(LocalDateTime.now());
        return toAjax(userGroupService.saveOrUpdate(userGroupEntity));
    }

    /**
     * 删除用户组
     */
    @DeleteMapping("/deleteGroup/{groupIds}")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "删除", procedureInfo = "删除用户组")
    public AjaxResult remove(@PathVariable Integer[] groupIds)
    {
        return toAjax(userGroupService.deleteByIds(groupIds));
    }

}
