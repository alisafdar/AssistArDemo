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
    private val libLoaded = AtomicBoolean(false)
    @Volatile private var delegate: String = "TFLite_CPU"

    /** Fire-and-forget async kick-off (kept for convenience). */
    fun kickOff(context: Context) {
        if (ready.isCompleted || ready.isActive) return
        val appCtx = context.applicationContext
        GlobalScope.launch(Dispatchers.Default) {
            runInit(appCtx, preferGpu = true)
        }
    }

    /** Blocking init you can call from Application.onCreate(). */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun initBlocking(context: Context, preferGpu: Boolean = true, timeoutMs: Long = 500): Boolean {
        if (ready.isCompleted && ready.getCompletionExceptionOrNull() == null) return true

        val appCtx = context.applicationContext
        val latch = CountDownLatch(1)
        var ok = false

        GlobalScope.launch(Dispatchers.Default) {
            ok = runInit(appCtx, preferGpu)
            latch.countDown()
        }

        latch.await(timeoutMs, TimeUnit.MILLISECONDS)

        return ok && ready.isCompleted && ready.getCompletionExceptionOrNull() == null
    }

    /** Must be called before first JNI TFLite call if you didn’t use initBlocking. */
    suspend fun awaitReady() = ready.await()

    fun delegateName(): String = delegate

    fun ensureNativeLibraryLoaded(soName: String = "assistvision") {
        if (libLoaded.compareAndSet(false, true)) {
            System.loadLibrary(soName)
        }
    }

    private suspend fun runInit(appCtx: Context, preferGpu: Boolean): Boolean {
        if (ready.isCompleted) return true
        return try {
            // 1) Check GPU availability (Task<Boolean>)
            val gpuAvailable = try { TfLiteGpu.isGpuDelegateAvailable(appCtx).await() } catch (_: Throwable) { false }
            val wantGpu = preferGpu && gpuAvailable

            fun opts(enableGpu: Boolean) = TfLiteInitializationOptions.builder()
                .setEnableGpuDelegateSupport(enableGpu)
                .build()

            // 2) Try GPU first (if desired), else CPU; on GPU failure, fall back to CPU
            try {
                TfLiteNative.initialize(appCtx, opts(wantGpu)).await()
                delegate = if (wantGpu) "TFLite_GPU" else "TFLite_CPU"
            } catch (gpuFail: Throwable) {
                if (wantGpu) {
                    // fallback to CPU
                    TfLiteNative.initialize(appCtx, opts(false)).await()
                    delegate = "TFLite_CPU"
                } else {
                    throw gpuFail
                }
            }

            ready.complete(Unit)
            true
        } catch (e: Throwable) {
            if (!ready.isCompleted) ready.completeExceptionally(e)
            false
        }
    }
}
