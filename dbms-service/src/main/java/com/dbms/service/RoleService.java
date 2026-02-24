package com.dbms.service;

import com.dbms.entity.RoleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dbms.entity.UserEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2022-03-23
 */
public interface RoleService extends IService<RoleEntity> {

    public List<RoleEntity> selectRoleList(RoleEntity roleEntity);
}
