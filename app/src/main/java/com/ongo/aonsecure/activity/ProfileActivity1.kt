import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.LoginActivity
import com.ongo.aonsecure.activity.ui.profile.ProfileViewModel
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.FragmentProfileBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.injection.ViewModelFactoryActivity
import com.ongo.aonsecure.model.OfflineJson
import com.ongo.aonsecure.model.OfflineJson_
import com.ongo.aonsecure.model.StatusDto
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import pdf.app.library.utils.Constants

class ProfileActivity1 : BaseCompatActivity() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
    private var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    private var offlineDBCount: Long = 0
    var userstatus: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_profile)
        profileViewModel = ViewModelProvider(this, ViewModelFactoryActivity(this,application))[ProfileViewModel::class.java]

        SharedPref.initialize(this)

        initUpdateViewValues()

        val loginEmailId: String = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
        if (checkInternet()) {
            profileViewModel.getUserStatus(loginEmailId)
        }

        profileViewModel.messageVal.observe(this, { getuserStatus(it) })
        profileViewModel.message.observe(this, { getZoneVals(it) })
    }

    private fun getuserStatus(it: Event<Any, String>?) {
        if (it != null && it.getType() == "userstatus") {
            var statusdto: StatusDto? = null
            statusdto = it.peekContent() as StatusDto
            val userstatus = statusdto.status
            if (userstatus == "In Active") {
                userStatusChanged()
            }
        }
    }

    private fun getZoneVals(postList: Event<Any, String>?) {
        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            if (postList.getType() == "Offline") {
                binding.offlineProgress.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (offlineBox.all.size > 0) {
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    if (postList.isNotEmpty()) {
                        offlineBox.remove(postList)

                        if (userstatus == "In Active") {
                            alertDialog(
                                this,
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
        val updateHandler = Handler()
        val runnable = Runnable {
            if (checkInternet()) {
                Log.e("this is offline check", ">>>>>>.." + checkInternet())
                if (offlineBox.all.size > 0) {
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    if (postList.isNotEmpty()) {
                        binding.offlineProgress.visibility = View.VISIBLE
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        val jsonData = postList[0].jsonData
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

    private fun initUpdateViewValues() {
        binding.nameEt.setText(SharedPref.read(Constants.PREF_FULL_NAME, ""))
        binding.mobileNoEt.setText(SharedPref.read(Constants.PREF_MOBILE, ""))
        binding.emailEt.setText(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, ""))
        binding.rmidEt.setText(SharedPref.read(Constants.PREF_RMID, ""))

        binding.logoutButton.setOnClickListener {
            if (offlineBox.all.size > 0)
                userStatusChanged()
            else
                alertDialog(
                    this,
                    "Alert!",
                    "Want to logout. Are you sure?",
                    false,
                    "Yes",
                    "No"
                )
        }
    }

    private fun userStatusChanged() {
        var checkInternetConnection=checkInternet()
        if ((checkInternetConnection))
            if (offlineBox.all.size > 0) {
                offlineDBCount = offlineBox.all.size.toLong()
                var postList: List<OfflineJson> = offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()

                if (postList.isNotEmpty()) {
                    binding.offlineProgress.visibility = View.VISIBLE
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )

                    var jsonData = postList[0].jsonData
                    profileViewModel.postQuoteAPI(jsonData.toString(),
                        SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(), "3")

                } else {
                    if (userstatus == "In Active") {
                        alertDialog(
                            this,
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

    private fun alertDialogForOfflineSync(
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String,
        negative: String
    ) {
        val builder = AlertDialog.Builder(this)
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
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
        builder.setNegativeButton(negative) { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }
}
