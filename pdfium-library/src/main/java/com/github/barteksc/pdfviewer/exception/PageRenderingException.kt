package com.github.barteksc.pdfviewer.exception

import java.lang.Exception

class PageRenderingException(
    val page: Int,
    cause: Throwable?
): Exception(cause)