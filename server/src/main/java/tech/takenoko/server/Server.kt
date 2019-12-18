package tech.takenoko.server

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import tech.takenoko.server.proto.GreeterGrpc
import tech.takenoko.server.proto.HelloReply
import tech.takenoko.server.proto.HelloRequest
import java.util.logging.Logger

fun main(args: Array<String>) {
    GrpcServer().start()
}

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

class GreeterImpl : GreeterGrpc.GreeterImplBase() {
    override fun sayHello(req: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        val reply = HelloReply.newBuilder().setMessage("Hello ${req.name}").build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
        println("---------> request")
        println("$req")
        println("<--------- reply")
        println("$reply")
    }
}
