package com.tofukma.serverorderapp.adapter

import android.content.Context
import android.icu.util.Output
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.printservice.PrintDocument
import androidx.annotation.RequiresApi
import com.tofukma.serverorderapp.common.Common
import java.io.*
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.KITKAT)
class PdfDocumentAdapter(var context: Context, var path: String): PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
      if(cancellationSignal!!.isCanceled)
          callback!!.onLayoutCancelled()
      else {
          val builder = PrintDocumentInfo.Builder(Common.FILE_PRINT)
          builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
              .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
              .build()
          callback!!.onLayoutFinished(builder.build(),newAttributes != oldAttributes)
      }
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        var `in` : InputStream?= null
        var `out`: OutputStream?=null
        try {
            val file = File(path)
            `in` = FileInputStream(file)
            `out` = FileOutputStream(destination!!.fileDescriptor)

            if (!cancellationSignal!!.isCanceled){
                `in`.copyTo(out)
                callback!!.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } else {
                callback!!.onWriteCancelled()

            }


        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            try {
                `in`!!.close()
                `out`!!.close()
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

}