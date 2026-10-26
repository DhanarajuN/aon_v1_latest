package com.ongo.aonsecure.activity.ui.quote_list

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.*
import com.ongo.aonsecure.activity.ui.filter.FilterActivity
import com.ongo.aonsecure.adapter.CustomAdapter
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.FragmentQuoteListFragmnetBinding
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.injection.ViewModelFactory
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.utils.*
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import pdf.app.library.utils.Constants


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var viewModel: QuoteListViewModel
private lateinit var binding: FragmentQuoteListFragmnetBinding
var quoteBox: Box<Post> = ObjectBox.boxStore.boxFor(Post::class.java)
private val request_Code: Int = 359
var userstatus: String? = ""
var userZonestatus: String? = ""
var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
var offlineDBCount: Long = 0
var isAlertShown: Boolean = false;
var mContext: Context? = null


//var context = this
var connectivity: ConnectivityManager? = null
var info: NetworkInfo? = null
var loginEmailId = ""

@RequiresApi(Build.VERSION_CODES.O)
class QuoteListFragmnet() : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var userstatus: String? = ""


    companion object {
        @JvmStatic
        fun newInstance() =
            QuoteListFragmnet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        var QuoteNo=0;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_quote_list_fragmnet,
            container,
            false
        )
        binding.usernameTextView.setText(SharedPref.read(Constants.PREF_FULL_NAME, ""))
        binding.profileEditIv.setOnClickListener{
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.emailTextView.setText(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, ""))
        binding.postList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel = ViewModelProvider(this, ViewModelFactory(this,requireActivity().application))[QuoteListViewModel::class.java]
        viewModel = ViewModelProvider(this, ViewModelFactory(this,requireActivity().application))[viewModel::class.java]


        var loginEmailId: String = SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString()
        viewModel.premiumcal.observe(viewLifecycleOwner, Observer {
            callAdapterSet(it)
        })
        if ((requireContext() as MainActivity).checkInternet()) {
            viewModel.getUserStatus(loginEmailId)
        }


        viewModel.message.observe(viewLifecycleOwner, Observer {
            showProgressbar(it)
        })

        viewModel.noInternet.observe(viewLifecycleOwner, Observer {
            noInternetSetAdapter()
        })
        viewModel.message.observe(viewLifecycleOwner, Observer {
            getFirstZoneStatus(it)
        })
        viewModel.zone.observe(viewLifecycleOwner, Observer {
            getZoneStatus(it)
        })
        viewModel.password.observe(viewLifecycleOwner, Observer {
            getPasswordStatus(it)
        })

        viewModel.message.observe(viewLifecycleOwner, Observer {
            getZoneVals(it)
        })

        viewModel.message.observe(viewLifecycleOwner, Observer {
            getofflineSyncStatus(it)
            Log.d("jsonData", "onCreateView: ${it.getType()}")
        })
        mContext = requireContext().applicationContext



        viewModel.messageVal.observe(viewLifecycleOwner, Observer {
            showProgressbar(it)
        })

        //  userstatus = SharedPref.read(Constants.PREF_USER_STATUS, "")


        //todo: change the logic as to function for both online and offline
        /*if ((requireContext() as MainActivity).isInternetConnected)
            viewModel.loadPosts() // this is online
        else*/
        callAdapter() //this is for offline
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
                    viewModel.getFirstUserStatus(loginEmailId)
                } else {
                    callViewModelForAPI()
                }
            } else {
                callViewModelForAPI()
            }
        }


        binding.filter.setOnClickListener {
            val filterIntent = Intent(requireContext(), FilterActivity::class.java)
            startActivityForResult(filterIntent, request_Code)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                querySearchText(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                querySearchText(query)
                return false
            }

        })

        binding.searchView.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                callAdapter() //this is we are recalling the entire list as it is
                return true
            }
        })

        downloadPDFPermissions()

        return binding.root
    }

    private fun showProgressbar(it: Event<Any, String>?) {
        if (it?.peekContent() == "show") {
            binding.progressBar3.visibility = View.VISIBLE
        } else {
            binding.progressBar3.visibility = View.GONE
        }
    }

    private fun alertQuoteDialog(
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


    private fun querySearchText(query: String) {
        val builder: QueryBuilder<Post> = quoteBox.query()
        builder.contains(Post_.Gender, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Coverage, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Premium, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.FundingType, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Tenure, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Insurer, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Product, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.ProductCategory, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Age, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Tax, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.TotalPremium, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.NetPremium, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.PremiumPaymentTerm, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.LoanAmount, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.LoanType, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Status, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Zone, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.Remarks, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.CalculatorVersion, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.MedicalsRequired, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.RMID, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.FundingType, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.BenefitType, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.ContractNo, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.QuoteGeneratedOn, query, QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
            .contains(Post_.QuoteNo, query, QueryBuilder.StringOrder.CASE_INSENSITIVE)


        val queryResult: List<Post> = builder.build().find()
        Log.e("query val123", ">>>>>>>>>>${queryResult}")

        if (queryResult.isNotEmpty())
            queryFilterAdapter(queryResult)
        else
           queryFilterAdapter(queryResult)
           // callAdapter()

    }

    fun queryFilterAdapter(queryResult: List<Post>) {

        val adapter = CustomAdapter(queryResult)
        //now adding the adapter to recyclerview
        binding.postList.adapter = adapter
        binding.postList.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == request_Code) {
            if (resultCode == RESULT_OK) {
                val zone = data!!.getStringExtra(FilterActivity.ZONE)
                val insurer = data!!.getStringExtra(FilterActivity.INSURER)
                val category = data!!.getStringExtra(FilterActivity.CATEGORY)
                val product = data!!.getStringExtra(FilterActivity.PRODUCT)
                val fundingType = data!!.getStringExtra(FilterActivity.FUNDINGTYPE)
                val gender = data!!.getStringExtra(FilterActivity.GENDER)
                val tenure = data!!.getStringExtra(FilterActivity.TENURE)
                val contractno = data!!.getStringExtra(FilterActivity.CONTRACTNO)

                Log.e("value is", ">>>>>>>>${insurer}")

                /**** query for the filtered list ***/
                val builder: QueryBuilder<Post> = quoteBox.query()
                builder.equal(Post_.Zone, zone,QueryBuilder.StringOrder.CASE_INSENSITIVE).and()
                    .equal(Post_.Insurer, insurer,QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
                    .equal(Post_.ProductCategory, category,QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
                    .equal(Post_.Product, product,QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
                    .equal(Post_.FundingType, fundingType,QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
                    .equal(Post_.Gender, gender,QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
                    .equal(Post_.Tenure, tenure,QueryBuilder.StringOrder.CASE_INSENSITIVE).or()
                    .equal(Post_.ContractNo, contractno,QueryBuilder.StringOrder.CASE_INSENSITIVE)


                val queryResult: List<Post> =
                    builder.build().find()
                Log.e("query val123", ">>>>>>>>>>${queryResult}")

                if (queryResult.isNotEmpty())
                    queryFilterAdapter(queryResult)
                else {
                    callAdapter()
                    (requireContext() as MainActivity).showToast("No data found for selected filter")
                }


            }
        }
    }

    private fun callAdapterSet(it: Event<Any, String>?) {
        if (it != null && it.getType() == "adapter") {
            var quoteList: QuoteList? = null
            quoteList = it.peekContent() as QuoteList

            /*var db =
                Room.databaseBuilder(requireContext(), NoteDatabase::class.java, "note_database")
                    .build()
            for (job in premQuote.jobs) {
                db.quoteDao().insertAll(job)
            }

            Log.e("this is quote save", ">>>>>>>>>>>${db.quoteDao().all}")*/

            addQuotesToLocalDB(quoteList)


        } else if (it != null && it.getType() == "userstatus") {
            var statusdto: StatusDto? = null
            statusdto = it.peekContent() as StatusDto
            userstatus = statusdto.status

            if (userstatus == "In Active") {
                alertQuoteDialog(
                    requireContext(),
                    "Alert!",
                    "Your profile has been made inactive, please contact Admin Support",
                    false,
                    "Okay"
                )
            }
        }
    }


    private fun addQuotesToLocalDB(quoteList: QuoteList) {
        Log.d("111111", "addQuotesToLocalDB: ${quoteList.jobs}")
        quoteBox.removeAll() //ToDo: check this and change it to update and insert
        quoteBox.put(quoteList.jobs)

        noInternetSetAdapter()
    }

    private fun noInternetSetAdapter() {
        binding.progressBar3.visibility = View.GONE
        callServerDataAdapter()
    }

    private fun callServerDataAdapter() {
        if (quoteBox.all.size > 0) {
            val adapter = CustomAdapter(quoteBox.all)
            //now adding the adapter to recyclerview
            binding.postList.adapter = adapter
            binding.postList.visibility = View.VISIBLE
            binding.noQuoteText.visibility = View.GONE
        } else {
           binding.noQuoteText.visibility = View.VISIBLE
            binding.postList.visibility = View.GONE
        }
    }


    private fun callAdapter() {
        //creating our adapter
        if (quoteBox.all.size > 0) {

            /*  var query = quoteBox.query().equal(Post_.Gender,"Male")
                  .equal(Post_.Name,"Sriram")
                  .build().find()
  */

            val adapter = CustomAdapter(quoteBox.all.reversed())
            //now adding the adapter to recyclerview
            binding.postList.adapter = adapter
            binding.postList.visibility = View.VISIBLE
            binding.searchView.visibility=View.VISIBLE
            binding.noQuoteText.visibility = View.GONE
        } else {
            viewModel.loadPosts() //this is to check online server if internal db is emopty

            binding.noQuoteText.visibility = View.VISIBLE
            binding.postList.visibility = View.GONE
            binding.searchView.visibility=View.GONE

        }
    }


    private fun downloadPDFPermissions() {
        requestPermission()

    }

    private fun requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){


        }else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                111
            )
        } else {
           // startActivity(new Intent(PdfCreationActivity.this, PdfCreate.class));
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           // pdfView()
        }
    }

/*    fun addNotes() {
        val title = "title"
        val description = "description"
        var postBox: Box<QuoteBox> = ObjectBox.boxStore.boxFor(QuoteBox::class.java)

        if (title.isNotEmpty() || description.isNotEmpty()) {
            postBox.put(QuoteBox(Name = title, Number = 5))
            postBox.put(QuoteBox(Name = title, Number = 7))
            postBox.put(QuoteBox(Name = title, Number =25))
            postBox.put(QuoteBox(Name = title, Number = 1))
        }

        var query = postBox.query().equal(QuoteBox_.Number, 1).build().find()
        Log.e("query val ",">>>>>>>>$query")
    }*/

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
              //  if(userZone.Zone.contains('|')){
                    SharedPref.writeArrayList(Constants.PREF_ZONES, ArrayList(userZone.Zone.split("|")))
            //    }
                if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {

                    SharedPref.write(Constants.PREF_ZONE, userZonestatus)

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
    fun userStatusChanged() {

        if (offlineBox.all.size > 0) {
            offlineDBCount = offlineBox.all.size.toLong()
            var postList: List<OfflineJson> = offlineBox.query().equal(
                OfflineJson_.ids,
                offlineDBCount
            ).build().find()

            if (postList.isNotEmpty()) {
                binding.offlineProgress.visibility = View.VISIBLE
                (requireContext() as MainActivity).window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )

                var jsonData = postList[0].jsonData
                Log.d("jsonData", "offlineListChecker:2 ${jsonData.toString()}")

                viewModel.postQuoteAPI(
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
                    Log.d("1111111", "userStatusChanged: $userstatus ${SharedPref.read(Constants.PREF_ZONE, "")}")
                    alertuserZonelogoutDialog(
                        requireContext(),
                        "Alert!1",
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
                Log.d("1111111", "userStatusChanged: $userZonestatus ${SharedPref.read(Constants.PREF_ZONE, "")}")

                alertuserZonelogoutDialog(
                    requireContext(),
                    "Alert!3",
                    "Database has undergone change. Please logout",
                    false,
                    "Okay"
                )
            }
        }
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
            (context as MainActivity).finishAffinity()
        }
        /*builder.setNegativeButton(negative) { dialogInterface, which ->
        }*/
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
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
                    Log.d("jsonData", "All items in offlineBox: ${offlineBox.all}")
                    for (item in offlineBox.all) {
                        Log.d("jsonData", "Item in offlineBox: $item")
                                Log.d("jsonData", "Item in postList: $item      qqqqqqqqqq${item.QuoteNo}        111111111${item.jsonData}")
                        var jsonData = item.jsonData
                        var quoteNo1 = item.QuoteNo?.toInt()
                        var quoteNo = SharedPref.read(Constants.PREF_QUOTE_NO, "0")?.toInt()

                        Log.d("All items in offlineBox1111", "offlineListChecker:   $quoteNo $quoteNo1")
                        if (quoteNo != (quoteNo1)) {
                            binding.offlineProgress.visibility = View.VISIBLE
                            SharedPref.write(Constants.PREF_QUOTE_NO, "${item.QuoteNo}")

                            Log.d(
                                "offlineListChecker",
                                "offlineListChecker22222222:   $quoteNo $quoteNo1"
                            )
                            if (quoteNo1 != null) {
                                SharedPref.write(Constants.PREF_QUOTE_NO, "${quoteNo1}")
                            }
                            viewModel.postQuoteAPI(
                                jsonData.toString(),
                                SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                                "3"
                            )
                        }
                    }
                    var postList: List<OfflineJson> =

                        offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()

                    for (item in postList) {

                    }

//                    if (postList.isNotEmpty()) {
//
////                        (requireContext() as MainActivity).window.setFlags(
////                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
////                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
////                        );
//                        // Iterate over each item in postList
//                        /*  for (offlineItem in postList) {
//                              var jsonData = offlineItem.jsonData
//                              viewModel.postQuoteAPI(
//                                  jsonData.toString(),
//                                  SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
//                                  "3"
//                              )
//                              // You can perform additional actions for each item if needed
//                          }*/
//
//                        var jsonData = postList[0].jsonData
//                        var quoteNo1= postList[0].QuoteNo?.toInt()
//                        var quoteNo=SharedPref.read(Constants.PREF_QUOTE_NO, "0")?.toInt()
//                        Log.d("jsonData", "offlineListChecker: ${jsonData.toString()}     Quottttt${postList[0].QuoteNo}")
//                        Log.d("offlineListChecker","offlineListChecker:   $quoteNo $quoteNo1")
//                        if(quoteNo!=(quoteNo1)){
//                            binding.offlineProgress.visibility = View.VISIBLE
//
//                            Log.d("offlineListChecker","offlineListChecker22222222:   $quoteNo $quoteNo1")
//                            if (quoteNo1 != null) {
//                                SharedPref.write(Constants.PREF_QUOTE_NO,"${quoteNo1}")
//                            }
//                        viewModel.postQuoteAPI(
//                            jsonData.toString(),
//                            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
//                            "3"
//                        )
//                    }}
                    binding.offlineProgress.visibility = View.GONE
                }
        }
        updateHandler.postDelayed(runnable, 100)

    }
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
                (requireContext() as MainActivity
                        ).window
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
                            Log.d("1111111", "userStatusChanged: $userZonestatus ${SharedPref.read(Constants.PREF_ZONE, "")}")

                            alertuserZonelogoutDialog(
                                requireContext(),
                                "Alert2",
                                "Database has undergone change. Please logout",
                                false,
                                "Okay"
                            )
                        }

                    }
                }
              //  offlineListChecker()
                Log.d("JsonData", "getofflineSyncStatus1:$postList ")

            }
        }
    }
    private fun getPasswordStatus(it: Event<Any, String>?){
        if (it != null) {
            if(it.getType()=="passwordChange"){
                alertDialogPassword("Alert!",
                    "Password Configuration Updated. Please Change the password",
                    true,
                    "Okay",
                    ""
                );

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
                if (userZonestatus != SharedPref.read(Constants.PREF_ZONE, "")) {
                    userStatusChanged()
                } else {
                    if (userstatus == "In Active" || userstatus == "Inactive") {

                    } else
                        viewModel.checkCalculatePasswordAPI()
                }
            }
            //viewModel.calculatePremium()
        }
    }
    private fun getZoneVals(postList: Event<Any, String>?) {

        if (postList != null) {
            Log.e("this is postlist", ">>>>>>>>>>>>${postList.peekContent()}")
            var jobArry = ArrayList<Job>()
            if (postList.peekContent() is ArrayList<*>) {
                // this is from DB
                if (postList.getType() == "ProdCat") {
                    var zoneArry = postList.peekContent() as ArrayList<ProdCat>

                    for (note in zoneArry) {
                        jobArry.addAll(note.jobs)

                    }
                } else if (postList.getType() == "Insurer") {
                    var prodandcatarray = postList.peekContent() as ArrayList<Insurer>

                    for (note in prodandcatarray) {
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
                }
            }

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
    private fun callPasswordAlertNotification(expiryDays: Int) {
        val intent = Intent(requireContext(), MessageService::class.java)
        intent.putExtra(MessageService.QUOTE_NUMBER, "password")
        intent.putExtra(
            MessageService.MESSAGE_EXTRA,
            "$expiryDays days for password to expire. Please change password"
        )
        requireContext().startService(intent)
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
            (context as MainActivity).finishAffinity()
        }
        builder.setNegativeButton(negative) { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()

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
            viewModel.loadInsurer()
        }
    }
    fun alertDialogPassword(
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

            if (!requireActivity().isFinishing) {
                // Start the new activity
                requireContext().startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))

                // Finish the current activity
                (requireContext() as MainActivity).finishAffinity()
            }
            //refresh frag here to get live data
            /*val homeFragment = HomeFragment.newInstance()
            (requireContext() as MainActivity).refreshFrag(homeFragment)*/
        }
        if (!requireActivity().isFinishing) {
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(cancellable)
            alertDialog.show()
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
            (requireContext() as MainActivity).finishAffinity()
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
            if (!isAdded) return@Runnable
            if ((requireContext() as MainActivity).checkInternet())
                viewModel.tokenRefrehAPI()
        }
        updateHandler.postDelayed(runnable, 100)
    }
    private fun callViewModelForAPI() {
        val updateHandler = Handler() //using handler to get the internet connection
        val runnable = Runnable {
            if (!isAdded) return@Runnable
            if (isInternetAvailable(mContext))
                tokenRefreshAPI()
            // this is for online check when internet is on
            else
                viewModel.loadInsurer()  //this is called to check if we have offline db present
        }
        updateHandler.postDelayed(runnable, 100)
    }
}
fun isInternetAvailable(context: Context?): Boolean {
    val connectivityManager =
        context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnected == true
    }
}
