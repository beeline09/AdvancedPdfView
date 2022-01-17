/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import org.benjinus.pdfium.Bookmark;
import org.benjinus.pdfium.Meta;

public class PDFViewActivity extends AppCompatActivity
        implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "3945.pdf";
    //public static final String SAMPLE_FILE = "test.pdf";
    //    public static final String SAMPLE_FILE = "matt_power.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    PDFView pdfView;

    Uri uri;

    Integer pageNumber = 0;

    String pdfFileName;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.pickFile) {
/*            if (pdfView.getSearchQuery().equalsIgnoreCase("Visual Studio")){
                pdfView.setSearchQuery("");
            } else {
                pdfView.setSearchQuery("Visual Studio");
            }*/

//            if (pdfView.isColorSchemeOverridden()){
//                pdfView.overrideColorScheme(null);
//            } else {
//                pdfView.overrideColorScheme(new ColorScheme(
//                        Color.parseColor("#AAFFFFFF"), //BLUE
//                        Color.parseColor("#FFFFFFFF"), //линии, рамки...
//                        Color.parseColor("#FFFFFFFF"), //текст
//                        Color.parseColor("#FF000000"),
//                        Color.BLACK
//                        ) //BLACL
//                );
//            }
            int countPages = pdfView.getTotalPagesCount();
            ArrayList<Integer> arr = new ArrayList<>();
            for (int i = 0; i < countPages; i++) {
                arr.add(i);
            }
//            pdfView.parseText(arr, new PDFView.OnTextParseListener() {
//                @Override
//                public void onTextParseSuccess(int pageIndex, @NonNull String text) {
//
//                }
//
//                @Override
//                public void onTextParseError(int pageIndex) {
//
//                }
//            });

            for (int i = 0 ; i < arr.size(); i+=10){
                pdfView.renderPageBitmap(i, true);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pdfView = findViewById(R.id.pdfView);
        afterViews();
    }

    void afterViews() {
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri == null) {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(50) // in dp
                .defaultPage(45)
                .textHighlightColor(Color.RED)
//                .scrollHandle(null)
                .onPageChange(
                        (page, pageCount) -> Log.e(TAG, "Current page is "+page+" of "+pageCount+""))
                //                .nightMode(true)
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .onPageBitmapRendered(
                        (pageIndex, bitmap) -> Log.e(TAG, "Page "+pageIndex+" bitmap rendered"))
                .load();

        pdfView.setSearchQuery("angular");
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        Meta meta = pdfView.getDocumentMeta();
        if (meta != null) {
            Log.e(TAG, "title = " + meta.getTitle());
            Log.e(TAG, "author = " + meta.getAuthor());
            Log.e(TAG, "subject = " + meta.getSubject());
            Log.e(TAG, "keywords = " + meta.getKeywords());
            Log.e(TAG, "creator = " + meta.getCreator());
            Log.e(TAG, "producer = " + meta.getProducer());
            Log.e(TAG, "creationDate = " + meta.getCreationDate());
            Log.e(TAG, "modDate = " + meta.getModDate());
        }

//        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }

    public void printBookmarksTree(@NonNull List<Bookmark> tree, String sep) {
        for (Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }
}
