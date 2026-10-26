package com.ongo.aonsecure.activity
import android.app.AlertDialog
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.LoginActivity
import com.ongo.aonsecure.activity.MainActivity
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.injection.ViewModelFactoryActivity
import com.ongo.aonsecure.model.ObjResponse
import com.ongo.aonsecure.model.StatusDto
import com.ongo.aonsecure.utils.OfflineService
import com.ongo.aonsecure.utils.SharedPref
import com.ongo.aonsecure.utils.alertDialog
import com.ongo.aonsecure.viewmodel.SplashScreenViewModel
import pdf.app.library.utils.Constants

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 1 // 1 sec
    private lateinit var splashScreenViewModel: SplashScreenViewModel
    var userzone: String? = null

    var context = this
    var connectivity: ConnectivityManager? = null
    var info: NetworkInfo? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        SharedPref.initialize(this)
        startService(Intent(this, OfflineService::class.java))
        splashScreenViewModel = ViewModelProvider(
            this,
            ViewModelFactoryActivity(this,application)
        )[SplashScreenViewModel::class.java]
        connectivity = context.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        FirebaseApp.initializeApp(this)
        if (SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "")!!.isNotEmpty()) {
            if (connectivity != null) {
                info = connectivity!!.activeNetworkInfo
                if (info != null) {
                    if (info!!.state == NetworkInfo.State.CONNECTED) {
                        getUserStatus()
                    } else {

                        splash()
                    }
                } else {

                    splash()
                }
            }
        } else {
            splash()
        }

        splashScreenViewModel.userStatus.observe(this, Observer {
            getZone(it)
        })
        initFireBase()
    }

    override fun onResume() {
        super.onResume()

    }



    private fun initFireBase() {
        Log.d("FirebaseInit", "Initializing Firebase")

        val firebaseDatabase = FirebaseDatabase.getInstance("https://cli-premium-calculator-default-rtdb.firebaseio.com/")
        Log.d("FirebaseInit", "Firebase Database Instance Obtained")

        val databaseReference = firebaseDatabase.getReference("Apps").child("Android").child("Version")
        Log.d("FirebaseInit", "Database Reference: $databaseReference")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FirebaseInit", "onDataChange triggered")

                if (dataSnapshot.value != null) {
                    var fireVersion: Long = -1
                    try {
                        fireVersion = dataSnapshot.value as Long
                        Log.d("FirebaseInit", "Firebase Version: $fireVersion")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (fireVersion > getVersionCode()) {
                        if (!isFinishing && !isDestroyed) {
                            showForceUpdateDialog()
                        }
                        Log.e("FirebaseInit", "New version available: $fireVersion")
                    } else {
                        splash()
                        Log.d("FirebaseInit", "App is up to date")
                    }
                } else {
                    splash()
                    Log.d("FirebaseInit", "No data found at reference")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseInit", "onCancelled: ${databaseError.message}")
            }
        }

        databaseReference.addValueEventListener(valueEventListener)
    }


    private fun getVersionCode(): Int {
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

    private fun getUserStatus() {
        val loginEmailId: String = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
        splashScreenViewModel.getUserStatus(loginEmailId)
    }

    private fun getZone(it: Event<Any, String>?) {
        if (it != null)
            if (it.getType() == "userstatus") {
                val status = it.peekContent() as StatusDto
                val userstatus = status.status
                if (userstatus == "In Active") {
                    splash()
                }
            } else if (it.getType() == "userzone") {
                val objectresponse: ObjResponse? = it.peekContent() as ObjResponse
                userzone = objectresponse?.Zone
                if (userzone != SharedPref.read(Constants.PREF_ZONE, "")) {
                    SharedPref.write(Constants.PREF_ZONE, userzone)
                    splashScreenViewModel.deleteDBAndGetServerData()
                    splash()
                } else {
                    splash()
                }
            }
    }
    private fun showForceUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Update Required")
        builder.setMessage("A new version of the app is available. Please update to continue.")
        builder.setCancelable(false)
        builder.setPositiveButton("Update") { dialog, which ->
            dialog.dismiss()
            redirectToPlayStore()
            splash()
        }
        builder.setOnDismissListener {
            redirectToPlayStore()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun redirectToPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$packageName")
        startActivity(intent)
        finish()
    }
    fun splash() {
        Handler().postDelayed({
            if (SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "")?.isNotEmpty()!!) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, SPLASH_TIME_OUT)
    }

    override fun onDestroy() {
        super.onDestroy()
        alertDialog?.dismiss()
    }
}
