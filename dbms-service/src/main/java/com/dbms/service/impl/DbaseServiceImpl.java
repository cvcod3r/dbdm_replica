package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.DbaseEntity;
import com.dbms.dao.DbaseDao;
import com.dbms.service.DbaseService;
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
public class DbaseServiceImpl extends ServiceImpl<DbaseDao, DbaseEntity> implements DbaseService {

    @Autowired
    private DbaseDao dbaseDao;

    @Override
    public List<DbaseEntity> selectDbaseList(DbaseEntity dbaseEntity) {
        QueryWrapper<DbaseEntity> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNotNull(dbaseEntity.getUrl())){
            queryWrapper.like(DbaseEntity.URL,dbaseEntity.getUrl());
        }
        if(StringUtils.isNotNull(dbaseEntity.getConnName())){
            queryWrapper.like(DbaseEntity.CONN_NAME,dbaseEntity.getConnName());
        }
        queryWrapper.eq(DbaseEntity.IS_DELETE,0);
        return dbaseDao.selectList(queryWrapper);
    }
}
