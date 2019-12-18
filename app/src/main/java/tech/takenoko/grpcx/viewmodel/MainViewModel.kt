package tech.takenoko.grpcx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tech.takenoko.grpcx.App
import tech.takenoko.grpcx.entities.UsecaseResult
import tech.takenoko.grpcx.usecase.MainUsecase

class MainViewModel : ViewModel() {

    private val _textLiveData = MediatorLiveData<String>()
    val textLiveData: LiveData<String> = _textLiveData

    private val mainUsecase = MainUsecase(App.context, viewModelScope)

    init {
        _textLiveData.postValue("")
        _textLiveData.addSource(mainUsecase.source) { handler(it) }
        mainUsecase.execute(Unit)
    }

    private fun handler(result: UsecaseResult<String>): Any = when (result) {
        is UsecaseResult.Pending -> Unit
        is UsecaseResult.Resolved -> _textLiveData.value = result.value
        is UsecaseResult.Rejected -> _textLiveData.value = result.reason.localizedMessage
    }
}
