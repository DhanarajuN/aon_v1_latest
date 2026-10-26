package com.ongo.aonsecure.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.ui.help.HelpViewModel
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.FragmentHelpBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.injection.ViewModelFactoryActivity
import com.ongo.aonsecure.model.OfflineJson
import com.ongo.aonsecure.model.OfflineJson_
import com.ongo.aonsecure.model.StatusDto
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import pdf.app.library.utils.Constants

class HelpActivity : BaseCompatActivity() {

    private lateinit var helpViewModel: HelpViewModel
    private lateinit var binding: FragmentHelpBinding
    var userstatus: String? = ""

    var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    var offlineDBCount: Long = 0

    private var fragmentCallback: FragmentCallback? = null

    fun setFragmentCallback(callback: FragmentCallback?) {
        this.fragmentCallback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_help)
// In your HelpActivity

// Use ViewModelFactoryActivity for HelpActivity
        helpViewModel = ViewModelProvider(this, ViewModelFactoryActivity(this,application))[HelpViewModel::class.java]
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.toolbar1);
        window.statusBarColor = resources.getColor(R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        val editFormImageView = toolbar.findViewById<ImageView>(R.id.back_press_iv)
        val title = toolbar.findViewById<TextView>(R.id.title_tv)
        title.setText("Help")
        editFormImageView.setOnClickListener {
            finish()
        }
        val loginEmailId: String = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
        if (checkInternet()) {
            helpViewModel.getUserStatus(loginEmailId)
        }
        binding.tvEmail.setOnClickListener {

            sendEmail("apacictservicedesk@cnhind.com", "Help", "")

        }
        binding.tvMobile.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:7021653123")
            startActivity(intent)
        }

        helpViewModel.messageVal.observe(this, Observer {
            getUserStatus(it)
        })
        helpViewModel.message.observe(this, Observer {
            getZoneVals(it)
        })
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, message)

        try {
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        } catch (e: Exception) {
            // Handle the exception
        }
    }

    private fun getUserStatus(it: Event<Any, String>?) {
        if (it != null && it.getType() == "userstatus") {
            var statusdto: StatusDto? = null
            statusdto = it.peekContent() as StatusDto
            userstatus = statusdto.status
            if (userstatus == "In Active") {
                alertDialog(
                    this,
                    "Alert!",
                    "You Are In-Active User. Please Logout!",
                    false,
                    "Yes",
                    ""
                )
            }
        }
    }

    // Other methods...

    private fun getZoneVals(postList: Event<Any, String>?) {
        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            if (postList.getType() == "Offline_Check") { //check offline here
                if (offlineBox.all.size > 0)
                    offlineListChecker()
            } else if (postList.getType() == "Offline") { //loop for next occurance in offline list
                binding.offlineProgress.visibility = View.GONE
//                (requireContext() as MainActivity).window
//                    .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (offlineBox.all.size > 0) {
                    //remove the first obj as its synced with server
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount)
                            .build().find()
                    if (postList.isNotEmpty()) {
                        offlineBox.remove(postList)
                    }
                }
                offlineListChecker()
            }
        }
        if (postList?.getType() == "showSyncAlert") {
            alertDialog("Alert!", "Server sync has been done successfully", true, "Okay", "")
        }
    }

    private fun offlineListChecker() {
        //  offlineBox.removeAll()
        val updateHandler = Handler() //using handler to get the internet connection
        val runnable = Runnable {
            if (checkInternet()) {
                Log.e(
                    "this is offline check",
                    ">>>>>>.." + checkInternet()
                )
                Log.d("1111111111111", "offlineListChecker: "+offlineBox.all.size )
                if (offlineBox.all.size > 0) {
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    Log.d("1111111111111", "offlineListChecker: "+postList.size )
                    if (postList.isNotEmpty()) {
                        binding.offlineProgress.visibility = View.VISIBLE
//                        (mContext).window.setFlags(
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//                        );
                        var jsonData = postList[0].jsonData
                        var quoteNo1= postList[0].QuoteNo?.toInt()
                        var quoteNo=SharedPref.read(Constants.PREF_QUOTE_NO, "0")?.toInt()
                        Log.d("jsonData", "offlineListChecker: ${jsonData.toString()}     Quottttt${postList[0].QuoteNo}")
                        Log.d("offlineListChecker","offlineListChecker:   $quoteNo $quoteNo1")
                        if(quoteNo!=(quoteNo1)){
                            Log.d("offlineListChecker","offlineListChecker22222222:   $quoteNo $quoteNo1")
                            if (quoteNo1 != null) {
                                SharedPref.write(Constants.PREF_QUOTE_NO,"${quoteNo1}")
                            }
                        helpViewModel.postQuoteAPI(
                            jsonData.toString(),
                            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                            "3"
                        )
                    }}
                }
            }
        }
        updateHandler.postDelayed(runnable, 100)

    }

    private fun alertDialog(
        context: Context,
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(positive) { dialogInterface, which ->
            var rememberMeUserName = SharedPref.read(PREF_USERNAME, "")
            var rememberMePassword = SharedPref.read(PREF_PASSWORD, "")
            SharedPref.write(Constants.PREF_LOGGED_IN, "")
            SharedPref.clearPref()
            SharedPref.write(PREF_USERNAME, rememberMeUserName)
            SharedPref.write(PREF_PASSWORD, rememberMePassword)
            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as MainActivity).finishAffinity()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        Log.e(
            "this is offline check",
            ">>>>>>.." + checkInternet()
        )
    }
}
