package com.mb.modulith.school.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Profile("!test")
@Component("schoolLoggerAspect")
public class LoggerAspect {

    @Before("anyController() || anyService()")
    public void loggingBefore(JoinPoint joinPoint) {
        String methodName = getMethodName(joinPoint);
        String className = getClassName(joinPoint);
        Object[] args = joinPoint.getArgs();

        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            parameters.append("arg ").append(i + 1).append(": ").append(args[i]);
            parameters.append(" --- ");
        }

        log.info("called {} method of class {} with parameters {}", methodName, className, parameters);
    }

    @AfterReturning(pointcut = "anyController() || anyService()", returning = "returnValue")
    public void loggingAfter(JoinPoint joinPoint, Object returnValue) {
        String methodName = getMethodName(joinPoint);
        String className = getClassName(joinPoint);

        log.info("returned {} method of class {} with value {}", methodName, className, returnValue);
    }

    @AfterThrowing(pointcut = "anyController() || anyService()", throwing = "e")
    public void loggingException(JoinPoint joinPoint, Exception e) {
        log.error(getClassName(joinPoint) + " has thrown an error: ", e);
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void anyController() {
    }

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void anyService() {
    }

    private String getMethodName(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    private String getClassName(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getName();
    }
}
