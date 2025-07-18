package com.vinaacademy.platform.feature.log;//package vn.cnj.jewelrystore.aop;
//
//import jakarta.transaction.Transactional;
//import lombok.extern.log4j.Log4j2;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import vn.cnj.jewelrystore.aop.olddata.OldDataNavigator;
//import vn.cnj.jewelrystore.log.dto.LogDto;
//import vn.cnj.jewelrystore.log.service.LogService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Aspect
//@Component
//@Log4j2
//public class LoggingAspect {
//    @Autowired
//    private LogService logService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private OldDataNavigator oldDataNavigator;
//    @Value("${app.enableLog:false}")
//    private boolean isLogEnable;
//
//    @Pointcut("execution(* vn.cnj.jewelrystore.service.*.*(..))")
//    public void serviceMethods() {
//    }
//
//
//    @Around("serviceMethods()")
//    @Transactional
//    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
//        if (!isLogEnable) {
//            return joinPoint.proceed();
//        }
//        String className = joinPoint.getTarget().getClass().getSimpleName().toLowerCase();
//        String methodName = joinPoint.getSignature().getName().toLowerCase();
//
//        if (!methodName.matches(".*(upsert|insert|delete|update|edit|add).*")) {
//            return joinPoint.proceed();
//        }
//
//        String type = className.matches(".*product.*") ? "product" :
//                className.matches(".*orderitem.*") ? "orderItem" :
//                        className.matches(".*order.*") ? "order" : "";
//
//        if (type.isEmpty()) {
//            return joinPoint.proceed();
//        }
//
//        LogDto logDto = LogDto.builder()
//                .name(type)
//                .event(methodName)
//                .build();
//
//        Object[] args = joinPoint.getArgs();
//        log.debug("Method: {}, args: {}", methodName, args);
//
//        try {
//            String oldData = oldDataNavigator.getOldData(type, args);
//            logDto.setOldData(oldData);
//            Object result = joinPoint.proceed();
//            String newData = objectMapper.writeValueAsString(result);
//            logDto.setNewData(newData);
//            logService.upsert(logDto);
//            return result;
//        } catch (Exception e) {
//            log.error("Error while write log using aop: ", e);
//        }
//        return joinPoint.proceed();
//    }
//}