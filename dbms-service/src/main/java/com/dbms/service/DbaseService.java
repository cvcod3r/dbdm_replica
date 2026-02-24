package com.dbms.service;

import com.dbms.entity.DbaseEntity;
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
public interface DbaseService extends IService<DbaseEntity> {

    List<DbaseEntity> selectDbaseList(DbaseEntity dbaseEntity);

}
