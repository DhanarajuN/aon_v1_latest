package com.ongo.aonsecure.activity

import android.content.Intent
import androidx.activity.addCallback
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.ongo.aonsecure.R
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.databinding.ActivityForgotPasswordBinding
import com.ongo.aonsecure.events.ActivityEvent
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.utils.MessageService
import com.ongo.aonsecure.utils.SharedPref
import com.ongo.aonsecure.viewmodel.ForgotPasswordViewModel

@RequiresApi(Build.VERSION_CODES.O)
class ForgotPasswordActivity : BaseCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBackHandler()
//        val window = window
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.statusBarColor = resources.getColor(R.color.white)
//        getSupportActionBar()?.hide();
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_forgot_password
        )
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.toolbar1);
        window.statusBarColor = resources.getColor(R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        val editFormImageView = toolbar.findViewById<ImageView>(R.id.back_press_iv)
        val title = toolbar.findViewById<TextView>(R.id.title_tv)
        title.setText("Forgot Password")
        editFormImageView.setOnClickListener {
            onBackPressed()
        }
        SharedPref.initialize(this)
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        binding.viewModel = viewModel

        viewModel.getStatus().observe(this, Observer { handleStatus(it) })


        viewModel.navEventSample.observe(this) {
            if (it is ActivityEvent) {
                alertDialog("Alert!", "Forgot password request submitted", false, "Okay", "", it)
            }
        }

        viewModel.message.observe(this, Observer {
            errorResponse(it)
        })
    }

    private fun errorResponse(it: Event<Any, String>?) {
        if (it != null) {
            alertDialog("Error Response!", it.peekContent() as String, false, "Okay", "")
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

    private fun handleStatus(status: Status?) {
        when (status) {
            SUCCESS -> {
                showToast("Forgot password request submitted")
                callPushNotification()
            }
            REMOVE_ERRORS -> {
                binding.emailTil.isErrorEnabled = false
                binding.rmidTil.isErrorEnabled = false
            }
            INVALID_EMAIL -> {
                binding.emailTil.error = "Invalid email!"
                binding.emailTil.isErrorEnabled = true
            }
            EMPTY_EMAIL -> {
                binding.emailTil.error = "Please enter email!"
                binding.emailTil.isErrorEnabled = true
            }
            EMPTY_RMID -> {
                binding.rmidTil.error = "Please enter FID!"
                binding.rmidTil.isErrorEnabled = true
            }
            ERROR -> {
                alertDialog("Error Response!", "Login Error", false, "Okay", "")
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
            EMPTY_MOBILE -> TODO()
            INVALID_MOBILE -> TODO()
            EMPTY_PASSWORD -> TODO()
            INVALID_PASSWORD -> TODO()
            EMPTY_FIRST_NAME -> TODO()
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

    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(this) {
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
        }
    }

    private fun callPushNotification() {
        val intent = Intent(this, MessageService::class.java)
        intent.putExtra(MessageService.QUOTE_NUMBER, "")
        intent.putExtra(MessageService.MESSAGE_EXTRA, "Forgot password request submitted")
        startService(intent)
    }
}
