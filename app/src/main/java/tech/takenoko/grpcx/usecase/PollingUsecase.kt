package tech.takenoko.grpcx.usecase

import android.content.Context
import kotlinx.coroutines.*
import tech.takenoko.grpcx.Usecase
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.repository.GrpcChannelRepository
import tech.takenoko.grpcx.utils.AppLog

class PollingUsecase(context: Context, private val scope: CoroutineScope) : Usecase<Unit, String>(context, scope) {

    @InternalCoroutinesApi
    override suspend fun callAsync(param: Unit): Deferred<String> = scope.async(Dispatchers.IO) {
        while (true) withTimeoutOrNull(500) {
            launch { GrpcChannelRepository.helloChannelOnNext() }
            val it = GrpcChannelRepository.channel.receiveOrClosed().valueOrNull
            AppLog.info(TAG, "receiveOrClosed. ${it}")
            return@withTimeoutOrNull when (it) {
                is GrpcStreamObserver.OnNext<String> -> result.postValue(UsecaseResult.Resolved(it.value ?: ""))
                is GrpcStreamObserver.OnCompleted -> ""
                is GrpcStreamObserver.OnError -> throw it.t ?: Error()
                else -> ""
            }
        }
        ""
    }

    companion object {
        private val TAG = PollingUsecase::class.java.simpleName
    }
}
