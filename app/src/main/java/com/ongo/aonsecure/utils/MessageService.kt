package com.ongo.aonsecure.utils

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.ChangePasswordActivity
import com.ongo.aonsecure.activity.MainActivity
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
class MessageService : IntentService("MessageService") {

    companion object {
        const val CHANNEL_ID = "com.ongo.aonsecure.utils"
        const val MESSAGE_EXTRA = "message"
        const val QUOTE_NUMBER = "quote_number"
        const val ID = 1234
    }

    private var pendingIntent: PendingIntent? = null
    private var handler: Handler? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler = Handler()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        synchronized(this) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        val text = intent!!.getStringExtra(MESSAGE_EXTRA)
        val quoteNo = intent!!.getStringExtra(QUOTE_NUMBER)
        if (text != null) {
            if (quoteNo != null) {
                showText(text, quoteNo)
            }
        }
    }


    private fun showText(text: String, quoteNo: String) {
        /*handler!!.post {
            Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
        }*/

       /* if (quoteNo.isNotEmpty() && quoteNo.equals(quoteNo, true)){
            val intent = Intent(this,ChangePasswordActivity::class.java)
           // val stackBuilder = TaskStackBuilder.create(this)

            val sBuilder = TaskStackBuilder.create(this)
            sBuilder.addParentStack(MainActivity::class.java)
            sBuilder.addNextIntent(intent)
            sBuilder.startActivities()
            pendingIntent = sBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        }*/
        Log.d("testing message", "showText: "+quoteNo)
        if (quoteNo.equals("password", true)) {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            val stackBuilder = TaskStackBuilder.create(this)

            stackBuilder.addParentStack(ChangePasswordActivity::class.java)
            stackBuilder.addNextIntent(intent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }else
        if (quoteNo.isNotEmpty()) {
            /*var templatePDF = TemplatePDF(this)
            templatePDF.viewQuoteFromNotification(this, quoteNo)*/

            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "$quoteNo.pdf"
            )


            val uri = FileProvider.getUriForFile(
                this, applicationContext.packageName + ".provider",
                file
            )

            val openFile = Intent()
            openFile.putExtra(Intent.EXTRA_STREAM, uri)
            openFile.type = "application/pdf"
            openFile.data = uri
            openFile.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            openFile.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION)
/*            openFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openFile.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)*/
            openFile.action = Intent.ACTION_VIEW

            val stackBuilder = TaskStackBuilder.create(this)

            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(openFile)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }

        else {

            val intent = Intent()
            val stackBuilder = TaskStackBuilder.create(this)

            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "App notification channel"
            channel.setShowBadge(true)

            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.tractor)
            setContentTitle(getString(R.string.app_name))
            setContentText(text)
            setContentIntent(pendingIntent)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(ID, notificationBuilder.build())
    }
}
