package tech.takenoko.grpcx

import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.utils.AppLog

abstract class Usecase<Q : Any, P : Any>(private val context: Context, private val scope: CoroutineScope) {

    protected var result = MediatorLiveData<UsecaseResult<P>>()
    val source: LiveData<UsecaseResult<P>> = result

    protected var _fpsLiveData = MediatorLiveData<Double>()
    val fpsLiveData: LiveData<Double> = _fpsLiveData

    @MainThread
    open fun execute(param: Q) {
        val start = startFps()
        result.postValue(UsecaseResult.Pending())
        scope.launch {
            runCatching {
                callAsync(param).await()
            }.fold({
                // AppLog.info("Usecase", "execute.")
                endFps(start)
                result.postValue(UsecaseResult.Resolved(it))
            }, {
                AppLog.warn("Usecase", it)
                result.postValue(UsecaseResult.Rejected(it))
            })
        }
    }

    @WorkerThread
    protected abstract suspend fun callAsync(param: Q): Deferred<P>

    private fun startFps(): Long {
        return System.currentTimeMillis()
    }

    private fun endFps(first: Long) {
        val last = System.currentTimeMillis()
        time.add(last - first)
        if (time.size == 30) {
            val result = time.size / (time.sum() / 1000.0)
            time = mutableListOf()
            _fpsLiveData.postValue(result)
        }
    }

    private var time = mutableListOf<Long>()
}
