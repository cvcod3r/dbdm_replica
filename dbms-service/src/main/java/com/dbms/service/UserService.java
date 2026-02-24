package com.dbms.service;

import com.dbms.entity.UserEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
public interface UserService extends IService<UserEntity> {
    public List<UserEntity> selectUserList(UserEntity userEntity);

    public boolean deleteByIds(Integer[] userIds);
}
