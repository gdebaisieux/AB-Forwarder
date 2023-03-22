package com.ab.forwarder.domain.decoder

interface Decoder {
    fun decode(raw: String, path: String): String
}