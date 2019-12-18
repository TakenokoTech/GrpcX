package tech.takenoko.grpcx.usecase

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import tech.takenoko.grpcx.Usecase
import tech.takenoko.grpcx.proto.GreeterGrpc
import tech.takenoko.grpcx.proto.HelloRequest

open class MainUsecase(context: Context, private val scope: CoroutineScope) : Usecase<Unit, String>(context, scope) {

    @WorkerThread
    override suspend fun callAsync(param: Unit): Deferred<String> = scope.async(Dispatchers.IO) {
        Log.i(TAG, "call")

        val host = "192.168.0.106"
        val port = 6565
        val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
        val stub = GreeterGrpc.newBlockingStub(channel)
        val request = HelloRequest.newBuilder().setName("-----").build()
        val reply = stub.sayHello(request)

        return@async reply.message // "Hallo World"
    }

    companion object {
        private val TAG = MainUsecase::class.java.simpleName
    }
}
