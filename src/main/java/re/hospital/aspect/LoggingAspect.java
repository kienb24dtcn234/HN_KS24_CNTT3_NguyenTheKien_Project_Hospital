package re.hospital.aspect;

import lombok.extern.slf4j. Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* re.hospital.controller..*(..))")
    public void controllerMethods() {}

    @Pointcut("execution(* re.hospital.service..*(..))")
    public void serviceMethods() {}

    @Around("controllerMethods() || serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String args = Arrays.toString(joinPoint.getArgs());

        log.info("[START] {} - Args: {}", methodName, args);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        log.info("[END] {} - Duration: {}ms", methodName, duration);

        return result;
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "ex")
    public void logException(org.aspectj.lang.JoinPoint joinPoint, Throwable ex) {
        log.error("[ERROR] {} - Exception: {} - Message: {}",
                joinPoint.getSignature().toShortString(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}
