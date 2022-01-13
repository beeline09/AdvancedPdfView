package com.github.barteksc.pdfviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.barteksc.pdfviewer.exception.PageRenderingException;
import com.github.barteksc.pdfviewer.model.PagePart;
import org.benjinus.pdfium.search.TextSearchContext;
import org.benjinus.pdfium.util.Size;

/**
 * A {@link Handler} that will process incoming {@link RenderingTask} messages
 * and alert {@link PDFView#onBitmapRendered(PagePart)} when the portion of the
 * PDF is ready to render.
 */
class RenderingHandler extends Handler {
    /**
     * {@link Message#what} kind of message this handler processes.
     */
    static final int MSG_RENDER_TASK = 1;
    static final int MSG_PARSE_TEXT_TASK = 2;

    private static final String TAG = RenderingHandler.class.getName();

    private final PDFView pdfView;

    private final RectF renderBounds = new RectF();
    private final Rect roundedRenderBounds = new Rect();
    private final Matrix renderMatrix = new Matrix();
    private boolean running = false;

    RenderingHandler(Looper looper, PDFView pdfView) {
        super(looper);
        this.pdfView = pdfView;
    }

    void addRenderingTask(int page, float width, float height, RectF bounds, boolean thumbnail,
            int cacheOrder, boolean bestQuality, boolean annotationRendering, String searchQuery) {
        RenderingTask task =
                new RenderingTask(width, height, bounds, page, thumbnail, cacheOrder, bestQuality,
                        annotationRendering, searchQuery);
        Message msg = obtainMessage(MSG_RENDER_TASK, task);
        sendMessage(msg);
    }

    void addParseTextTask(int page) {
        RenderingTask task = new RenderingTask(page, true);
        Message msg = obtainMessage(MSG_PARSE_TEXT_TASK, task);
        sendMessage(msg);
    }

    @Override
    public void handleMessage(Message message) {
        RenderingTask task = (RenderingTask) message.obj;
        try {
            if (task.parseTextFromPdf && message.what == MSG_PARSE_TEXT_TASK) {
                final String pageStr = parseText(task);
                if (running) {
                    pdfView.post(() -> pdfView.onTextParsed(task.page, pageStr));
                }
            } else {
                final PagePart part = proceed(task);
                if (part != null) {
                    if (running) {
                        pdfView.post(() -> pdfView.onBitmapRendered(part));
                    } else {
                        part.getRenderedBitmap().recycle();
                    }
                }
            }
        } catch (final PageRenderingException ex) {
            pdfView.post(() -> pdfView.onPageError(ex));
        }
    }

    @Nullable
    private String parseText(@NonNull RenderingTask renderingTask) throws PageRenderingException {
        PdfFile pdfFile = pdfView.pdfFile;
        pdfFile.openPage(renderingTask.page);
        int countCharsOnPage = pdfFile.countCharactersOnPage(renderingTask.page);
        if (countCharsOnPage > 0) {
            return pdfFile.extractCharacters(renderingTask.page, 0, countCharsOnPage);
        } else {
            return null;
        }
    }

