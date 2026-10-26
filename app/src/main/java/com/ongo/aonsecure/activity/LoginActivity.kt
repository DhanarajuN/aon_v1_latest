package com.ongo.aonsecure.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.ui.quoteview.QuoteViewModel
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.databinding.ActivityLoginBinding
import com.ongo.aonsecure.events.ActivityEvent
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.injection.ViewModelFactory
import com.ongo.aonsecure.injection.ViewModelFactoryActivity
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.utils.*
import com.ongo.aonsecure.viewmodel.LoginActivityViewModel
//import kotlinx.android.synthetic.main.activity_login.*
//import org.jetbrains.anko.alert
//import org.jetbrains.anko.noButton
//import org.jetbrains.anko.yesButton


open class LoginActivity : BaseCompatActivity() {

    val requestCode: Int = 200
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginActivityViewModel
    lateinit var loginInfo: LoginInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        // Remove the title bar (action bar)

        // Remove the toolbar
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.white)
        getSupportActionBar()?.hide();



        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_login
        )
        SharedPref.initialize(this)
        initFireBase()
      //  viewModel = ViewModelProvider(this)[LoginActivityViewModel::class.java]
        viewModel = ViewModelProvider(this,ViewModelFactoryActivity(this,application)
        )[LoginActivityViewModel::class.java]

        viewModel.getTermsApi()

        // viewModel.bind(loginInfo)
        binding.viewModel = viewModel
        viewModel.getStatus().observe(this, Observer { handleStatus(it) })
        viewModel.terms.observe(this,Observer{
            Log.d("testing", "onCreate: ")
            termsCheck(it)        })

        viewModel.message.observe(this, Observer {
            errorResponse(it)
        })
        viewModel.isButtonEnabled.observe(this, { isEnabled ->
            // Update the button's state
            binding.loginButton .isEnabled = isEnabled
        })
        binding.terms.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("222222222", "onCreate: $isChecked")


        }

        viewModel.loginEventSample.observe(this) {
            if (it is ActivityEvent) {
                /*** check and save the login credentials if remember me is selected ***/
                checkRemeberMeSelected()

                startActivity(it.buildIntent(this))
                finishAffinity()
            }
        }

        viewModel.navEventSample.observe(this) {
            if (it is ActivityEvent) {
                startActivity(it.buildIntent(this))
                finishAffinity()
            } /*else if (it is FragmentEvent) {
                supportFragmentManager.beginTransaction().apply {
                    addToBackStack(null)
                    replace(R.id.fragmentContainer, it.buildFragment(), it.tag)
                    commit()
                }
            }*/
        }
        getPhoneStatePermission()



        updateEdittextFromPrefs()
    }

    private fun checkRemeberMeSelected() {
        //login pref
        SharedPref.write(PREF_LOGGED_IN, "YES")
        SharedPref.write(PREF_LOGGED_IN_EMAIL, binding.usernameEt.text.toString())

        if (binding.checkbox.isChecked) {
            SharedPref.write(PREF_USERNAME, binding.usernameEt.text.toString())
            SharedPref.write(PREF_PASSWORD, binding.passwordEt.text.toString())
        } else {
            SharedPref.write(PREF_USERNAME, "")
            SharedPref.write(PREF_PASSWORD, "")
        }
    }

    private fun errorResponse(it: Event<String, String>?) {
        if (it != null) {
            alertDialog("Error Response!", it.peekContent(), false, "Okay", "")
        }
    }

    private fun getPhoneStatePermission() {
        if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
            // Great, got the permission
            getImei()

        } else {
            // Show permission rationale and request
            showPermissionReasonAndRequest(
                "Notice!",
                "Requesting phone permissions to capture IMEI number. Please click OK",
                Manifest.permission.READ_PHONE_STATE,
                this.requestCode
            )
        }
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    private fun Activity.showPermissionReasonAndRequest(
        title: String,
        message: String,
        permission: String,
        requestCode: Int
    ) {

        if (isFinishing || isDestroyed) {
            Log.d("PERMISSION_DIALOG", "Activity already closed")
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    requestCode
                )
            }
            .setNegativeButton("Cancel") { _, _ ->

                if (!isFinishing && !isDestroyed) {
                    showPermissionReasonAndRequest(
                        "Alert!",
                        "Permission to capture IMEI number is mandatory. Please click OK",
                        permission,
                        requestCode
                    )
                }
            }
            .setCancelable(false)
            .show()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {

            requestCode -> if (grantResults.size > 0) {

                var locationPermissions = grantResults[0] === PackageManager.PERMISSION_GRANTED

                if (locationPermissions) {
                    getImei()
                } else {
                    /*alert("Please grant permissoins to accase phone number.") {
                        title = "Read Phone Number"
                        negativeButton("RETRY") { requestPermssion() }
                        onCancelled { recreate() }
                    }.show()*/

                    showPermissionReasonAndRequest(
                        "Alert!",
                        "Permission to capture IMEI number is mandatory. Please click OK",
                        Manifest.permission.READ_PHONE_STATE,
                        this.requestCode
                    )
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun getImei() {
        Log.e("imei number permission", "granted")
        val tm =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {


            var deviceId = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deviceId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
                )
            } else {
                val mTelephony =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                deviceId = if (mTelephony.deviceId != null) {
                    mTelephony.deviceId
                } else {
                    Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ANDROID_ID
                    )
                }
            }
            //set value to bind edittext
            viewModel.setImei(deviceId)
            //inflate data into edittext if remember me is selected
        }


    }

    private fun updateEdittextFromPrefs() {
        if (SharedPref.read(PREF_USERNAME, "")?.isNotEmpty()!!) {
            var name = SharedPref.read(PREF_USERNAME, "").toString()
            var password = SharedPref.read(PREF_PASSWORD, "").toString()

            binding.usernameEt.setText(name)
            viewModel.setUserName(name)

            binding.passwordEt.setText(password)
            viewModel.setPassword(password)
         //   viewModel.setTerms(binding.terms.isChecked)

            binding.checkbox.isChecked = true

            Log.d("11111111", "updateEdittextFromPrefs:${binding.terms.isChecked} ")
        }
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
                        Log.d("FirebaseInit", "App is up to date")
                    }
                } else {
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
            val pInfo = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
            versionCode = pInfo.versionCode
            Log.d("FirebaseInit", "$versionCode")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode
    }

    private fun showForceUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Update Required")
        builder.setMessage("A new version of the app is available. Please update to continue.")
        builder.setCancelable(false)
        builder.setPositiveButton("Update") { dialog, which ->
            dialog.dismiss()
            redirectToPlayStore()
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
    private fun setValueToNote(response: TermsConfig) {
       var enableTerms=response.jobs[0].IS_TERMS_ENABLE
        var termsLink=response.jobs[0].TERMS_LINK
        if (enableTerms.equals("False",true)){
            binding.terms.visibility=View.GONE
            binding.termsTv.visibility=View.GONE
        }else{
            binding.terms.visibility=View.VISIBLE
            binding.termsTv.visibility=View.VISIBLE


        }


    }

private fun termsCheck(it: Event<Any, String>?){
    Log.d("testing",
        ("termsCheck: "+it?.peekContent()+ "  "+it?.getType()  +it?.peekContent().toString()).toString()
    )
    if (it != null) {
        if (it.peekContent() is TermsConfig) {
            var response: TermsConfig = it.peekContent() as TermsConfig
            setValueToNote(response)
        } else if (it.peekContent() is String) {
            alertDialog("Error Response!", it.peekContent().toString(), false, "Okay", "")
        }
    }
}
    private fun handleStatus(status: Status?) {
        when (status) {
            REMOVE_ERRORS -> {
                binding.usernameTil.isErrorEnabled = false
                binding.passwordTil.isErrorEnabled = false
            }
            EMPTY_EMAIL -> {
                binding.usernameTil.error = "Please enter username!"
                binding.usernameTil.isErrorEnabled = true
            }
            INVALID_EMAIL -> {
                binding.usernameTil.error = "Invalid username!"
                binding.usernameTil.isErrorEnabled = true
            }
            EMPTY_PASSWORD -> {
                binding.passwordTil.error = "Please enter password!"
                binding.passwordTil.isErrorEnabled = true
            }
            INVALID_PASSWORD -> {
                binding.passwordTil.error = "Password should be minimum six characters!"
                binding.passwordTil.isErrorEnabled = true
            }
            TERMS ->{
                Log.d("cccccccccc", "handleStatus:${binding.terms.isChecked} ")
            //  showToast("Please Enable Terms&Conditions")
            }
            ERROR -> {
                alertDialog("Error Response!", "Login Error", false, "Okay", "")
            }
            TOAST_SHOW -> {
                showToast("Under Progress")
            }
            SUCCESS -> TODO()
            LOADING -> TODO()
            SERVER_CONNECTION_ERROR -> TODO()
            SIGN_UP -> TODO()
            SIGN_IN -> TODO()
            INVALID_CREDENTIALS -> TODO()
            INVALID_ACCESS_TOKEN -> TODO()
            INVALID_USER -> TODO()
            IMAGE_UPLOAD_FAILED -> TODO()
            CHANGE_FRAG -> TODO()
            TENURE_EMPTY -> TODO()
            TENURE_MISMATCH -> TODO()
            TERMS_CHECK -> TODO()
            EMPTY_MOBILE -> TODO()
            INVALID_MOBILE -> TODO()
            EMPTY_FIRST_NAME -> TODO()
            EMPTY_LAST_NAME -> TODO()
            EMPTY_CITY -> TODO()
            INVALID_URI -> TODO()
            EMPTY_RMID -> TODO()
            INVALID_RMID -> TODO()
            LOAN_EMPTY -> TODO()
            LOAN_INVALID -> TODO()
            LOAN_INVALID1 -> TODO()
            LOAN_MIN -> TODO()
            SUMINSURED_RANGE -> TODO()
            COVERAGE_EMPTY -> TODO()
            COVERAGE_MIN -> TODO()
            DOB_EMPTY -> TODO()
            AGE_EMPTY -> TODO()
            AGE_INVALID -> TODO()
            AGE_INVALID_COVER -> TODO()
            QUOTE_SUCCESS -> TODO()
            CONTACT_NUMBER_EMPTY -> TODO()
            CONTACT_NUMBER -> TODO()
            ZONE -> TODO()
            INSURER -> TODO()
            PRODCAT -> TODO()
            PRODUCT -> TODO()
            FUNDTYPE -> TODO()
            Gender -> TODO()
            DATERANGE -> TODO()
            NO_PREMIUM -> TODO()
            CURRENT_JOB_STATUS -> TODO()
            OLD_PASSWORD_EMPTY -> TODO()
            OLD_PASSWORD_INVALID -> TODO()
            NOT_ALLOWED_SPECIAL_CHARS -> TODO()
            NEW_PASSWORD_EMPTY -> TODO()
            NEW_PASSWORD_INVALID -> TODO()
            UPPERCASE_INVALID -> TODO()
            SPECIAL_CHARS_INVALID -> TODO()
            PASSWORD_LENGTH_INVALID -> TODO()
            CONFIRM_PASSWORD_EMPTY -> TODO()
            CONFIRM_PASSWORD_INVALID -> TODO()
            CONFIRM_PASSWORD_MISMATCH -> TODO()
            null -> TODO()
            PRODUCT_TYPE_EMPTY  -> TODO()
        }
    }

}
