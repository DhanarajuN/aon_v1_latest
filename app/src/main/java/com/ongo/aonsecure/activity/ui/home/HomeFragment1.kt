package com.ongo.aonsecure.activity.ui.home

import android.app.DatePickerDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.Contacts
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.HomeActivity1
import com.ongo.aonsecure.activity.LoginActivity
import com.ongo.aonsecure.activity.ui.quoteview.QuoteFragment
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.FragmentHome1Binding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.injection.ViewModelFactory
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import pdf.app.library.utils.Constants
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment1 : Fragment() {

    companion object {
        fun newInstance() = HomeFragment1()
    }

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHome1Binding
    private lateinit var zoneSpinner: AppCompatSpinner
    private lateinit var insurerSpinner: AppCompatSpinner
    private lateinit var prodCatSpinner: AppCompatSpinner
    private lateinit var productSpinner: AppCompatSpinner
    private lateinit var fundingSpinner: AppCompatSpinner
    private lateinit var genderSpinner: AppCompatSpinner
    private lateinit var tenureSpinner: AppCompatSpinner
    private lateinit var prodTypeSpinner: AppCompatSpinner
    private lateinit var loanTypeSpinner: AppCompatSpinner
    var prodCatArry: ArrayList<Job> = ArrayList()
    var prodArry: ArrayList<Job> = ArrayList()

    var insurerArry: ArrayList<Job> = ArrayList()


    var selectedInsurerName: String = ""
    var selectedZone: String = ""
    var selectedCategory: String = ""


    var userstatus: String? = ""
    var userZonestatus: String? = ""
    var loginEmailId = ""
    var minAge="";
    var maxAge="";
    var maxSumInsured=""

    var mContext: Context? = null

    //var context = this
    var connectivity: ConnectivityManager? = null
    var info: NetworkInfo? = null

    var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    var offlineDBCount: Long = 0
    var isAlertShown: Boolean = false;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home1, container, false)
        homeViewModel = ViewModelProvider(this, ViewModelFactory(this,requireActivity().application))[HomeViewModel::class.java]
        insurerSpinner = binding.insureSpinner1
        zoneSpinner = binding.zoneSpinner
        prodCatSpinner = binding.categorySpinner
        productSpinner = binding.productSpinner
        fundingSpinner = binding.fundingSpinner
        genderSpinner = binding.genderSpinner
        tenureSpinner = binding.tenureSpinner
       // prodTypeSpinner = binding.prod
        loanTypeSpinner = binding.loanTypeSpinner

        mContext = requireContext().applicationContext


        //binding.loanEt.filters = arrayOf(DecimalDigitsInputFilter(10, 3))

        binding.loanEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.coverageText.setText(s)
                homeViewModel.coverageText(s.toString())
            }
        })

        binding.coverageText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                homeViewModel.coverageText(s.toString())
            }
        })

        /******
         * calling the api's from view model
         ******/

        if (SharedPref.read(Constants.PREF_USER_STATUS, "").toString() == "Inactive"
            || SharedPref.read(Constants.PREF_USER_STATUS, "").toString() == "In Active"
        ) {
            alertHomeDialog(
                requireContext(),
                "Alert!",
                "Your profile has been made inactive, please contact Admin Support",
                false,
                "okay"
            )
        } else {
            callViewModelForAPI()
        }

        connectivity =
            mContext?.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            info = connectivity!!.activeNetworkInfo
            if (info != null) {
                if (info!!.state == NetworkInfo.State.CONNECTED) {
                    loginEmailId = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
                    homeViewModel.getFirstUserStatus(loginEmailId)
                } else {
                    callViewModelForAPI()
                }
            } else {
                callViewModelForAPI()
            }
        }


        homeViewModel.message.observe(viewLifecycleOwner, Observer {
            getFirstZoneStatus(it)
        })

        homeViewModel.zone.observe(viewLifecycleOwner, Observer {
            getZoneStatus(it)
        })

        homeViewModel.message.observe(viewLifecycleOwner, Observer {
            getZoneVals(it)
        })

        homeViewModel.message.observe(viewLifecycleOwner, Observer {
            getofflineSyncStatus(it)
            Log.d("jsonData", "onCreateView: ${it.getType()}")
        })

        homeViewModel.premiumcal.observe(viewLifecycleOwner, Observer {
            callQuoteFrag(it)
        })

        binding.syncButton.setOnClickListener {
            homeViewModel.deleteDBAndGetServerData()
        }


        binding.loanEt.addTextChangedListener(NumberTextWatcher(binding.loanEt))
        binding.coverageText.addTextChangedListener(NumberTextWatcher(binding.coverageText));


        homeViewModel.getStatus().observe(viewLifecycleOwner, Observer { handleStatus(it) })

        homeViewModel.getAlertMsg().observe(viewLifecycleOwner, Observer { alertHandle(it) })

        homeViewModel.messageVal.observe(viewLifecycleOwner, Observer {
            showProgress(it)
        })
        zoneSpin()
        fundingSpin()
        genderSpin()
        tenureAutoText()
        clickFunctions()


        binding.scroll.setOnTouchListener { view, ev ->
            hideKeyboard(view)
            false
        }

        return binding.root
    }


    fun autoSyncCallForOfflineCHeck(isInternetAvailable: Boolean) {
        Log.d("5555555", "onNetworkConnectionChanged3: $isInternetAvailable")

        if (isInternetAvailable)
            offlineListChecker()
        else
            Log.e("called for offline", "yes")
    }

    private fun getFirstZoneStatus(it: Event<Any, String>?) {
        if (it != null) {

            if (it.getType() == "userstatus") {
                var status = it.peekContent() as StatusDto
                userstatus = status.status
                if (userstatus == "In Active" || userstatus == "Inactive") {
                    userStatusChanged()
                }
            } else if (it.getType() == "userzone") {
                var userZone: ObjResponse? = null
                userZone = it.peekContent() as ObjResponse
                userZonestatus = userZone.Zone.split("(").first()
                /*binding.zoneEt.setText(userZonestatus)*/
               // if(userZone.Zone.contains('|')){
                    SharedPref.writeArrayList(Constants.PREF_ZONES, ArrayList(userZone.Zone.split("|")))
              //  }
                Log.d("testing...", "getFirstZoneStatus: "+SharedPref.readArrayList(Constants.PREF_ZONES))

                if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                    SharedPref.write(Constants.PREF_ZONE, userZonestatus)
                    zoneSpin()

                    callViewModelForAPI()
                    userStatusChanged()
                } else {
                    Log.d("jsonData", "getFirstZoneStatus: ")
                    // offlineListChecker()
                    //  offlineListChecker()
                }
            }

        }
    }

    private fun getZoneStatus(it: Event<Any, String>?) {
        if (it != null) {

            if (it.getType() == "userstatus") {
                var status = it.peekContent() as StatusDto
                userstatus = status.status
                if (userstatus == "In Active" || userstatus == "Inactive") {
                    userStatusChanged()
                }
            } else if (it.getType() == "userzone") {
                var userZone: ObjResponse? = null
                userZone = it.peekContent() as ObjResponse
                userZonestatus = userZone.Zone.split("(").first()
                val aaa=SharedPref.readArrayList(Constants.PREF_ZONES);
                if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                    userStatusChanged()
                } else {
                    if (userstatus == "In Active" || userstatus == "Inactive") {

                    } else
                        homeViewModel.checkCalculatePasswordAPI()
                }
            }else if(it.getType() == "userzone1"){
                var userZone: ObjResponse? = null
                userZone = it.peekContent() as ObjResponse
                userZonestatus = userZone.Zone.split("(").first()
              // if(userZone.Zone.contains('|')){
                    SharedPref.writeArrayList(Constants.PREF_ZONES, ArrayList(userZone.Zone.split("|")))
            //    }
                zoneSpin()
            }
            //homeViewModel.calculatePremium()
        }
    }

    private fun alertHomeDialog(
        context: Context,
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String
    ) {

        isAlertShown = true

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

            requireContext().startActivity(Intent(requireContext(), LoginActivity::class.java))
            (requireContext() as HomeActivity1).finishAffinity()
        }
        /*builder.setNegativeButton(negative) { dialogInterface, which ->
        }*/
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }

    private fun alertuserZonelogoutDialog(
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
            homeViewModel.logoutdeleteDBAndGetServerData()

            /**** saving this pref because remember me is saved with these two prefs ****/
            var rememberMeUserName = SharedPref.read(PREF_USERNAME, "")
            var rememberMePassword = SharedPref.read(PREF_PASSWORD, "")
            //logout user
            SharedPref.write(Constants.PREF_LOGGED_IN, "")
            SharedPref.clearPref()

            SharedPref.write(PREF_USERNAME, rememberMeUserName)
            SharedPref.write(PREF_PASSWORD, rememberMePassword)

            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as HomeActivity1).finishAffinity()
        }
        /*builder.setNegativeButton(negative) { dialogInterface, which ->
        }*/
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }
    private  fun tokenRefreshAPI(){
        val updateHandler = Handler() //using handler to get the internet connection
        val runnable = Runnable {
            if ((requireContext() as HomeActivity1).checkInternet())
                homeViewModel.tokenRefrehAPI()
        }
        updateHandler.postDelayed(runnable, 100)
    }
    private fun callViewModelForAPI() {
        val updateHandler = Handler() //using handler to get the internet connection
        val runnable = Runnable {
            if ((requireContext() as HomeActivity1).checkInternet())
                tokenRefreshAPI()
            // this is for online check when internet is on
            else
                homeViewModel.loadInsurer()  //this is called to check if we have offline db present
        }
        updateHandler.postDelayed(runnable, 100)
    }

    override fun onStart() {
        super.onStart()
        Log.e("onstart", "called")
    }

    fun showProgress(it: Event<Any, String>?) {
        if (it?.peekContent() == "show") {
            binding.progressBar3.visibility = View.VISIBLE
            binding.relative.alpha = 0.5F
            (requireContext() as HomeActivity1).window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

        } else {
            binding.progressBar3.visibility = View.GONE
            binding.relative.alpha = 1.0F
            (requireContext() as HomeActivity1).window.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }

    }


    private fun alertHandle(it: Status?) {
        when (it) {
            ZONE -> {
                alertDialog("Alert!", "Zone type not found", false, "Okay", "")
            }
            INSURER -> {
                alertDialog("Alert!", "Insurer type not found", false, "Okay", "")
            }
            PRODCAT -> {
                alertDialog("Alert!", "Category type not found", false, "Okay", "")
            }
            PRODUCT -> {
                alertDialog("Alert!", "Product type not found", false, "Okay", "")
            }
            FUNDTYPE -> {
                alertDialog("Alert!", "Fund type not found", false, "Okay", "")
            }
            DATERANGE -> {
                alertDialog(
                    "Alert!",
                    "No Active Calculator found for this Product",
                    false,
                    "Okay",
                    ""
                )
            }
            CURRENT_JOB_STATUS -> {
                alertHomeDialog(
                    requireContext(),
                    "Alert!",
                    "Database has undergone change. Please logout",
                    false,
                    "Okay"
                )
            }

            NO_PREMIUM -> {
                alertDialog(
                    "Alert!",
                    "No suitable premium value found for selected values",
                    false,
                    "Okay",
                    ""
                )
            }
            SUCCESS -> TODO()
            LOADING -> TODO()
            ERROR -> TODO()
            SERVER_CONNECTION_ERROR -> TODO()
            SIGN_UP -> TODO()
            SIGN_IN -> TODO()
            INVALID_CREDENTIALS -> TODO()
            INVALID_ACCESS_TOKEN -> TODO()
            INVALID_USER -> TODO()
            IMAGE_UPLOAD_FAILED -> TODO()
            REMOVE_ERRORS -> TODO()
            CHANGE_FRAG -> TODO()
            TENURE_EMPTY -> TODO()
            TENURE_MISMATCH -> TODO()
            TERMS_CHECK -> TODO()
            EMPTY_EMAIL -> TODO()
            EMPTY_MOBILE -> TODO()
            INVALID_MOBILE -> TODO()
            INVALID_EMAIL -> TODO()
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
            Gender -> TODO()
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
            PRODUCT_TYPE_EMPTY  -> TODO()
        }
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
        if (cancellable) {
            homeViewModel.loadInsurer()
        }
    }


    private fun hideKeyboard(view: View?) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun handleStatus(status: Status?) {
        when (status) {
            REMOVE_ERRORS -> {
                binding.insurerTxtTil.isErrorEnabled=false
                binding.categoryTxtTil.isErrorEnabled=false
                binding.genderTxtTil.isErrorEnabled=false
                binding.productTxtTil.isErrorEnabled=false
                binding.fundingTxtTil.isErrorEnabled=false
                binding.dobTil.isErrorEnabled = false
                binding.ageTil.isErrorEnabled = false
                binding.loanTil.isErrorEnabled = false
                binding.coverageTil.isErrorEnabled = false
                binding.tenureTxtTil.isErrorEnabled = false
                binding.ageTil.isErrorEnabled = false
                binding.contractTil.isErrorEnabled=false
                binding.productTypeTxtTil.isErrorEnabled=false
            }
            INSURER -> {
                binding.insurerTxtTil.isErrorEnabled=true
                binding.insurerTxtTil.error = "Select Insurer"
            }
            PRODCAT -> {
                binding.categoryTxtTil.isErrorEnabled=true
                binding.categoryTxtTil.error = "Select Category"
            }
            Gender -> {
                binding.genderTxtTil.isErrorEnabled=true
                binding.genderTxtTil.error = "Select Gender"
            } PRODUCT -> {
            binding.productTxtTil.isErrorEnabled=true
            binding.productTxtTil.error = "Select Product"
        }
            FUNDTYPE -> {
                binding.fundingTxtTil.isErrorEnabled=true
                binding.fundingTxtTil.error = "Select FundingType"
            }
            DOB_EMPTY -> {
                binding.dobTil.isErrorEnabled = true
                binding.dobTil.error = "Enter date of birth!"
            }
            PRODUCT_TYPE_EMPTY ->{
                binding.productTypeTxtTil.isErrorEnabled=true
                binding.productTypeTxtTil.error="Select Type"
            }
            AGE_EMPTY -> {
                binding.ageTil.isErrorEnabled = true
                binding.ageTil.error = "Enter age!"
            }
            LOAN_EMPTY -> {
                binding.loanTil.isErrorEnabled = true
                binding.loanTil.error = "Enter loan amount!"
            }
            LOAN_INVALID -> {
                binding.coverageTil.isErrorEnabled = true
                binding.coverageTil.error = "Coverage amount should be lesser than loan amount!"
            }
            LOAN_INVALID1 -> {
                binding.loanTil.isErrorEnabled = true
                binding.loanTil.error = "Loan amount should be between 10,000 to $maxSumInsured"
            }

            LOAN_MIN -> {
                binding.loanTil.isErrorEnabled = true
                binding.loanTil.error = "Loan amount should be between 10,000 to $maxSumInsured"
            }
            COVERAGE_EMPTY -> {
                binding.coverageTil.isErrorEnabled = true
                binding.coverageTil.error = "Enter coverage amount!"
            }
            COVERAGE_MIN -> {
                binding.coverageTil.isErrorEnabled = true
                binding.coverageTil.error = "Loan amount should be between 10,000 to $maxSumInsured"
            }
            PRODUCT_TYPE_EMPTY -> {
                binding.productTypeTxtTil.isErrorEnabled = true
                binding.productTypeTxtTil.error = "Enter type value!"
            }
            TENURE_EMPTY -> {
                binding.tenureTxtTil.isErrorEnabled = true
                binding.tenureTxtTil.error = "Enter tenure value!"
            }
            TENURE_MISMATCH -> {
                binding.tenureTxtTil.isErrorEnabled = true
                binding.tenureTxtTil.error = "Loan tenure should be between 6 to 72 months"
            }
            AGE_INVALID -> {
                binding.ageTil.isErrorEnabled = true
                binding.ageTil.error = "Age should be between $minAge-$maxAge years"
            }
            AGE_INVALID_COVER -> {
                binding.ageTil.isErrorEnabled = true
                binding.ageTil.error = "Maximum age at maturity should be 80 years"
            }
            CONTACT_NUMBER_EMPTY -> {
                binding.contractTil.isErrorEnabled = true
                binding.contractTil.error = "Enter Contact Number!"
            }
CONTACT_NUMBER -> {
                binding.contractTil.isErrorEnabled = true
                binding.contractTil.error = "Invalid Contact Number!"
            }
            SUCCESS -> TODO()
            LOADING -> TODO()
            ERROR -> TODO()
            SERVER_CONNECTION_ERROR -> TODO()
            SIGN_UP -> TODO()
            SIGN_IN -> TODO()
            INVALID_CREDENTIALS -> TODO()
            INVALID_ACCESS_TOKEN -> TODO()
            INVALID_USER -> TODO()
            IMAGE_UPLOAD_FAILED -> TODO()
            CHANGE_FRAG -> TODO()
            TERMS_CHECK -> TODO()
            EMPTY_EMAIL -> TODO()
            EMPTY_MOBILE -> TODO()
            INVALID_MOBILE -> TODO()
            INVALID_EMAIL -> TODO()
            EMPTY_PASSWORD -> TODO()
            INVALID_PASSWORD -> TODO()
            EMPTY_FIRST_NAME -> TODO()
            EMPTY_LAST_NAME -> TODO()
            EMPTY_CITY -> TODO()
            INVALID_URI -> TODO()
            EMPTY_RMID -> TODO()
            INVALID_RMID -> TODO()
            SUMINSURED_RANGE -> TODO()
            QUOTE_SUCCESS -> TODO()
            TERMS -> TODO()
            ZONE -> TODO()
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
        }
    }

    private fun clickFunctions() {

        binding.dobEt.setOnClickListener(View.OnClickListener {
            clickDataPicker()
        })

        binding.calcButton.setOnClickListener(View.OnClickListener {
            homeViewModel.inflatetextValues(
                binding.loanEt.text.toString(),
                binding.remarkEt.text.toString(),
                binding.contractnoEt.text.toString()
            )
            connectivity =
                requireContext().getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                info = connectivity!!.activeNetworkInfo
                if (info != null) {
                    if (info!!.state == NetworkInfo.State.CONNECTED) {
                        loginEmailId =
                            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
                        homeViewModel.getUserStatus(loginEmailId)
                    } else {
                        homeViewModel.calculatePremium()
                    }
                } else {
                    homeViewModel.calculatePremium()
                }
            }
            //  homeViewModel.calculatePremium()
        })
    }

    private fun clickDataPicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) - 18 // to reduce number of years in datepicker
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in Toast
                //Toast.makeText(this, """$dayOfMonth - ${monthOfYear + 1} - $year""", Toast.LENGTH_LONG).show()
                binding.dobEt.setText("""$dayOfMonth/${monthOfYear + 1}/$year""")
                //inflate age text
                binding.ageEt.setText(homeViewModel.getAge(year, monthOfYear + 1, dayOfMonth))

                homeViewModel.addAgeDOBToDTO(
                    binding.dobEt.text.toString(),
                    binding.ageEt.text.toString()
                )
            },
            year,
            month,
            day
        )
        //this is to restrict future dates
        dpd.datePicker.maxDate = (System.currentTimeMillis())
        dpd.show()
    }

    private fun callQuoteFrag(it: Event<Any, String>?) {
        if (it != null && it.getType() == "premCal") {
            var premQuote: PremQuote? = null
            premQuote = it.peekContent() as PremQuote

            val quoteFrag = QuoteFragment.newInstance(premQuote)
            (requireContext() as HomeActivity1).addQuoteFrag(quoteFrag)

        }
        if (it != null && it.getType() == "loanVal") {
            val formatter: NumberFormat = DecimalFormat("#.##")
            var dobVal: Double = it.peekContent() as Double
            binding.loanEt.text = formatter.format(dobVal) as Editable
        }
    }


    private fun getZoneVals(postList: Event<Any, String>?) {

        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            var jobArry = ArrayList<Job>()
            if (postList.peekContent() is ArrayList<*>) { // this is from DB
                if (postList.getType() == "ProdCat") {
                    var zoneArry = postList.peekContent() as ArrayList<ProdCat>

                    for (note in zoneArry) {
                        jobArry.addAll(note.jobs)

                    }
                } else if (postList.getType() == "Insurer") {
                    var prodandcatarray = postList.peekContent() as ArrayList<Insurer>

                    for (insurer in prodandcatarray) {
                        Log.d("testing", "getZoneVals: $insurer")

                        if (insurer != null && insurer.jobs != null) {
                            jobArry.addAll(insurer.jobs)
                        }
                    }

                } else if (postList.getType() == "Product") {

                    var zoneArry = postList.peekContent() as ArrayList<Product>

                    for (note in zoneArry) {
                        jobArry.addAll(note.jobs)

                    }
                } else if (postList.getType() == "Tenure") {
                    var insurerArry = postList.peekContent() as ArrayList<Tenure>
                    for (note in insurerArry) {
                        jobArry.addAll(note.jobs)
                    }
                } else if (postList.getType() == "LoanType") {
                    var insurerArry = postList.peekContent() as ArrayList<LoanType>
                    for (note in insurerArry) {
                        jobArry.addAll(note.jobs)
                    }
                } else if (postList.getType() == "taxval") {
                    var taxarray = postList.peekContent() as ArrayList<Tax>
                    for (tax in taxarray) {
                        SharedPref.write(Constants.PREF_TAX, tax.tax)
                    }
                } else if (postList.getType() == "userstatus") {
                    var statusarray = postList.peekContent() as ArrayList<StatusDto>
                    for (status in statusarray) {
                        userstatus = status.status
                        SharedPref.write(Constants.PREF_USER_STATUS, status.status)
                    }
                } else if(postList.getType()=="ProductType"){
                    var productTypeArry = postList.peekContent() as ArrayList<String>
                    var adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, productTypeArry);
                    productTypeAutoText(adapter1,productTypeArry)
                }
                /*else if (postList.getType() == "userstatus") {
                    var statusarray = postList.peekContent() as ArrayList<StatusDto>
                    for (status in statusarray) {
                        userstatus = status.status
                        SharedPref.write(Constants.PREF_USER_STATUS, status.status)
                    }*/
            } else { //this is from server api hit
                if (postList.getType() == "ProdCat") {
                    var zoneArry = postList.peekContent() as ProdCat
                    jobArry.addAll(zoneArry.jobs)

                } else if (postList.getType() == "Insurer") {
                    var prodandcat = postList.peekContent() as Insurer
                    jobArry.addAll(prodandcat.jobs)
                } else if (postList.getType() == "Product") {
                    var zoneArry = postList.peekContent() as Product
                    jobArry.addAll(zoneArry.jobs)
                } else if (postList.getType() == "Tenure") {
                    var insurerArry = postList.peekContent() as Tenure
                    jobArry.addAll(insurerArry.jobs)
                } else if (postList.getType() == "LoanType") {
                    var insurerArry = postList.peekContent() as LoanType
                    jobArry.addAll(insurerArry.jobs)
                } else if (postList.getType() == "taxval") {
                    var taxvalue = postList.peekContent() as Tax
                    SharedPref.write(Constants.PREF_TAX, taxvalue.tax)
                } else if (postList.getType() == "userstatus") {
                    var status = postList.peekContent() as StatusDto
                    userstatus = status.status
                    SharedPref.write(Constants.PREF_USER_STATUS, status.status)
                }else if (postList.getType() == "ProductType") {
                    val productTypeArry = postList.peekContent() as? ArrayList<String>
                    if (productTypeArry != null) {
                        val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, productTypeArry)
                        productTypeAutoText(adapter1, productTypeArry)
                    } else {
                        Log.e("testing", "ProductType content not a valid ArrayList<String>")
                    }
                }
            }

            spinnerAdapter(jobArry, postList.getType())

            //this is for offline sync db
            /*if (postList.getType() == "Offline_Check") { //check offline here
                if (offlineBox.all.size > 0)
                    offlineListChecker()
            }
            else if (postList.getType() == "Offline") {
                //loop for next occurance in offline list
                binding.offlineProgress.visibility = View.GONE
                (requireContext() as MainActivity).window
                    .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (offlineBox.all.size > 0) {
                    //remove the first obj as its synced with server
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> = offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    if (postList.isNotEmpty()) {
                        offlineBox.remove(postList)
                    }
                }
                offlineListChecker()
            }
*/

            if (postList.getType() == "showSyncAlert") {

                alertDialog(
                    "Alert!",
                    "Server sync has been done successfully",
                    true,
                    "Okay",
                    ""
                )
            }


            if (SharedPref.read(Constants.PREF_USER_STATUS, "").toString() == "Inactive"
                || SharedPref.read(Constants.PREF_USER_STATUS, "").toString() == "In Active"
            ) {
                if (!isAlertShown)
                    alertHomeDialog(
                        requireContext(),
                        "Alert!",
                        "Your profile has been made inactive, please contact Admin Support",
                        false,
                        "okay"
                    )

            }

            if (postList.getType() == "showLogout") {
                alertofInactiveDialog(
                    requireContext(),
                    "Alert!",
                    "Password Expired. Please Change the password",
                    false,
                    "Yes",
                    ""
                )
                /* com.ongo.aonsecure.utils.alertDialog(
                     requireContext(),
                     "Alert!",
                     "Password Expired. Please Change the password",
                     false,
                     "Yes",
                     ""
                 )*/
            }


            if (postList.getType() == "showPasswordNotification") {
                callPasswordAlertNotification(postList.peekContent() as Int)
            }

        }


    }

    private fun alertofInactiveDialog(
        context: Context,
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String,
        negative: String
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
            SharedPref.write(PREF_USER_PASSWORD_EXPIRED, "passwordexpired")

            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as HomeActivity1).finishAffinity()
        }
        builder.setNegativeButton(negative) { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()

    }

    override fun onPause() {
        super.onPause()
        Log.e(
            "this is offline check",
            ">>>>>>.." + (requireContext() as HomeActivity1).checkInternet()
        )
    }

    private fun offlineListChecker() {
        //  offlineBox.removeAll()
        val updateHandler = Handler() //using handler to get the internet connection
        val runnable = Runnable {


                if (offlineBox.all.size > 0) {
                    Log.d("jsonData", "offlineListChecker: ${offlineBox.count()}")
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =

                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()
                    Log.d("jsonData", "offlineListChecker: ${postList.count()}")
                    Log.d("jsonData", "offlineListChecker: $offlineDBCount")

                    if (postList.isNotEmpty()) {



                        // Iterate over each item in postList
                        /*  for (offlineItem in postList) {
                              var jsonData = offlineItem.jsonData
                              homeViewModel.postQuoteAPI(
                                  jsonData.toString(),
                                  SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                                  "3"
                              )
                              // You can perform additional actions for each item if needed
                          }*/


                        var jsonData = postList[0].jsonData
                        var quoteNo1= postList[0].QuoteNo?.toInt()
                        var quoteNo=SharedPref.read(Constants.PREF_QUOTE_NO, "0")?.toInt()
                        Log.d("jsonData", "offlineListChecker: ${jsonData.toString()}     Quottttt${postList[0].QuoteNo}")
                        Log.d("offlineListChecker","offlineListChecker:   $quoteNo $quoteNo1")
                        if(quoteNo!=(quoteNo1)){
                            binding.offlineProgress.visibility = View.VISIBLE
                            Log.d("offlineListChecker","offlineListChecker22222222:   $quoteNo $quoteNo1")
                            if (quoteNo1 != null) {
                                SharedPref.write(Constants.PREF_QUOTE_NO,"${quoteNo1}")
                            }
                        Log.d("jsonData", "offlineListChecker: ${jsonData.toString()}")
                        homeViewModel.postQuoteAPI(
                            jsonData.toString(),
                            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                            "3"
                        )
                            binding.offlineProgress.visibility = View.GONE

                        }}
                }

        }
        updateHandler.postDelayed(runnable, 100)

    }

    private fun spinnerAdapter(jobArry: ArrayList<Job>, type: String) {
        // val languages = resources.getStringArray(R.array.Languages)
        var nameArry = ArrayList<String>()

        for (value in jobArry) {
            if (type == "Tenure")
                nameArry.addAll(listOf(value.Tenure))
            else if (type == "Insurer") {
                nameArry.addAll(listOf(value.Name))
            } else if (type == "ProdCat") {
                nameArry.addAll(
                    listOf(
                        value.Name
                    )
                )
            } else if (type == "Product") {
                nameArry.addAll(listOf(value.Name))
            }
            else
                nameArry.addAll(listOf(value.Name))

            when (type) {
                "Tenure" -> nameArry.addAll(listOf(value.Tenure))
                "Insurer" -> {
                    nameArry.removeIf(Objects::isNull)
                    /*  val hs = HashSet<String>()
                     hs.addAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
                     nameArry.removeAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
                     nameArry.addAll(hs)*/
                    // nameArry.addAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
                }
                "ProdCat" -> {
                    nameArry.addAll(
                        listOf(
                            value.Name
                        )
                    )
                }
                "Product" -> {
                    nameArry.addAll(listOf(value.Name))
                }
                "LoanType" -> {
                    nameArry.removeIf(Objects::isNull)
                }
                else -> {
                    nameArry.addAll(listOf(value.Name))

                }
            }
        }
        Log.e("name arry", ">>>>>>>>$nameArry")
        var adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nameArry)

        when (type) {
            "Insurer" -> {
                insurerArry=jobArry
                insurerSpin(adapter, jobArry)
                Log.d("testing", "spinnerAdapter: insurer"+jobArry.toString())
            }
            "ProdCat" -> {
                if (jobArry != null) {
                    Log.d("testing", "spinnerAdapter: cataaa"+jobArry.toString())
                    prodCatArry.addAll(jobArry)
                    prodCatSpin(prodCatArry, selectedInsurerName, selectedZone)
                } else {
                    alertDialog(
                        "Alert!",
                        "No Active Category Found for this Insurer",
                        false,
                        "Okay",
                        ""
                    )
                }
            }
            "Product" -> {
                if (jobArry != null) {
                    prodArry.addAll(jobArry)
                    Log.d("testing", "spinnerAdapter: product1   "+jobArry.toString())

                    productSpin(prodArry, selectedInsurerName, selectedZone, selectedCategory)
                } else {
                    alertDialog(
                        "Alert!",
                        "No Active Product Found for this Category",
                        false,
                        "Okay",
                        ""
                    )
                }
            }
            "Tenure" -> {
                tenureSpin(adapter, jobArry)
            }
            "LoanType" -> {
                loanTypeSpin(adapter, jobArry)
            }
//            "ProductType" -> {
//                prodTypeSpin(adapter, jobArry)
//            }
        }

    }

    private fun loanTypeSpin(adapter: ArrayAdapter<String>, jobArry: ArrayList<Job>) {

        loanTypeSpinner.adapter = adapter
        binding.loanTypeTxtText.setAdapter(adapter)
       // binding.loanTypeTxtText.setText(adapter.getItem(0).toString(),false)
       // homeViewModel.loanTypeSpinText(jobArry[0].Name)
        binding.loanTypeTxtText.setOnItemClickListener { parent, view, position, id ->
            homeViewModel.loanTypeSpinText(jobArry[position].Name)

        }
//        binding.loanTypeTxtText.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?, position: Int, id: Long
//            ) {
//                homeViewModel.loanTypeSpinText(jobArry[position].Name)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // write code to perform some action
//            }
//        }
    }

