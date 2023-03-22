package com.ab.forwarder.domain.strategy.balancer

import com.ab.forwarder.domain.bean.ForwarderConfiguration
import com.ab.forwarder.domain.decoder.jwt.JwtDecoder
import com.ab.forwarder.infrastructure.exception.MissingForwarderHeaderException
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class ForwarderStrategy(
    var forwarderConfiguration: ForwarderConfiguration,
    val httpClient: HttpClient,
    val jwtDecoder: JwtDecoder
) {
    fun forward(
        requestedURI: String,
        method: String,
        headers: Map<String, Any>,
        cookies: Map<String, Any>,
        body: String?,
        queryParameters: String?
    ): HttpResponse<String> {
        val forwardedHost = this.getForwardedLocation(
            values = if (forwarderConfiguration.discriminatorLocation == DiscriminatorLocation.HEADER) headers else cookies
        ).plus(requestedURI)
        val newRequest = HttpRequest.newBuilder()
            .uri(
                URI.create(forwardedHost.plus("?").plus(queryParameters))
            )
        headers.forEach { (key, value) -> newRequest.header(key, value.toString()) }
        newRequest.method(
            method,
            if (body?.isEmpty()!!) HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(body)
        )

        return httpClient.send(newRequest.build(), HttpResponse.BodyHandlers.ofString())
    }

    fun getForwardedLocation(values: Map<String, Any>): String {
        if (!values.containsKey(forwarderConfiguration.discriminatorName.uppercase())) {
            return when (forwarderConfiguration.missingDiscriminatorStrategy) {
                MissingHeaderStrategyType.A -> forwarderConfiguration.aUrl
                MissingHeaderStrategyType.B -> forwarderConfiguration.bUrl
                MissingHeaderStrategyType.REJECT -> throw MissingForwarderHeaderException()
            }
        }
        val headerValue = getHeaderValue(values)
        val expected = forwarderConfiguration.forwarderStrategy == StrategyType.INCLUSION
        return if (forwarderConfiguration.discriminatorValues.map { it.lowercase() }
                .contains(headerValue) == expected) {
            forwarderConfiguration.aUrl
        } else {
            forwarderConfiguration.bUrl
        }
    }

    private fun getHeaderValue(headers: Map<String, Any>): String {
        return when (forwarderConfiguration.discriminatorType) {
            HeaderType.PLAIN -> headers[forwarderConfiguration.discriminatorName.uppercase()].toString().lowercase()
            HeaderType.JWT -> {
                jwtDecoder.decode(
                    raw = headers[forwarderConfiguration.discriminatorName.uppercase()].toString(),
                    path = forwarderConfiguration.discriminatorValuePath!!
                ).lowercase()
            }
        }
    }

    enum class StrategyType {
        INCLUSION, EXCLUSION
    }

    enum class MissingHeaderStrategyType {
        A, B, REJECT
    }

    enum class HeaderType {
        PLAIN, JWT
    }

    enum class DiscriminatorLocation {
        HEADER, COOKIE
    }
}