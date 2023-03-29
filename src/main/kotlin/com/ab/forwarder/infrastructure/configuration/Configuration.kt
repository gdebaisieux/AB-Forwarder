package com.ab.forwarder.infrastructure.configuration

import com.ab.forwarder.domain.bean.ForwarderConfiguration
import com.ab.forwarder.domain.strategy.balancer.ForwarderStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient

@Configuration
class Configuration {
    @Value("\${forwarder.a.destination}")
    lateinit var aUrl: String
    @Value("\${forwarder.a.values}")
    lateinit var values: Array<String>
    @Value("\${forwarder.b.destination}")
    lateinit var bUrl: String
    @Value("\${forwarder.strategy}")
    lateinit var strategyType: ForwarderStrategy.StrategyType
    @Value("#{'\${forwarder.discriminator.name}'.toLowerCase()}")
    lateinit var headerName: String
    @Value("\${forwarder.missing-discriminator.strategy}")
    lateinit var missingHeaderStrategyType: ForwarderStrategy.MissingHeaderStrategyType
    @Value("\${forwarder.discriminator.type}")
    lateinit var headerType: ForwarderStrategy.HeaderType
    @Value("\${forwarder.discriminator.value.path:''}")
    lateinit var headerValuePath: String
    @Value("\${forwarder.discriminator.location}")
    lateinit var discriminatorLocation: ForwarderStrategy.DiscriminatorLocation

    @Bean
    fun httpClient(): HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

    @Bean
    fun forwarderConfiguration() = ForwarderConfiguration(
        aUrl = aUrl,
        bUrl = bUrl,
        discriminatorValues = values,
        discriminatorName = headerName,
        forwarderStrategy = strategyType,
        missingDiscriminatorStrategy = missingHeaderStrategyType,
        discriminatorLocation = discriminatorLocation,
        discriminatorType = headerType,
        discriminatorValuePath = headerValuePath
    )
}