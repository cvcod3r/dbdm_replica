package com.dbms.core;

import com.alibaba.fastjson.JSONObject;
import com.dbms.entity.LogSystemEntity;
import com.dbms.service.LogSystemService;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.dbms.utils.SecurityUtils.*;

@Aspect
@Component
@Slf4j
public class SysLogAspect {

    @Autowired
    LogSystemService logSystemService;

    /**
     * 设置操作日志切入点   在注解的位置切入代码
     */
    @Pointcut("@annotation(com.dbms.annotation.SysLogAnnotation)")
    public void sysLogPointCut() {
    }

    /**
     * 记录操作日志
     * @param joinPoint 方法的执行点
     * @param result  方法返回值
     * @throws Throwable
     */
    @AfterReturning(returning = "result", value = "sysLogPointCut()")
    public void saveSysLog(JoinPoint joinPoint, Object result) throws Throwable {

        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        try {
            LogSystemEntity sysLog = new LogSystemEntity();
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取操作
            SysLogAnnotation annotation = method.getAnnotation(SysLogAnnotation.class);
            if (annotation != null) {
                sysLog.setModuleName(annotation.moduleName());
                sysLog.setChildModule(annotation.childModule());
                sysLog.setOperation(annotation.operation());
                sysLog.setProcedureInfo(annotation.procedureInfo());
            }
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName;
//            sysLog.setMethod(methodName); // 类名.请求方法
            sysLog.setCreatetime(LocalDateTime.now()); //操作时间
            //操作用户 --登录时有把用户的信息保存在session中，可以直接取出
            sysLog.setUserId(getUserId());
            sysLog.setUserName(getUsername());
            sysLog.setRealName(getRealname());
            sysLog.setIpAddress(IpUtils.getIpAddress(request)); //操作IP IPUtils工具类网上大把的，比如工具类集锦的hutool.jar
//            sysLog.setUrl(request.getRequestURI()); // 请求URI
            // 方法请求的参数
//            Map<String, String> rtnMap = convertMap(request.getParameterMap());
//            // 将参数所在的数组转换成json
//            String params = JSON.toJSONString(rtnMap);
//            //获取json的请求参数
//            if (rtnMap == null || rtnMap.size() == 0) {
//                params = getJsonStrByRequest(request);
//            }
//            sysLog.setParams(params); // 请求参数
            try{
                Map<String, Object> dataResult = (Map<String, Object>)result;  //返回值信息
                sysLog.setResultDetail(dataResult.getOrDefault("msg","操作成功").toString());
            }catch (Exception ignored){
                sysLog.setResultDetail("操作成功");
            }
            //保存日志
            logSystemService.save(sysLog);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("日志记录异常，请检查返回值是否是Map <String, Object>类型");
        }

    }


    /**
     * 转换request 请求参数
     *
     * @param paramMap request获取的参数数组
     */
    public Map<String, String> convertMap(Map<String, String[]> paramMap) {
        Map<String, String> rtnMap = new HashMap<String, String>();
        for (String key : paramMap.keySet()) {
            rtnMap.put(key, paramMap.get(key)[0]);
        }
        return rtnMap;
    }

    /**
     * 获取json格式 请求参数
     */
    public String getJsonStrByRequest(HttpServletRequest request) {
        String param = null;
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStrBuilder.toString());
            param = jsonObject.toJSONString();
            System.out.println(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return param;
    }


    /**
     * 转换异常信息为字符串
     *
     * @param exceptionName    异常名称
     * @param exceptionMessage 异常信息
     * @param elements         堆栈信息
     */
    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuffer strbuff = new StringBuffer();
        for (StackTraceElement stet : elements) {
            strbuff.append(stet + "\n");
        }
        String message = exceptionName + ":" + exceptionMessage + "\n\t" + strbuff.toString();
        return message;
    }
}
