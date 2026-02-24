package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.dao.LogRiskAlertDao;
import com.dbms.dao.UnusualBehaviorDao;
import com.dbms.entity.*;
import com.dbms.dao.LogUnusualBehaviorDao;
import com.dbms.service.LogRiskAlertService;
import com.dbms.service.LogUnusualBehaviorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.service.UserService;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static com.dbms.utils.SecurityUtils.getUserId;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author HLH
 * @since 2023-06-15
 */
@Service
public class LogUnusualBehaviorServiceImpl extends ServiceImpl<LogUnusualBehaviorDao, LogUnusualBehaviorEntity> implements LogUnusualBehaviorService {

    @Autowired
    UserService userService;
    @Autowired
    LogUnusualBehaviorDao logUnusualBehaviorDao;

    @Autowired
    UnusualBehaviorDao unusualBehaviorDao;

    @Override
    public List<LogUnusualBehaviorEntity> selectLogList(LogUnusualBehaviorEntity logUnusualBehaviorEntity) {
        QueryWrapper<LogUnusualBehaviorEntity> queryWrapper=new QueryWrapper<>();

        if(StringUtils.isNotNull(logUnusualBehaviorEntity.getUsername())){
            queryWrapper.like(LogUnusualBehaviorEntity.USERNAME,logUnusualBehaviorEntity.getUsername());
        }
        if(StringUtils.isNotNull(logUnusualBehaviorEntity.getUrl())){
            queryWrapper.like(LogUnusualBehaviorEntity.URL,logUnusualBehaviorEntity.getUrl());
        }
        queryWrapper.orderByDesc("CREATETIME");
        return logUnusualBehaviorDao.selectList(queryWrapper);
    }

    @Override
    public void timeLimitCheck(DbaseEntity dbaseEntity) throws ParseException {
        System.out.println("进行时间限制检查");
        // 先拿当前角色下，启用状态的时间限制的最新配置
//        System.out.println("userId: " + getUserId());
        QueryWrapper<UnusualBehaviorEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(UnusualBehaviorEntity.ALTER_TYPE,"timeLimit");
        queryWrapper.eq(UnusualBehaviorEntity.USER_ID,getUserId());
        queryWrapper.eq(UnusualBehaviorEntity.STATUS,0);
        List<UnusualBehaviorEntity> res = unusualBehaviorDao.selectList(queryWrapper);
//        System.out.println(res);
        if(res.size()==0) {
//            System.out.println("未进行时间异常限制");
            return;
        }
        String time = res.get(res.size()-1).getTimeLimit();
//        System.out.println("timeLimit: " + time);
        // 将时间变回Date类型方便比较
        String[] timeList = time.split(",");
//        System.out.println("timeList: " + timeList);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        Date date1 = dateFormat.parse(timeList[0]);
        Date date2 = dateFormat.parse(timeList[1]);
//        System.out.println("date1: " + date1);
//        System.out.println("date2: " + date2);
        Date now = new Date(System.currentTimeMillis());
        System.out.println("now: " + now);
        // 比较时间，now必须在time1之后又在time2之前
        LocalTime time1 = LocalTime.of(date1.getHours(), date1.getMinutes(), date1.getSeconds());
        LocalTime time2 = LocalTime.of(date2.getHours(), date2.getMinutes(), date2.getSeconds());
        LocalTime nowTime = LocalTime.of(now.getHours(), now.getMinutes(), now.getSeconds());
        if(time1.isBefore(nowTime) && time2.isAfter(nowTime)){
            System.out.println("nowTime在time1之后,nowTime在time2之前");
        }else {
            // 不在时间规定范围内则
            System.out.println("不在规定时间内，触发时间限制告警");
            LogUnusualBehaviorEntity logUnusualBehaviorEntity = new LogUnusualBehaviorEntity();
            logUnusualBehaviorEntity.setUserId(getUserId());
            //根据userID去查用户名
            QueryWrapper<UserEntity> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq(UserEntity.IS_DELETE,0);
            queryWrapper2.eq(UserEntity.USER_ID,getUserId());
            List<UserEntity> r = userService.list(queryWrapper2);
            logUnusualBehaviorEntity.setUsername(r.get(0).getUserName());
            logUnusualBehaviorEntity.setUrl(dbaseEntity.getUrl());
            logUnusualBehaviorEntity.setCreatetime(LocalDateTime.now());
            logUnusualBehaviorEntity.setAlterType("timeLimit");
            logUnusualBehaviorDao.insert(logUnusualBehaviorEntity);
        }
    }

}
