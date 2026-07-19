package io.github.daisukikaffuchino.han1meviewer.util

import com.chad.library.adapter4.BaseQuickAdapter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <T : Any> BaseQuickAdapter<T, *>.awaitSubmitList(list: List<T>?) =
    suspendCoroutine { cont ->
        submitList(list) {
            cont.resume(Unit)
        }
    }

@OptIn(ExperimentalContracts::class)
@Suppress("NOTHING_TO_INLINE")
@Deprecated("Use safe call instead, this can easily cause NPE.", ReplaceWith("this ?: return"))
inline fun <T> T?.notNull(): T {
    contract {
        returns() implies (this@notNull != null)
    }
    return checkNotNull(this)
}
