package com.dbms.domain;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.dao.LogDbaseDao;
import com.dbms.dao.LogUnusualBehaviorDao;
import com.dbms.dao.UnusualBehaviorDao;
import com.dbms.dao.UserDao;
import com.dbms.entity.LogDbaseEntity;
import com.dbms.entity.LogUnusualBehaviorEntity;
import com.dbms.entity.UnusualBehaviorEntity;
import com.dbms.entity.UserEntity;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.dbms.utils.SecurityUtils.getUserId;

@Slf4j
@Component
public class ScheduleTask {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${monitor.filePath}")
    private String filePath;

    @Autowired
    private DataBaseUtilService dataBaseUtilService;

    @Autowired
    private LogDbaseDao logDbaseDao;

    @Autowired
    private UnusualBehaviorDao unusualBehaviorDao;

    @Autowired
    private LogUnusualBehaviorDao logUnusualBehaviorDao;
    /**
     * 定时任务的使用
     *
     **/
    @Scheduled(cron = "0/30 * * * * ? ")   //每30秒执行一次
    public void reportService() {
        try{
            Properties prop = new Properties();
            prop.load(Files.newInputStream(new File(filePath).toPath()));
            String ip = null, port = null, serviceIdentity = null;
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if(key.equals("monitor_ip")) {
                    ip = (String) value;
                }else if(key.equals("monitor_port")) {
                    port = (String) value;
                }else if(key.equals("monitor_serviceIdentity")) {
                    serviceIdentity = (String) value;
                }
            }
            JSONObject param = new JSONObject();
            //http://{ip}:30001/agent/service/getStrategy
            param.put("serviceIdentity", serviceIdentity);
            String finalUrl = "http://" + ip + ":" + port + "/agent/service/getStrategy";
//            System.out.println(finalUrl);
            String result = restTemplate.postForObject(finalUrl, param, String.class);
            JSONObject js = JSONObject.parseObject(result);
//            JSONObject js1 = (JSONObject) js.get("data");
//            System.out.println(JSON.toJSONString(js1));
            JSONObject js2 = (JSONObject) js.get("strategy");
            boolean status = (Boolean) js2.get("status");
//            System.out.println(status);
            if (status){
                Server server = new Server();
                try {
                    Map<String, Object> metricMap = new HashMap<>();
                    Map<String, String> serverInfo = server.getServerInfo();
                    List<Metric> metricList = server.getMetricInfo(serverInfo);
                    metricMap.put("serviceIdentity", serviceIdentity);
                    metricMap.put("dataType", "metric");
                    metricMap.put("metrics", metricList);
                    String postUrl = "http://" + ip + ":" + port + "/agent/service/reportStatus";
                    restTemplate.postForObject(postUrl, metricMap, String.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }catch (Exception e){

        }
    }

    @Scheduled(cron = "0 0 0 * * ?")   // 每天0点执行一次
    public void numLimitCheck() {
        System.out.println("次数检测");
        // 查询log_dbase表下前一天内所有行为
        // 获取昨天的日期，并转换为相应的Date类型
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(Date.from(yesterday.atZone(ZoneId.systemDefault()).toInstant()));
//        System.out.println("yesterday: " + yesterday);

        QueryWrapper<LogDbaseEntity> queryWrapper=new QueryWrapper<>();
        // le是小于等于的意思，也就是在这以前，ge则与之相反
        queryWrapper.ge("CREATETIME", format);
        List<LogDbaseEntity> logList = logDbaseDao.selectList(queryWrapper);
        // 获取所有角色的nunLimit参数
        QueryWrapper<UnusualBehaviorEntity> queryWrapper2=new QueryWrapper<>();
        queryWrapper2.eq("ALTER_TYPE","numLimit");
        List<UnusualBehaviorEntity> ubList = unusualBehaviorDao.selectList(queryWrapper2);
        HashMap<String, Integer> userNumLimit = new HashMap();
        HashMap<String, Integer> userNameToId = new HashMap();
        for(int i=0;i<ubList.size();i++) {
            userNumLimit.put(ubList.get(i).getUsername(),ubList.get(i).getNumLimit());
            userNameToId.put(ubList.get(i).getUsername(),ubList.get(i).getUserId());
        }
        System.out.println("userNumLimit:");
        System.out.println(userNumLimit);
        // 从获取的操作列表中开始统计每个角色昨天的操作次数
        HashMap<String, Integer> userNum = new HashMap();
        for(int i=0;i<logList.size();i++) {
            String nowUserName = logList.get(i).getUserName();
            if(userNum.containsKey(nowUserName)){
                Integer temp = userNum.get(nowUserName);
                userNum.put(nowUserName,temp + 1);
            }else{
                userNum.put(nowUserName, 1);
            }
        }
        System.out.println("userNum:");
        System.out.println(userNum);
        // 将参数和累计次数进行对比，如果超出次数限制则添加告警信息
        for(String userName : userNum.keySet()){
            Integer num = userNum.get(userName);
            Integer numLimit = userNumLimit.get(userName);
            if(num > numLimit){
                System.out.println(userName + "超出使用次数限制，触发异常行为告警");
                LogUnusualBehaviorEntity logUnusualBehaviorEntity = new LogUnusualBehaviorEntity();
                logUnusualBehaviorEntity.setUserId(userNameToId.get(userName));
                logUnusualBehaviorEntity.setUsername(userName);
                logUnusualBehaviorEntity.setCreatetime(LocalDateTime.now());
                logUnusualBehaviorEntity.setAlterType("numLimit");
                logUnusualBehaviorDao.insert(logUnusualBehaviorEntity);
            }
        }
    }

    @Scheduled(cron = "0 0 * * * ? ")   //每小时执行一次
    public void checkDatabaseSession(){
        System.out.println("checkDatabaseSession: 检查连接会话，并删除失效连接");
        dataBaseUtilService.checkConnectionSession();
    }
}
