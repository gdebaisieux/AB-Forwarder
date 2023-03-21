package com.ab.forwarder.web.advice

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.net.ConnectException

@ControllerAdvice
class ControllerAdvice {

    @ExceptionHandler(ConnectException::class)
    fun handleConnectException(): ResponseEntity<Any> {
        return ResponseEntity.status(502).build()
    }
}