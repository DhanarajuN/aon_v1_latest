package com.ongo.aonsecure.base

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ongo.aonsecure.activity.HomeActivity1
import com.ongo.aonsecure.activity.MainActivity
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.model.OfflineJson
import com.ongo.aonsecure.utils.ConnectionLiveData
import com.ongo.aonsecure.utils.ConnectivityReceiver
import com.ongo.aonsecure.utils.SharedPref
import io.objectbox.Box

open abstract class BaseCompatActivity : AppCompatActivity(),
    ConnectivityReceiver.ConnectivityReceiverListener {

    private var myBroadcastReceiver: ConnectivityReceiver = ConnectivityReceiver()
    lateinit var connectionLiveData: ConnectionLiveData
    lateinit var mContext: Context
    var isInternetConnected: Boolean = true
    var offlineBox1: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    var offlineDBCount1: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        SharedPref.initialize(mContext)
        connectionLiveData = ConnectionLiveData(this)
        // Check the internet connection
        registerReceiver(
            myBroadcastReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onResume() {
        super.onResume()

        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
    }

    private fun showMessage(isConnected: Boolean) {
        isInternetConnected = isConnected
        if(isConnected){
            
        }
        if (this is MainActivity)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (this).callInternetCheck(isConnected)
            } else if (this is HomeActivity1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    (this).callInternetCheck1(isConnected)
                }
    }
        fun checkInternet(): Boolean {
            return isInternetConnected
        }


        fun showToast(msg: String) {
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
        }

        fun hideSoftKeyboard() {
            try {
                val inputMethodManager =
                    getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                if (inputMethodManager.isAcceptingText) {
                    inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                } else {
                }
            } catch (e: Exception) {
            }
        }


    open fun alertDialog(
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String,
        negative: String
    ) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(title)
        //set message for alert dialog
        builder.setMessage(msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton(positive) { dialogInterface, which ->
        }
        // Set other dialog properties
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }


}