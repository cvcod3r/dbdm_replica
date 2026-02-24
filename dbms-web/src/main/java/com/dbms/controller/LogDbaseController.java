package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.bean.DbaseOpBean;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.LogDbaseEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.LogDbaseService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dbms.service.*;
import java.time.LocalDateTime;
import java.util.*;

import static com.dbms.utils.SecurityUtils.*;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/log-dbase")
public class LogDbaseController extends BaseController {

    @Autowired
    private LogDbaseService logDbaseService;

    @Autowired
    private DataBaseUtilService dataBaseUtilService;

    @Autowired
    private DbaseService dbaseService;

    @GetMapping("/list")
    public TableDataInfo list(LogDbaseEntity logDbaseEntity)
    {
        startPage();
        List<LogDbaseEntity> list = logDbaseService.selectLogList(logDbaseEntity);
        return getDataTable(list);
    }


    @RequestMapping("/accessMonitorSession/{dbId}")
    public AjaxResult accessMonitorSession(@PathVariable Integer dbId){
        Integer sessionAccount = dataBaseUtilService.countConnectionSession(dbId);
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.put("sessionAccount", sessionAccount);
        return ajaxResult;
    }

    @RequestMapping("/accessMonitorSqlTypeCount/{dbId}/{dbType}/{beginTime}/{endTime}")
    public AjaxResult accessMonitorSqlTypeCount(@PathVariable Integer dbId,@PathVariable String dbType, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        AjaxResult ajaxResult = new AjaxResult();
        List<Map<String, Object>> resultList = logDbaseService.getSqlTypeCountMonitor(dbId, dbType,beginTime, endTime);
        ajaxResult.put("resultList", resultList);
        return ajaxResult;
    }

    @GetMapping("/accessMonitorDataCount/{dbId}/{beginTime}/{endTime}")
    public AjaxResult accessMonitorDataCount(@PathVariable Integer dbId, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        AjaxResult ajaxResult = new AjaxResult();
//        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        System.out.println();
//        LocalDateTime exTime=dbaseEntity.getCreatetime();
        System.out.println("时间"+beginTime);
        Long dataCount = logDbaseService.getSelectDataCount(dbId, beginTime, endTime);
        ajaxResult.put("selectDataCount", dataCount);
        return ajaxResult;
    }


