package com.dbms.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dbms.constant.DeleteStatus;
import com.dbms.constant.UserStatus;
import com.dbms.entity.*;
import com.dbms.exception.ServiceException;
import com.dbms.service.*;
import com.dbms.utils.SecurityUtils;
import com.dbms.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户验证处理
 *
 * @author
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        QueryWrapper<UserEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(UserEntity.USER_NAME,username);
        UserEntity user = userService.getOne(queryWrapper);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        }
        else if (DeleteStatus.DELETED.getCode().equals(user.getIsDelete()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        return createLoginUser(user);
    }

    public UserDetails createLoginUser(UserEntity user)
    {
//        System.out.println(user.getUserId()+" :: "+user.getRoleId());
        QueryWrapper<RoleMenuEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(RoleEntity.ROLE_ID,user.getRoleId());
        List<RoleMenuEntity> roleMenuEntities=roleMenuService.list(queryWrapper);
        List<Integer> idList=new ArrayList<>();
        for (RoleMenuEntity roleMenuEntity : roleMenuEntities) {
            idList.add(roleMenuEntity.getMenuId());
        }
        //权限集合
        Set<String> permissions=new HashSet<>();
        //限制条数
        int limit = -1;
        if (idList.size()>0){
            List<MenuEntity> menuEntities =menuService.listByIds(idList);
            for (MenuEntity menuEntity : menuEntities) {
                String permission = menuEntity.getPermission();
                permissions.add(permission);
                if (menuEntity.getPermission().contains("limit")) {
                    String[] strings = permission.split(":");
                    String limitStr = strings[strings.length-1];
                    limit = Integer.parseInt(limitStr);
                }
            }
        }
        return new LoginUser(user.getUserId(), user.getRoleId(), user, permissions, limit);
    }


}
