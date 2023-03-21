package com.ab.forwarder.domain.bean

import org.springframework.stereotype.Component
import kotlin.concurrent.getOrSet

@Component
class Context {
    var holder = ThreadLocal<MutableMap<String, String>>()

    fun setHeader(name: String, value: String) {
        holder.getOrSet { mutableMapOf() }[name] = value
    }
}