    // TODO: 分析用户对数据库的操作类型，包括查询、修改、删除等，以便管理员能够及时发现异常操作行为
    @GetMapping("/getMonitorUserOperationType/{userId}/{dbId}/{beginTime}/{endTime}")
    public AjaxResult getMonitorUserOperationType(@PathVariable Integer userId, @PathVariable Integer dbId, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        try {
            AjaxResult ajaxResult = new AjaxResult();
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
                    .eq(LogDbaseEntity.DB_ID, dbId)
                    .groupBy(LogDbaseEntity.SQL_OPERATION)
                    .select(LogDbaseEntity.SQL_OPERATION, "count(*) as COUNT")
                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime);
            List<Map<String, Object>> resultList = logDbaseService.listMaps(queryWrapper);
            // 过滤操作类型为空的数据行, 避免抛异常
            resultList.removeIf(map -> map.getOrDefault(LogDbaseEntity.SQL_OPERATION, null) == null);
            ajaxResult.put("resultList", resultList);
            return ajaxResult;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取用户操作类型信息失败");
        }
    }

    // TODO 操作频率：分析用户对数据库的操作频率，包括每日、每周、每月的操作次数，以便管理员能够了解用户的操作习惯和行为模式
    // TODO 操作频率：每日统计
    @GetMapping("/getMonitorUserOperationDayFrequency/{userId}/{dbId}/{beginTime}/{endTime}")
    public AjaxResult getMonitorUserOperationDayFrequency(@PathVariable Integer userId, @PathVariable Integer dbId, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        try {
            AjaxResult ajaxResult = new AjaxResult();
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
                    .eq(LogDbaseEntity.DB_ID, dbId)
                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime) // 时间范围查询
                    .groupBy("date_format("+ LogDbaseEntity.CREATETIME + ", '%Y-%m-%d')")
                    .select("date_format("+ LogDbaseEntity.CREATETIME + ", '%Y-%m-%d') as TIME", "count(*) as COUNT");
            List<Map<String, Object>> dayList = logDbaseService.listMaps(queryWrapper);
            ajaxResult.put("dayList", dayList);
            return ajaxResult;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取用户每日操作次数统计信息失败");
        }
    }

    // TODO 操作频率：每周统计
    @GetMapping("/getMonitorUserOperationWeekFrequency/{userId}/{dbId}/{beginTime}/{endTime}")
    public AjaxResult getMonitorUserOperationWeekFrequency(@PathVariable Integer userId, @PathVariable Integer dbId, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        try {
            AjaxResult ajaxResult = new AjaxResult();
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
                    .eq(LogDbaseEntity.DB_ID, dbId)
                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime) // 时间范围查询
                    .groupBy("date_format("+ LogDbaseEntity.CREATETIME + ", '%Y-%u')")
                    .select("date_format("+ LogDbaseEntity.CREATETIME + ", '%Y-%u') as TIME", "count(*) as COUNT");
            List<Map<String, Object>> weekList = logDbaseService.listMaps(queryWrapper);
            ajaxResult.put("weekList", weekList);
            return ajaxResult;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取用户每周操作次数统计信息失败");
        }
    }

    // TODO 操作频率：每月统计
    @GetMapping("/getMonitorUserOperationMonthFrequency/{userId}/{dbId}/{beginTime}/{endTime}")
    public AjaxResult getMonitorUserOperationMonthFrequency(@PathVariable Integer userId, @PathVariable Integer dbId, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        try {
            AjaxResult ajaxResult = new AjaxResult();
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
                    .eq(LogDbaseEntity.DB_ID, dbId)
                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime) // 时间范围查询
                    .groupBy("date_format("+ LogDbaseEntity.CREATETIME + ", '%Y-%m')")
                    .select("date_format("+ LogDbaseEntity.CREATETIME + ", '%Y-%m') as TIME", "count(*) as COUNT");
            List<Map<String, Object>> monthList = logDbaseService.listMaps(queryWrapper);
            ajaxResult.put("monthList", monthList);
            return ajaxResult;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取用户每月操作次数统计信息失败");
        }
    }

    // TODO 操作时间：分析用户对数据库的操作时间，包括每日、每周、每月的操作时间段，以便管理员能够了解用户的工作习惯和行为模式

    // TODO 操作时间：每日统计不同时间段的平均操作次数
    @GetMapping("/getMonitorUserOperationDayTime/{userId}/{beginTime}/{endTime}")
    public AjaxResult getMonitorUserOperationDayTime(@PathVariable Integer userId, @PathVariable Date beginTime, @PathVariable Date
            endTime) {
        try {
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime) // 时间范围查询
                    .groupBy("date_format("+ LogDbaseEntity.CREATETIME + ", '%H')")
                    .select("date_format("+ LogDbaseEntity.CREATETIME + ", '%H') as TIME", "count(*) as COUNT"); // 使用 avg 聚合函数

            List<Map<String, Object>> dayTimeList = logDbaseService.listMaps(queryWrapper);
            // 补充map中的时间段信息
            Map<String, Integer> map = new HashMap<>();
            for (int i = 0; i < 24; i+=2) {
                // 键的格式为00:00-02:00, 每两个小时为一个时间段
                String time1 = i < 10 ? "0" + i : "" + i;
                String time2 = (i+2) < 10 ? "0" + (i+2) : "" + (i+2);
                map.put(time1 + ":00-" + time2 + ":00", 0);
            }
            for (Map<String, Object> dayMap : dayTimeList) {
                // 键值是 00, 01, 02, 03, 04, 05, 06, 07, 08, 09，一直到 23,但是中间可能会缺失,我想将00, 01统计在一起，也就是两小时的
                // 一段统计在一起，所以需要将键值转换为00:00-02:00, 02:00-04:00, 04:00-06:00, 06:00-08:00, 08:00-10:00, 10:00-12:00,
                // 12:00-14:00, 14:00-16:00, 16:00-18:00, 18:00-20:00, 20:00-22:00, 22:00-00:00
                String time = (String) dayMap.get("TIME");
                switch (time){
                    case "00":
                        Integer count = map.get("00:00-02:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("00:00-02:00", count);
                        break;
                    case "01":
                        Integer count1 = map.get("00:00-02:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("00:00-02:00", count1);
                        break;
                    case "02":
                        Integer count2 = map.get("02:00-04:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("02:00-04:00", count2);
                        break;
                    case "03":
                        Integer count3 = map.get("02:00-04:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("02:00-04:00", count3);
                        break;
                    case "04":
                        Integer count4 = map.get("04:00-06:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("04:00-06:00", count4);
                        break;
                    case "05":
                        Integer count5 = map.get("04:00-06:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("04:00-06:00", count5);
                        break;
                    case "06":
                        Integer count6 = map.get("06:00-08:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("06:00-08:00", count6);
                        break;
                    case "07":
                        Integer count7 = map.get("06:00-08:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("06:00-08:00", count7);
                        break;
                    case "08":
                        Integer count8 = map.get("08:00-10:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("08:00-10:00", count8);
                        break;
                    case "09":
                        Integer count9 = map.get("08:00-10:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("08:00-10:00", count9);
                        break;
                    case "10":
                        Integer count10 = map.get("10:00-12:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("10:00-12:00", count10);
                        break;
                    case "11":
                        Integer count11 = map.get("10:00-12:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("10:00-12:00", count11);
                        break;
                    case "12":
                        Integer count12 = map.get("12:00-14:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("12:00-14:00", count12);
                        break;
                    case "13":
                        Integer count13 = map.get("12:00-14:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("12:00-14:00", count13);
                        break;
                    case "14":
                        Integer count14 = map.get("14:00-16:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("14:00-16:00", count14);
                        break;
                    case "15":
                        Integer count15 = map.get("14:00-16:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("14:00-16:00", count15);
                        break;
                    case "16":
                        Integer count16 = map.get("16:00-18:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("16:00-18:00", count16);
                        break;
                    case "17":
                        Integer count17 = map.get("16:00-18:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("16:00-18:00", count17);
                        break;
                    case "18":
                        Integer count18 = map.get("18:00-20:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("18:00-20:00", count18);
                        break;
                    case "19":
                        Integer count19 = map.get("18:00-20:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("18:00-20:00", count19);
                        break;
                    case "20":
                        Integer count20 = map.get("20:00-22:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("20:00-22:00", count20);
                        break;
                    case "21":
                        Integer count21 = map.get("20:00-22:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("20:00-22:00", count21);
                        break;
                    case "22":
                        Integer count22 = map.get("22:00-24:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("22:00-24:00", count22);
                        break;
                    case "23":
                        Integer count23 = map.get("22:00-24:00") + Integer.parseInt(dayMap.get("COUNT").toString());
                        map.put("22:00-24:00", count23);
                        break;
                    default:
                        break;
                }
            }
            System.out.println(JSON.toJSONString(map));
            AjaxResult ajaxResult = new AjaxResult();
            ajaxResult.put("dayTimeList", map);
            return ajaxResult;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取用户每日操作时间段统计信息失败");
        }
    }

    // TODO 操作时间：每周统计
    @GetMapping("/getMonitorUserOperationWeekTime/{userId}/{beginTime}/{endTime}")
    public AjaxResult getMonitorUserOperationWeekTime(@PathVariable Integer userId, @PathVariable Date beginTime, @PathVariable Date
            endTime){
        try {
            AjaxResult ajaxResult = new AjaxResult();
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime) // 时间范围查询
                    .groupBy("DAYOFWEEK("+ LogDbaseEntity.CREATETIME + ")")  // 按照周几进行分组
                    .select("CASE DAYOFWEEK("+ LogDbaseEntity.CREATETIME + ") " +
                            "WHEN 1 THEN '周日' WHEN 2 THEN '周一' WHEN 3 THEN '周二' " +
                            "WHEN 4 THEN '周三' WHEN 5 THEN '周四' WHEN 6 THEN '周五' " +
                            "WHEN 7 THEN '周六' END AS WEEKDAY", "COUNT(*) AS COUNT"); // 查询周几和数量
            List<Map<String, Object>> weekTimeList = logDbaseService.listMaps(queryWrapper);
            ajaxResult.put("weekTimeList", weekTimeList);
            System.out.println("weekTimeList");
            System.out.println(JSON.toJSONString(weekTimeList));
            return ajaxResult;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取用户每周操作时间段统计信息失败");
        }
    }

//    // TODO 操作时间：每月统计
//    @GetMapping("/getMonitorUserOperationMonthTime/{userId}/{dbId}/{beginTime}/{endTime}")
//    public AjaxResult getMonitorUserOperationMonthTime(@PathVariable Integer userId, @PathVariable Integer dbId, @PathVariable Date beginTime, @PathVariable Date
//            endTime){
//        try {
//            AjaxResult ajaxResult = new AjaxResult();
//            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq(LogDbaseEntity.USER_ID, userId)
//                    .eq(LogDbaseEntity.DB_ID, dbId)
//                    .between(LogDbaseEntity.CREATETIME, beginTime, endTime) // 时间范围查询
//                    .groupBy("date_format("+ LogDbaseEntity.CREATETIME + ", '%d')")
//                    .select("date_format("+ LogDbaseEntity.CREATETIME + ", '%d') as TIME", "count(*) as COUNT");
//            List<Map<String, Object>> monthTimeList = logDbaseService.listMaps(queryWrapper);
//            ajaxResult.put("monthTimeList", monthTimeList);
//            return ajaxResult;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return AjaxResult.error("获取用户每月操作时间段统计信息失败");
//        }
//    }

}
