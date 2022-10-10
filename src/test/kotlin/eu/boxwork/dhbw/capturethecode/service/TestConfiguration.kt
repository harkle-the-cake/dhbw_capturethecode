package eu.boxwork.dhbw.capturethecode.service

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.ssl.SslContextBuilder
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import reactor.netty.http.client.HttpClient
import javax.net.ssl.SSLException

@EnableAutoConfiguration
@TestConfiguration
class TestConfiguration {

    @Bean
    @Throws(SSLException::class)
    fun createWebClient(): WebClient? {
        val sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()
        val httpClient: HttpClient = HttpClient.create().secure { t -> t.sslContext(sslContext) }
        return WebClient.builder().clientConnector(ReactorClientHttpConnector(httpClient)).build()
    }
}