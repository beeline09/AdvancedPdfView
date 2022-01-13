package com.github.barteksc.pdfviewer.scroll

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.Util
import org.benjinus.pdfium.R

class DefaultScrollHandle @JvmOverloads constructor(
    context: Context,
    private val inverted: Boolean = false
): RelativeLayout(context), ScrollHandle {
    private var relativeHandlerMiddle = 0f
    private var textView: TextView = TextView(context)
    private var pdfView: PDFView? = null
    private var currentPos = 0f

    //    private val handler: Handler = Handler(Looper.getMainLooper())
    private val hidePageScrollerRunnable = Runnable { hide() }
    override fun setupLayout(pdfView: PDFView) {
        val align: Int
        val width: Int
        val height: Int
        val background: Drawable?
        // determine handler position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical) {
            width = HANDLE_LONG
            height = HANDLE_SHORT
            if (inverted) { // left
                align = ALIGN_PARENT_LEFT
                background = ContextCompat.getDrawable(
                    context,
                    R.drawable.default_scroll_handle_left
                )
            } else { // right
                align = ALIGN_PARENT_RIGHT
                background = ContextCompat.getDrawable(
                    context,
                    R.drawable.default_scroll_handle_right
                )
            }
        } else {
            width = HANDLE_SHORT
            height = HANDLE_LONG
            if (inverted) { // top
                align = ALIGN_PARENT_TOP
                background = ContextCompat.getDrawable(
                    context,
                    R.drawable.default_scroll_handle_top
                )
            } else { // bottom
                align = ALIGN_PARENT_BOTTOM
                background = ContextCompat.getDrawable(
                    context,
                    R.drawable.default_scroll_handle_bottom
                )
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(background)
        } else {
            setBackground(background)
        }
        val lp = LayoutParams(
            Util.getDP(
                context,
                width
            ),
            Util.getDP(
                context,
                height
            )
        )
        lp.setMargins(
            0,
            0,
            0,
            0
        )
        val tvlp = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tvlp.addRule(
            CENTER_IN_PARENT,
            TRUE
        )
        addView(
            textView,
            tvlp
        )
        lp.addRule(align)
        pdfView.addView(
            this,
            lp
        )
        this.pdfView = pdfView
    }

    override fun destroyLayout() {
        pdfView?.removeView(this)
    }

    override fun setScroll(position: Float) {
        if (!shown()) {
            show()
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable)
        }
        if (pdfView != null) {
            setPosition(
                (if (pdfView?.isSwipeVertical == true) pdfView?.height ?: 0 else pdfView?.width
                    ?: 0) * position
            )
        }
    }

    private fun setPosition(pos: Float) {
        var position: Float = pos
        if (java.lang.Float.isInfinite(position) || java.lang.Float.isNaN(position)) {
            return
        }
        val pdfViewSize: Float = if (pdfView?.isSwipeVertical == true) {
            pdfView?.height?.toFloat() ?: 0f
        } else {
            pdfView?.width?.toFloat() ?: 0f
        }
        position -= relativeHandlerMiddle
        if (position < 0) {
            position = 0f
        } else if (position > pdfViewSize - Util.getDP(
                context,
                HANDLE_SHORT
            )
        ) {
            position = pdfViewSize - Util.getDP(
                context,
                HANDLE_SHORT
            )
        }
        if (pdfView?.isSwipeVertical == true) {
            y = position
        } else {
            x = position
        }
        calculateMiddle()
        invalidate()
    }

    private fun calculateMiddle() {
        val pos: Float
        val viewSize: Float
        val pdfViewSize: Float
        if (pdfView?.isSwipeVertical == true) {
            pos = y
            viewSize = height.toFloat()
            pdfViewSize = pdfView?.height?.toFloat() ?: 0f
        } else {
            pos = x
            viewSize = width.toFloat()
            pdfViewSize = pdfView?.width?.toFloat() ?: 0f
        }
        relativeHandlerMiddle = (pos + relativeHandlerMiddle) / pdfViewSize * viewSize
    }

    override fun hideDelayed() {
        handler.postDelayed(
            hidePageScrollerRunnable,
            1000
        )
    }

    override fun setPageNum(pageNum: Int) {
        val text = pageNum.toString()
        if (textView.text != text) {
            textView.text = text
        }
    }

    override fun shown(): Boolean {
        return visibility == VISIBLE
    }

    override fun show() {
        visibility = VISIBLE
    }

    override fun hide() {
        visibility = INVISIBLE
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    /**
     * @param size text size in dp
     */
    fun setTextSize(size: Int) {
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_DIP,
            size.toFloat()
        )
    }

    private val isPDFViewReady: Boolean
        get() = pdfView != null && pdfView?.pageCount ?: 0 > 0 && pdfView?.documentFitsView() != true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isPDFViewReady) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                pdfView?.stopFling()
                handler.removeCallbacks(hidePageScrollerRunnable)
                currentPos = if (pdfView?.isSwipeVertical == true) {
                    event.rawY - y
                } else {
                    event.rawX - x
                }
                if (pdfView?.isSwipeVertical == true) {
                    setPosition(event.rawY - currentPos + relativeHandlerMiddle)
                    pdfView?.setPositionOffset(
                        relativeHandlerMiddle / height.toFloat(),
                        false
                    )
                } else {
                    setPosition(event.rawX - currentPos + relativeHandlerMiddle)
                    pdfView?.setPositionOffset(
                        relativeHandlerMiddle / width.toFloat(),
                        false
                    )
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (pdfView?.isSwipeVertical == true) {
                    setPosition(event.rawY - currentPos + relativeHandlerMiddle)
                    pdfView?.setPositionOffset(
                        relativeHandlerMiddle / height.toFloat(),
                        false
                    )
                } else {
                    setPosition(event.rawX - currentPos + relativeHandlerMiddle)
                    pdfView?.setPositionOffset(
                        relativeHandlerMiddle / width.toFloat(),
                        false
                    )
                }
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                hideDelayed()
                pdfView?.performPageSnap()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val HANDLE_LONG = 65
        private const val HANDLE_SHORT = 40
        private const val DEFAULT_TEXT_SIZE = 16
    }

    init {
        visibility = INVISIBLE
        setTextColor(Color.BLACK)
        setTextSize(DEFAULT_TEXT_SIZE)
    }
}