package com.ab.forwarder.domain.strategy

import com.ab.forwarder.domain.bean.ForwarderConfiguration
import com.ab.forwarder.domain.decoder.jwt.JwtDecoder
import com.ab.forwarder.domain.strategy.balancer.ForwarderStrategy
import com.ab.forwarder.infrastructure.exception.MissingForwarderHeaderException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.net.http.HttpClient

@ExtendWith(MockitoExtension::class)
internal class ForwarderStrategyTest {
    @Mock
    private lateinit var httpClient: HttpClient

    @Mock
    private lateinit var jwtDecoder: JwtDecoder

    @Mock
    private lateinit var forwarderConfiguration: ForwarderConfiguration

    @InjectMocks
    private lateinit var strategy: ForwarderStrategy

    @Test
    fun getForwardedLocation_correctlyForwardToA_whenInclusionAndHeaderPresent() {
        // prepare
        Mockito.`when`(forwarderConfiguration.aUrl).thenReturn("http://urlA")
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.discriminatorValues).thenReturn(arrayOf("VALA","VALB"))
        Mockito.`when`(forwarderConfiguration.forwarderStrategy).thenReturn(ForwarderStrategy.StrategyType.INCLUSION)
        Mockito.`when`(forwarderConfiguration.discriminatorType).thenReturn(ForwarderStrategy.HeaderType.PLAIN)

        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo("http://urlA")
    }

    @Test
    fun getForwardedLocation_correctlyForwardToB_whenInclusionAndNoIncludeValues() {
        // prepare
        Mockito.`when`(forwarderConfiguration.bUrl).thenReturn("http://urlB")
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.discriminatorValues).thenReturn(arrayOf())
        Mockito.`when`(forwarderConfiguration.forwarderStrategy).thenReturn(ForwarderStrategy.StrategyType.INCLUSION)
        Mockito.`when`(forwarderConfiguration.discriminatorType).thenReturn(ForwarderStrategy.HeaderType.PLAIN)
        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo("http://urlB")
    }

    @Test
    fun getForwardedLocation_correctlyForwardToA_whenExclusionAndNoExcludeValues() {
        // prepare
        Mockito.`when`(forwarderConfiguration.aUrl).thenReturn("http://urlA")
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.discriminatorValues).thenReturn(arrayOf())
        Mockito.`when`(forwarderConfiguration.forwarderStrategy).thenReturn(ForwarderStrategy.StrategyType.EXCLUSION)
        Mockito.`when`(forwarderConfiguration.discriminatorType).thenReturn(ForwarderStrategy.HeaderType.PLAIN)
        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo("http://urlA")
    }

    @Test
    fun getForwardedLocation_correctlyForwardToB_whenExclusionAndHeaderPresent() {
        // prepare
        Mockito.`when`(forwarderConfiguration.bUrl).thenReturn("http://urlB")
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.discriminatorValues).thenReturn(arrayOf("VALA","VALB"))
        Mockito.`when`(forwarderConfiguration.forwarderStrategy).thenReturn(ForwarderStrategy.StrategyType.EXCLUSION)
        Mockito.`when`(forwarderConfiguration.discriminatorType).thenReturn(ForwarderStrategy.HeaderType.PLAIN)
        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo("http://urlB")
    }

    @Test
    fun getForwardedLocation_correctlyForwardToA_whenHeaderMissingAndDefaultToA() {
        // prepare
        Mockito.`when`(forwarderConfiguration.aUrl).thenReturn("http://urlA")
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.missingDiscriminatorStrategy).thenReturn(ForwarderStrategy.MissingHeaderStrategyType.A)
        val headers = mapOf<String, Any>()

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo("http://urlA")
    }

    @Test
    fun getForwardedLocation_correctlyForwardToB_whenHeaderMissingAndDefaultToB() {
        // prepare
        Mockito.`when`(forwarderConfiguration.bUrl).thenReturn("http://urlB")
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.missingDiscriminatorStrategy).thenReturn(ForwarderStrategy.MissingHeaderStrategyType.B)
        val headers = mapOf<String, Any>()

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo("http://urlB")
    }

    @Test
    fun getForwardedLocation_correctlyRejects_whenHeaderMissingAndDefaultToReject() {
        // prepare
        Mockito.`when`(forwarderConfiguration.discriminatorName).thenReturn("SWITCHER")
        Mockito.`when`(forwarderConfiguration.missingDiscriminatorStrategy).thenReturn(ForwarderStrategy.MissingHeaderStrategyType.REJECT)
        val headers = mapOf<String, Any>()

        // execute
        assertThrows<MissingForwarderHeaderException> {
            strategy.getForwardedLocation(headers)
        }
    }
}