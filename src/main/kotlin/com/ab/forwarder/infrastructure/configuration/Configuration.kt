package com.ab.forwarder.infrastructure.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient

@Configuration
class Configuration {
    @Bean
    fun httpClient(): HttpClient = HttpClient.newHttpClient()
}