package com.ongo.aonsecure.activity.ui.quoteview

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ongo.aonsecure.BuildConfig
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.HomeActivity1
import com.ongo.aonsecure.activity.LoginActivity
import com.ongo.aonsecure.activity.MainActivity
import com.ongo.aonsecure.activity.ui.quote_list.QuoteListFragmnet
import com.ongo.aonsecure.activity.ui.quote_list.offlineDBCount
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.FragmentQuoteBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.injection.ViewModelFactory
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.pdf_gen.TemplatePDF
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
//import kotlinx.android.synthetic.main.activity_splash_screen.*
//import kotlinx.android.synthetic.main.fragment_quote_list_fragmnet.*
import org.json.JSONArray
import org.json.JSONObject
import pdf.app.library.utils.Constants
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.roundToLong

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuoteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@RequiresApi(Build.VERSION_CODES.O)
class QuoteFragment : Fragment() {

    var taxPercent: Long = 0
    var total: Long = 0
    var netPremium: Long = 0

    var context = this
    var connectivity: ConnectivityManager? = null
    var info: NetworkInfo? = null

    var loginEmailId = ""
    var userstatus: String? = ""
    var userZonestatus: String? = ""

    // TODO: Rename and change types of parameters
    private var premQuote: PremQuote? = null

    // private var taxvalue: Tax? = null
    private lateinit var viewModel: QuoteViewModel
    private lateinit var binding: FragmentQuoteBinding
    lateinit var templatePDF: TemplatePDF
    var showSharePop: Boolean = false

    var relativeLayout: RelativeLayout? = null

    var quoteBox: Box<Post> = ObjectBox.boxStore.boxFor(Post::class.java)

