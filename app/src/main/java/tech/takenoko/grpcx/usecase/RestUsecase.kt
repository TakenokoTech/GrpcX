package tech.takenoko.grpcx.usecase

import android.content.Context
import kotlinx.coroutines.*
import tech.takenoko.grpcx.Usecase
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.repository.RestRepository
import tech.takenoko.grpcx.utils.AppLog

class RestUsecase(context: Context, private val scope: CoroutineScope) : Usecase<Unit, String>(context, scope) {

    @InternalCoroutinesApi
    override suspend fun callAsync(param: Unit): Deferred<String> = scope.async(Dispatchers.IO) {
        AppLog.info(TAG, "call")
        while (true) withTimeoutOrNull(500) {
            countFPS(RestUsecase::class.java.simpleName)
            val hallo = RestRepository.getHallo() ?: ""
            result.postValue(UsecaseResult.Resolved(hallo))
        }
        ""
    }

    companion object {
        private val TAG = RestUsecase::class.java.simpleName
    }
}
