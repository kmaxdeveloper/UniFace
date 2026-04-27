package com.uniface.mizan.util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.charset.StandardCharsets

@Component
class DocumentParser {

    fun parseFile(file: MultipartFile): String {
        val fileName = file.originalFilename?.lowercase() ?: return ""
        val inputStream = file.inputStream

        return when {
            fileName.endsWith(".pdf") -> extractTextFromPdf(inputStream)
            fileName.endsWith(".docx") -> extractTextFromDocx(inputStream)
            fileName.endsWith(".txt") -> extractTextFromTxt(inputStream)
            else -> throw IllegalArgumentException("Unsupported file type: $fileName")
        }
    }

    private fun extractTextFromPdf(inputStream: InputStream): String {
        return PDDocument.load(inputStream).use { document ->
            val pdfStripper = PDFTextStripper()
            pdfStripper.getText(document)
        }
    }

    private fun extractTextFromDocx(inputStream: InputStream): String {
        return XWPFDocument(inputStream).use { document ->
            val extractor = XWPFWordExtractor(document)
            extractor.text
        }
    }

    private fun extractTextFromTxt(inputStream: InputStream): String {
        return String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
    }
}
