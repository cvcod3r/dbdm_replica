package com.dbms.aop;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * AOP 打印日志
 */
@Aspect
@Component
public class SysAspect
{
    static final Logger LOGGER = LoggerFactory.getLogger(SysAspect.class);

    @Around("within(com.dbms.controller.*)")
    public Object around(ProceedingJoinPoint joinPoint)
            throws Throwable
    {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = new StringBuilder(className).append(".").append(method.getName()).toString();
        Object[] args = joinPoint.getArgs();
        StopWatch clock = new StopWatch();
        clock.start(methodName);
        Object object = joinPoint.proceed();
        clock.stop();
        LOGGER.info("running {} ms, method = {} {}", clock.getTotalTimeMillis(), clock.getLastTaskName(), JSON.toJSONString(Arrays.asList(args)));
        return object;
    }



}
