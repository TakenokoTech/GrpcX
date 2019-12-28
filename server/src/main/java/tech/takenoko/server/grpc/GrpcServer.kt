package tech.takenoko.server.grpc

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import java.util.logging.Logger

class GrpcServer {
    lateinit var server: Server

    fun start() {
        println("Server started, listening on $port")
        server = ServerBuilder.forPort(port).apply { service.forEach { addService(it) } }.build().start()
        server.awaitTermination()
    }

    fun stop() {
        server.shutdown()
    }

    companion object {
        private val logger = Logger.getLogger(GrpcServer::class.java.name)
        private const val port = 6565
        private val service: List<BindableService> = listOf(GreeterImpl())
    }
}