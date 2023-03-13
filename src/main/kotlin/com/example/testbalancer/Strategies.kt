package com.example.testbalancer

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class ForwarderStrategy(val httpClient: HttpClient) {
    @Value("\${forwarder.a.destination}")
    lateinit var aUrl: String
    @Value("\${forwarder.a.values}")
    lateinit var values: Array<String>
    @Value("\${forwarder.b.destination}")
    lateinit var bUrl: String
    @Value("\${forwarder.strategy}")
    lateinit var strategyType: StrategyType
    @Value("#{'\${forwarder.header.name}'.toLowerCase()}")
    lateinit var headerName: String
    @Value("\${forwarder.missing-header.strategy}")
    lateinit var missingHeaderStrategyType: MissingHeaderStrategyType

    fun forward(
        requestedURI: String,
        method: String,
        headers: Map<String, Any>,
        body: String?,
        queryParameters: String?
    ): HttpResponse<String> {
        val forwardedHost = this.getForwardedLocation(headers= headers).plus(requestedURI)
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

    fun getForwardedLocation(headers: Map<String, Any>): String {
        if(! headers.containsKey(headerName.lowercase())) {
            return when(missingHeaderStrategyType) {
                MissingHeaderStrategyType.A ->  aUrl
                MissingHeaderStrategyType.B -> bUrl
                MissingHeaderStrategyType.REJECT -> throw MissingForwarderHeaderException()
            }
        }
        val headerValue = headers[headerName].toString().lowercase()
        val expected = strategyType == StrategyType.INCLUSION
        return if (values.map { it.lowercase() }.contains(headerValue) == expected) {
            aUrl
        } else {
            bUrl
        }
    }

    enum class StrategyType {
        INCLUSION, EXCLUSION
    }

    enum class MissingHeaderStrategyType {
        A, B, REJECT
    }
}