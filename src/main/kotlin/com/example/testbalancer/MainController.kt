package com.example.testbalancer

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


@RestController
class MainController(val forwarder: ForwarderStrategy) {

    @RequestMapping("**")
    fun genericServlet(request: HttpServletRequest, response: HttpServletResponse): Any {
        val requestedURI = request.requestURI
        val forbiddenHeaders = listOf(
            "connection",
            "content-length",
            "expect",
            "host",
            "upgrade"
        )
        val headers = request.headerNames.toList()
            .filter { !forbiddenHeaders.contains(it) }.stream()
            .map { it.toString() }
            .collect(Collectors.toMap(Function.identity()) { request.getHeader(it) })

        val params = request.parameterMap.map { entry ->
            entry.value.joinToString("&") {
                entry.key.plus("=").plus(it)
            }
        }.joinToString("&")

        val body = request.reader.lines().toList().joinToString()

        val answer = forwarder.forward(
            requestedURI = requestedURI,
            queryParameters = params,
            method = request.method,
            body = body,
            headers = headers
        )

        return ResponseEntity.status(answer.statusCode())
            .body(answer.body())
    }
}