package com.github.barteksc.pdfviewer.util

import android.graphics.RectF

fun RectF.addArea(scaleFactorVertical: Float = 0.3f) {
    if (this.top < this.bottom) {
        this.top -= this.height() * scaleFactorVertical
        this.bottom += this.height() * scaleFactorVertical
    } else {
        this.top += this.height() * scaleFactorVertical
        this.bottom -= this.height() * scaleFactorVertical
    }
}