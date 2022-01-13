/**
 * Copyright 2017 Bartosz Schiller
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer.link

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.model.LinkTapEvent

class DefaultLinkHandler(
    private val pdfView: PDFView,
    private val chooserTitle: String = "Select app for open link"
): LinkHandler {
    override fun handleLinkEvent(event: LinkTapEvent?) {
        val uri = event?.link?.uri
        val page = event?.link?.destPageIdx
        if (!uri.isNullOrBlank()) {
            handleUri(uri)
        } else page?.let { handlePage(it) }
    }

    private fun handleUri(uri: String) {
        val parsedUri = Uri.parse(uri)
        val intent = Intent(
            Intent.ACTION_VIEW,
            parsedUri
        )
        val context = pdfView.context
        // Create intent to show chooser
        val chooser = Intent.createChooser(
            intent,
            chooserTitle
        )

        // Try to invoke the intent.
        try {
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            // Define what your app should do if no activity can handle the intent.
            Log.e(
                TAG,
                e.message ?: "NULL"
            )
            Toast.makeText(
                context,
                "No apps can open this link",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handlePage(page: Int) {
        pdfView.jumpTo(page)
    }

    companion object {
        private val TAG = DefaultLinkHandler::class.java.simpleName
    }
}