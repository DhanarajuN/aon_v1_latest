package com.ongo.aonsecure.activity.ui.filter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ongo.aonsecure.R
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.ActivityFilterBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.id.Status.*
import com.ongo.aonsecure.injection.ViewModelFactoryActivity
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.utils.Communicator
import com.ongo.aonsecure.utils.SharedPref
import com.ongo.aonsecure.utils.tenureArry
import io.objectbox.Box
import pdf.app.library.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class FilterActivity : BaseCompatActivity() {

    companion object {
        const val ZONE = "ZONE"
        const val INSURER = "INSURER"
        const val CATEGORY = "CATEGORY"
        const val PRODUCT = "PRODUCT"
        const val FUNDINGTYPE = "FUNDINGTYPE"
        const val GENDER = "GENDER"
        const val TENURE = "Tenure"
        const val CONTRACTNO = "ContractNumber"
    }

    private var model: Communicator? = null
    private lateinit var viewModel: FilterActivityViewModel
    private lateinit var binding: ActivityFilterBinding
    private lateinit var zoneSpinner: AppCompatSpinner
    private lateinit var insurerSpinner: AppCompatSpinner
    private lateinit var tenureSpinner: AppCompatSpinner
    private lateinit var prodCatSpinner: AppCompatSpinner
    private lateinit var loanTypeSpinner: AppCompatSpinner
    private lateinit var productSpinner: AppCompatSpinner
    private lateinit var fundingSpinner: AppCompatSpinner
    private lateinit var genderSpinner: AppCompatSpinner

    var prodCatArry: ArrayList<Job> = ArrayList()
    var prodArry: ArrayList<Job> = ArrayList()

    var selectedInsurerName: String = ""
    private var selectedZone: String = ""
    private var selectedCategory: String = ""

    var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    var offlineDBCount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter)
        //  viewModel = ViewModelProvider(this)[FilterActivityViewModel::class.java]
        viewModel = ViewModelProvider(this, ViewModelFactoryActivity(this,application))[FilterActivityViewModel::class.java]
        binding.viewModel = viewModel
        zoneSpinner = binding.zoneSpinner
        insurerSpinner = binding.insureSpinner
        prodCatSpinner = binding.categorySpinner
        productSpinner = binding.productSpinner
        tenureSpinner = binding.tenureSpinner
        loanTypeSpinner = binding.loanTypeSpinner
        fundingSpinner = binding.fundingSpinner
        genderSpinner = binding.genderSpinner

        if (isInternetConnected) {
            viewModel.loadInsurer()
        }else{
            viewModel.loadInsurer()
        }

        var button: Button = findViewById(R.id.submit_button)

        button.setOnClickListener {
            val resultIntent = Intent()

            resultIntent.putExtra(ZONE, viewModel.homeFragInfo.zone)

            /*if (viewModel.homeFragInfo.insurer == "--Select--()")
                resultIntent.putExtra(INSURER, "")
            else*/
                resultIntent.putExtra(INSURER, viewModel.homeFragInfo.insurer)

          /*  if (viewModel.homeFragInfo.category == "--Select--()")
                resultIntent.putExtra(CATEGORY, "")
            else*/
                resultIntent.putExtra(CATEGORY, viewModel.homeFragInfo.category)

            /*if (viewModel.homeFragInfo.product == "--Select--()")
                resultIntent.putExtra(PRODUCT, "")
            else*/
                resultIntent.putExtra(PRODUCT, viewModel.homeFragInfo.product)

           /* if (viewModel.homeFragInfo.funding == "--Select--")
                resultIntent.putExtra(FUNDINGTYPE, "")
            else*/
                resultIntent.putExtra(FUNDINGTYPE, viewModel.homeFragInfo.funding)

           /* if (viewModel.homeFragInfo.gender == "--Select--")
                resultIntent.putExtra(GENDER, "")
            else*/
                resultIntent.putExtra(GENDER, viewModel.homeFragInfo.gender)


          /*  if (binding.tenureTxtText.text.isEmpty())
                resultIntent.putExtra(TENURE, "")
            else*/
                resultIntent.putExtra(TENURE, binding.tenureTxtText.text.toString())

           /* if (binding.contractnoEt.text!!.isEmpty())
                resultIntent.putExtra(CONTRACTNO, "")
            else*/
                resultIntent.putExtra(CONTRACTNO, binding.contractnoEt.text.toString())


            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.backPress.setOnClickListener {
            finish()
        }

        viewModel.message.observe(this, Observer {
            getZoneVals(it)
        })
/*        viewModel.premiumcal.observe(this, Observer {
            callQuoteFrag(it)
        })*/

        /* viewModel.getStatus().observe(this, Observer { handleStatus(it) })*/

        viewModel.getAlertMsg().observe(this, Observer { alertHandle(it) })

         viewModel.messageVal.observe(this, Observer {
             showProgress(it)
         })
        zoneSpin()
        fundingSpin()
        genderSpin()
        tenureAutoText()

        binding.scroll.setOnTouchListener { view, ev ->
            hideSoftKeyboard()
            false
        }
    }

    fun showProgress(it: Event<Any, String>?) {
        if (it?.peekContent() == "show")
            binding.progressBar3.visibility = View.VISIBLE
        else
            binding.progressBar3.visibility = View.GONE

    }


    private fun alertHandle(it: Status?) {
        when (it) {
            Status.ZONE -> {
                alertDialog("Alert!", "Zone type not found", false, "Okay", "")
            }
            Status.INSURER -> {
                alertDialog("Alert!", "Insurer type not found", false, "Okay", "")
            }
            PRODCAT -> {
                alertDialog("Alert!", "Category type not found", false, "Okay", "")
            }
            Status.PRODUCT -> {
                alertDialog("Alert!", "Product type not found", false, "Okay", "")
            }
            FUNDTYPE -> {
                alertDialog("Alert!", "Fund type not found", false, "Okay", "")
            }
            DATERANGE -> {
                alertDialog(
                    "Alert!",
                    "Date range for calculator version not found",
                    false,
                    "Okay",
                    ""
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


    private fun getZoneVals(postList: Event<Any, String>?) {
        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            var jobArry = ArrayList<Job>()
            if (postList.peekContent() is ArrayList<*>) { // this is from DB
                if (postList.getType() == "Insurer") {
                    var prodandcatarray = postList.peekContent() as ArrayList<Insurer>
                    for (note in prodandcatarray) {
                        jobArry.addAll(note.jobs)
                    }
                } else if (postList.getType() == "ProdCat") {
                    var zoneArry = postList.peekContent() as ArrayList<ProdCat>
                    for (note in zoneArry) {
                        jobArry.addAll(note.jobs)
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
                }
            }

            /*else { //this is from server api hit
                if (postList.getType() == "Insurer") {
                    var prodandcat = postList.peekContent() as Insurer
                    jobArry.addAll(prodandcat.jobs)
                } else if (postList.getType() == "ProdCat") {
                    var zoneArry = postList.peekContent() as ProdCat
                    jobArry.addAll(zoneArry.jobs)
                } else if (postList.getType() == "Product") {
                    var zoneArry = postList.peekContent() as Product
                    jobArry.addAll(zoneArry.jobs)
                } else if (postList.getType() == "Tenure") {
                    var insurerArry = postList.peekContent() as Tenure
                    jobArry.addAll(insurerArry.jobs)
                } else if (postList.getType() == "LoanType") {
                    var insurerArry = postList.peekContent() as LoanType
                    jobArry.addAll(insurerArry.jobs)
                }
            }*/
         //   setSelectTextToSpinner(jobArry)
            spinnerAdapter(jobArry, postList.getType())
        };


    }

    private fun setSelectTextToSpinner(jobArry: ArrayList<Job>) {
        if (jobArry.size > 0) {
            var createList = jobArry.get(0).CreatedSubJobs
            //var jobComments=jobArry.get(0).jobComments

            var job: Job? =
                Job(
                    0,
                    "--Select--()",
                    "",
                    "",
                    "--Select--()",
                    "--Select--()",
                    "",
                    "",
                    "--Select--()",
                    "--Select--()",
                    "--Select--()",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    createList
                )
            if (job != null) {
                jobArry.add(0, job)
            }
        }
    }

    private fun spinnerAdapter(jobArry: ArrayList<Job>, type: String) {
        // val languages = resources.getStringArray(R.array.Languages)
        var nameArry = ArrayList<String>()

        for (value in jobArry) {
            if (type == "Tenure")
                nameArry.addAll(listOf(value.Tenure))
            else if (type == "Insurer") {
                nameArry.addAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
            } else if (type == "ProdCat") {
                nameArry.addAll(listOf(value.ProdCategory.substring(0, value.ProdCategory.indexOf("("))))
            } else if (type == "Product") {
                nameArry.addAll(listOf(value.Name))
            } else
                nameArry.addAll(listOf(value.Name))

            when (type) {
                "Tenure" -> nameArry.addAll(listOf(value.Tenure))
                "Insurer" -> {
                    nameArry.removeIf(Objects::isNull)
                   // nameArry.addAll(listOf(value.Insurer))
                    /*  val hs = HashSet<String>()
                     hs.addAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
                     nameArry.removeAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
                     nameArry.addAll(hs)*/
                    // nameArry.addAll(listOf(value.Insurer.substring(0, value.Insurer.indexOf("("))))
                }
                "ProdCat" -> {
                    nameArry.addAll(listOf(value.ProdCategory.substring(0, value.ProdCategory.indexOf("("))))
                   // nameArry.removeIf(Objects::isNull)
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
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArry)

        when (type) {
            "Insurer" -> {
                insurerSpin(adapter, jobArry)
            }
            "ProdCat" -> {
                prodCatArry.addAll(jobArry)
                prodCatSpin(prodCatArry, selectedInsurerName, selectedZone)
            }
            "Product" -> {
                prodArry.addAll(jobArry)
                productSpin(prodArry, selectedInsurerName, selectedZone, selectedCategory)
            }
            "Tenure" -> {
                tenureSpin(adapter, jobArry)
            }
            "LoanType" -> {
                loanTypeSpin(adapter, jobArry)
            }
        }

    }

    private fun zoneSpin() {
        var zoneFromLoggedInUser: String? = SharedPref.read(Constants.PREF_ZONE, "")

        /*binding.zoneSpinner.setText(zoneFromLoggedInUser)
        viewModel.zoneSpinText(zoneFromLoggedInUser.toString())*/

        var nameArry = listOf<String>(zoneFromLoggedInUser.toString())
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArry)
        zoneSpinner.adapter = adapter
        zoneSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.zoneSpinText(nameArry[position])
                selectedZone = nameArry[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

    }


    private fun genderSpin() {
        var nameArry = listOf<String>( "Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArry)
        genderSpinner.adapter = adapter
        genderSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.setGenderSpinText(nameArry[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun fundingSpin() {
        var nameArry1 = listOf<String>(  "Non Funded")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArry1)
        fundingSpinner.adapter = adapter
        fundingSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.setFundingSpinText(nameArry1[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun prodCatSpin(
        nameArry: ArrayList<Job>,
        insurerName: String? = "",
        selectedZone: String = ""
    ) {
        var filterJob: ArrayList<Job> = ArrayList()
        var filterJobString: ArrayList<String> = ArrayList()

        if (insurerName!!.isNotEmpty() && selectedZone!!.isNotEmpty()) {
            for (job in nameArry) {
                if (insurerName.equals(job.Insurer, true)
                    && insurerName.equals(job.Insurer, true) != null
                    && selectedZone.equals(job.Zone.substring(0, job.Zone.indexOf("(")), true)
                    && selectedZone.equals(
                        job.Zone.substring(0, job.Zone.indexOf("(")),
                        true
                    ) != null
                ) {

                    if (filterJob.size > 0) {

                        var contains = false
                        for (jobInFilter in filterJob) {
                            val strFilter = jobInFilter.ProdCategory;
                            val jobNameStr = job.ProdCategory;

                            if (strFilter.equals(jobNameStr)) {
                                contains = true;
                            }
                        }

                        if (!contains) {

                            filterJob.add(job)
                            filterJobString.add(
                                job.ProdCategory.substring(
                                    0,
                                    job.ProdCategory.indexOf("(")
                                )
                            )
                            selectedCategory = job.ProdCategory

                        }


                    } else {

                        filterJob.add(job)
                        filterJobString.add(
                            job.ProdCategory.substring(
                                0,
                                job.ProdCategory.indexOf("(")
                            )
                        )
                        selectedCategory = job.ProdCategory
                    }
                }

            }
        }

        if (filterJob.size > 0) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, filterJobString)
            prodCatSpinner.adapter = adapter
            prodCatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.prodCatSpinText(filterJob[position].ProdCategory)
                    selectedCategory = filterJob[position].ProdCategory
                    adapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }

    private fun productSpin(
        jobArry: ArrayList<Job>,
        insurerName: String? = "",
        selectedZone: String? = "",
        selectedCategory: String? = "") {
        var filterJob: ArrayList<Job> = ArrayList()
        var filterJobString: ArrayList<String> = ArrayList()
        if (insurerName!!.isNotEmpty() && selectedZone!!.isNotEmpty() && selectedCategory!!.isNotEmpty()) {
            for (job in jobArry) {
                if (insurerName.equals(job.Insurer, true)
                    && insurerName.equals(job.Insurer, true) != null
                    && selectedZone.equals(job.Zone.substring(0, job.Zone.indexOf("(")), true)
                    && selectedZone.equals(job.Zone.substring(0, job.Zone.indexOf("("))) != null
                    && selectedCategory.equals(job.Category, true)
                    && selectedCategory.equals(job.Category, true) != null
                ) {
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

        if (filterJob.size > 0) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, filterJobString)
            productSpinner.adapter = adapter
            productSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    viewModel.prodSpinText(filterJob[position].Name)
                    adapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }


    private fun tenureAutoText() {

        val adapter = ArrayAdapter(mContext,
            android.R.layout.simple_list_item_1, tenureArry
        )
        binding.tenureTxtText.setAdapter(adapter)
        //homeViewModel.tenureSpinText(binding.tenureTxtText.text.toString())
        //this is for adapter spinner suggestion click
        binding.tenureTxtText.setOnItemClickListener { parent, view, position, id ->
            viewModel.tenureSpinText(binding.tenureTxtText.text.toString())
        }

        // this is for text entered
        binding.tenureTxtText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.tenureTxtText.isPerformingCompletion) {
                    // An item has been selected from the list. Ignore.
                    return
                }
                viewModel.tenureSpinText(s.toString())
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
                viewModel.tenureSpinText(jobArry.get(position).Tenure)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun insurerSpin(adapter: ArrayAdapter<String>, nameArry: ArrayList<Job>) {

        insurerSpinner.adapter = adapter
        insurerSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.insureSpinText(nameArry[position].Insurer)
                Log.e("insurer spin selected", ">>>>>>>>>>${nameArry[position].Insurer}")
                selectedInsurerName = nameArry[position].Insurer
                prodCatSpin(prodCatArry, nameArry[position].Insurer, selectedZone)
                productSpin(prodArry, nameArry[position].Insurer, selectedZone, selectedCategory)

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun loanTypeSpin(adapter: ArrayAdapter<String>, jobArry: ArrayList<Job>) {

        loanTypeSpinner.adapter = adapter
        loanTypeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                viewModel.loanTypeSpinText(jobArry[position].Name)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }
}
