package com.teamsparta.jobtopia

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class JobtopiaApplication

fun main(args: Array<String>) {
    runApplication<JobtopiaApplication>(*args)
}
