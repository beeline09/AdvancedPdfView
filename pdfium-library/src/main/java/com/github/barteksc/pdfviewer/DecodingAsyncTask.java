package com.github.barteksc.pdfviewer;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.github.barteksc.pdfviewer.source.DocumentSource;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.util.Size;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;

    private final WeakReference<PDFView> pdfViewReference;

    private final String password;
    private final DocumentSource docSource;
    private final int[] userPages;
    private PdfFile pdfFile;

    DecodingAsyncTask(DocumentSource docSource, String password, int[] userPages, PDFView pdfView) {
        this.docSource = docSource;
        this.userPages = userPages;
        this.cancelled = false;
        this.pdfViewReference = new WeakReference<>(pdfView);
        this.password = password;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            PDFView pdfView = pdfViewReference.get();
            if (pdfView != null) {
                PdfiumSDK pdfiumSDK = docSource.createDocument(pdfView.getContext(), password);
                pdfFile = new PdfFile(pdfiumSDK, pdfView.getPageFitPolicy(), getViewSize(pdfView),
                        userPages, pdfView.isSwipeVertical(), pdfView.getSpacingPx(),
                        pdfView.isAutoSpacingEnabled(), pdfView.isFitEachPage());
                return null;
            } else {
                return new NullPointerException("pdfView == null");
            }

        } catch (Throwable t) {
            return t;
        }
    }

    private Size getViewSize(PDFView pdfView) {
        return new Size(pdfView.getWidth(), pdfView.getHeight());
    }

    @Override
    protected void onPostExecute(Throwable t) {
        PDFView pdfView = pdfViewReference.get();
        if (pdfView != null) {
            if (t != null) {
                pdfView.loadError(t);
                return;
            }
            if (!cancelled) {
                pdfView.loadComplete(pdfFile);
            }
        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
