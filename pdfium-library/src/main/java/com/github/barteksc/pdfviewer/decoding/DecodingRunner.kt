package com.github.barteksc.pdfviewer.decoding

import android.os.Handler
import android.os.Looper
import androidx.annotation.Nullable
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.PdfFile
import com.github.barteksc.pdfviewer.source.DocumentSource
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DecodingRunner(
    private val docSource: DocumentSource,
    private val password: String? = null,
    pages: IntArray? = null,
    private val pdfView: PDFView
) {
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val userPages: IntArray = pages ?: intArrayOf()

    interface Callback {
        fun onComplete(
            @Nullable
            result: PdfFile?
        )

        fun onError(
            @Nullable
            t: Throwable
        )
    }

    fun executeAsync(
        callback: Callback
    ) {

        val task = DecodingTask(
            docSource = docSource,
            password = password,
            userPages = userPages,
            pdfView = pdfView
        )

        executor.execute {
            try {
                val pdfFile: PdfFile? = task.call()
                handler.post {
                    callback.onComplete(pdfFile)
                }
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }
}