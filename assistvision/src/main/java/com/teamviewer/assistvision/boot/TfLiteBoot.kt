package com.teamviewer.assistvision.boot

import android.content.Context
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLiteNative
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.ExperimentalCoroutinesApi

object TfLiteBoot {
    private val ready = CompletableDeferred<Unit>()
    private val isLibraryLoaded = AtomicBoolean(false)
    @Volatile private var delegate: String = "TFLite_CPU"

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initBlocking(context: Context, preferGpu: Boolean = true, timeoutMs: Long = 500): Boolean {
        if (ready.isCompleted && ready.getCompletionExceptionOrNull() == null) return true

        val context = context.applicationContext
        val latch = CountDownLatch(1)
        var initialized = false

        GlobalScope.launch(Dispatchers.Default) {
            initialized = runInit(context, preferGpu)
            latch.countDown()
        }

        latch.await(timeoutMs, TimeUnit.MILLISECONDS)

        return initialized && ready.isCompleted && ready.getCompletionExceptionOrNull() == null
    }

    suspend fun awaitReady() = ready.await()

    fun ensureNativeLibraryLoaded(libraryName: String = "assistvision") {
        if (isLibraryLoaded.compareAndSet(false, true)) {
            System.loadLibrary(libraryName)
        }
    }
    private suspend fun runInit(
        context: Context,
        preferGpu: Boolean
    ): Boolean {
        if (ready.isCompleted) return true
        return try {
            // 1) Check GPU availability (Task<Boolean>)
            val gpuAvailable = try { TfLiteGpu.isGpuDelegateAvailable(context).await() } catch (_: Throwable) { false }
            val wantGpu = preferGpu && gpuAvailable

            fun opts(enableGpu: Boolean) = TfLiteInitializationOptions.builder()
                .setEnableGpuDelegateSupport(enableGpu)
                .build()

            // 2) Try GPU first (if desired), else CPU; on GPU failure, fall back to CPU
            try {
                TfLiteNative.initialize(context, opts(wantGpu)).await()
                delegate = if (wantGpu) "TFLite_GPU" else "TFLite_CPU"
            } catch (gpuFail: Throwable) {
                if (wantGpu) {
                    // fallback to CPU
                    TfLiteNative.initialize(context, opts(false)).await()
                    delegate = "TFLite_CPU"
                } else {
                    throw gpuFail
                }
            }

            ready.complete(Unit)
            true
        } catch (e: Throwable) {
            if (ready.isCompleted.not()) ready.completeExceptionally(e)
            false
        }
    }
}
