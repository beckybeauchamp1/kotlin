/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:kotlin.jvm.JvmName("CoroutineIntrinsics")
package kotlin.jvm.internal

import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationDispatcher

fun <T> normalizeContinuation(c: Continuation<T>): Continuation<T> {
    if (c is CoroutineImpl) {
        return c.facade
    }

    return c
}

internal fun <T> wrapContinuationIfNeeded(c: Continuation<T>, dispatcher: ContinuationDispatcher?): Continuation<T> {
    if (dispatcher == null) return c
    return DispatchedContinuationImpl(c, dispatcher)
}

private class DispatchedContinuationImpl<in T>(
        private val c: Continuation<T>,
        private val dispatcher: ContinuationDispatcher
) : Continuation<T> {
    override fun resume(value: T) {
        if (!dispatcher.dispatchResume(value, c)) {
            c.resume(value)
        }
    }

    override fun resumeWithException(exception: Throwable) {
        if (!dispatcher.dispatchResumeWithException(exception, c)) {
            c.resumeWithException(exception)
        }
    }
}
