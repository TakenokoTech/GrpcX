package tech.takenoko.grpcx.repository

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.channels.Channel
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.proto.GreeterGrpc
import tech.takenoko.grpcx.proto.HelloReply
import tech.takenoko.grpcx.proto.HelloRequest

object GrpcChannelRepository : BaseRepository() {

    const val host = "192.168.0.106"
    const val port = 6565
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPANESE)

    private val managedChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    private val stub: GreeterGrpc.GreeterStub by lazy { GreeterGrpc.newStub(managedChannel) }

    val channel = Channel<GrpcStreamObserver<String>>()
    private var streamHello: StreamObserver<HelloRequest>? = null

    private fun connect() {
        streamHello = stub.streamHello(object : StreamObserver<HelloReply> {
            override fun onNext(value: HelloReply?) {
                // AppLog.info(TAG, "onNext. $value")
                channel.offer(GrpcStreamObserver.OnNext(value?.message ?: ""))
            }
            override fun onError(t: Throwable?) {
                // AppLog.info(TAG, "onError. $t")
                channel.offer(GrpcStreamObserver.OnError(t))
            }
            override fun onCompleted() {
                // AppLog.info(TAG, "onCompleted.")
                channel.offer(GrpcStreamObserver.OnCompleted())
                connect()
            }
        })
    }

    fun helloChannelOnNext() {
        // AppLog.info(TAG, "helloChannelOnNext.")
        val request = HelloRequest.newBuilder().setName(" Again !!").build()
        if (streamHello == null) connect()
        streamHello?.onNext(request)
    }

    private val TAG = GrpcChannelRepository::class.java.simpleName
}
