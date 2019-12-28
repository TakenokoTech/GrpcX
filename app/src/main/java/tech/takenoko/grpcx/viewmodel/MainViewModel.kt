package tech.takenoko.grpcx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tech.takenoko.grpcx.App
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.usecase.unuse.ChannelUsecase

class MainViewModel : ViewModel() {

    private val _textLiveData = MediatorLiveData<String>()
    val textLiveData: LiveData<String> = _textLiveData

    private val _listLiveData = MediatorLiveData<List<String>>()
    val listLiveData: LiveData<List<String>> = _listLiveData

    // private val mainUsecase = StreamUsecase(App.context, viewModelScope)
    private val mainUsecase = ChannelUsecase(App.context, viewModelScope)

    init {
        _listLiveData.postValue(listOf())
        _textLiveData.postValue("")
        _textLiveData.addSource(mainUsecase.source) { handler(it) }
        mainUsecase.execute(Unit)

        _textLiveData.addSource(mainUsecase.fpsLiveData) { _textLiveData.value = "FPS: $it" }
//        viewModelScope.launch (Dispatchers.IO) { while (true) fps { Thread.sleep(1000/60) }}
    }

    private fun handler(result: UsecaseResult<String>): Any = when (result) {
        is UsecaseResult.Pending -> {
            // _textLiveData.value = "loading"-
        }
        is UsecaseResult.Resolved -> {
            // _textLiveData.value = result.value

            var list = _listLiveData.value?.toMutableList() ?: mutableListOf()
            list.add(result.value)
            list = list.drop(if (list.size > 5) 1 else 0).toMutableList()
            _listLiveData.value = list

            mainUsecase.execute(Unit)
        }
        is UsecaseResult.Rejected -> {
            _textLiveData.value = result.reason.localizedMessage
        }
    }
}
