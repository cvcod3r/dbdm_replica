package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.LogLoginEntity;
import com.dbms.dao.LogLoginDao;
import com.dbms.service.LogLoginService;
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
public class LogLoginServiceImpl extends ServiceImpl<LogLoginDao, LogLoginEntity> implements LogLoginService {


    @Autowired
    private LogLoginDao logLoginDao;

    @Override
    public List<LogLoginEntity> selectLogList(LogLoginEntity logLoginEntity) {
        QueryWrapper<LogLoginEntity> queryWrapper=new QueryWrapper<>();

        if(StringUtils.isNotNull(logLoginEntity.getUserName())){
            queryWrapper.like(LogLoginEntity.USER_NAME,logLoginEntity.getUserName());
        }
        if(StringUtils.isNotNull(logLoginEntity.getRealName())){
            queryWrapper.like(LogLoginEntity.REAL_NAME,logLoginEntity.getRealName());
        }
        queryWrapper.orderByDesc("CREATETIME");
        return logLoginDao.selectList(queryWrapper);
    }
}
