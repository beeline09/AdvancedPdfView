# AdvancedPdfView

This project is a merger of two libraries [barteksc Android PdfViewer](https://github.com/barteksc/AndroidPdfViewer) and [benjinus PDFium Library for Android](https://github.com/benjinus/android-support-pdfium), translated into Kotlin, as well as a refinement to implement the following functionality:
1. Highlighting the found fragment with search query on the page
2. Extract text pages for further analysis. This is necessary, for example, to store information in a database for faster retrieval in the future.
3. Standard generation of bitmaps for custom actions.
4. **Dark theme support!!!** Not a simple inversion, as implemented in most solutions on the network, but by changing the color scheme. In this case, the pictures remain unchanged, and the text is drawn in the color that you specify.

To implement my needs, I had to make some changes to the pdfium C++ code
