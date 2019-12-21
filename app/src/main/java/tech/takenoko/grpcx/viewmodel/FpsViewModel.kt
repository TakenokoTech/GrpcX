package tech.takenoko.grpcx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tech.takenoko.grpcx.App
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.usecase.ChannelUsecase
import tech.takenoko.grpcx.usecase.PollingUsecase
import kotlin.math.max
import kotlin.math.min

class FpsViewModel : ViewModel() {

    private val _textLiveData = MediatorLiveData<String>()
    val textLiveData: LiveData<String> = _textLiveData

    private val _listLiveData = MediatorLiveData<List<String>>()
    val listLiveData: LiveData<List<String>> = _listLiveData

    private val _fpsLiveData = MediatorLiveData<List<Long>>()
    val fpsLiveData: LiveData<List<Long>> = _fpsLiveData

    private val pollingUsecase = PollingUsecase(App.context, viewModelScope)

    init {
        _listLiveData.postValue(listOf())
        _textLiveData.postValue("")
        _textLiveData.addSource(pollingUsecase.source) { handler(it) }

        _fpsLiveData.postValue((0..100).map { 0L })
        pollingUsecase.execute(Unit)
    }

    private fun handler(result: UsecaseResult<String>): Any = when (result) {
        is UsecaseResult.Pending -> { }
        is UsecaseResult.Resolved -> {
            fps()
            _listLiveData.value = listOf(result.value)
        }
        is UsecaseResult.Rejected -> {
            _textLiveData.value = result.reason.localizedMessage
        }
    }

    private fun fps() {
        time.add(System.currentTimeMillis())
        if (time.size == 30) {
            val list = (0 until time.size-1).map { time[it+1] - time[it] }
            val result = list.size / (list.sum() / 1000.0)
            time = mutableListOf()
            _textLiveData.postValue("FPS: $result")

            val newList = (_fpsLiveData.value?.toMutableList() ?: mutableListOf()).apply {add(result.toLong()) }.drop( max(0, (_fpsLiveData.value?.size ?: 0) - 100))
            _fpsLiveData.postValue(newList)
        }
    }

    private var time = mutableListOf<Long>()
}
