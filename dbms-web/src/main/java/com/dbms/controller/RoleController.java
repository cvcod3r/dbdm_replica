package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.MenuEntity;
import com.dbms.entity.RoleEntity;
import com.dbms.entity.RoleMenuEntity;
import com.dbms.entity.UserEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.RoleMenuService;
import com.dbms.service.RoleService;
import com.dbms.utils.GlobalMessageUtil;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
@RestController
@RequestMapping("/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMenuService roleMenuService;

    @GetMapping("/list")
    public TableDataInfo list(RoleEntity roleEntity)
    {
        startPage();
        List<RoleEntity> list = roleService.selectRoleList(roleEntity);
        return getDataTable(list);
    }

    @GetMapping("/all")
    public List<RoleEntity> all()
    {
        QueryWrapper<RoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete",0);
        List<RoleEntity> res = roleService.list(queryWrapper);
        return res;
    }

    /**
     * 根据角色id获取角色
     * @param roleId
     * @return
     */
    @GetMapping("/getRole/{roleId}")
    public AjaxResult getRole(@PathVariable Integer roleId){
        RoleEntity roleEntity = roleService.getById(roleId);
        return AjaxResult.success(roleEntity);
    }

    /**
     * 添加角色以及角色菜单
     * @param roleEntity
     * @return
     */
    @PostMapping("/addRole")
    public AjaxResult addRole(@RequestBody RoleEntity roleEntity){

        roleEntity.setCreatetime(LocalDateTime.now());
        boolean addFlag = roleService.saveOrUpdate(roleEntity);
//        System.out.println(JSON.toJSONString(roleEntity));
        if (addFlag){
            Integer roleId = roleEntity.getRoleId();
            List<Integer> menuIds = roleEntity.getMenuIds();
            for (Integer menuId:menuIds){
                RoleMenuEntity roleMenuEntity = new RoleMenuEntity();
                roleMenuEntity.setRoleId(roleId);
                roleMenuEntity.setMenuId(menuId);
                roleMenuService.save(roleMenuEntity);
//                System.out.println(JSON.toJSONString(roleMenuEntity));
            }
            return AjaxResult.success(GlobalMessageUtil.addSuccess);
        }
        return AjaxResult.error(GlobalMessageUtil.opFailure);
    }

    /**
     * 删除角色，逻辑删除
     * @param roleId
     * @return
     */
    @GetMapping("/delRole/{roleId}")
    public AjaxResult delRole(@PathVariable Integer roleId){
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleId(roleId);
        roleEntity.setIsDelete(1);
        boolean delFlag = roleService.saveOrUpdate(roleEntity);
        if (delFlag){
            return AjaxResult.success(GlobalMessageUtil.dropSuccess);
        }
        return AjaxResult.error(GlobalMessageUtil.dropFailure);
    }

    /**
     * 获取角色权限
     * @param roleId
     * @return
     */
    @GetMapping("/getRoleMenus/{roleId}")
    public AjaxResult getRoleMenus(@PathVariable Integer roleId){
        QueryWrapper<RoleMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id",roleId);
        List<RoleMenuEntity> roleMenuEntities = roleMenuService.list(queryWrapper);
//        System.out.println(JSON.toJSONString(roleMenuEntities));
        List<Integer> menuCheckedIds = new ArrayList<>();
        for (RoleMenuEntity roleMenuEntity:roleMenuEntities){
//            System.out.println(JSON.toJSONString(roleMenuEntity));
            menuCheckedIds.add(roleMenuEntity.getMenuId());
        }
//        System.out.println(menuCheckedIds);
        return AjaxResult.success(menuCheckedIds);
    }

    /**
     * 修改角色权限
     * @param roleEntity
     * @return
     */
    @RequestMapping("/updateRoleMenus")
    public AjaxResult updateRoleMenus(@RequestBody RoleEntity roleEntity){
        List<Integer> roleMenuIds = roleEntity.getMenuIds();
        try{
            roleService.saveOrUpdate(roleEntity);
            if (roleMenuIds!=null&&roleMenuIds.size()>0){
                QueryWrapper<RoleMenuEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("role_id", roleEntity.getRoleId());
                roleMenuService.remove(queryWrapper);
                RoleMenuEntity roleMenuEntity = new RoleMenuEntity();
                roleMenuEntity.setRoleId(roleEntity.getRoleId());
                for (Integer menuId:roleMenuIds){
                    roleMenuEntity.setMenuId(menuId);
                    roleMenuService.save(roleMenuEntity);
                }
            }
        }catch (Exception e){
            return AjaxResult.error(GlobalMessageUtil.editFailure);
        }
        return AjaxResult.success(GlobalMessageUtil.editSuccess);
    }
}
