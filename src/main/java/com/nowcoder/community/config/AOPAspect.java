package com.nowcoder.community.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
//public class AOPAspect {
//    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
//    public void pointcut(){
//    }
//
//    @Before("pointcut()")
//    public void before(){
//        System.out.println("before");
//    }
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
//}