/*    private fun zoneSpin(
        adapter: ArrayAdapter<String>,
        nameArry: ArrayList<Job>
    ) {
        zoneSpinner.adapter = adapter
        zoneSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                homeViewModel.zoneSpinText(nameArry.get(position).Name)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }*/

    private fun zoneSpin() {
        var zoneFromLoggedInUser: String? = SharedPref.read(Constants.PREF_ZONE, "")

       //    binding.zoneEt.setText(zoneFromLoggedInUser)
       // homeViewModel.zoneSpinText(zoneFromLoggedInUser.toString())

        selectedZone = zoneFromLoggedInUser.toString()
        Log.d("testing", "zoneSpin: "+SharedPref.readArrayList(Constants.PREF_ZONES))
        val zonesList = SharedPref.readArrayList(Constants.PREF_ZONES)
        val nameArry = zonesList.map { it.split("(").first() }.toList()
        Log.d("testing", "zoneSpin: "+nameArry)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nameArry)
        binding.zoneAT.setAdapter(adapter)
        if(!nameArry.isEmpty()){
        binding.zoneAT.setText(adapter.getItem(0).toString(),false)
        homeViewModel.zoneSpinText(adapter.getItem(0).toString())}
        binding.zoneAT.setOnItemClickListener { parent, _, position, _ ->
            val zone = parent.adapter.getItem(position).toString()
            homeViewModel.zoneSpinText(zone.toString())
            selectedZone=zone
            binding.insurerTxtText.setText("")
            selectedInsurerName=""
            insurerSpin(adapter,insurerArry);
        }

        /*var nameArry = listOf<String>(zoneFromLoggedInUser.toString())
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nameArry)
        zoneSpinner.adapter = adapter
        zoneSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                homeViewModel.zoneSpinText(nameArry[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }*/

    }

    private fun insurerSpin(adapter: ArrayAdapter<String>, nameArry: ArrayList<Job>) {
        Log.d("testing", "insurerSpin: "+nameArry.toString())
        if (nameArry != null && nameArry.isNotEmpty()) {
            var filterJob: ArrayList<Job> = ArrayList()
            var filterJobString: ArrayList<String> = ArrayList()
            for(insurer in nameArry){
               if(insurer.Zone.split("(").first().equals(selectedZone)){
                   filterJob.add(insurer)
                   filterJobString.add(insurer.Name)
               }
            }
            if (filterJob != null && filterJob.isNotEmpty() && filterJob.size > 0) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    filterJobString
                )
                insurerSpinner.adapter = adapter
                binding.insurerTxtText.setAdapter(adapter)
                //  binding.insurerTxtText.setText(adapter.getItem(0),false)
               // selectedInsurerName= adapter.getItem(0).toString();
                binding.insurerTxtText.setOnItemClickListener { parent, _, position, _ ->
                    val selectedInsurer = parent.adapter.getItem(position).toString()
                    homeViewModel.insureSpinText(selectedInsurer)
                    Log.d("11111", "onItemSelected: $selectedInsurer")
                    Log.e("insurer spin selected", ">>>>>>>>>>$selectedInsurer")
                    selectedInsurerName = selectedInsurer
                    binding.insurerTxtTil.isErrorEnabled=false

                    prodCatSpin(prodCatArry, selectedInsurer, selectedZone)
                    Log.d("testing", "spinnerAdapter: product  2 "+prodArry.toString())

                    productSpin(prodArry, selectedInsurer, selectedZone, selectedCategory)
            }

            }else{
                binding.insurerTxtText.setText("")
                binding.insurerTxtText.setAdapter(null)
                selectedInsurerName=""
                binding.categoryTxtText.setText("")
                binding.categoryTxtText.setAdapter(null)
                binding.productTxtText.setAdapter(null)
                binding.productTxtText.setText("")
                binding.genderTxtText.setText("")
            }

            binding.insurerTxtText.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    homeViewModel.insureSpinText(nameArry[position].Name)
                    Log.d("11111", "onItemSelected: ${nameArry[position].Name}")
                    Log.e("insurer spin selected", ">>>>>>>>>>${nameArry[position].Name}")
                    selectedInsurerName = nameArry[position].Name
                    prodCatSpin(prodCatArry, nameArry[position].Name, selectedZone)
                    Log.d("testing", "onItemSelected:4 "+prodArry)
                    productSpin(
                        prodArry,
                        nameArry[position].Name,
                        selectedZone,
                        selectedCategory
                    )

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }

        } else {
            alertDialog(
                "Alert!",
                "No Active Insurer Found for this Zone",
                false,
                "Okay",
                ""
            )
        }
    }

    private fun prodCatSpin(
        nameArry: ArrayList<Job>,
        insurerName: String? = "",
        selectedzone: String = ""
    ) {
        var filterJob: ArrayList<Job> = ArrayList()
        var filterJobString: ArrayList<String> = ArrayList()

        if (insurerName!!.isNotEmpty() && selectedzone!!.isNotEmpty()) {
            for (job in nameArry) {
                Log.d("testing", "prodCatSpin: "+insurerName +selectedzone +job.Insurer  +job.Zone)

                if (insurerName.equals(job.Insurer.split("(").firstOrNull()?.trim(), true)
                    && insurerName.equals(job.Insurer.split("(").firstOrNull()?.trim(), true) != null
                    /*&& (job.Zone).contains(selectedZone)*/
                    && selectedzone.equals(job.Zone.substring(0, job.Zone.indexOf("(")), true)
                    && selectedzone.equals(
                        job.Zone.substring(0, job.Zone.indexOf("(")),
                        true
                    ) != null
                ) {

                    if (filterJob.size > 0) {

                        var contains = false
                        for (jobInFilter in filterJob) {
                            val strFilter = jobInFilter.Name;
                            val jobNameStr = job.Name;

                            if (strFilter.equals(jobNameStr)) {
                                contains = true;
                            }
                        }

                        if (!contains) {

                            filterJob.add(job)
                            filterJobString.add(
                                job.Name
                            )
                            selectedCategory = job.Name

                        }


                    } else {

                        filterJob.add(job)
                        filterJobString.add(
                            job.Name
                        )
                        selectedCategory = job.Name
                    }
                }

            }
        }


        if (filterJob != null && filterJob.isNotEmpty() && filterJob.size > 0) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                filterJobString
            )
            prodCatSpinner.adapter = adapter
            binding.categoryTxtText.setAdapter(adapter)
           // binding.categoryTxtText.setText(adapter.getItem(0),false)
            selectedCategory=adapter.getItem(0).toString()
            binding.categoryTxtText.setOnItemClickListener { parent, _, position, _ ->
                homeViewModel.prodCatSpinText(filterJob[position].Name)
                selectedCategory = filterJob[position].Name
                Log.d("testing", "prodCatSpin   product5: "+prodArry)
                productSpin(
                    prodArry,
                    insurerName,
                    selectedZone,
                    filterJob[position].Name)
            }
