package com.github.barteksc.pdfviewer.util

import androidx.annotation.ColorInt

data class ColorScheme(
    /**
     * Цвет заливки (форм, цитат, сносок, цветного обрамления текста и т.п.)
     */
    @ColorInt
    var pathFillColor: Int,
    /**
     * Цвет линий, рамок таблиц и т.п. По умолчанию такой же как цвет текста
     */
    @ColorInt
    var pathStrokeColor: Int,
    /**
     * Цвет текста
     */
    @ColorInt
    var textFillColor: Int,

    @ColorInt
    var textStrokeColor: Int,

    /**
     * Фоновый цвет битмапа
     */
    @ColorInt
    var backgroundColor: Int,
)