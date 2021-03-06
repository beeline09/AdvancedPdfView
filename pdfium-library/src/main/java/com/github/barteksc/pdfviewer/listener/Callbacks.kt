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
package com.github.barteksc.pdfviewer.listener

import android.view.MotionEvent
import com.github.barteksc.pdfviewer.link.LinkHandler
import com.github.barteksc.pdfviewer.model.LinkTapEvent

class Callbacks {
    /**
     * Call back object to call when the PDF is loaded
     */
    private var onLoadCompleteListener: OnLoadCompleteListener? = null

    /**
     * Call back object to call when document loading error occurs
     */
    var onError: OnErrorListener? = null

    /**
     * Call back object to call when the page load error occurs
     */
    private var onPageErrorListener: OnPageErrorListener? = null

    /**
     * Call back object to call when the document is initially rendered
     */
    private var onRenderListener: OnRenderListener? = null

    /**
     * Call back object to call when the page has changed
     */
    private var onPageChangeListener: OnPageChangeListener? = null

    /**
     * Call back object to call when the page is scrolled
     */
    private var onPageScrollListener: OnPageScrollListener? = null

    /**
     * Call back object to call when the above layer is to drawn
     */
    var onDraw: OnDrawListener? = null
    var onDrawAll: OnDrawListener? = null

    /**
     * Call back object to call when the user does a tap gesture
     */
    private var onTapListener: OnTapListener? = null

    /**
     * Call back object to call when the user does a long tap gesture
     */
    private var onLongPressListener: OnLongPressListener? = null

    /**
     * Call back object to call when clicking link
     */
    private var linkHandler: LinkHandler? = null
    fun setOnLoadComplete(onLoadCompleteListener: OnLoadCompleteListener?) {
        this.onLoadCompleteListener = onLoadCompleteListener
    }

    fun callOnLoadComplete(pagesCount: Int) {
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener?.loadComplete(pagesCount)
        }
    }

    fun setOnPageError(onPageErrorListener: OnPageErrorListener?) {
        this.onPageErrorListener = onPageErrorListener
    }

    fun callOnPageError(
        page: Int,
        error: Throwable?
    ): Boolean {
        if (onPageErrorListener != null) {
            onPageErrorListener?.onPageError(
                page,
                error
            )
            return true
        }
        return false
    }

    fun setOnRender(onRenderListener: OnRenderListener?) {
        this.onRenderListener = onRenderListener
    }

    fun callOnRender(pagesCount: Int) {
        if (onRenderListener != null) {
            onRenderListener?.onInitiallyRendered(pagesCount)
        }
    }

    fun setOnPageChange(onPageChangeListener: OnPageChangeListener?) {
        this.onPageChangeListener = onPageChangeListener
    }

    fun callOnPageChange(
        page: Int,
        pagesCount: Int
    ) {
        if (onPageChangeListener != null) {
            onPageChangeListener?.onPageChanged(
                page,
                pagesCount
            )
        }
    }

    fun setOnPageScroll(onPageScrollListener: OnPageScrollListener?) {
        this.onPageScrollListener = onPageScrollListener
    }

    fun callOnPageScroll(
        currentPage: Int,
        offset: Float
    ) {
        if (onPageScrollListener != null) {
            onPageScrollListener?.onPageScrolled(
                currentPage,
                offset
            )
        }
    }

    fun setOnTap(onTapListener: OnTapListener?) {
        this.onTapListener = onTapListener
    }

    fun callOnTap(event: MotionEvent?): Boolean {
        return onTapListener?.onTap(event) == true
    }

    fun setOnLongPress(onLongPressListener: OnLongPressListener?) {
        this.onLongPressListener = onLongPressListener
    }

    fun callOnLongPress(event: MotionEvent?) {
        if (onLongPressListener != null) {
            onLongPressListener?.onLongPress(event)
        }
    }

    fun setLinkHandler(linkHandler: LinkHandler?) {
        this.linkHandler = linkHandler
    }

    fun callLinkHandler(event: LinkTapEvent?) {
        if (linkHandler != null) {
            linkHandler?.handleLinkEvent(event)
        }
    }
}