//            binding.categoryTxtText.onItemSelectedListener =
//                object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        parent: AdapterView<*>,
//                        view: View?,
//                        position: Int,
//                        id: Long
//                    ) {
//                        homeViewModel.prodCatSpinText(filterJob[position].ProdCategory)
//                        selectedCategory = filterJob[position].ProdCategory
//                        productSpin(
//                            prodArry,
//                            insurerName,
//                            selectedZone,
//                            filterJob[position].ProdCategory
//                        )
//                        adapter.notifyDataSetChanged()
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>) {
//                        // write code to perform some action
//                    }
//                }
        } else {
            binding.categoryTxtText.setAdapter(null)
            binding.productTxtText.setAdapter(null)
            binding.categoryTxtText.setText("")
            binding.productTxtText.setText("")

        }
    }

    private fun productSpin(
        jobArry: ArrayList<Job>,
        insurerName: String? = "",
        selectedZone: String? = "",
        selectedCategory: String? = ""
    ) {
        homeViewModel.prodSpinText("")
        var filterJob: ArrayList<Job> = ArrayList()
        var productArry: ArrayList<Job> = ArrayList()

        var filterJobString: ArrayList<String> = ArrayList()
        if (insurerName!!.isNotEmpty() && selectedZone!!.isNotEmpty() && selectedCategory!!.isNotEmpty()) {
            for (job in jobArry) {
                Log.d("Productttt", "Insurer: ${insurerName}   ${(job.Insurer.split("(").firstOrNull()?.trim())}")
                Log.d("Productttt", "zone: ${selectedZone}  ${(job.Zone.split("(").firstOrNull()?.trim())}")
                Log.d("Productttt", "category: ${selectedCategory}  ${(job.Category.split("(").firstOrNull()?.trim())}")
                Log.d("Productttt", "gender: ${homeViewModel.getGenderSpinText()}  ${(job.Gender.split("(").firstOrNull()?.trim())}")
               var aa=insurerName.equals(job.Insurer.split("(").firstOrNull()?.trim(), true)
                var bb=insurerName.equals(job.Insurer.split("(").firstOrNull()?.trim(), true) != null
            //    var cc=(job.Zone.split("(").firstOrNull()?.trim()).equals(selectedZone)
                var dd=selectedCategory.equals(job.Category.split("(").firstOrNull()?.trim(), true)
                var ee=selectedCategory.equals(job.Category.split("(").firstOrNull()?.trim(), true) != null
                var ff= homeViewModel.getGenderSpinText().equals(job.Gender.split("(").firstOrNull()?.trim())
                Log.d("Productttt", aa.toString()+bb.toString()  +dd.toString() +ee.toString() +ff.toString())
                if (insurerName.equals(job.Insurer.split("(").firstOrNull()?.trim(), true)
                    && insurerName.equals(job.Insurer.split("(").firstOrNull()?.trim(), true) != null
                    && (job.Zone.split("(").firstOrNull()?.trim()).equals(selectedZone)
                    /*&& selectedZone.equals(job.Zone.substring(0, job.Zone.indexOf("(")), true)
                    && selectedZone.equals(job.Zone.substring(0, job.Zone.indexOf("("))) != null*/
                    && selectedCategory.split("(").firstOrNull()?.trim().equals(job.Category.split("(").firstOrNull()?.trim(), true)
                    && selectedCategory.split("(").firstOrNull()?.trim().equals(job.Category.split("(").firstOrNull()?.trim(), true) != null
                    && homeViewModel.getGenderSpinText().equals(job.Gender.split("(").firstOrNull()?.trim())

                ) {
                    val indexOfJob = jobArry.indexOfFirst {
                        it.Name == job.Name
                    }
                    Log.d("testttttt", "productSpin: "+indexOfJob)
                    if (filterJob.size > 0) {

                        var contains = false;
                        for (filterJobIn in filterJob) {
                            val strFilter = filterJobIn.Name;
                            val strJob = job.Name;
                            if (strFilter.equals(strJob)) {
                                contains = true;
                            }
                        }
                        if (!contains) {


                            filterJob.add(job)
                            filterJobString.add(job.Name)

                        }

                    } else {
                        filterJob.add(job)
                        var prodArry1  = prodArry[indexOfJob]
                        productArry.add(prodArry1)
                    filterJobString.add(job.Name)
                    }
                }

            }
        }
        /*val selectedView: View = productSpinner.getSelectedView()
       if (selectedView != null && selectedView is TextView) {
           val selectedTextView: TextView = selectedView
           if (!valid) {
               val errorString: String = selectedTextView.getResources().getString(R.string.coverage)
               selectedTextView.setError(errorString)
           } else {
               selectedTextView.setError(null)
           }
       }*/

        if (filterJob != null && filterJob.isNotEmpty() && filterJob.size > 0) {
            val adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    filterJobString
                )
            productSpinner.adapter = adapter
            binding.productTxtText.setAdapter(adapter)
           // binding.productTxtText.setText(adapter.getItem(0),false)
           // homeViewModel.prodSpinText(filterJob[0].Name)
            binding.productTxtText.setOnItemClickListener { parent, view, position, id ->
                homeViewModel.prodSpinText(filterJob[position].Name)
                Log.d("product", "productSpin: "+{filterJob[position].MaximumAge.toString()})
                Log.d("product", "productSpin: "+{filterJob[position].MinimumAge})
                Log.d("product", "productSpin: "+productArry[position].MinimumAge)
                Log.d("product", "productSpin: "+productArry[position].MaximumAge)
                Log.d("product", "productSpin: "+productArry.size)
               homeViewModel.maxAge1= productArry[position].MaximumAge.toString()
               homeViewModel.maxSumInsured1= productArry[position].MaximumSumAssured

                homeViewModel.minAge1=  filterJob[position].MinimumAge
                minAge=homeViewModel.minAge1
                maxAge=homeViewModel.maxAge1
                maxSumInsured=homeViewModel.maxSumInsured1



                adapter.notifyDataSetChanged()
            }
//            binding.productTxtText.onItemSelectedListener =
//                object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        parent: AdapterView<*>,
//                        view: View?, position: Int, id: Long
//                    ) {
//                        homeViewModel.prodSpinText(filterJob[position].Name)
//                        adapter.notifyDataSetChanged()
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>) {
//                        // write code to perform some action
//                    }
//                }
        } else {
            binding.productTxtText.setAdapter(null)
            binding.productTxtText.setText("")
        }
    }

    private fun genderSpin() {
        var nameArry = listOf<String>("Male", "Female")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nameArry)
        genderSpinner.adapter = adapter
        binding.genderTxtText.setAdapter(adapter)
        //binding.genderTxtText.setText(adapter.getItem(0).toString(), false)
      //  homeViewModel.setGenderSpinText(adapter.getItem(0).toString())

        binding.genderTxtText.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.adapter.getItem(position).toString()
            homeViewModel.setGenderSpinText(selectedItem)
            Log.d("testing", "genderSpin: product6 "+prodArry.toString())
            productSpin(prodArry, selectedInsurerName, selectedZone, selectedCategory)
            binding.productTxtText.setText("")
        }
    }

    private fun fundingSpin() {
        var nameArry1 = listOf<String>("Non Funded")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nameArry1)

        fundingSpinner.adapter = adapter
      //  binding.fundingTxtText.setAdapter(adapter)
        binding.fundingTxtText.setText(adapter.getItem(0).toString(),false)
        homeViewModel.setFundingSpinText(nameArry1[0].toString())
        binding.fundingTxtText.setOnItemClickListener { parent, view, position, id ->
            homeViewModel.setFundingSpinText(nameArry1[position])

        }
//        binding.fundingTxtText.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?, position: Int, id: Long
//            ) {
//                homeViewModel.setFundingSpinText(nameArry1[position])
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // write code to perform some action
//            }
//        }
    }

    private fun tenureAutoText() {

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, tenureArry
        )
        binding.tenureTxtText.setAdapter(adapter)
        //homeViewModel.tenureSpinText(binding.tenureTxtText.text.toString())
        //this is for adapter spinner suggestion click
        binding.tenureTxtText.setOnItemClickListener { parent, view, position, id ->
            homeViewModel.tenureSpinText(binding.tenureTxtText.text.toString())
        }

        // this is for text entered
        binding.tenureTxtText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.tenureTxtText.isPerformingCompletion) {
                    // An item has been selected from the list. Ignore.
                    return
                }
                homeViewModel.tenureSpinText(s.toString())
            }
        })


    }

    private fun tenureSpin(
        adapter: ArrayAdapter<String>,
        jobArry: ArrayList<Job>
    ) {

        tenureSpinner.adapter = adapter

        tenureSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                homeViewModel.tenureSpinText(jobArry.get(position).Tenure)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun productTypeAutoText(   adapter: ArrayAdapter<String>,
                                       jobArry: ArrayList<String>) {


        binding.productTypeTxtText.setAdapter(adapter)
        //homeViewModel.tenureSpinText(binding.tenureTxtText.text.toString())
        //this is for adapter spinner suggestion click
        binding.productTypeTxtText.setOnItemClickListener { parent, view, position, id ->
            homeViewModel.productTypeText(binding.productTypeTxtText.text.toString())
        }

        // this is for text entered
        binding.productTypeTxtText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.productTypeTxtText.isPerformingCompletion) {
                    // An item has been selected from the list. Ignore.
                    return
                }
                homeViewModel.productTypeText(s.toString())
            }
        })


    }

    private fun callPasswordAlertNotification(expiryDays: Int) {
        val intent = Intent(requireContext(), MessageService::class.java)
        intent.putExtra(MessageService.QUOTE_NUMBER, "password")
        intent.putExtra(
            MessageService.MESSAGE_EXTRA,
            "$expiryDays days for password to expire. Please change password"
        )
        requireContext().startService(intent)
    }


    fun userStatusChanged() {

        if (offlineBox.all.size > 0) {
            offlineDBCount = offlineBox.all.size.toLong()
            var postList: List<OfflineJson> = offlineBox.query().equal(
                OfflineJson_.ids,
                offlineDBCount
            ).build().find()

            if (postList.isNotEmpty()) {
                binding.offlineProgress.visibility = View.VISIBLE
                (requireContext() as HomeActivity1).window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )

                var jsonData = postList[0].jsonData
                Log.d("jsonData", "offlineListChecker:2 ${jsonData.toString()}")

                homeViewModel.postQuoteAPI(
                    jsonData.toString(),
                    SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(), "3"
                )

            } else {
                if (userstatus == "Inactive" || userstatus == "In Active") {
                    alertHomeDialog(
                        requireContext(),
                        "Alert!",
                        "Your profile has been made inactive, please contact Admin Support",
                        false,
                        "okay"
                    )
                } else if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                    alertuserZonelogoutDialog(
                        requireContext(),
                        "Alert!",
                        "Database has undergone change. Please logout",
                        false,
                        "Okay"
                    )
                }
            }

        } else {
            if (userstatus == "Inactive" || userstatus == "In Active") {
                alertHomeDialog(
                    requireContext(),
                    "Alert!",
                    "Your profile has been made inactive, please contact Admin Support",
                    false,
                    "okay"
                )
            } else if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                alertuserZonelogoutDialog(
                    requireContext(),
                    "Alert!",
                    "Database has undergone change. Please logout",
                    false,
                    "Okay"
                )
            }
        }
    }

    //this is for offline sync db
    private fun getofflineSyncStatus(postList: Event<Any, String>) {
        if (postList != null) {
            Log.d("jsonData1", "getofflineSyncStatus:${postList.getType()} ")

            if (postList.getType() == "Offline_Check") { //check offline here
                if (offlineBox.all.size > 0) {
                    offlineListChecker()
                    Log.d("JsonData", "getofflineSyncStatus3:${postList.getType()} ")
                }
            } else if (postList.getType() == "Offline") { //loop for next occurance in offline list
                binding.offlineProgress.visibility = View.GONE
                (requireContext() as HomeActivity1).window
                    .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (offlineBox.all.size > 0) {
                    //remove the first obj as its synced with server
                    offlineDBCount = offlineBox.all.size.toLong()
                    var postList: List<OfflineJson> =
                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build()
                            .find()

                    if (postList.isNotEmpty()) {
                        offlineBox.remove(postList)

                        if (userstatus == "Inactive" || userstatus == "In Active") {
                            alertHomeDialog(
                                requireContext(),
                                "Alert!",
                                "Your profile has been made inactive, please contact Admin Support",
                                false,
                                "okay"
                            )
                        } else if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                            alertuserZonelogoutDialog(
                                requireContext(),
                                "Alert!",
                                "Database has undergone change. Please logout",
                                false,
                                "Okay"
                            )
                        }

                    }
                }
                offlineListChecker()
                Log.d("JsonData", "getofflineSyncStatus1:$postList ")

            }
        }
    }
}
