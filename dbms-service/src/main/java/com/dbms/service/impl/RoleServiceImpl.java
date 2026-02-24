package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.RoleEntity;
import com.dbms.dao.RoleDao;
import com.dbms.entity.UserEntity;
import com.dbms.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleDao, RoleEntity> implements RoleService {

    @Autowired
    private RoleDao roleDao;
    @Override
    public List<RoleEntity> selectRoleList(RoleEntity roleEntity) {
        QueryWrapper<RoleEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(roleEntity.getRoleName())) {
            queryWrapper.like("role_name",roleEntity.getRoleName());
        }
        else if(StringUtils.isNotNull(roleEntity.getStatus())){
            queryWrapper.like("status",roleEntity.getStatus());
        }
        queryWrapper.eq("is_delete",0);
        return roleDao.selectList(queryWrapper);
    }
}
