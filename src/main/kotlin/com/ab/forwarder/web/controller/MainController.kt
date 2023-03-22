package com.ab.forwarder.web.controller

import com.ab.forwarder.domain.strategy.balancer.ForwarderStrategy
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
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
            .collect(Collectors.toMap({it.uppercase()},{ request.getHeader(it) }))

        val cookies = mutableMapOf<String, Any>()
        request.cookies?.forEach { cookies[it.name.uppercase()] = it.value }

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
            headers = headers,
            cookies = cookies
        )

        return ResponseEntity.status(answer.statusCode())
            .body(answer.body())
    }

}