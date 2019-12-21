package tech.takenoko.grpcx.usecase

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import tech.takenoko.grpcx.Usecase
import tech.takenoko.grpcx.entities.GrpcStreamObserver
import tech.takenoko.grpcx.repository.GrpcRepository
import tech.takenoko.grpcx.utils.AppLog

open class MainUsecase(context: Context, private val scope: CoroutineScope) : Usecase<Unit, String>(context, scope) {

    @WorkerThread
    override suspend fun callAsync(param: Unit): Deferred<String> = scope.async(Dispatchers.IO) {
        AppLog.info(TAG, "call")
        GrpcRepository.sayHello()
    }

    companion object {
        private val TAG = MainUsecase::class.java.simpleName
    }
}
