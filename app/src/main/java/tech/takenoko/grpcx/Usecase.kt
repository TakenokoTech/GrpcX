package tech.takenoko.grpcx

import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.utils.AppLog

abstract class Usecase<Q : Any, P : Any>(private val context: Context, private val scope: CoroutineScope) {

    protected var result = MediatorLiveData<UsecaseResult<P>>()
    val source: LiveData<UsecaseResult<P>> = result

    // protected var _fpsLiveData = MediatorLiveData<Double>()
    // val fpsLiveData: LiveData<Double> = _fpsLiveData

    private val _fpsLiveData = MediatorLiveData<List<Long>>()
    val fpsLiveData: LiveData<List<Long>> = _fpsLiveData

    @MainThread
    open fun execute(param: Q) {
        result.postValue(UsecaseResult.Pending())
        scope.launch {
            runCatching {
                callAsync(param).await()
            }.fold({
                // AppLog.info("Usecase", "execute.")
                result.postValue(UsecaseResult.Resolved(it))
            }, {
                AppLog.warn("Usecase", it)
                result.postValue(UsecaseResult.Rejected(it))
            })
        }
    }

    @WorkerThread
    protected abstract suspend fun callAsync(param: Q): Deferred<P>

    protected fun countFPS(name: String = "") {
        time.add(System.currentTimeMillis())
        if (time.size == 30) {
            val list = (0 until time.size - 1).map { time[it + 1] - time[it] }
            val result = list.size / (list.sum() / 1000.0)
            time = mutableListOf()
            val newList = (_fpsLiveData.value?.toMutableList() ?: mutableListOf()).apply { add(result.toLong()) }.drop(max(0, (_fpsLiveData.value?.size ?: 0) - 30))
            _fpsLiveData.postValue(newList)
            AppLog.info("[%16s] >>>> ".format(name), "${newList.average()}")
        }
    }

    private var first: Long = System.currentTimeMillis()
    private var time = mutableListOf<Long>()
}
