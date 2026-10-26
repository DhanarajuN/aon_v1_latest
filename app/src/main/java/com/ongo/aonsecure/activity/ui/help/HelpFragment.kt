package com.ongo.aonsecure.activity.ui.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.LoginActivity
import com.ongo.aonsecure.activity.MainActivity
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.FragmentHelpBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.injection.ViewModelFactory
import com.ongo.aonsecure.model.OfflineJson
import com.ongo.aonsecure.model.OfflineJson_
import com.ongo.aonsecure.model.StatusDto
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import pdf.app.library.utils.Constants

class HelpFragment : Fragment() {

    companion object {
        fun newInstance() = HelpFragment()
    }
    private lateinit var helpViewModel: HelpViewModel
    private lateinit var binding: FragmentHelpBinding
    var userstatus: String? = ""

    var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    var offlineDBCount: Long = 0

    private var fragmentCallback: FragmentCallback? = null

    fun setFragmentCallback(callback: FragmentCallback?) {
        this.fragmentCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_help, container, false);
        helpViewModel = ViewModelProvider(this, ViewModelFactory(this,requireActivity().application))[HelpViewModel::class.java]

        val loginEmailId: String = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
        if ((requireContext() as MainActivity).checkInternet()) {
            helpViewModel.getUserStatus(loginEmailId)
        }
        binding.tvEmail.setOnClickListener {

            sendEmail("apacictservicedesk@cnhind.com", "Help", "");

        }
        binding.tvMobile.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL);
            intent.data = Uri.parse("tel:18001084050");
            startActivity(intent);
        }

        helpViewModel.messageVal.observe(viewLifecycleOwner, Observer {
            getUserStatus(it)
        })
        helpViewModel.message.observe(viewLifecycleOwner, Observer {
            getZoneVals(it)
        })

        return binding.root
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SEND)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, message)


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        } catch (e: Exception) {
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message

        }

    }

    private fun getUserStatus(it: Event<Any, String>?) {
        if (it != null && it.getType() == "userstatus") {
            var statusdto: StatusDto? = null
            statusdto = it.peekContent() as StatusDto
            userstatus = statusdto.status
            if (userstatus == "In Active") {
                alertDialog(
                    requireContext(),
                    "Alert!",
                    "You Are In-Active User. Please Logout!",
                    false,
                    "Yes",
                    ""
                )
            }
        }
    }

    fun autoSyncCallForOfflineCHeckHelp(isInternetAvailable: Boolean) {
        if (isInternetAvailable) {
            offlineListChecker()
        } else
            Log.e("called for offline", "yes")
    }

    private fun alertHelpDialog(
        context: Context,
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String
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

            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as MainActivity).finishAffinity()
        }
        /*builder.setNegativeButton(negative) { dialogInterface, which ->
        }*/
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }


    private fun getZoneVals(postList: Event<Any, String>?) {
        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            if (postList.getType() == "Offline_Check") { //check offline here
                if (offlineBox.all.size > 0)
                    offlineListChecker()
            } else if (postList.getType() == "Offline") { //loop for next occurance in offline list
                binding.offlineProgress.visibility = View.GONE
                (requireContext() as MainActivity).window
                    .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
            if ((requireContext() as MainActivity).checkInternet()) {
                Log.e(
                    "this is offline check",
                    ">>>>>>.." + (requireContext() as MainActivity).checkInternet()
                )
                Log.d("1111111111111", "offlineListChecker: "+offlineBox.all.size )
                if (offlineBox.all.size > 0) {
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    Log.d("1111111111111", "offlineListChecker: "+postList.size )
                    if (postList.isNotEmpty()) {
                        binding.offlineProgress.visibility = View.VISIBLE
                        (requireContext() as MainActivity).window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        );
                        var jsonData = postList[0].jsonData
                        helpViewModel.postQuoteAPI(
                            jsonData.toString(),
                            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                            "3"
                        )
                    }
                }
            }
        }
        updateHandler.postDelayed(runnable, 100)

    }

    fun alertDialog(
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String,
        negative: String
    ) {
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        builder.setTitle(title)
        //set message for alert dialog
        builder.setMessage(msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton(positive) { dialogInterface, which ->
            //refresh frag here to get live data
            /*val homeFragment = HomeFragment.newInstance()
            (requireContext() as MainActivity).refreshFrag(homeFragment)*/
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
            "this is offline check", ">>>>>>.." + (requireContext() as MainActivity).checkInternet()
        )
    }

}