    private PagePart proceed(RenderingTask renderingTask) throws PageRenderingException {
        PdfFile pdfFile = pdfView.pdfFile;
        pdfFile.openPage(renderingTask.page);

        int w = Math.round(renderingTask.width);
        int h = Math.round(renderingTask.height);

        Size nativePageSize = pdfFile.getPageSizeNative(renderingTask.page);
        int nativePageWidth = nativePageSize.getWidth();
        float nativePageHeight = nativePageSize.getHeight();

        if (w == 0 || h == 0 || pdfFile.pageHasError(renderingTask.page)) {
            return null;
        }

        Bitmap pageBitmap;
        try {
            pageBitmap = Bitmap.createBitmap(w, h,
                    /*renderingTask.bestQuality ? */
                    Bitmap.Config.ARGB_8888/* : Bitmap.Config.RGB_565*/);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot create bitmap", e);
            return null;
        }
        Canvas c = new Canvas(pageBitmap);
        Paint p = new Paint();
        if (pdfView.getColorScheme() != null) {
            p.setColor(pdfView.getColorScheme().getBackgroundColor());
        } else {
            p.setColor(Color.WHITE);
        }
        Rect bRect = new Rect(0, 0, pageBitmap.getWidth(), pageBitmap.getHeight());
        c.drawRect(bRect, p);

        calculateBounds(w, h, renderingTask.bounds);

        int rotation = pdfFile.getPageRotation(renderingTask.page);
        if (rotation != 0){
            Log.e(TAG, "Page rotation: "+ rotation+"_");
        }

        if (renderingTask.searchQuery != null && renderingTask.searchQuery.length() > 0) {
            TextSearchContext search =
                    pdfFile.newPageSearch(renderingTask.page, renderingTask.searchQuery, false,
                            false);

            if (search.hasNext()) {
                while (true) {
                    RectF rect = search.searchNext();
                    if (rect == null) {
                        break;
                    }
                    //If thumbnail
                    if (roundedRenderBounds.width() <= nativePageWidth) {
                        RectF currentRenderedRealRectByBounds =
                                new RectF(renderingTask.bounds.left * nativePageWidth,
                                        renderingTask.bounds.top * nativePageHeight,
                                        renderingTask.bounds.right * nativePageWidth,
                                        renderingTask.bounds.bottom * nativePageHeight);

                        if (rect.intersect(currentRenderedRealRectByBounds)) {

                            float l1 = (rect.left * roundedRenderBounds.width()) / nativePageWidth;
                            float t1 = roundedRenderBounds.height() -
                                    ((rect.top * roundedRenderBounds.height()) / nativePageHeight);
                            float r1 = (rect.right * roundedRenderBounds.width()) / nativePageWidth;
                            float b1 = roundedRenderBounds.height() -
                                    ((rect.bottom * roundedRenderBounds.height()) /
                                            nativePageHeight);

                            int strLen = renderingTask.searchQuery.length() - 1;
                            if (strLen < 1) {
                                strLen = 1;
                            }
                            float w1 = l1 + (r1 - l1) * (strLen);

                            p.setColor(pdfFile.getTextHighlightColor());
                            c.drawRect(new RectF(l1, t1, w1, b1), p);
                        } else {
                            break;
                        }
                    } else {
                        RectF rectForBitmap =
                                new RectF(renderingTask.bounds.left * renderBounds.width(),
                                        renderingTask.bounds.top * renderBounds.height(),
                                        renderingTask.bounds.right * renderBounds.width(),
                                        renderingTask.bounds.bottom * renderBounds.height());

                        float left = rect.left / nativePageWidth * renderBounds.width();
                        float rr = rect.right / nativePageWidth * renderBounds.width();
                        int strLen = renderingTask.searchQuery.length() - 2;
                        if (strLen < 1) {
                            strLen = 1;
                        }
                        if (renderingTask.searchQuery.length() <= 2 && strLen < 2) {
                            strLen = 2;
                        }
                        float symbolWidth = rr - left;
                        float ww1 = left + (symbolWidth) * (strLen);

                        RectF rectForSearch = new RectF(left, renderBounds.height() -
                                (rect.top / nativePageHeight * renderBounds.height()), ww1,
                                renderBounds.height() -
                                        (rect.bottom / nativePageHeight * renderBounds.height()));

                        if (/*rectForSearch.left <= rectForBitmap.right &&
                            rectForSearch.right >= rectForBitmap.left*/ rectForSearch
                                .intersect(rectForBitmap)) {

                            //                        float halfSymbolWidth = symbolWidth / 4.0f;

                            float l1 = (Math.abs(
                                    Math.abs(rectForSearch.left) - Math.abs(rectForBitmap.left)));
                            float t1 = Math.abs(
                                    Math.abs(rectForSearch.top) - Math.abs(rectForBitmap.top));
                            float r1 = l1 + rectForSearch.width();
                            float b1 = t1 + rectForSearch.height();

                            //                        float w1 = l1 + (r1 - l1) * (strLen);

                            RectF realRect =
                                    new RectF(Math.max(0, Math.min(pageBitmap.getWidth(), l1)),
                                            Math.max(0, Math.min(pageBitmap.getHeight(), t1)),
                                            Math.min(r1, pageBitmap.getWidth()),
                                            /*Math.min(pageBitmap.getHeight(), b1)*/
                                            Math.min(pageBitmap.getHeight(), b1));

                            p.setColor(pdfFile.getTextHighlightColor());
                            c.drawRect(realRect, p);
                        }
                    }
                }
            }
        }

        pdfFile.renderPageBitmap(pageBitmap, renderingTask.page, roundedRenderBounds,
                renderingTask.annotationRendering, pdfView.getColorScheme());

        return new PagePart(renderingTask.page, pageBitmap, renderingTask.bounds,
                renderingTask.thumbnail, renderingTask.cacheOrder, renderingTask.searchQuery);
    }

    private RectF rectToRectF(Rect rect) {
        return new RectF((float) rect.left, (float) rect.top, (float) rect.right,
                (float) rect.bottom);
    }

    private void calculateBounds(int width, int height, RectF pageSliceBounds) {
        renderMatrix.reset();
        renderMatrix.postTranslate(-pageSliceBounds.left * width, -pageSliceBounds.top * height);
        renderMatrix.postScale(1 / pageSliceBounds.width(), 1 / pageSliceBounds.height());

        renderBounds.set(0, 0, width, height);
        renderMatrix.mapRect(renderBounds);
        renderBounds.round(roundedRenderBounds);
    }

    void stop() {
        running = false;
    }

    void start() {
        running = true;
    }

    private static class RenderingTask {

        float width, height;

        RectF bounds;

        int page;

        boolean thumbnail;

        int cacheOrder;

        boolean bestQuality;

        boolean annotationRendering;

        String searchQuery;

        boolean parseTextFromPdf = false;

        RenderingTask(float width, float height, RectF bounds, int page, boolean thumbnail,
                int cacheOrder, boolean bestQuality, boolean annotationRendering,
                String searchQuery) {
            this.page = page;
            this.width = width;
            this.height = height;
            this.bounds = bounds;
            this.thumbnail = thumbnail;
            this.cacheOrder = cacheOrder;
            this.bestQuality = bestQuality;
            this.annotationRendering = annotationRendering;
            this.searchQuery = searchQuery;
        }

        RenderingTask(int page, boolean parseText) {
            this.parseTextFromPdf = parseText;
            this.page = page;
            this.width = 0;
            this.height = 0;
            this.bounds = null;
            this.thumbnail = false;
            this.cacheOrder = 0;
            this.bestQuality = false;
            this.annotationRendering = false;
            this.searchQuery = searchQuery;
        }
    }
}
