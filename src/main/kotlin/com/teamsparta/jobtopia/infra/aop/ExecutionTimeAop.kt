package com.teamsparta.jobtopia.infra.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class ExecutionTimeAop {
    private val log = LoggerFactory.getLogger("EXECUTION_TIME_LOGGER")

    @Around("@annotation(LogExecutionTime)")
    fun executionTime(joinPoint: ProceedingJoinPoint): Any? {
        val start = System.currentTimeMillis()
        val proceed = joinPoint.proceed()
        val executionTime = System.currentTimeMillis() - start
        log.info("${joinPoint.signature} executed in ${executionTime}ms")
        return proceed
    }
}