    var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)


    private var fragmentCallback: FragmentCallback? = null

    fun setFragmentCallback(callback: FragmentCallback?) {
        this.fragmentCallback = callback
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            premQuote = it.getParcelable(ARG_PARAM1)
            Log.e("param1 is ", ">>>>>>>>>>$premQuote")
        }
        if(!checkStoragePermissions()){
        requestPermission1()}
        if (!isNotificationEnabled(requireContext())){
            showNotificationPermissionDialog(requireContext())
          //  requestNotificationPermission(requireContext())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: PremQuote) =
            QuoteFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quote, container, false)
        viewModel = ViewModelProvider(this, ViewModelFactory(this,requireActivity().application))[QuoteViewModel::class.java]
        viewModel.getStatus().observe(viewLifecycleOwner, Observer {
            quoteUploadSuccess(it)
        })
        SharedPref.initialize(requireContext())


        initViews()
        clickFunctions()

        /* connectivity = requireContext().getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager

         if (connectivity != null) {
             info = connectivity!!.activeNetworkInfo
             if (info != null) {
                 viewModel.getUserStatus()
             }
         }*/

        viewModel.messageVal.observe(viewLifecycleOwner, Observer {
            showProgress(it)
        })
        viewModel.message.observe(viewLifecycleOwner, Observer {
            getStatuses(it)
        })
        /* viewModel.taxvalue.observe(viewLifecycleOwner, Observer {
             setTaxValues(it)
         })*/

        return binding.root
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
            viewModel.logoutdeleteDBAndGetServerData()

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

    fun showProgress(it: Event<Any, String>?) {
        if (it?.peekContent() == "show")
            binding.quoteProgressbar.visibility = View.VISIBLE
        else
            binding.quoteProgressbar.visibility = View.GONE
    }


    private fun getStatuses(it: Event<Any, String>?) {
        if (it != null) {
            if (it.getType() == "userstatus") {
                var statusdto: StatusDto? = null
                statusdto = it.peekContent() as StatusDto
                userstatus = statusdto.status
                if (userstatus == "In Active") {
                    alertDialog(
                        requireContext(),
                        "Alert!",
                        "Your profile has been made inactive, please contact Admin Support",
                        false,
                        "Okay",
                        ""
                    )
                }
            } else if (it.getType() == "userzone") {
                var userZone: ObjResponse? = null
                userZone = it.peekContent() as ObjResponse
                userZonestatus = userZone.Zone.split("(").first()
                if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                    alertuserZonelogoutDialog(
                        requireContext(),
                        "Alert!",
                        "Database has undergone change. Please logout",
                        false,
                        "Okay"
                    )
                }
            }
            else if (it.getType() == "calculate") {
                var calculate: ObjResponse? = null
                calculate = it.peekContent() as ObjResponse
                var cal = calculate.isServerSync


                    binding.lay.visibility = View.VISIBLE
                    binding.lay0.visibility = View.VISIBLE
                    binding.downloadButton.visibility = View.VISIBLE
                    binding.shareButton.visibility = View.VISIBLE

                    binding.saveButton.visibility = View.GONE
                    binding.editButton.visibility = View.GONE

                    addValueToQuoteList()
                    if ((requireContext() as HomeActivity1).isInternetConnected) {
                        uploadQuoteDetailsAPI()
                    } else {
                        saveToOffline()
                    }

            }
            /*else {
                binding.lay.visibility = View.VISIBLE
                binding.lay0.visibility = View.VISIBLE
                binding.downloadButton.visibility = View.VISIBLE
                binding.shareButton.visibility = View.VISIBLE

                binding.saveButton.visibility = View.GONE
                binding.editButton.visibility = View.GONE

                addValueToQuoteList()
                if ((requireContext() as MainActivity).isInternetConnected) {
                    uploadQuoteDetailsAPI()
                } else {
                    saveToOffline()
                }
            }*/
        }
    }


    private fun quoteUploadSuccess(it: Status?) {
        when (it) {
            QUOTE_SUCCESS -> {
                (requireContext() as HomeActivity1).showToast("Quote has been saved successfully")
                binding.offlineProgress.visibility = View.
                        GONE

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
        builder.setIcon(R.drawable.ic_vec_upload_success)
        //performing positive action
        builder.setPositiveButton(positive) { dialogInterface, which ->
            retrivePdf(premQuote?.QuoteNumber)

            activity?.onBackPressed()
            fragmentCallback?.onDataSent("callQuotePage")



        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }

    private fun initViews() {
        var insurer = ""
        insurer = if (premQuote?.insurer!!.contains("(")) {
            premQuote?.insurer?.substring(
                0,
                premQuote?.insurer!!.indexOf("(")
            ).toString()
        } else {
            premQuote?.insurer.toString()
        }
        var category = ""
        category = if (premQuote?.prodCat!!.contains("(")) {
            premQuote?.prodCat?.substring(0, premQuote?.prodCat!!.indexOf("(")).toString()
        } else {
            premQuote?.prodCat.toString()
        }

        binding.insurerText.text = insurer
        binding.quoteText.text = premQuote?.QuoteNumber ?: ""
        binding.generatedText.text = premQuote?.QuoteGeneratedOn ?: ""
        binding.validText.text = premQuote?.validUntil ?: ""
        binding.rmidText.text = premQuote?.RMID ?: ""
        binding.calcVersionText.text = premQuote?.CalculatorVersion ?: ""
        binding.deviceIdText.text = premQuote?.DeviceId ?: ""
        binding.useridText.text = premQuote?.UserId ?: ""
        binding.userNameText.text = premQuote?.UserName ?: ""
        // binding.statusText.text = premQuote?.status ?: ""
        binding.categoryText.text = category
        binding.zoneText.text = premQuote?.zone ?: ""
        binding.fundText.text = premQuote?.fundType ?: ""
        binding.prodcutText.text = premQuote?.product ?: ""
        binding.loanText.text = premQuote?.Loantype ?: ""
        binding.genderText.text = premQuote?.gender ?: ""
        binding.dobText.text = premQuote?.dob ?: ""
        binding.ageText.text = premQuote?.age?.toString() + " Years"
        binding.coverageText.text = premQuote?.coverage?.toString()
        binding.tenureText.text = premQuote?.tenure ?: ""
        binding.loanAmtText.text = premQuote?.loanAmount?.toString()
        binding.medicalReqText.text = premQuote?.medicalsRequired ?: ""
        binding.benefitTypeText.text = premQuote?.BenefitType ?: ""
        binding.membersCoveredText.text = premQuote?.MembersCovered ?: ""
        binding.paymentTermText.text = premQuote?.PreminumPaymentTerm ?: ""
        binding.remarkText.text = premQuote?.remarks ?: ""
        binding.contractnoText.text = premQuote?.contractno ?: ""

        var message: String = ""
        message = premQuote?.message.toString()
        if (message.isNotEmpty()) {
            messagealertDialog(
                "Alert!",
                message,
                false,
                "Okay",
                ""
            )
        }


        var sumInsured: String = ""
        Log.d("callllll12345678", "initViews: "+premQuote!!.coverage  +"  "+premQuote!!.preminum.toDouble())

        if (premQuote!!.coverage.contains(","))
            sumInsured = premQuote!!.coverage.replace(",", "")

        //val formater = DecimalFormat("#.###")
        //formater.roundingMode = RoundingMode.CEILING

        var loanAmount: Double = (sumInsured.toDouble()) //this is sum Insured
       // var premRate: Double = String.format("%.2f",(premQuote!!.preminum.toDouble())).toDouble()// this is premium rate per 1000
        var premRate: Double =(premQuote!!.preminum.toDouble())// this is premium rate per 1000

        var premium:Double=((loanAmount * premRate) / 1000).toDouble()
        premium =  premium.toDouble()

        /*val number3digits1:Double = Math.round(netPremium * 1000.0) / 1000.0
        val number2digits1:Double = Math.round(number3digits1 * 100.0) / 100.0
        */
        netPremium = Math.round ((premium * 10.0) / 10.0)
       // Log.d("callllll", "initViews: ${Math.round(1.24)}  ${Math.round(1.50)}")

        binding.premiumText.text = "$netPremium  INR"

        taxPercent = SharedPref.read(Constants.PREF_TAX, "")?.toLong()!!
        var tax:Double = ((premium * taxPercent / 100)).toDouble()
        tax=String.format("%.2f", tax.toDouble()).toDouble()
        Log.d("callllll", "initViews: +${Math.round(1.24)}+ \"  \"+${Math.round(1.50)} +\" \"+$" +
                "{Math.round(1.51)}")
        Log.d("Quote generation:::::","loan amount"+loanAmount+"  net remium:"+netPremium+" tax:"+taxPercent)

       /* val number3digits2:Double = Math.round(taxPercent * 1000.0) / 1000.0
        val number2digits2:Double = Math.round(number3digits2 * 100.0) / 100.0
       */ val solution2:Double = (tax * 10.0) / 10.0
          taxPercent = Math.round(solution2+solution2)
        binding.taxText.text =
            "$taxPercent INR"
//        netPremium=Math.round(solution1).toDouble()
//       // total = (ceil(solution1) + ceil(solution2 + solution2)).toDouble()
//        val solution3:Double=Math.round(solution2).toDouble()
        total =netPremium+taxPercent

        Log.d("callllll", "initViews: $total")

        binding.totalText.text = "$total  INR"

        //calculator version
        binding.loginHead.text = "Calculator Version " + premQuote?.CalculatorVersion ?: ""

        binding.lay.visibility = View.GONE
        binding.lay0.visibility = View.GONE

    }

    private fun messagealertDialog(
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
            //  fragmentCallback?.onDataSent("callHomeFrag")
            dialogInterface.dismiss()
           // activity?.onBackPressed();
            activity?.supportFragmentManager?.popBackStack()

        }
        /*builder.setNegativeButton(negative){ dialogInterface, i ->
            dialogInterface.dismiss()
            activity?.onBackPressed();
        }*/
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        val window: Window? = alertDialog.getWindow()
        val lp: WindowManager.LayoutParams? = alertDialog.getWindow()?.getAttributes()
        lp?.dimAmount = 0.0f
        alertDialog.getWindow()?.setAttributes(lp)
        window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        alertDialog.show()
    }


    private fun clickFunctions() {
        binding.editButton.setOnClickListener(View.OnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        })

        binding.saveButton.setOnClickListener(View.OnClickListener {

            connectivity =
                requireContext().getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                info = connectivity!!.activeNetworkInfo
                if (info != null) {
                    if (info!!.state == NetworkInfo.State.CONNECTED) {
                        viewModel.getUserStatus()
                    } else {
                        binding.lay.visibility = View.VISIBLE
                        binding.lay0.visibility = View.VISIBLE
                        binding.downloadButton.visibility = View.VISIBLE
                        binding.shareButton.visibility = View.VISIBLE

                        binding.saveButton.visibility = View.GONE
                        binding.editButton.visibility = View.GONE

                        addValueToQuoteList()
                        if ((requireContext() as HomeActivity1).isInternetConnected) {
                            uploadQuoteDetailsAPI()
                        } else {
                            saveToOffline()
                        }
                    }
                } else {
                    binding.lay.visibility = View.VISIBLE
                    binding.lay0.visibility = View.VISIBLE
                    binding.downloadButton.visibility = View.VISIBLE
                    binding.shareButton.visibility = View.VISIBLE

                    binding.saveButton.visibility = View.GONE
                    binding.editButton.visibility = View.GONE

                    addValueToQuoteList()
                    if ((requireContext() as HomeActivity1).isInternetConnected) {
                        uploadQuoteDetailsAPI()
                    } else {
                        saveToOffline()
                    }
                }
            }

        })

        binding.downloadButton.setOnClickListener(View.OnClickListener {
            showSharePop = false
            downloadPDF()
        })

        binding.shareButton.setOnClickListener {
            showSharePop = true
            downloadPDF()
        }


    }

    private fun downloadPDF() {
        relativeLayout = binding.mainLayout
        requestPermission()

    }
    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true   // NO PERMISSIONS NEEDED for Android 10+
        } else {
            val read = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            val write = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission1() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and above → No storage permission needed
            pdfView()
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6–9 → Ask for legacy storage permissions
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                111
            )
        }
        else {
            // Below Android 6
            pdfView()
        }
    }

    private fun requestPermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Log.d("22233", "requestPermission: "+checkStoragePermissions())
            //  if (!checkStoragePermissions()) {
            // showStoragePermissionDialog(requireContext())
            //  }else{
             pdfView()
            // }
        }else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    111
                )
            } else {
                pdfView()
                //startActivity(new Intent(PdfCreationActivity.this, PdfCreate.class));
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pdfView()
        }
    }


    fun pdfView() {

        /**
         * How to USE FONT....
         */
        // val urName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED)

        templatePDF = TemplatePDF(requireContext())
        templatePDF.openDocument(premQuote?.QuoteNumber)
        // templatePDF.addBorder()

        /*  templatePDF.addMetaData("clientes", "Ventas", "Marines")
          templatePDF.addTitles("Teorema-Sistemas", "Comprovante de venda", "Rafael Reynoud Benetti", "R$ 60,00")*/
        templatePDF.addBorder()
        // templatePDF.addImage(getByteArry())
        templatePDF.addImage(com.ongo.aonsecure.utils.getByteArry(requireContext()))
        var arry = arrayOf("      ", "Quote Number", "Generated On")
        // val clients: ArrayList<Array<String>>? = null
        val clients = ArrayList<Array<String>>()
        templatePDF.createTable(arry, "fSubTitle", clients)

        var arry1 = arrayOf(
            "      ",
            premQuote?.QuoteNumber,
            premQuote?.QuoteGeneratedOn
            /*  premQuote?.validUntil,
              "Active"*/
        )
        val clients1: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry1, "", clients1)
        templatePDF.lineSeperator()

        var arry2 = arrayOf("User ID", "User Name", "FID")
        val clients2: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry2, "fSubTitle", clients2)

        var arry3 = arrayOf(premQuote?.UserId, premQuote?.UserName, premQuote?.RMID)
        val clients3: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry3, "", clients3)


        var arry4 = arrayOf("Insurer Name", "Product Category", "Product")
        val clients4: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry4, "fSubTitle", clients4)
        var insurer = ""
        insurer = if (premQuote?.insurer!!.contains("(")) {
            premQuote?.insurer?.substring(
                0,
                premQuote?.insurer!!.indexOf("(")
            ).toString()
        } else {
            premQuote?.insurer.toString()
        }
        var category = ""
        category = if (premQuote?.prodCat!!.contains("(")) {
            premQuote?.prodCat?.substring(0, premQuote?.prodCat!!.indexOf("(")).toString()
        } else {
            premQuote?.prodCat.toString()
        }
        var arry5 = arrayOf(insurer, category, premQuote?.product)
        val clients5: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry5, "", clients5)

        var arry6 = arrayOf("Funding Type", "Gender", "Date Of Birth")
        val clients6: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry6, "fSubTitle", clients6)

        var arry7 = arrayOf(premQuote?.fundType, premQuote?.gender, premQuote?.dob)
        val clients7: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry7, "", clients7)


        var arry8 = arrayOf("Age", "Loan Amount", "Sum Insured")
        val clients8: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry8, "fSubTitle", clients8)

        var arry9 = arrayOf(
            premQuote?.age + " Years",
            premQuote?.loanAmount + " INR",
            premQuote?.coverage + " INR"
        )
        val clients9: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry9, "", clients9)

        var arry10 = arrayOf("Tenure", "Medicals Required", "Calculator Version")
        val clients10: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry10, "fSubTitle", clients10)

        var arry11 =
            arrayOf(premQuote?.tenure, premQuote?.medicalsRequired, premQuote?.CalculatorVersion)
        val clients11: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry11, "", clients11)

        var arry12 = arrayOf("Premium Payment Term", "Loan Type", "Benefit Type")
        val clients12: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry12, "fSubTitle", clients12)

        var arry13 = arrayOf(
            premQuote?.PreminumPaymentTerm,
            premQuote?.Loantype,
            premQuote?.BenefitType
        )

        val clients13: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry13, "", clients13)

        var arry14 = arrayOf("Zone", "Remarks", "Contract Number")
        val clients14: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry14, "fSubTitle", clients14)

        var remarks: String = if (premQuote?.remarks?.isNotEmpty()!!)
            premQuote?.remarks!!
        else
            " -------- "
        var contractno: String = if (premQuote?.contractno?.isNotEmpty()!!)
            premQuote?.contractno!!
        else
            "---------"

        var arry15 = arrayOf(premQuote?.zone, remarks, contractno)
        val clients15: ArrayList<Array<String>>? = null
        templatePDF.createTable(arry15, "", clients15)


        var arry16 = arrayOf("Net Premium", "Tax", "Total Premium")
        val clients16: ArrayList<Array<String>>? = null
        templatePDF.tableWithBackgroundColor(arry16, "fSubTitle", clients16)

        var arry17 = arrayOf(
            netPremium.toString()+ " INR",
            (taxPercent).toString() + " INR",
            (total).toString() + " INR"
        )

        val clients17: ArrayList<Array<String>>? = null
        templatePDF.tableWithBackgroundColor(arry17, "", clients17)


        templatePDF.addSubTitleParagraph("Terms & Conditions", "")
        templatePDF.addvalueParagraph("* This is just an indicative Premium calculation and should not be considered as an official document/final policy document in any sense")
        templatePDF.addvalueParagraph("* Premium quoted is including GST of 18%")
    //    templatePDF.addvalueParagraph("* Final Policy issuance & Acceptance of Risk is the decision of the Insurance company (subject to all relevant documents & terms) and CNH Industrial Capital (India) Pvt Ltd does not hold any liability/made responsible for any such decisions")
        templatePDF.addvalueParagraph("* Final Policy issuance & Acceptance of Risk is the decision of the Insurance company (subject to all relevant documents & terms) and Aon India Insurance Brokers Private Limited does not hold any liability/made responsible for any such decisions")
        templatePDF.addvalueParagraph(" ")

        templatePDF.addSubTitleParagraph(
            "This is a system generated document and does not require any signature",
            "center"
        )
        templatePDF.closeDocument()
        if (showSharePop) {
           // if (isExternalStorageWritable()) {
                templatePDF.viewPDF(requireActivity())
          //  }
        } else {
            alertDialog(
                "Alert!",
                "Quote downloaded successfully. Check in downloads folder ",
                false,
                "Okay",
                ""
            )

            callNotificationForQuoteGenerated(premQuote?.QuoteNumber)
        }
    }
    private  fun retrivePdf(quoteNo: String?){
        if (quoteNo != null) {
            if (quoteNo.isNotEmpty()) {
                /*var templatePDF = TemplatePDF(this)
                templatePDF.viewQuoteFromNotification(this, quoteNo)*/

                val file = File(
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "$quoteNo.pdf"
                )

                val uri = FileProvider.getUriForFile(
                    requireContext().applicationContext,
                    requireContext().applicationContext.packageName + ".provider",
                    file
                )

                val openFile = Intent()
                openFile.putExtra(Intent.EXTRA_STREAM, uri)
                openFile.type = "application/pdf"
                openFile.data = uri
                openFile.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                openFile.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION)
    /*            openFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                openFile.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)*/
                openFile.action = Intent.ACTION_VIEW
                openFile.action = Intent.ACTION_VIEW
                try {
                    context.startActivity(openFile)
                } catch (e: ActivityNotFoundException) {
                    // Handle exception when no application can handle the intent
                }
                /*val stackBuilder = TaskStackBuilder.create(requireContext())

                stackBuilder.addParentStack(MainActivity::class.java)
                stackBuilder.addNextIntent(openFile)
                 var pendingIntent: PendingIntent? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);
                } else {
                    pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                }*/
            }
        }
    }
    fun isNotificationEnabled(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }
    fun requestNotificationPermission(context: Context) {
        val intent = Intent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
            else -> {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:" + context.packageName)
            }
        }
        context.startActivity(intent)
    }
    fun showStoragePermissionDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Storage Permission")
        alertDialogBuilder.setMessage("Please enable storage permissions to access this feature.")
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            startActivity(intent)
            // Open the app settings screen for storage permissions

        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            // Handle cancel button click if needed
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun showNotificationPermissionDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Notification Permission")
        alertDialogBuilder.setMessage("Please enable notification permissions to receive updates.")
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            // Open the notification settings screen
            requestNotificationPermission(context)
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            // Handle cancel button click if needed
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun callNotificationForQuoteGenerated(quoteNo: String?) {
        val intent = Intent(requireContext(), MessageService::class.java)
        intent.putExtra(MessageService.QUOTE_NUMBER, quoteNo)
        intent.putExtra(
            MessageService.MESSAGE_EXTRA,
            "Quote downloaded successfully.Click to view Pdf"
        )
        requireContext().startService(intent)
    }

    private fun saveToOffline() {
        Log.d("22222", "saveToOffline:+${offlineBox.count()} ")
        var offlineSize: Int = offlineBox.all.size
        var jsonData = jsonfieldsData()
        var offlineJson = OfflineJson(0, offlineSize, premQuote?.QuoteNumber, jsonData.toString())
        offlineBox.put(offlineJson)

        (requireContext() as HomeActivity1).showToast("No internet available. Saved to offline")
        Log.d("22222", "saveToOffline:+${offlineBox.count()}   ${offlineJson.jsonData}")

    }
    fun autoSyncCallForOfflineCHeck(isInternetAvailable: Boolean) {
        Log.d("5555555", "onNetworkConnectionChanged3: $isInternetAvailable")

        if (isInternetAvailable)
            offlineListChecker()
        else
            Log.e("called for offline", "yes")
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

//                        (requireContext() as HomeActivity1).window.setFlags(
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//                        );
                        // Iterate over each item in postList
                        /*  for (offlineItem in postList) {
                              var jsonData = offlineItem.jsonData
                              viewModel.postQuoteAPI(
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
                            viewModel.postQuoteAPI1(
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


    /***
     * this is to add oflline data to quote list page
     */
    private fun addValueToQuoteList() {
        var quoteSize: Int = quoteBox.all.size
        if (quoteSize > 0) {
            var postList: List<Post> = quoteBox.query().equal(Post_.QuoteNo, premQuote?.QuoteNumber,
                QueryBuilder.StringOrder.CASE_INSENSITIVE)
                .build().find()
//            val quoteNoString: String? = premQuote?.QuoteNumber
//            val quoteNoLong: Long = quoteNoString?.toLongOrNull() ?: 0L
//
//            val postList: List<Post> = quoteBox.query().equal(Post_.QuoteNo, quoteNoLong)
//                .build().find()
            if (postList.isNotEmpty()) {
                quoteBox.remove(postList)
                quoteSize = quoteBox.all.size
            }
        }


/*        var sizeVal: Long = quoteSize.toLong()
        if(!sizeVal.equals(0))
            sizeVal++ //this is to increment the size of the id which is sl.no kind for object box*/
        Log.d("netPremium",netPremium.toString())
        Log.d("tax",taxPercent.toString())
        var post = Post(
            0,
            quoteSize,
            premQuote?.QuoteGeneratedOn,
            premQuote?.insurer,
            premQuote?.prodCat,
            premQuote?.fundType,
            premQuote?.product,
            premQuote?.medicalsRequired,
            premQuote?.gender,
            SharedPref.read(Constants.PREF_FULL_NAME, ""),
            premQuote?.dob,
            premQuote?.coverage,
            premQuote?.age,
            premQuote?.tenure,
            premQuote?.preminum,
            (taxPercent).toString(),
            total.toString(),
            premQuote?.DeviceId,
            premQuote?.RMID,
            SharedPref.read(Constants.PREF_USERID, ""),
            SharedPref.read(Constants.PREF_FULL_NAME, ""),
            premQuote?.CalculatorVersion,
            premQuote?.validUntil,
            premQuote?.status,
            premQuote?.insurer,
            premQuote?.loanAmount,
            premQuote?.medicalsRequired,
            premQuote?.PreminumPaymentTerm,
            premQuote?.BenefitType,
            premQuote?.MembersCovered,
            netPremium.toString(),
            total.toString(),
            premQuote?.QuoteNumber,
            premQuote?.zone,
            premQuote?.Loantype,
            premQuote?.remarks,
            premQuote?.contractno
        )
        quoteBox.put(post)

    }

    private fun uploadQuoteDetailsAPI() {
        var jsonData = jsonfieldsData()
      ;
        viewModel.postQuoteAPI(
            jsonData,
            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
            "3"
        )
    }

    /* jsonObject.put("Status", premQuote?.status)
     jsonObject.put("ValidUntil", premQuote?.validUntil)*/

    private fun jsonfieldsData(): JSONObject {
        val jsonArray = JSONArray()
        val jsonObject = JSONObject()
        jsonObject.put("Name", SharedPref.read(Constants.PREF_FULL_NAME, ""))
        jsonObject.put("Insurer", premQuote?.insurer)
        jsonObject.put("ProductCategory", premQuote?.prodCat)
        jsonObject.put("Product", premQuote?.product)
        jsonObject.put("FundingType", premQuote?.fundType)
        jsonObject.put("Gender", premQuote?.gender)
        jsonObject.put("DateofBirth", changeDateFormat(premQuote?.dob) )
        jsonObject.put("Age", premQuote?.age + " Years")
        jsonObject.put("Coverage", premQuote?.coverage?.replace(",", ""))
        jsonObject.put("Tenure", premQuote?.tenure)
        jsonObject.put("Premium", premQuote?.preminum)
        jsonObject.put(
            "Tax",
            ((taxPercent)).toString()
        )
        jsonObject.put("Total", total.toLong().toString())
        jsonObject.put("Remarks", premQuote?.remarks)
        jsonObject.put("QuoteNo", premQuote?.QuoteNumber)
        jsonObject.put("QuoteGeneratedOn", premQuote?.QuoteGeneratedOn)
        jsonObject.put("DeviceID", premQuote?.DeviceId)
        jsonObject.put("RMID", premQuote?.RMID)
        jsonObject.put("UserID", SharedPref.read(Constants.PREF_USERID, ""))
        jsonObject.put("UserName", SharedPref.read(Constants.PREF_FULL_NAME, ""))
        jsonObject.put("CalculatorVersion", premQuote?.CalculatorVersion)
        jsonObject.put("InsurerName", premQuote?.insurer)
        jsonObject.put("LoanAmount", premQuote?.loanAmount?.replace(",", ""))
        jsonObject.put("MedicalsRequired", premQuote?.medicalsRequired)
        jsonObject.put("PremiumPaymentTerm", premQuote?.PreminumPaymentTerm)
        jsonObject.put("BenefitType", premQuote?.BenefitType)
        jsonObject.put("MembersCovered", premQuote?.MembersCovered)
        jsonObject.put("Zone", premQuote?.zone)
        jsonObject.put("LoanType", premQuote?.Loantype)
        jsonObject.put("NetPremium", netPremium.toLong().toString())
        jsonObject.put("TotalPremium", total.toLong().toString())
        jsonObject.put("Contract Number", premQuote?.contractno)
        jsonObject.put("Email",SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString())
        jsonArray.put(jsonObject)

        val jsonData = JSONObject()
        jsonData.put("list", jsonArray)
        Log.e(
            "username",
            ">>>>>>>>${SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "")}"
        )
        Log.e("userid", ">>>>>>>>${jsonData}")

        return jsonData
    }

//    private fun isExternalStorageWritable(): Boolean {
//        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
//    }

    private fun getByteArry(): ByteArray {
        val bm = BitmapFactory.decodeResource(resources, R.drawable.trac_img)
        val stream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        return byteArray
    }

/* private fun callgenePdf() {
     relativeLayout?.let {
         DesignPdf.with(requireContext()).addView(it)
             .setFilePath(getAppPath(requireContext()), premQuote?.QuoteNumber)
             .setHeightnWidthMultiplier(1, 1)
             .create(object : Callback {
                 override fun onSuccess(file: File) {
                     //showToast("path: " + file.absolutePath)
                     Log.d("path", file.absolutePath)

                     val jsonArray = JSONArray()
                     val jsonObject = JSONObject()
                     jsonObject.put("Name", SharedPref.read(Constants.PREF_FULL_NAME, ""))
                     jsonObject.put("Insurer", premQuote?.insurer)
                     jsonObject.put("ProductCategory", premQuote?.prodCat)
                     jsonObject.put("Product", premQuote?.product)
                     jsonObject.put("FundingType", premQuote?.fundType)
                     jsonObject.put("Gender", premQuote?.gender)
                     jsonObject.put("DateofBirth", premQuote?.dob)
                     jsonObject.put("Age", premQuote?.age)
                     jsonObject.put("Coverage", premQuote?.coverage)
                     jsonObject.put("Tenure", premQuote?.tenure)
                     jsonObject.put("Premium", premQuote?.preminum)
                     jsonObject.put("Tax", taxPercent.toString())
                     jsonObject.put("Total", total.toString())
                     jsonObject.put("Remarks", premQuote?.remarks)
                     jsonObject.put("QuoteNo", premQuote?.QuoteNumber)
                     jsonObject.put("QuoteGeneratedOn", premQuote?.QuoteGeneratedOn)
                     jsonObject.put("DeviceID", premQuote?.DeviceId)
                     jsonObject.put("RMID", premQuote?.RMID)
                     jsonObject.put("UserID", SharedPref.read(Constants.PREF_USERID, ""))
                     jsonObject.put("UserName", SharedPref.read(Constants.PREF_FULL_NAME, ""))
                     jsonObject.put("CalculatorVersion", premQuote?.CalculatorVersion)
                     jsonObject.put("ValidUntil", premQuote?.validUntil)
                     jsonObject.put("Status", premQuote?.status)
                     jsonObject.put("InsurerName", premQuote?.insurer)
                     jsonObject.put("LoanAmount", premQuote?.loanAmount)
                     jsonObject.put("MedicalsRequired", premQuote?.medicalsRequired)
                     jsonObject.put("PremiumPaymentTerm", premQuote?.PreminumPaymentTerm)
                     jsonObject.put("BenefitType", premQuote?.BenefitType)
                     jsonObject.put("MembersCovered", premQuote?.MembersCovered)
                     jsonObject.put("LoanType", premQuote?.Loantype)
                     jsonObject.put("NetPremium", premQuote?.preminum)
                     jsonObject.put("TotalPremium", total.toString())
                     jsonArray.put(jsonObject)

                     val jsonData =
                         JSONObject()
                     jsonData.put("list", jsonArray)
                     Log.e(
                         "username",
                         ">>>>>>>>${SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "")}"
                     )
                     Log.e("userid", ">>>>>>>>${jsonData.toString()}")
                     viewModel.postQuoteAPI(
                         jsonData,
                         SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                         "3"
                     )
                 }

                 override fun onError(e: Exception) {
                     e.printStackTrace()
                 }
             })
     }
 }*/

    fun getAppPath(context: Context?): String? {
        val dir = File(
            Environment.getExternalStorageDirectory()
                .toString() + File.separator
                    + getString(R.string.app_name)
                    + File.separator
        )
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir.path + File.separator
    }


}

