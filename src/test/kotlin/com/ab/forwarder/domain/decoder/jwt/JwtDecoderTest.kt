package com.ab.forwarder.domain.decoder.jwt

import org.assertj.core.api.Assertions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class JwtDecoderTest {

    @InjectMocks
    private lateinit var jwtDecoder: JwtDecoder

    @ParameterizedTest
    @CsvSource(value = [
        "name,John Doe",
        "myarray[].myvalue2,test2",
        "test1.innertest[].val1,myvalue1",
        "testvalue.value.inner,toto"
    ])
    fun decode_correctlyDecodesJwtToken(name: String, value: String) {
        // prepare
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwibXlhcnJheSI6W3sibXl2YWx1ZSI6InRlc3QxIn0seyJteXZhbHVlMiI6InRlc3QyIn1dLCJ0ZXN0dmFsdWUiOnsidmFsdWUiOnsiaW5uZXIiOiJ0b3RvIn19LCJpYXQiOjE1MTYyMzkwMjIsInRlc3QxIjp7ImlubmVydGVzdCI6W3sidmFsMSI6Im15dmFsdWUxIn1dfX0.qwPOJLyTS51WR4VTVe9-D9uPz6di0rI2ol0qDUXSCbM"

        // execute
        val result = jwtDecoder.decode(jwt, name)

        // verify
        Assertions.assertThat(result).isNotNull.isEqualTo(value)

    }
}