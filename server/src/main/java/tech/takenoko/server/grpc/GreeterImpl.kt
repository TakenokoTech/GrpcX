package tech.takenoko.server.grpc

import io.grpc.stub.StreamObserver
import tech.takenoko.server.proto.GreeterGrpc
import tech.takenoko.server.proto.HelloReply
import tech.takenoko.server.proto.HelloRequest
import java.text.SimpleDateFormat
import java.util.*

class GreeterImpl : GreeterGrpc.GreeterImplBase() {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPANESE)

    override fun sayHello(req: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        val reply = HelloReply.newBuilder().setMessage("Hello ${req.name}").build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
        println("---------> request")
        println("$req")
        println("<--------- reply")
        println("$reply")
    }

    override fun streamHello(responseObserver: StreamObserver<HelloReply>?): StreamObserver<HelloRequest> = object:
        StreamObserver<HelloRequest> {
        override fun onNext(value: HelloRequest?) {
            val reply1 = HelloReply.newBuilder().setMessage("Hello ${value?.name} ${sdf.format(Date().time)}").build()
            responseObserver?.onNext(reply1)
            // responseObserver?.onCompleted()
            println("---------> request")
            println("${value?.name}")
        }
        override fun onError(t: Throwable?) {
            println("<--------- error")
            println("${t}")
        }
        override fun onCompleted() {
            println("<--------- reply")
            responseObserver?.onCompleted()
        }
    }
}
