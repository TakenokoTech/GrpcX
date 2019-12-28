package tech.takenoko.grpcx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.math.max
import tech.takenoko.grpcx.App
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.usecase.PollingUsecase
import tech.takenoko.grpcx.usecase.RestUsecase

class FpsViewModel : ViewModel() {

    private val _textLiveData = MediatorLiveData<String>()
    val textLiveData: LiveData<String> = _textLiveData

    private val _listLiveData = MediatorLiveData<List<String>>()
    val listLiveData: LiveData<List<String>> = _listLiveData

    private val _fpsLiveData = MediatorLiveData<List<Long>>()
    val fpsLiveData: LiveData<List<Long>> = _fpsLiveData

    val pollingUsecase = PollingUsecase(App.context, viewModelScope)
    val restUsecase = RestUsecase(App.context, viewModelScope)

    init {
        _listLiveData.postValue(listOf("", ""))
        _textLiveData.postValue("")
        _textLiveData.addSource(pollingUsecase.source) { pollingUsecaseHandler(it) }
        _textLiveData.addSource(restUsecase.source) { restUsecaseHandler(it) }

        _fpsLiveData.postValue((0..100).map { 0L })
        pollingUsecase.execute(Unit)
        restUsecase.execute(Unit)
    }

    private fun pollingUsecaseHandler(result: UsecaseResult<String>): Any = when (result) {
        is UsecaseResult.Pending -> { }
        is UsecaseResult.Resolved -> {
            fps()
            val list = _listLiveData.value?.toMutableList() ?: mutableListOf("", "")
            list[0] = result.value
            _listLiveData.value = list
        }
        is UsecaseResult.Rejected -> {
            _textLiveData.value = result.reason.localizedMessage
        }
    }

    private fun restUsecaseHandler(result: UsecaseResult<String>): Any = when (result) {
        is UsecaseResult.Pending -> { }
        is UsecaseResult.Resolved -> {
            // fps()
            val list = _listLiveData.value?.toMutableList() ?: mutableListOf("", "")
            list[1] = result.value
            _listLiveData.value = list
        }
        is UsecaseResult.Rejected -> {
            _textLiveData.value = result.reason.localizedMessage
        }
    }

    private fun fps() {
        time.add(System.currentTimeMillis())
        if (time.size == 30) {
            val list = (0 until time.size - 1).map { time[it + 1] - time[it] }
            val result = list.size / (list.sum() / 1000.0)
            time = mutableListOf()
            _textLiveData.postValue("FPS: $result")

            val newList = (_fpsLiveData.value?.toMutableList() ?: mutableListOf()).apply { add(result.toLong()) }.drop(max(0, (_fpsLiveData.value?.size ?: 0) - 100))
            _fpsLiveData.postValue(newList)
        }
    }

    private var time = mutableListOf<Long>()
}
