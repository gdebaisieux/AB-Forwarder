package com.ab.forwarder.domain.strategy

import com.ab.forwarder.domain.strategy.balancer.ForwarderStrategy
import com.example.testbalancer.MissingForwarderHeaderException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.net.http.HttpClient

@ExtendWith(MockitoExtension::class)
internal class ForwarderStrategyTest {

    @Mock
    private lateinit var httpClient: HttpClient

    @InjectMocks
    private lateinit var strategy: ForwarderStrategy

    @Test
    fun getForwardedLocation_correctlyForwardToA_whenInclusionAndHeaderPresent() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf("VALA","VALB")
        strategy.strategyType = ForwarderStrategy.StrategyType.INCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.REJECT

        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo(strategy.aUrl)
    }

    @Test
    fun getForwardedLocation_correctlyForwardToB_whenInclusionAndNoIncludeValues() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf()
        strategy.strategyType = ForwarderStrategy.StrategyType.INCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.REJECT

        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo(strategy.bUrl)
    }

    @Test
    fun getForwardedLocation_correctlyForwardToA_whenExclusionAndNoExcludeValues() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf()
        strategy.strategyType = ForwarderStrategy.StrategyType.EXCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.REJECT

        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo(strategy.aUrl)
    }

    @Test
    fun getForwardedLocation_correctlyForwardToB_whenExclusionAndHeaderPresent() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf("VALA","VALB")
        strategy.strategyType = ForwarderStrategy.StrategyType.EXCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.REJECT

        val headers = mapOf("HEADER1" to "TEST", "SWITCHER" to "VALB")

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo(strategy.bUrl)
    }

    @Test
    fun getForwardedLocation_correctlyForwardToA_whenHeaderMissingAndDefaultToA() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf("VALA","VALB")
        strategy.strategyType = ForwarderStrategy.StrategyType.INCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.A

        val headers = mapOf<String, Any>()

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo(strategy.aUrl)
    }

    @Test
    fun getForwardedLocation_correctlyForwardToB_whenHeaderMissingAndDefaultToB() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf("VALA","VALB")
        strategy.strategyType = ForwarderStrategy.StrategyType.INCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.B

        val headers = mapOf<String, Any>()

        // execute
        val result = strategy.getForwardedLocation(headers)

        // verify
        Assertions.assertThat(result).isNotBlank.isEqualTo(strategy.bUrl)
    }

    @Test
    fun getForwardedLocation_correctlyRejects_whenHeaderMissingAndDefaultToReject() {
        // prepare
        strategy.aUrl = "http://urlA"
        strategy.bUrl = "http://urlB"
        strategy.headerName = "SWITCHER"
        strategy.values = arrayOf("VALA","VALB")
        strategy.strategyType = ForwarderStrategy.StrategyType.INCLUSION
        strategy.missingHeaderStrategyType = ForwarderStrategy.MissingHeaderStrategyType.REJECT

        val headers = mapOf<String, Any>()

        // execute
        assertThrows<MissingForwarderHeaderException> {
            strategy.getForwardedLocation(headers)
        }
    }
}