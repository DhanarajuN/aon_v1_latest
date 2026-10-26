package com.ongo.aonsecure.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.ongo.aonsecure.BuildConfig
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.LoginActivity
import pdf.app.library.utils.Constants
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


/** The base URL of the API */
const val BASE_URL: String = BuildConfig.BASE_URL
const val PREF_NAME: String = "Pref"

const val EXTENSIONADDER = ".pdf"
val PDF_FILE_PATH =
    Environment.getExternalStorageDirectory().toString() + "/PDF/"
const val SPLASH_DISPLAY_LENGTH: Long = 2000
val FILEPATH = PDF_FILE_PATH + "sample.pdf"
const val PREF_USERNAME = "PREF_USERNAME"
const val PREF_LOGGED_IN_EMAIL = "PREF_LOGGED_IN_EMAIL"
const val PREF_PASSWORD = "PREF_PASSWORD"
const val PREF_LOGGED_IN = "PREF_LOGGED_IN"

const val PREF_USERID = "PREF_USERID"
const val PREF_FULL_NAME = "PREF_FULL_NAME"
const val PREF_MOBILE = "PREF_MOBILE"
const val PREF_IMEI = "PREF_IMEI"
const val PREF_ZONE = "PREF_ZONE"
const val PREF_RMID = "PREF_RMID"
const val PREF_USER_PASSWORD_EXPIRED="PREF_USER_PASSWORD_EXPIRED"

//val tenureArry= listOf<String>(
//    "2 Years",
//    "3 Years",
//    "4 Years",
//    "5 Years",
//    "6 Years",
//    "7 Years"
//)
val tenureArry = listOf<String>(
    "6 Months",
    "12 Months",
//    "13 Months",
//    "14 Months",
//    "15 Months",
//    "16 Months",
//    "17 Months",
//    "18 Months",
//    "19 Months",
//    "20 Months",
//    "21 Months",
//    "22 Months",
//    "23 Months",
    "24 Months",
    "25 Months",
    "26 Months",
    "27 Months",
    "28 Months",
    "29 Months",
    "30 Months",
    "31 Months",
    "32 Months",
    "33 Months",
    "34 Months",
    "35 Months",
    "36 Months",
    "37 Months",
    "38 Months",
    "39 Months",
    "40 Months",
    "41 Months",
    "42 Months",
    "43 Months",
    "44 Months",
    "45 Months",
    "46 Months",
    "47 Months",
    "48 Months",
    "49 Months",
    "50 Months",
    "51 Months",
    "52 Months",
    "53 Months",
    "54 Months",
    "55 Months",
    "56 Months",
    "57 Months",
    "58 Months",
    "59 Months",
    "60 Months",
    "61 Months",
    "62 Months",
    "63 Months",
    "64 Months",
    "65 Months",
    "66 Months",
    "67 Months",
    "68 Months",
    "69 Months",
    "70 Months",
    "71 Months",
    "72 Months",
    "73 Months",
    "74 Months",
    "75 Months",
    "76 Months",
    "77 Months",
    "78 Months",
    "79 Months",
    "80 Months",
    "81 Months",
    "82 Months",
    "83 Months",
    "84 Months"


)

fun getByteArry(context: Context): ByteArray {
    val bm = BitmapFactory.decodeResource(context.resources, R.drawable.tractor_)
    val stream = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
fun getByteArray1(context: Context): ByteArray {
    try {
        val bm = BitmapFactory.decodeResource(context.resources, R.drawable.tractor)

        if (bm == null) {
            return ByteArray(0)
        }

        val stream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream)

        bm.recycle()

        return stream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()

        return ByteArray(0)
    }
}

fun getCurrentDate(): String {
    val c: Date = Calendar.getInstance().time
    val df = SimpleDateFormat("dd/MM/yyyy")
    return df.format(c)
}
fun changeDateFormat(date:String?): String {
    val originalFormat = SimpleDateFormat("dd/MM/yyyy")
        // Parse the original date string
        val originalDate = originalFormat.parse(date)
        // Define the format for the desired date format
        val desiredFormat = SimpleDateFormat("MM/dd/yyyy")
        // Format the date in the desired format
        return desiredFormat.format(originalDate)

}

fun getCurrentDateForm(): String {
    val c: Date = Calendar.getInstance().time
    val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
    return df.format(c)
}

fun getValidTillDateForm(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, 30)
    val c: Date = cal.time
    val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
    return df.format(c)
}
private fun getVersionCode(context: Context): Int {
    var versionCode: Int = -1
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        versionCode = pInfo.versionCode
        Log.d("FirebaseInit", "$versionCode")
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionCode
}

fun alertDialog(
    context: Context,
    title: String,
    msg: String,
    cancellable: Boolean,
    positive: String,
    negative: String
) {
    val builder = AlertDialog.Builder(context)
    //set title for alert dialog
    builder.setTitle(title)
    //set message for alert dialog
    builder.setMessage(msg)
    builder.setIcon(android.R.drawable.ic_dialog_alert)
    //performing positive action
    builder.setPositiveButton(positive) { dialogInterface, which ->

        /**** saving this pref because remember me is saved with these two prefs ****/
        var rememberMeUserName = SharedPref.read(PREF_USERNAME, "")
        var rememberMePassword = SharedPref.read(PREF_PASSWORD, "")
        //logout user
        SharedPref.write(Constants.PREF_LOGGED_IN, "")
        SharedPref.clearPref()

        SharedPref.write(PREF_USERNAME, rememberMeUserName)
        SharedPref.write(PREF_PASSWORD, rememberMePassword)
      //  SharedPref.write(PREF_USER_PASSWORD_EXPIRED,"passwordexpired")

        context.startActivity(Intent(context, LoginActivity::class.java))
        (context as? Activity)?.finish()

    }
    builder.setNegativeButton(negative) { dialogInterface, which ->
    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.setCancelable(cancellable)
    alertDialog.show()

}