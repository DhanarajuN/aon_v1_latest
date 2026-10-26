package com.ongo.aonsecure.activity

import android.content.Intent
import androidx.activity.addCallback
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
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
import com.ongo.aonsecure.databinding.ActivityChangePasswordBinding
import com.ongo.aonsecure.events.ActivityEvent
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.model.PasswordConfig
import com.ongo.aonsecure.utils.MessageService
import com.ongo.aonsecure.utils.SharedPref
import com.ongo.aonsecure.viewmodel.ChangePasswordViewModel

@RequiresApi(Build.VERSION_CODES.O)
class ChangePasswordActivity : BaseCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBackHandler()
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_change_password
        )
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.toolbar1);
        window.statusBarColor = resources.getColor(R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        val editFormImageView = toolbar.findViewById<ImageView>(R.id.back_press_iv)
        val title = toolbar.findViewById<TextView>(R.id.title_tv)
        title.setText("Change Password")
        editFormImageView.setOnClickListener {
            onBackPressed()        }
        SharedPref.initialize(this)
        viewModel = ViewModelProvider(this)[ChangePasswordViewModel::class.java]
        // viewModel.bind(loginInfo)
        binding.viewModel = viewModel

        viewModel.getStatus().observe(this, Observer { handleStatus(it) })

        viewModel.message.observe(this, Observer {
            responseHandle(it)
        })

        viewModel.navEventSample.observe(this) {
            if (it is ActivityEvent) {
                alertDialog("Alert!", "Password changed successful", false, "Okay", "", it)
            }
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

    private fun responseHandle(it: Event<Any, String>?) {
        if (it != null) {
            if (it.peekContent() is PasswordConfig) {
                var response: PasswordConfig = it.peekContent() as PasswordConfig
                setValueToNote(response)
            } else if (it.peekContent() is String) {
                alertDialog("Error Response!", it.peekContent().toString(), false, "Okay", "")
            }
        }
    }

    var blockCharacterSet = ""
    private fun setValueToNote(response: PasswordConfig) {
        var passwordLength = "* Password length should be - " + response.jobs[0].LengthOfPassword.toInt() + "\n"
        var upperCaseLimit = "* Uppercase limit - " + response.jobs[0].UpperCase.toInt() + "\n"
        var specialCharactersLimit = "* Special characters limit - " + response.jobs[0].SpecialCharacter.toInt() + "\n"
        var notAllowedChars = "* Not allowed characters - " + response.jobs[0].NotAllowed + "\n"

        var passwordNoteText =
            "NOTE:\n $passwordLength$upperCaseLimit$specialCharactersLimit$notAllowedChars"

        binding.passwordNote.text = passwordNoteText
        blockCharacterSet = response.jobs[0].NotAllowed

        binding.newPasswordEt.filters = arrayOf(filter)
        binding.confirmPasswordEt.filters  = arrayOf(filter)
    }

    private val filter =
        InputFilter { source, start, end, dest, dstart, dend ->
            if (source != null && blockCharacterSet.contains("" + source)) {
                ""
            } else null
        }

    private fun handleStatus(status: Status?) {
        when (status) {
            SUCCESS -> {
                showToast("Password changed successful")
                callPushNotification()
            }
            REMOVE_ERRORS -> {
                binding.emailTil.isErrorEnabled = false
                binding.oldPassTil.isErrorEnabled = false
                binding.newPassTil.isErrorEnabled = false
                binding.confirmPassTil.isErrorEnabled = false
            }
            OLD_PASSWORD_EMPTY -> {
                binding.oldPassTil.isErrorEnabled = true
                binding.oldPassTil.error = "Please enter old password!"
            }
            NEW_PASSWORD_EMPTY -> {
                binding.newPassTil.isErrorEnabled = true
                binding.newPassTil.error = "Please enter new password!"
            }
            NEW_PASSWORD_INVALID -> {
                binding.newPassTil.isErrorEnabled = true
                binding.newPassTil.error = "Invalid password pattern. Check with the note!"
            }
            PASSWORD_LENGTH_INVALID -> {
                binding.newPassTil.isErrorEnabled = true
                binding.newPassTil.error = "Password length does'nt matched. Check with the note!"
            }
            UPPERCASE_INVALID -> {
                binding.newPassTil.isErrorEnabled = true
                binding.newPassTil.error = "Uppercase length does'nt matched. Check with the note!"
            }
            SPECIAL_CHARS_INVALID -> {
                binding.newPassTil.isErrorEnabled = true
                binding.newPassTil.error = "Special characters length does'nt matched. Check with the note!"
            }
            NOT_ALLOWED_SPECIAL_CHARS -> {
                binding.newPassTil.isErrorEnabled = true
                binding.newPassTil.error = "Entered special chars not allowed. Check with the note!"
            }
            CONFIRM_PASSWORD_EMPTY -> {
                binding.confirmPassTil.isErrorEnabled = true
                binding.confirmPassTil.error = "Please enter confirm password!"
            }
            CONFIRM_PASSWORD_MISMATCH -> {
                binding.confirmPassTil.isErrorEnabled = true
                binding.confirmPassTil.error =
                    "Confirm Password entered does'nt match with new password!"
            }
            INVALID_EMAIL -> {
                binding.emailTil.error = "Invalid email!"
                binding.emailTil.isErrorEnabled = true
            }
            EMPTY_EMAIL -> {
                binding.emailTil.error = "Please enter email!"
                binding.emailTil.isErrorEnabled = true
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
            OLD_PASSWORD_INVALID -> TODO()
            CONFIRM_PASSWORD_INVALID -> TODO()
            null -> TODO()
            PRODUCT_TYPE_EMPTY  -> TODO()
        }
    }

    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(this) {
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            startActivity(Intent(this@ChangePasswordActivity, MainActivity::class.java))
        }
    }

    private fun callPushNotification() {
        val intent = Intent(this, MessageService::class.java)
        intent.putExtra(MessageService.QUOTE_NUMBER, "")
        intent.putExtra(MessageService.MESSAGE_EXTRA, "Password changed successful")
        startService(intent)
    }
}
