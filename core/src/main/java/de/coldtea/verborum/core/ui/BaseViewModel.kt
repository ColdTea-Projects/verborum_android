package de.coldtea.verborum.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    // UiText, not String: messages are resolved to a localized string in the UI layer, since a
    // ViewModel has no Context. See [ShowSnackbarMessages].
    protected val _snackbarMessages = MutableSharedFlow<UiText>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ViewModel", "Uncaught exception", exception)
    }

    protected fun <T> Flow<T>.observe(
        onSuccess: suspend (T) -> Unit = {},
        onCompleted: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
    ): Job = viewModelScope.launch(exceptionHandler) {
        this@observe
            // onSuccess runs upstream of catch on purpose: catch is exception-transparent, so a
            // throw inside a collect block would bypass onError and silently kill the collection.
            .onEach { onSuccess(it) }
            .onCompletion {
                onCompleted?.invoke()
            }
            .catch { e ->
                onError?.invoke(e) ?: throw e
            }
            .collect()
    }
}