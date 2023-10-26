package com.example.pdffilegenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pdffilegenerator.ui.theme.PdfFileGeneratorTheme
import com.example.pdffilegenerator.ui.theme.Red
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import androidx.core.view.get
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


import android.Manifest
import android.content.Intent
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.TextFieldValue

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfFileGeneratorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red),

                                title = {
                                    Text(

                                        text = "PDF Generator",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }
                            )
                        }
                    ) {

                        pdfGenerator()
                    }
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {

            if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pdfGenerator() {
    var userTextInput by remember { mutableStateOf(TextFieldValue()) }

    println("Generator")

    val ctx = LocalContext.current
//    val activity = (LocalContext.current as? ComponentActivity)
//    if (checkPermissions(ctx)) {
//        Toast.makeText(ctx, "Permissions Granted..", Toast.LENGTH_SHORT).show()
//    } else {
//        requestPermission(activity!!)
//    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .fillMaxSize()
            .padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PDF Generator",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // User-input text field
        TextField(
            value = userTextInput,
            onValueChange = { userTextInput = it },
            modifier = Modifier.fillMaxWidth().fillMaxHeight(fraction = .6f),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            placeholder = { Text(text = "Enter text") },
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            onClick = {
                val pdfFile = createPdf(userTextInput.text, ctx)
                if (pdfFile != null) {
                    Toast.makeText(ctx, "PDF file generated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "Failed to generate PDF file.", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(modifier = Modifier.padding(6.dp), text = "Generate PDF")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun createPdf(userText: String, context: Context) {

    val document = Document()
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val pdfFileName = "PDF_$timestamp.pdf"

    // Use context to get the external files directory
    val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

    val pdfFile = File(externalFilesDir, pdfFileName)

    val writer = PdfWriter.getInstance(document, FileOutputStream(pdfFile))
    document.open()

    // Adding user-input text to the PDF
    val paragraph = Paragraph(userText)
    document.add(paragraph)

    document.close()
    writer.close()

    // Open the file location using an Intent
    val pdfUri = FileProvider.getUriForFile(context, context.packageName + ".provider", pdfFile)
    val openPdfIntent = Intent(Intent.ACTION_VIEW)
    openPdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    openPdfIntent.setDataAndType(pdfUri, "application/pdf")

    context.startActivity(openPdfIntent)
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun generatePDF(context: Context) {
    var pageHeight = 1120
    var pageWidth = 792
    lateinit var bmp: Bitmap
    lateinit var scaledbmp: Bitmap
    var pdfDocument: PdfDocument = PdfDocument()
    var paint: Paint = Paint()
    var title: Paint = Paint()
    bmp = BitmapFactory.decodeResource(context.resources, R.drawable.pict)
    scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false)
    var myPageInfo: PdfDocument.PageInfo? =
        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    var myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)
    var canvas: Canvas = myPage.canvas
    canvas.drawBitmap(scaledbmp, 56F, 40F, paint)
    title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    title.textSize = 15F
    title.setColor(ContextCompat.getColor(context, R.color.purple_200))
    canvas.drawText("A portal for IT professionals.", 209F, 100F, title)
    canvas.drawText("Geeks for Geeks", 209F, 80F, title)
    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
    title.setColor(ContextCompat.getColor(context, R.color.purple_200))
    title.textSize = 15F
    title.textAlign = Paint.Align.CENTER
    canvas.drawText("This is sample document which we have created.", 396F, 560F, title)
    pdfDocument.finishPage(myPage)
    val file: File = File(Environment.getExternalStorageDirectory(), "candle.pdf")

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF file generated..", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
        Toast.makeText(context, "Fail to generate PDF file..", Toast.LENGTH_SHORT)
            .show()
    }
    pdfDocument.close()
}

//fun checkPermissions(context: Context): Boolean {
//    var writeStoragePermission = ContextCompat.checkSelfPermission(
//        context,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    )
//    var readStoragePermission = ContextCompat.checkSelfPermission(
//        context,
//        Manifest.permission.READ_EXTERNAL_STORAGE
//    )
//    return writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED
//}
//fun requestPermission(activity: Activity) {
//    ActivityCompat.requestPermissions(
//        activity,
//        arrayOf(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ), 101
//
//    )
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PdfFileGeneratorTheme {
        pdfGenerator()
    }
}