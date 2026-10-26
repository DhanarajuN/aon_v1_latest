package com.ongo.aonsecure.activity

import android.Manifest
import android.Manifest.permission.READ_PHONE_STATE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ongo.aonsecure.R
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.databinding.ActivitySignUpBinding
import com.ongo.aonsecure.events.ActivityEvent
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.utils.MessageService
import com.ongo.aonsecure.viewmodel.SignUpActivityViewModel
//import kotlinx.android.synthetic.main.activity_login.*
//import kotlinx.android.synthetic.main.activity_sign_up.*
//import org.jetbrains.anko.alert
//import org.jetbrains.anko.noButton
//import org.jetbrains.anko.yesButton

@RequiresApi(Build.VERSION_CODES.O)
class SignUpActivity : BaseCompatActivity() {

    val requestCode: Int = 200
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpActivityViewModel
    private lateinit var context: Context
    private lateinit var deviceSpinner: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val window = window
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.statusBarColor = resources.getColor(R.color.white)
//        getSupportActionBar()?.hide();
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.toolbar1);
        window.statusBarColor = resources.getColor(R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        val editFormImageView = toolbar.findViewById<ImageView>(R.id.back_press_iv)
        val title = toolbar.findViewById<TextView>(R.id.title_tv)
        title.setText("Sign Up")
        editFormImageView.setOnClickListener {
            onBackPressed()
        }
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_sign_up
        )
        context = this
        viewModel = ViewModelProvider(this)[SignUpActivityViewModel::class.java]
        binding.viewModel = viewModel
        viewModel.getStatus().observe(this, Observer { handleStatus(it) })

        viewModel.message.observe(this, Observer {
            errorResponse(it)
        })
        deviceSpinner = binding.deviceSpinner
        deviceSpin()
        viewModel.isButtonEnabled.observe(this) { isEnabled ->
            // Update the button's state
            binding.signUp.isEnabled = isEnabled
        }
        viewModel.navEventSample.observe(this) {
            if (it is ActivityEvent) {


                alertDialog("Alert!", "User registration request submitted", false, "Okay", "", it)

            } /*else if (it is FragmentEvent) {
                supportFragmentManager.beginTransaction().apply {
                    addToBackStack(null)
                    replace(R.id.fragmentContainer, it.buildFragment(), it.tag)
                    commit()
                }
            }*/
        }

        getPhoneStatePermission()
    }

    private fun deviceSpin() {
        var nameArry1 = listOf<String>("Mobile", "Tablet")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArry1)
        binding.deviceSpinner.setAdapter(adapter)
        binding.deviceSpinner.setText(adapter.getItem(0).toString(), false)
        viewModel.setDeviceType(nameArry1[0])
        binding.deviceSpinner.setOnItemClickListener { parent, _, position, _ ->
            viewModel.setDeviceType(nameArry1[position])
        }
//        deviceSpinner.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View, position: Int, id: Long
//            ) {
//                viewModel.setDeviceType(nameArry1[position])
//
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // write code to perform some action
//            }
//        }
    }


    private fun errorResponse(it: Event<String, String>?) {
        if (it != null) {
            alertDialog("Error Response!", it.peekContent(), false, "Okay", "")
        }
    }

    fun alertDialog(
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String,
        negative: String,
        it: ActivityEvent
    ) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(title)
        //set message for alert dialog
        builder.setMessage(msg)
        builder.setIcon(R.drawable.ic_vec_reg_success)
        //performing positive action
        builder.setPositiveButton(positive) { dialogInterface, which ->
            startActivity(it.buildIntent(this))
            finishAffinity()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }

    private fun getPhoneStatePermission() {
        if (isPermissionGranted(READ_PHONE_STATE)) {
            // Great, got the permission
            getImei()

        } else {
            // Show permission rationale and request
            showPermissionReasonAndRequest(
                "Notice!",
                "Requesting phone permissions to capture IMEI number. Please click OK",
                READ_PHONE_STATE,
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
                showPermissionReasonAndRequest(
                    "Alert!",
                    "Permission to capture IMEI number is mandatory. Please click OK",
                    Manifest.permission.READ_PHONE_STATE,
                    requestCode
                )
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
                READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //set value to bind edittext

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
            binding.imeiEt.setText(deviceId)
            viewModel.setImei(binding.imeiEt.text.toString())
        }


    }

    private fun handleStatus(status: Status?) {
        when (status) {
            SUCCESS -> {
                showToast("User registration request submitted")
                callPushNotification()
            }
            REMOVE_ERRORS -> {
                binding.emailTil.isErrorEnabled = false
                binding.rmidTil.isErrorEnabled = false
                binding.mobileNoTil.isErrorEnabled = false
                binding.nameTil.isErrorEnabled = false
            }
            INVALID_EMAIL -> {
                binding.emailTil.error = "Invalid email!"
                binding.emailTil.isErrorEnabled = true
            }
            EMPTY_EMAIL -> {
                binding.emailTil.error = "Please enter email!"
                binding.emailTil.isErrorEnabled = true
            }
            EMPTY_MOBILE -> {
                binding.mobileNoTil.error = "Please enter mobile number!"
                binding.mobileNoTil.isErrorEnabled = true
            }
            INVALID_MOBILE -> {
                binding.mobileNoTil.error = "Invalid mobile number!"
                binding.mobileNoTil.isErrorEnabled = true
            }
            EMPTY_FIRST_NAME -> {
                binding.nameTil.error = "Please enter name!"
                binding.nameTil.isErrorEnabled = true
            }
            EMPTY_RMID -> {
                binding.rmidTil.error = "Please enter RMID!"
                binding.rmidTil.isErrorEnabled = true
            }
            ERROR -> {
                alertDialog("Error Response!", "User registered already", false, "Okay", "")
            }
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
            EMPTY_PASSWORD -> TODO()
            INVALID_PASSWORD -> TODO()
            EMPTY_LAST_NAME -> TODO()
            EMPTY_CITY -> TODO()
            INVALID_URI -> TODO()
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
            TERMS -> TODO()
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
            TOAST_SHOW -> TODO()
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

    private fun callPushNotification() {
        val intent = Intent(this, MessageService::class.java)
        intent.putExtra(MessageService.QUOTE_NUMBER, "")
        intent.putExtra(MessageService.MESSAGE_EXTRA, "User registration request submitted")
        startService(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
