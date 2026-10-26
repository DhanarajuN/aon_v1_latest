package com.ongo.aonsecure.activity.ui.profile

import android.content.Intent
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
import com.ongo.aonsecure.databinding.FragmentProfileBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.injection.ViewModelFactory
import com.ongo.aonsecure.model.OfflineJson
import com.ongo.aonsecure.model.OfflineJson_
import com.ongo.aonsecure.model.StatusDto
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import pdf.app.library.utils.Constants

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        profileViewModel =
            ViewModelProvider(this, ViewModelFactory(this,requireActivity().application))[ProfileViewModel::class.java]

        SharedPref.initialize(requireActivity())

        initUpdateViewValues()

        // userstatus = SharedPref.read(Constants.PREF_USER_STATUS, "")

        /*if (userstatus == "In Active") {
            alertDialog(
                requireContext(),
                "Alert!",
                "You Are In-Active User. Please Logout!",
                false,
                "Yes",
                "")
        }*/

        val loginEmailId: String = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
        if ((requireContext() as MainActivity).checkInternet()) {
            profileViewModel.getUserStatus(loginEmailId)
        }


        profileViewModel.messageVal.observe(viewLifecycleOwner, Observer {
            getuserStatus(it)
        })
        profileViewModel.message.observe(viewLifecycleOwner, Observer {
            getZoneVals(it)
        })

        //  throw RuntimeException("Test Crash") // Force a crash
        return binding.root
    }

    fun autoSyncCallForOfflineCHeckProfile(isInternetAvailable: Boolean) {
        if (isInternetAvailable) {
            offlineListChecker()
        } else
            Log.e("called for offline", "yes")
    }

    private fun getuserStatus(it: Event<Any, String>?) {
        if (it != null && it.getType() == "userstatus") {
            var statusdto: StatusDto? = null
            statusdto = it.peekContent() as StatusDto
            userstatus = statusdto.status
            if (userstatus == "In Active") {
                userStatusChanged()
            }
        }
    }


    fun userStatusChanged() {
        if ((requireContext() as MainActivity).isInternetConnected)
            if (offlineBox.all.size > 0) {
                offlineDBCount = offlineBox.all.size.toLong()
                var postList: List<OfflineJson> = offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()

                if (postList.isNotEmpty()) {
                    binding.offlineProgress.visibility = View.VISIBLE
                    (requireContext() as MainActivity).window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )

                    var jsonData = postList[0].jsonData
                    profileViewModel.postQuoteAPI(jsonData.toString(),
                        SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(), "3")

                } else {
                    if (userstatus == "In Active") {
                        alertDialog(
                            requireContext(),
                            "Alert!",
                            "Your profile has been made inactive, please contact Admin Support",
                            false,
                            "okay",
                            ""
                        )
                    } else {
                        alertDialogForOfflineSync(
                            "Alert!",
                            "Offline quotes available. Kindly check with internet connection to sync?",
                            false,
                            "Yes",
                            "No"
                        )
                    }
                }

            }
    }


    private fun getZoneVals(postList: Event<Any, String>?) {
        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            if (postList.getType() == "Offline") { //loop for next occurance in offline list
                binding.offlineProgress.visibility = View.GONE
                (requireContext() as MainActivity).window
                    .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (offlineBox.all.size > 0) {
                    offlineDBCount = offlineBox.all.size.toLong()
                    //remove the first obj as its synced with server
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount)
                            .build().find()
                    if (postList.isNotEmpty()) {
                        offlineBox.remove(postList)

                        if (userstatus == "In Active") {
                            alertDialog(
                                requireContext(),
                                "Alert!",
                                "Your profile has been made inactive, please contact Admin Support",
                                false,
                                "okay",
                                ""
                            )
                        } else {
                            alertDialogForOfflineSync(
                                "Alert!",
                                "Offline quotes available. Kindly check with internet connection to sync?",
                                false,
                                "Yes",
                                "No"
                            )
                        }

                    }
                }
                offlineListChecker()
            }

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
                if (offlineBox.all.size > 0) {
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    if (postList.isNotEmpty()) {
                        binding.offlineProgress.visibility = View.VISIBLE
                        (requireContext() as MainActivity).window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        );
                        var jsonData = postList[0].jsonData
                        profileViewModel.postQuoteAPI(
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

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        Log.e(
            "this is offline check", ">>>>>>.." + (requireContext() as MainActivity).checkInternet()
        )
    }

    private fun initUpdateViewValues() {

        //inflate the values here
        binding.nameEt.setText(SharedPref.read(Constants.PREF_FULL_NAME, ""))
        binding.mobileNoEt.setText(SharedPref.read(Constants.PREF_MOBILE, ""))
        binding.emailEt.setText(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, ""))
        binding.rmidEt.setText(SharedPref.read(Constants.PREF_RMID, ""))

        binding.logoutButton.setOnClickListener {
            if (offlineBox.all.size > 0) //if you have anything to be synced with server show this alert
                userStatusChanged()
            else
                alertDialog(
                    requireContext(),
                    "Alert!",
                    "Want to logout. Are you sure?",
                    false,
                    "Yes",
                    "No"
                )
        }
    }

    fun alertDialogForOfflineSync(
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

            //   userStatusChanged()

            var rememberMeUserName = SharedPref.read(PREF_USERNAME, "")
            var rememberMePassword = SharedPref.read(PREF_PASSWORD, "")
            //logout user
            SharedPref.write(Constants.PREF_LOGGED_IN, "")
            SharedPref.clearPref()

            SharedPref.write(PREF_USERNAME, rememberMeUserName)
            SharedPref.write(PREF_PASSWORD, rememberMePassword)
            // SharedPref.write(PREF_USER_PASSWORD_EXPIRED,"passwordexpired")

            context?.startActivity(Intent(context, LoginActivity::class.java))
            (context as MainActivity).finishAffinity()
        }
        builder.setNegativeButton(negative) { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()

    }
}
