package com.dbms.dao;

import com.dbms.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author
 * @since 2022-03-29
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

}
