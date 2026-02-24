package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.LogSystemEntity;
import com.dbms.dao.LogSystemDao;
import com.dbms.service.LogSystemService;
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
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Service
public class LogSystemServiceImpl extends ServiceImpl<LogSystemDao, LogSystemEntity> implements LogSystemService {

    @Autowired
    LogSystemDao logSystemDao;

    @Override
    public List<LogSystemEntity> selectLogList(LogSystemEntity logSystemEntity) {
        QueryWrapper<LogSystemEntity> queryWrapper=new QueryWrapper<>();

        if(StringUtils.isNotNull(logSystemEntity.getUserName())){
            queryWrapper.like(LogSystemEntity.USER_NAME,logSystemEntity.getUserName());
        }
        if(StringUtils.isNotNull(logSystemEntity.getRealName())){
            queryWrapper.like(LogSystemEntity.REAL_NAME,logSystemEntity.getRealName());
        }
        if(StringUtils.isNotNull(logSystemEntity.getModuleName())){
            queryWrapper.like(LogSystemEntity.MODULE_NAME,logSystemEntity.getModuleName());
        }
        queryWrapper.orderByDesc("CREATETIME");
        return logSystemDao.selectList(queryWrapper);
    }

}
