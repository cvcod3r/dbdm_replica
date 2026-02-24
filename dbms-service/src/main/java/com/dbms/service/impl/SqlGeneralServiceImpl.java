package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.SqlGeneralEntity;
import com.dbms.dao.SqlGeneralDao;
import com.dbms.service.SqlGeneralService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class SqlGeneralServiceImpl extends ServiceImpl<SqlGeneralDao, SqlGeneralEntity> implements SqlGeneralService {
    @Autowired
    private SqlGeneralDao generalDao;

    @Override
    public List<SqlGeneralEntity> sqlList() {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("pid",2);
        return generalDao.selectList(queryWrapper);
    }

    @Override
    public List<SqlGeneralEntity> tableList() {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("pid",8);
        return generalDao.selectList(queryWrapper);
    }

    @Override
    public List<SqlGeneralEntity> viewList() {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("pid",16);
        return generalDao.selectList(queryWrapper);
    }

    @Override
    public List<SqlGeneralEntity> funcList() {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("pid",20);
        return generalDao.selectList(queryWrapper);
    }
}
