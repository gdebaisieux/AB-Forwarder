package com.example.testbalancer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestBalancerApplication

fun main(args: Array<String>) {
    runApplication<TestBalancerApplication>(*args)
}
