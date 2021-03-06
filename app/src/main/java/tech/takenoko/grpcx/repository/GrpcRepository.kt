package tech.takenoko.grpcx.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.text.SimpleDateFormat
import java.util.*
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.proto.GreeterGrpc
import tech.takenoko.grpcx.proto.HelloReply
import tech.takenoko.grpcx.proto.HelloRequest
import tech.takenoko.grpcx.utils.AppLog

object GrpcRepository : BaseRepository() {

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
