package de.coldtea.verborum.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {
    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ViewModel", "Uncaught exception", exception)
    }

    protected fun <T> Flow<T>.observe(
        onSuccess: suspend (T) -> Unit = {},
        onCompleted: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
    ): Job = viewModelScope.launch(exceptionHandler) {
        this@observe
            .onCompletion {
                onCompleted?.invoke()
            }
            .catch { e ->
                onError?.invoke(e) ?: throw e
            }
            .collect { onSuccess(it) }
    }
}