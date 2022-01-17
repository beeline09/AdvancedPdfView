package com.github.barteksc.pdfviewer.decoding

import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.PdfFile
import com.github.barteksc.pdfviewer.source.DocumentSource
import org.benjinus.pdfium.util.Size
import java.lang.ref.WeakReference
import java.util.concurrent.Callable

internal class DecodingTask(
    private val docSource: DocumentSource,
    private val password: String? = null,
    private val userPages: IntArray = intArrayOf(),
    pdfView: PDFView
): Callable<PdfFile?> {

    private var pdfFile: PdfFile? = null

    private var pdfViewReference: WeakReference<PDFView>? = null

    init {
        pdfViewReference = WeakReference(pdfView)
    }

    @Throws(Exception::class)
    override fun call(): PdfFile? {
        val pdfView = pdfViewReference?.get()
        return if (pdfView != null) {
            val pdfiumSDK = docSource.createDocument(
                pdfView.context,
                password
            )
            pdfFile = PdfFile(
                pdfiumCore = pdfiumSDK,
                pageFitPolicy = pdfView.pageFitPolicy,
                viewSize = getViewSize(pdfView),
                originalUserPages = userPages,
                isVertical = pdfView.isSwipeVertical,
                spacingPx = pdfView.spacingPx,
                autoSpacing = pdfView.isAutoSpacingEnabled(),
                fitEachPage = pdfView.isFitEachPage
            )
            pdfFile?.textHighlightColor = pdfView.getTextHighlightColor()
            pdfFile
        } else {
            throw NullPointerException("pdfView == null")
        }
    }

    private fun getViewSize(pdfView: PDFView): Size {
        return Size(
            pdfView.width,
            pdfView.height
        )
    }
}