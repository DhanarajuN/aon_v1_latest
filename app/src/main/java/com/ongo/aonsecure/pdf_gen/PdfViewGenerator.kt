package com.ongo.aonsecure.pdf_gen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.ongo.aonsecure.BuildConfig
import com.ongo.aonsecure.R
import com.ongo.aonsecure.base.BaseCompatActivity
import pdf.app.library.Callback
import pdf.app.library.DesignPdf
import java.io.File

class PdfViewGenerator : BaseCompatActivity() {

   // var layout: LinearLayout? = null
    var linearLayout: RelativeLayout? = null
    var views: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view_generator)

        linearLayout = findViewById(R.id.layout)
        views = View(this)
       showToast(checkStoragePermissions().toString())
        requestPermission()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callgenePdf()
        }
    }
    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (Q) and above → Scoped storage → Permission NOT required
            true
        } else {
            // Android 9 and below → request old storage permissions
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callgenePdf()
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                111
            )
        }
        else {
            // Android 5 and below
            callgenePdf()
        }
    }


    private fun callgenePdf() {
        linearLayout?.let {
            DesignPdf.with(this).addView(it)
                .setFilePath(getAppPath(this), "CNHI_PDF")
                .setHeightnWidthMultiplier(1, 1)
                .create(object : Callback {
                    override fun onSuccess(file: File) {
                        showToast("path: " + file.absolutePath)
                        Log.d("path", file.absolutePath)
                    }

                    override fun onError(e: Exception) {
                        e.printStackTrace()
                    }
                })
        }
    }


    fun getAppPath(context: Context?): String? {
        val dir = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator
                    + getString(R.string.app_name)
                    + File.separator
        )
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir.path + File.separator
    }
}
