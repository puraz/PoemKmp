package viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewModel {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    open fun onCleared() {
        viewModelScope.cancel()
    }

    protected fun <T> MutableState<T>.asState(): State<T> = this
} 