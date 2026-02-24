package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.constant.Constants;
import com.dbms.constant.UserStatus;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.*;
import com.dbms.page.TableDataInfo;
import com.dbms.service.*;
import com.dbms.utils.*;
import com.dbms.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

import static com.dbms.utils.SecurityUtils.getLimitCount;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private LogLoginService logLoginService;

    @Autowired
    HttpServletRequest request;

    @PostMapping("/login")
    @ResponseBody
    public AjaxResult login(@RequestBody LoginBodyEntity loginBodyEntity)
    {
        QueryWrapper<UserEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(UserEntity.USER_NAME,loginBodyEntity.getUsername());
        UserEntity user = userService.getOne(queryWrapper);
//        System.out.println(JSON.toJSONString(user));
        String ip = IpUtils.getIpAddress(request);
        LogLoginEntity logLoginEntity = new LogLoginEntity();
        logLoginEntity.setIpAddress(ip);
        logLoginEntity.setUserName(loginBodyEntity.getUsername());
        logLoginEntity.setCreatetime(LocalDateTime.now());
        if (StringUtils.isNull(user)) {
            logLoginEntity.setComments("用户名不存在");
            logLoginEntity.setLoginStatus("登陆失败");
            logLoginService.save(logLoginEntity);
            return AjaxResult.error("用户：" + loginBodyEntity.getUsername() + " 不存在");
        }
        else if (UserStatus.DISABLE.getCode().equals(String.valueOf(user.getStatus()))) {
            logLoginEntity.setUserId(user.getUserId());
            logLoginEntity.setRealName(user.getRealname());
            logLoginEntity.setComments("账号停用");
            logLoginEntity.setLoginStatus("登陆失败");
            logLoginService.save(logLoginEntity);
            return AjaxResult.error("对不起，您的账号：" + loginBodyEntity.getUsername() + " 已停用");
        }
        else if(!SecurityUtils.matchesPassword(loginBodyEntity.getPassword(),user.getPassword())){
            logLoginEntity.setUserId(user.getUserId());
            logLoginEntity.setRealName(user.getRealname());
            logLoginEntity.setComments("账号密码不匹配");
            logLoginEntity.setLoginStatus("登陆失败");
            logLoginService.save(logLoginEntity);
            return AjaxResult.error("对不起，您的账号密码不匹配");
        } else {
            AjaxResult ajaxResult = AjaxResult.success();
            // 生成令牌
            loginBodyEntity.setUuid(IdUtils.fastSimpleUUID());
//            System.out.println(JSON.toJSONString(loginBodyEntity));
            String token = loginService.login(loginBodyEntity.getUsername(), loginBodyEntity.getPassword(), loginBodyEntity.getUuid());
            logLoginEntity.setUserId(user.getUserId());
            logLoginEntity.setRealName(user.getRealname());
            logLoginEntity.setComments("账号密码匹配");
            logLoginEntity.setLoginStatus("登陆成功");
            logLoginService.save(logLoginEntity);
            ajaxResult.put(Constants.TOKEN, token);
            return ajaxResult;
        }
    }
    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo()
    {
        UserEntity user = SecurityUtils.getLoginUser().getUser();
        RoleEntity roleEntity=roleService.getById(user.getRoleId());
        QueryWrapper<RoleMenuEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(RoleEntity.ROLE_ID,user.getRoleId());
        List<RoleMenuEntity> roleMenuEntities=roleMenuService.list(queryWrapper);
        List<Integer> idList=new ArrayList<>();
        for(int i=0;i<roleMenuEntities.size();i++){
            idList.add(roleMenuEntities.get(i).getMenuId());
        }
        //角色
        String role = roleEntity.getRoleName();
        //权限集合
        Set<String> permissions=new HashSet<>();
        if (idList.size()>0){
            List<MenuEntity> menuEntities =menuService.listByIds(idList);
            for (int i=0;i<menuEntities.size();i++){
                permissions.add(menuEntities.get(i).getPermission());
            }
        }
//        System.out.println(JSON.toJSONString(permissions));
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", role);
        ajax.put("permissions", permissions);
        ajax.put("limitCount", getLimitCount());
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
//    @GetMapping("/getRouters")
//    public AjaxResult getRouters()
//    {
//        Integer userId = SecurityUtils.getUserId();
//        QueryWrapper<UserEntity> queryWrapper=new QueryWrapper<>();
//        queryWrapper.eq("user_id",userId);
//        UserEntity user = userService.getOne(queryWrapper);
//        QueryWrapper<RoleMenuEntity> queryWrapper1 = new QueryWrapper<>();
//        queryWrapper1.eq("role_id",user.getRoleId());
//        List<RoleMenuEntity> roleMenuEntities=roleMenuService.list(queryWrapper1);
//        List<Integer> idList=new ArrayList<>();
//        for(int i=0;i<roleMenuEntities.size();i++){
//            idList.add(roleMenuEntities.get(i).getMenuId());
//        }
//        List<MenuEntity> menuEntities =menuService.listByIds(idList);
//        menuEntities = menuService.getChildPerms(menuEntities, 0);
//        List<RouterVo> routers = menuService.buildMenus(menuEntities);
//        System.out.println(JSON.toJSONString(routers));
//        return AjaxResult.success(routers);
//    }


    @GetMapping("/listUser")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户", operation = "查询", procedureInfo = "查询用户")
    public TableDataInfo listUser(UserEntity userEntity)
    {
        startPage();
        List<UserEntity> list = userService.selectUserList(userEntity);
        return getDataTable(list);
    }

    @GetMapping("/allUser")
    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户", operation = "查询", procedureInfo = "查询用户")
    public List<UserEntity> allUser()
    {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserEntity.IS_DELETE,0);
        List<UserEntity> res = userService.list(queryWrapper);
        return res;
    }

    @GetMapping("/getUsersByGroupId/{groupId}")
    public AjaxResult getUsersByGroupId(@PathVariable Integer groupId){
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserEntity.GROUP_ID, groupId);
        queryWrapper.eq(UserGroupEntity.IS_DELETE,0);
