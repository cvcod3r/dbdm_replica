package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.UserEntity;
import com.dbms.dao.UserDao;
import com.dbms.service.UserService;
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
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public List<UserEntity> selectUserList(UserEntity userEntity) {
        QueryWrapper<UserEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(userEntity.getUserName())) {
            queryWrapper.like(UserEntity.USER_NAME,userEntity.getUserName());
        }
        if(StringUtils.isNotNull(userEntity.getRoleName())){
            queryWrapper.like(UserEntity.ROLE_NAME,userEntity.getRoleName());
        }
        if(StringUtils.isNotNull(userEntity.getStatus())){
            queryWrapper.like(UserEntity.STATUS,userEntity.getStatus());
        }
        queryWrapper.eq(UserEntity.IS_DELETE,0);
        return userDao.selectList(queryWrapper);
    }

    @Override
    public boolean deleteByIds(Integer[] userIds) {
        if (userIds.length==0){
            return false;
        }else{
            for (int i=0;i<userIds.length;i++){
                UserEntity userEntity=userDao.selectById(userIds[i]);
                userEntity.setIsDelete(1);
                userDao.updateById(userEntity);
            }
        }
        return true;
    }

}
