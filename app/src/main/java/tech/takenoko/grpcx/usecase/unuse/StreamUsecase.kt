package tech.takenoko.grpcx.usecase.unuse

import android.content.Context
import androidx.annotation.WorkerThread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*
import tech.takenoko.grpcx.Usecase
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.repository.GrpcRepository
import tech.takenoko.grpcx.utils.AppLog

open class StreamUsecase(context: Context, private val scope: CoroutineScope) : Usecase<Unit, String>(context, scope) {

    @WorkerThread
    override suspend fun callAsync(param: Unit): Deferred<String> = scope.async(Dispatchers.IO) {
        return@async suspendCoroutine<String> { continuation ->
            AppLog.info(TAG, "call")
            GrpcRepository.streamHello {
                val text = it as? GrpcStreamObserver.OnNext<String>?
                kotlin.runCatching {
                    continuation.resume(text?.value ?: "")
                }.onFailure {
                    AppLog.warn(TAG, it)
                }
            }
        }
    }

    companion object {
        private val TAG = StreamUsecase::class.java.simpleName
    }
}