//        select * from user where group_Id = 1 and is_delete =0;
        List<UserEntity> list = userService.list(queryWrapper);
        return AjaxResult.success(list);
    }


    @GetMapping(value = "/selectById/{userId}")
    public AjaxResult selectById(@PathVariable Integer userId)
    {
        return AjaxResult.success(userService.getById(userId));
    }

    /**
     * 校验用户名是否重复
     * @param userId
     * @param userName
     * @return
     */
    @GetMapping(value = "/checkUserName/{userId}/{userName}")
    public AjaxResult checkUserName(@PathVariable Integer userId, @PathVariable String userName)
    {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (userId != -1){
            queryWrapper.ne(UserEntity.USER_ID,userId);
        }
        queryWrapper.eq(UserEntity.USER_NAME, userName);
        queryWrapper.ne(UserEntity.IS_DELETE, 1);
        List<UserEntity> userEntities = userService.list(queryWrapper);
        if (userEntities != null && userEntities.size() != 0){
            return AjaxResult.success(GlobalMessageUtil.statusSuccess);
        }
        return AjaxResult.success(GlobalMessageUtil.statusFailure);
    }


    /**
     * 新增用户
     */
    @PostMapping(value = "/addUser")
    public AjaxResult add(@Validated @RequestBody UserEntity userEntity)
    {
        //存储创建的用户信息
        userEntity.setPassword(SecurityUtils.encryptPassword(userEntity.getPassword()));
        userEntity.setIsDelete(0);
        userEntity.setCreatetime(LocalDateTime.now());
        userEntity.setRoleName(roleService.getById(userEntity.getRoleId()).getRoleKey());
        return toAjax(userService.save(userEntity));
    }

    /**
     * 修改用户
     */
    @PutMapping(value = "/updateUser")
    public AjaxResult edit(@Validated @RequestBody UserEntity userEntity)
    {
        //存储更改的用户信息
        if(userEntity.getPassword().equals("")){
            return AjaxResult.error();
        }else{
            userEntity.setPassword(SecurityUtils.encryptPassword(userEntity.getPassword()));
        }
        return toAjax(userService.saveOrUpdate(userEntity));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/deleteUser/{userIds}")
    public AjaxResult remove(@PathVariable Integer[] userIds)
    {
        return toAjax(userService.deleteByIds(userIds));
    }

}
