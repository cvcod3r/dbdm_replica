package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.dao.MenuDao;
import com.dbms.entity.LoginUser;
import com.dbms.entity.MenuEntity;
import com.dbms.service.MenuService;
import com.dbms.utils.SecurityUtils;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.dbms.utils.SecurityUtils.getLimitCount;
import static com.dbms.utils.SecurityUtils.getUserId;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2022-04-11
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取菜单树
     * @return 菜单树
     */
    @RequestMapping("/getMenus")
    public AjaxResult getMenus(){
        List<MenuEntity> menusBase = menuService.list(new QueryWrapper<MenuEntity>()
                .eq(MenuEntity.PARENT_ID,0)
                .eq(MenuEntity.MENU_TYPE,"M")
        );
        List<MenuEntity> menus = menuService.list(new QueryWrapper<MenuEntity>()
                .ne(MenuEntity.PARENT_ID,0)
                .eq(MenuEntity.MENU_TYPE,"C")
        );
        for (MenuEntity menuEntity:menusBase){
            List<MenuEntity> menuList = buildMenus(menus, menuEntity.getMenuId());
            menuEntity.setChildren(menuList);
        }
        System.out.println(JSON.toJSONString(menusBase));
        return AjaxResult.success(menusBase);
    }

    /**
     * 获取操作权限，树结构
     * @return
     */
    @RequestMapping("/getOpPermissions")
    public AjaxResult getOpPermissions(){
        List<MenuEntity> menusBase = menuService.list(new QueryWrapper<MenuEntity>()
                .eq(MenuEntity.PARENT_ID,0)
                .eq(MenuEntity.MENU_TYPE,"D")
        );
        List<MenuEntity> menus = menuService.list(new QueryWrapper<MenuEntity>()
                .ne(MenuEntity.PARENT_ID,0)
                .eq(MenuEntity.MENU_TYPE,"P")
        );
        for (MenuEntity menuEntity:menusBase){
            List<MenuEntity> menuList = buildMenus(menus,menuEntity.getMenuId());
            menuEntity.setChildren(menuList);
        }
//        System.out.println(JSON.toJSONString(menusBase));
        return AjaxResult.success(menusBase);
    }

    /**
     * 获取DML,SQL限制条数
     * @return
     */
    @RequestMapping("/getLimit")
    public AjaxResult getLimit(){
        List<MenuEntity> menus = menuService.list(new QueryWrapper<MenuEntity>().eq(MenuEntity.PARENT_ID,-1));
        return AjaxResult.success(menus);
    }

    /**
     * 递归构建菜单树
     * @param menus
     * @param parentId
     * @return
     */
    public List<MenuEntity> buildMenus(List<MenuEntity> menus, Integer parentId){
        List<MenuEntity> result = new ArrayList<>();
        for (MenuEntity menu : menus) {
            //菜单id
            Integer menuId = menu.getMenuId();
            //判断父id是否匹配
            if(menu.getParentId().equals(parentId)){
                List<MenuEntity> childMenu = buildMenus(menus,menuId);
                menu.setChildren(childMenu);
                result.add(menu);
            }
        }
        return result;
    }
}
