package tech.takenoko.grpcx.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.proto.GreeterGrpc
import tech.takenoko.grpcx.proto.HelloReply
import tech.takenoko.grpcx.proto.HelloRequest
import tech.takenoko.grpcx.utils.AppLog
import java.text.SimpleDateFormat
import java.util.*

object GrpcRepository: BaseRepository() {

    const val host = "192.168.0.106"
    const val port = 6565
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPANESE)

    private val managedChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    private val blockingStub: GreeterGrpc.GreeterBlockingStub by lazy { GreeterGrpc.newBlockingStub(managedChannel) }
    private val stub: GreeterGrpc.GreeterStub by lazy { GreeterGrpc.newStub(managedChannel) }

    fun sayHello(): String {
        val request = HelloRequest.newBuilder().setName("New World !!").build()
        val reply = blockingStub.sayHello(request)
        AppLog.info(TAG, "sayHello. ${reply.message}")
        return "[${sdf.format(Date().time)}] ${reply.message}" ?: throw Exception()
    }

    fun streamHello(callback: (GrpcStreamObserver<String>) -> Unit) {
        AppLog.info(TAG, "streamHello.")
        this.callback = callback
        val request = HelloRequest.newBuilder().setName("New World !!").build()
        callbackObserver.onNext(request)
    }

    fun helloChannelOnNext(): Channel<GrpcStreamObserver<String>> {
        // AppLog.info(TAG, "helloChannelOnNext.")

        val channel = Channel<GrpcStreamObserver<String>>()
        val streamHello = stub.streamHello(object : StreamObserver<HelloReply> {
            override fun onNext(value: HelloReply?) {
                AppLog.info(TAG, "onNext. $value")
                channel.offer(GrpcStreamObserver.OnNext(value?.message ?: ""))
            }
            override fun onError(t: Throwable?) {
                AppLog.info(TAG, "onError. $t")
                channel.offer(GrpcStreamObserver.OnError(t))
            }
            override fun onCompleted() {
                AppLog.info(TAG, "onCompleted.")
                channel.offer(GrpcStreamObserver.OnCompleted())
            }
        })

        val request = HelloRequest.newBuilder().setName(" Again !!").build()
        streamHello.onNext(request)
        return channel
    }

    private val _streamHelloLiveData = MediatorLiveData<GrpcStreamObserver<String>>()
    val streamHelloLiveData: LiveData<GrpcStreamObserver<String>> = _streamHelloLiveData
    private val observer = stub.streamHello(object : StreamObserver<HelloReply> {
        override fun onNext(value: HelloReply?) = _streamHelloLiveData.postValue(GrpcStreamObserver.OnNext(value?.message))
        override fun onError(t: Throwable?) = _streamHelloLiveData.postValue(GrpcStreamObserver.OnError(t))
        override fun onCompleted() = _streamHelloLiveData.postValue(GrpcStreamObserver.OnCompleted())
    })

    private var callback: (GrpcStreamObserver<String>) -> Unit = {}
    private val callbackObserver = stub.streamHello(object : StreamObserver<HelloReply> {
        override fun onNext(value: HelloReply?) = callback(GrpcStreamObserver.OnNext(value?.message))
        override fun onError(t: Throwable?) = callback(GrpcStreamObserver.OnError(t))
        override fun onCompleted() = callback(GrpcStreamObserver.OnCompleted())
    })

    private val TAG = GrpcRepository::class.java.simpleName
}
