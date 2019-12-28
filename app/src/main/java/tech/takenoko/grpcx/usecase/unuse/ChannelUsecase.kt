package tech.takenoko.grpcx.usecase.unuse

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import tech.takenoko.grpcx.Usecase
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.repository.GrpcChannelRepository
import tech.takenoko.grpcx.utils.AppLog

open class ChannelUsecase(context: Context, private val scope: CoroutineScope) :
    Usecase<Unit, String>(context, scope) {

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @WorkerThread
    override suspend fun callAsync(param: Unit): Deferred<String> = scope.async(Dispatchers.IO) {
        AppLog.info(TAG, "call")

        // val channel = GrpcRepository.helloChannelOnNext()
        scope.launch(Dispatchers.IO) {
            GrpcChannelRepository.helloChannelOnNext()
        }

        return@async withTimeoutOrNull(3000) {
            val it = GrpcChannelRepository.channel.receiveOrClosed().valueOrNull
            AppLog.info(TAG, "receiveOrClosed. $it")
            return@withTimeoutOrNull when (it) {
                is GrpcStreamObserver.OnNext<String> -> it.value ?: ""
                is GrpcStreamObserver.OnCompleted -> ""
                is GrpcStreamObserver.OnError -> throw it.t ?: Error()
                else -> ""
            }
        } ?: ""
    }

    companion object {
        private val TAG = ChannelUsecase::class.java.simpleName
    }
}
