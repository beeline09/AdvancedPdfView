package com.github.barteksc.pdfviewer.util

fun ColorScheme.toArray(): IntArray {
    return intArrayOf(
        pathFillColor,
        pathStrokeColor,
        textFillColor,
        textStrokeColor,
        backgroundColor
    )
}