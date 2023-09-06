package com.nowcoder.community.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceAspect {
    public static final Logger logger = LoggerFactory.getLogger(ServiceAspect.class);
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //用户在。。。时间访问功能
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        logger.info(String.format(("用户[%s],在[%s],访问了[%s]"), ip,now,target));
    }
//    @After("pointcut()")
//    public void after(){
//        System.out.println("after");
//    }
//    @Around("pointcut()")
//    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
//        System.out.println("around before");
//        Object Obj = joinPoint.proceed();
//        System.out.println("around after");
//        return Obj;
//    }
//    @AfterReturning("pointcut()")
//    public void afterReturning(){
//        System.out.println("AfterReturning");
//    }
//    @AfterThrowing("pointcut()")
//    public void afterThrowing(){
//        System.out.println("afterThrowing");
//    }
}
