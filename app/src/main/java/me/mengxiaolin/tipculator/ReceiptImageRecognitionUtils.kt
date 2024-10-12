package me.mengxiaolin.tipculator

import android.content.Context
import android.graphics.Rect
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

data class ReceiptImageRecognitionResult(val subTotal: Int, val tax: Int)

fun receiptImageTextRecognition(context: Context, file: File, callback: (ReceiptImageRecognitionResult?, Exception?)-> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = try {
        InputImage.fromFilePath(context, file.toUri())
    } catch (e: IOException) {
        callback(null, e)
        return
    }
    recognizer.process(image)
        .addOnSuccessListener { text ->
            android.util.Log.d("receiptImageTextRecognition", "total lines = " + text.textBlocks.size)
            var subTotalLabelBoundingBox: Rect? = null
            var taxLabelBoundingBox: Rect? = null
            var totalLabelBoundingBox: Rect? = null
            val priceTextRects = mutableListOf<Pair<String, Rect>>()
            for (block in text.textBlocks) {
                val textContent = block.text.uppercase()
                if (textContent.contains("SUBTOTAL") || textContent.contains("SUB-TOTAL")) {
                    subTotalLabelBoundingBox = block.boundingBox
                }
                if (textContent.contains("TAX")) {
                    taxLabelBoundingBox = block.boundingBox
                }
                if (textContent.contains("TOTAL") || textContent.contains("AMOUNT")) {
                    totalLabelBoundingBox = block.boundingBox
                }
                for (line in block.text.lines()) {
                    val boundingBox = block.boundingBox
                    if (boundingBox!= null && line.trim().matches(Regex("\\$?[0-9]+\\.[0-9]+"))) {
                        priceTextRects.add(
                            Pair(line, boundingBox)
                        )
                    }
                }
            }
            val subTotalValue = if (subTotalLabelBoundingBox!=null) {
                findBestMatchText(subTotalLabelBoundingBox, priceTextRects)
            } else null
            val taxValue = if (taxLabelBoundingBox!=null) {
                findBestMatchText(taxLabelBoundingBox, priceTextRects)
            } else null
            val totalValue = if (totalLabelBoundingBox!=null) {
                findBestMatchText(totalLabelBoundingBox, priceTextRects)
            } else null

            if (subTotalValue != null && taxValue != null) {
                val subtotal = (subTotalValue.trim().trim('$').toFloat()* 100).toInt()
                val tax = (taxValue.trim().trim('$').toFloat() * 100).toInt()
                callback(ReceiptImageRecognitionResult(subtotal, tax), null)
                return@addOnSuccessListener
            }

            if (subTotalValue != null ) {
                val subtotal = (subTotalValue.trim().trim('$').toFloat()* 100).toInt()
                callback(ReceiptImageRecognitionResult(subtotal, 0), null)
                return@addOnSuccessListener
            }

            if (totalValue != null ) {
                val total = (totalValue.trim().trim('$').toFloat()* 100).toInt()
                callback(ReceiptImageRecognitionResult(total, 0), null)
                return@addOnSuccessListener
            }
            callback(null, null)
        }
        .addOnFailureListener { e ->
            callback(null, e)
        }
}

private fun verticalOverlap(rect1: Rect, rect2: Rect): Int{
    if (rect1.bottom <= rect2.top || rect1.top >= rect2.bottom) {
        return 0
    }
    val maxTop = max(rect1.top, rect2.top)
    val minBottom = min(rect1.bottom, rect2.bottom)
    return minBottom - maxTop
}

private fun findBestMatchText(labelBoundingBox: Rect, textBlocks: List<Pair<String, Rect>>): String? {
    var bestOverlap = 0
    var bestAnswer: String? = null
    for (block in textBlocks) {
        val boundingBox = block.second
        val overlap = verticalOverlap(labelBoundingBox, boundingBox)
        if (overlap > bestOverlap) {
            bestOverlap = overlap
            bestAnswer = block.first
        }
    }
    return bestAnswer
}