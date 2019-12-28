package tech.takenoko.server.rest

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class RestServer {
    private lateinit var server: NettyApplicationEngine

    fun start() {
        println("Server started, listening on $port")
        server = embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                }
            }
            routing {
                halloController()
            }
        }
        server.start()
    }

    fun stop() {
        server.stop(0, 0, TimeUnit.MINUTES)
    }

    companion object {
        private val logger = Logger.getLogger(RestServer::class.java.name)
        private const val port = 8088
    }
}