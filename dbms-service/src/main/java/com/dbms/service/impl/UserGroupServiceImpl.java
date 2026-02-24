package com.dbms.service.impl;

import com.dbms.entity.UserEntity;
import com.dbms.entity.UserGroupEntity;
import com.dbms.dao.UserGroupDao;
import com.dbms.service.UserGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupDao, UserGroupEntity> implements UserGroupService {

    @Autowired
    private UserGroupDao userGroupDao;

    @Override
    public boolean deleteByIds(Integer[] groupIds) {
        if (groupIds.length==0){
            return false;
        }else{
            for (Integer groupId : groupIds) {
                UserGroupEntity userGroupEntity = userGroupDao.selectById(groupId);
                userGroupEntity.setIsDelete(1);
                userGroupDao.updateById(userGroupEntity);
            }
        }
        return true;
    }
}
