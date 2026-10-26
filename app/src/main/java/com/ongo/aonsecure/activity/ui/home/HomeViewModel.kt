package com.ongo.aonsecure.activity.ui.home

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.SingleLiveEvent
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pdf.app.library.utils.Constants
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(
    private val insurerDao: InsurerDao,
    private val prodCatDao: ProdCatDao,
    private val productDao: ProductDao,
    private val tenureDao: TenureDao,
    private val loanTypeDao: LoanTypeDao,
    private val taxDao: TaxDao,
    private val calculateDao: CalculateDao,
    private val termsDao: TermsDao,
   // private val productTypeDao: ProductTypeDao,

    private val jobDao: JobDao
) : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    private val homeFragInfo = HomeFragInfo()
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    var isFromDBDrop: Boolean = false
    var callServerSyncAPI: Boolean = false
    var minAge1="";
    var min_age=18;
    var max_age=85;
    val sum_assured=10000000
    var maxAge1="";
    var maxSumInsured1=""
    var passwordChange: Boolean=false
    val offlineProgressLayout: MutableLiveData<Int> = MutableLiveData()

    private val statusMessage = MutableLiveData<Event<Any, String>>()
    val message: LiveData<Event<Any, String>>
        get() = statusMessage

    private val zonesMessage = MutableLiveData<Event<Any, String>>()
    val zone: LiveData<Event<Any, String>>
        get() = zonesMessage

    private val premiumCalCallMsg = MutableLiveData<Event<Any, String>>()
    private var NML_BOOL: Boolean = false

    private val statusMessageVal = MutableLiveData<Event<String, String>>()

    val messageVal: LiveData<Event<String, String>>
        get() = statusMessageVal
    val mobile_regex = Regex("^[6789]\\d{9}$")

    private var  productType="";
    private val status = SingleLiveEvent<Status>()
    fun getStatus(): LiveData<Status> {
        return status
    }

    private val alertMsg = SingleLiveEvent<Status>()
    fun getAlertMsg(): LiveData<Status> {
        return alertMsg
    }

    val premiumcal: LiveData<Event<Any, String>> get() = premiumCalCallMsg

    init {
        //TODO: remove the seperate calls and merge to single call using doOnNext() --retrofit & RxJava
    }

    fun getFirstUserStatus(email: String) {
        subscription = postApi.userStatus(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            //  .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onFirstStatusSuccess(result) },
                { result -> onFirstStatusFailure(result) })
    }

    private fun onFirstStatusSuccess(result: Any) {
        //SharedPref.write(Constants.PREF_USER_STATUS, result?.status.toString())
        statusMessage.value = Event(result, "userstatus")
        //zonesMessage.value = Event(result, "userstatus")
        getFirstUserZone(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString())
    }

    private fun onFirstStatusFailure(result: Throwable) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
    }


    fun getFirstUserZone(email: String) {
        subscription = postApi.getUserZone(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //.doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onFirstZoneChangeSuccess(result) },
                { result -> onFirstZoneChangeFailure(result) })
    }

    private fun onFirstZoneChangeSuccess(result: ObjResponse) {
        statusMessage.value = Event(result, "userzone")
    }

    private fun onFirstZoneChangeFailure(result: Throwable) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
    }

    fun getUserStatus(email: String) {
        subscription = postApi.userStatus(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            //  .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onStatusSuccess(result) },
                { result -> onStatusFailure(result) })
    }

    private fun onStatusSuccess(result: Any) {
        // SharedPref.write(Constants.PREF_USER_STATUS, result?.status.toString())
        //statusMessage.value = Event(result, "userstatus")
        zonesMessage.value = Event(result, "userstatus")
        getUserZone(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString())
    }

    private fun onStatusFailure(result: Throwable) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
    }

    //checking user zone everytime after user status
    fun getUserZone(email: String) {
        subscription = postApi.getUserZone(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //  .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onZoneChangeSuccess(result) },
                { result -> onZoneChangeFailure(result) })
    }

    private fun onZoneChangeSuccess(result: ObjResponse) {
        zonesMessage.value = Event(result, "userzone")
    }

    private fun onZoneChangeFailure(result: Throwable) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
    }
    fun getUserZoneUpdate(email: String) {
        subscription = postApi.getUserZone(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //  .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> updateOnZoneChangeSuccess(result) },
                { result -> UpdateOnZoneChangeFailure(result) })
    }
    private fun updateOnZoneChangeSuccess(result: ObjResponse) {
        zonesMessage.value = Event(result, "userzone1")
        loadInsurer()
    }

    private fun UpdateOnZoneChangeFailure(result: Throwable) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
    }
    private fun syncServerCall() {
        //check's password validity
        // auto sync option when premium calculator is changed
        subscription = postApi.syncServerUpdateCall(
            SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, ""),
            "True" // sending this as true to confirm that this device has sync'ed with server successfully
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onSyncServerSuccess(result) },
                { result -> onSyncServerFailure(result) })
    }

    private fun onSyncServerFailure(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
    }

    private fun onSyncServerSuccess(result: ObjResponse?) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
        //  loadingVisibility.value = View.GONE
        callServerSyncAPI = false
        statusMessageVal.value = Event("hide", "")
    }

    fun checkPasswordAPI() {
        //check's password validity
        // auto sync option when premium calculator is changed
        subscription = postApi.checkPWDValidity(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, ""))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            /* .doOnSubscribe { onRetrievePostListStart() }
             .doOnTerminate { onRetrievePostListFinish() }*/
            .subscribe(
                { result -> onPasswordValiditySuccess(result) },
                { result -> onPasswordValidityFailure(result) })
    }

    fun tokenRefrehAPI(){
        subscription=postApi.tokenRefresh(SharedPref.read(Constants.PREF_TOKEN,""))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            /* .doOnSubscribe { onRetrievePostListStart() }
             .doOnTerminate { onRetrievePostListFinish() }*/
            .subscribe(
                { result -> onTokenRefreshSuccess(result) },
                { result -> onTokenRefreshFailure() })
    }
    private fun onTokenRefreshFailure() {
        Log.e("this is obj response", ">>>>>>>$")

    }
    private fun onTokenRefreshSuccess(obj: ObjResponse?) {
        Log.e("this is obj response", ">>>>>>>$obj")
        if (obj!!.accessToken.isNotEmpty()) {
            SharedPref.write(Constants.PREF_TOKEN,obj.accessToken)
            checkPasswordAPI()

        } else if (obj.accessToken.isEmpty()) {
            Log.e("this is password -1", ">>>>>>>$obj")
        }

    }

    private fun onPasswordValiditySuccess(obj: ObjResponse?) {
        Log.e("this is obj response", ">>>>>>>$obj")
        if (obj!!.status == "1") {
            passwordValidityCheck(obj)

        } else if (obj.status == "-1") {
            Log.e("this is password -1", ">>>>>>>$obj")
        }

    }

    private fun onPasswordValidityFailure(result: Throwable?) {
        if (result!!.message.equals("Failed to connect to /$BASE_URL"))  //this is to check for offline/no internet
            loadInsurer()
    }

    private fun passwordValidityCheck(obj: ObjResponse) {
        var dayReminder: Int = obj.DaysToRemaind.toInt()
        var expiryDay: Int = obj.DaysToExpiry
        if (expiryDay <= 0) {
            //logout alert
            statusMessage.value = Event(expiryDay, "showLogout")
            return
        }
        if (expiryDay <= dayReminder) {
            //call push notification alert
            statusMessage.value = Event(expiryDay, "showPasswordNotification")
        }

        // above condition fails te
        serverAutoSyncCheck(obj)
    }

    private fun serverAutoSyncCheck(obj: ObjResponse) {
        //callServerSyncAPI = true

        if(obj.isPasswordChanged=="True"||obj.isServerSync.isEmpty()){
            statusMessage.value = Event(obj, "passwordChange")

        }
        if (obj.isServerSync == "False" || obj.isServerSync.isEmpty()) { //server auto sync
            callServerSyncAPI = true
            deleteDBAndGetServerData()
        } else if (obj.isServerSync == "True") { //from db load data
            callServerSyncAPI = false
            loadInsurer()
        }
       prodType()
    }


    fun loadInsurer() {
        subscription = Observable.fromCallable { insurerDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getInsurer()
                        .concatMap { apiPostList ->
                            insurerDao.insertAll(apiPostList)
                            Observable.just(apiPostList)
                        }
                else
                    Observable.just(dbPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            //  .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onInsurerSuccess(result) },
                { result -> onInsurerFailure(result) })
    }

    private fun onInsurerSuccess(result: Any) {
        statusMessage.value = Event(result, "Insurer")
        Log.d("testing", "onInsurerSuccess: "+result.toString())
        loadTenure()
    }

    private fun onInsurerFailure(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.d("testing", "onInsurerFailure: "+result.toString())

    }

    private fun loadTenure() {
        subscription = Observable.fromCallable { tenureDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getTenure().concatMap { apiPostList ->
                        tenureDao.insertAll(apiPostList)
                        Observable.just(apiPostList)
                    }
                else
                    Observable.just(dbPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onTenureSuccess(result) },
                { result -> onRetrievePostListError(result) }
            )
    }

    private fun onTenureSuccess(result: Any) {
        statusMessage.value = Event(result, "Tenure")
        loadCategory()
        /*  var category: String?= ""
          category="Zone\$" + homeFragInfo.zone +"~Insurer\$" + homeFragInfo.insurer?.substring(0, homeFragInfo.insurer?.indexOf("(")!!).toString()
          loadCategory(category)*/

    }



    private fun loadCategory() {
        var mallId: String? = "3"
        subscription = Observable.fromCallable { prodCatDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty()) {
                    postApi.getProdCate(mallId).concatMap { apiPostlist ->
                        prodCatDao.insertAll(apiPostlist)
                        Observable.just(apiPostlist)
                    }
                } else {
                    Observable.just(dbPostList)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onCategorySuccess(result) },
                { result -> onCategoryError(result) })

    }

    private fun onCategorySuccess(result: Any) {
        statusMessage.value = Event(result, "ProdCat")
        Log.d("testing", "onCategorySuccess: "+result)
        loadLoanType()
    }

    private fun onCategoryError(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.d("testing", "onCategoryfailure: "+result)

        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
    }
    private fun prodType() {
        val mallId = "3"
        subscription = postApi.getProductType(mallId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onProductTypeSuccess(result) },
                { error -> onProductTypePostListError(error) }
            )
    }
    private fun loadLoanType() {
        subscription = Observable.fromCallable { loanTypeDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getLoanType().concatMap { apiPostList ->
                        loanTypeDao.insertAll(apiPostList)
                        Observable.just(apiPostList)
                    }
                else
                    Observable.just(dbPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onLoanTypeSuccess(result) },
                { result -> onRetrievePostListError(result) }
            )
    }

    private fun onLoanTypeSuccess(result: Any) {
        statusMessage.value = Event(result, "LoanType")
        loadProducts()

    }

    private fun loadProducts() {

        var mallId: String? = "3"
        subscription = Observable.fromCallable { productDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty()) {
                    postApi.getProducts(mallId.toString()).concatMap { apiPostList ->
                        productDao.insertAll(apiPostList)
                        Observable.just(apiPostList)
                    }
                } else {
                    Observable.just(dbPostList)
                }

            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onProductSuccess(result) },
                { result -> onProductPostListError(result) }
            )

    }

    private fun onProductSuccess(result: Any) {
        Log.d("testing", "onProductSuccess: "+result)
        Log.d("testing", "onProductSuccess: "+productDao)
        statusMessage.value = Event(result, "Product")
        loadTax()
    }


    private fun onProductPostListError(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
    }

    private fun onProductTypeSuccess(result: Any) {
        val productNames = mutableListOf<String>()

        if (result is ProductType) {
            result.jobs?.forEach { job ->
                job.Name?.let {
                    productNames.add(it)
                }
            }
        }

        // Now productNames is your List<String> of names
        Log.d("testing", "Product Names: $productNames")

         val productType: ArrayList<String> =ArrayList( productNames)

        statusMessage.value = Event(productType, "ProductType")

    }


    private fun onProductTypePostListError(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
    }

    private fun loadTax() {
        val sdf = android.icu.text.SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        subscription = Observable.fromCallable { taxDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty()) {
                    postApi.getTax(currentDate.toString()).concatMap { apiPostList ->
                        taxDao.insertAll(apiPostList)
                        Observable.just(apiPostList)
                    }
                } else {
                    Observable.just(dbPostList)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //.doOnSubscribe { onRetrievePostListStart() }
            // .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onTaxSuccess(result) },
                { result -> onTaxFailure(result) })
    }

    private fun onTaxSuccess(result: Any) {
        statusMessage.value = Event(result, "taxval")
        loadPremiumCalc()
    }

    private fun onTaxFailure(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");

    }


    private fun loadPremiumCalc() {
        Log.d("Calculate Premium:222", "calculatePremium: "+homeFragInfo.product)

        var zone=homeFragInfo.zone
        Log.d("Calculate Premium:222", "calculatePremium: "+homeFragInfo.zone+"  "+zone)

        subscription = Observable.fromCallable { calculateDao.all(zone.toString()) }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getCalculator().concatMap { apiPostList ->
                        val jobs: List<Calculate1> = apiPostList.jobs
                        jobs.forEach { job ->
                            calculateDao.insert(job)
                            Log.d("1222222222222", "calculatePremium554: "+job.productType +job)
                        }
                        //calculateDao.insertAll(apiPostList)
                        Observable.just(jobs)
                    }
                else
                    Observable.just(dbPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onPremiumCalSuccess(result) },
                { result -> onRetrievePostListError(result) }
            )
    }

    private fun onPremiumCalSuccess(result: Any) {
        //calculateDao.getQuery("Zone C(467)"/*, "ICICI Life Insurance(504)"*/)
        // Log.e("result prem succ", ">>>>>>>>>>$result")
        if (isFromDBDrop) {
            isFromDBDrop = false
            statusMessage.value = Event(result, "showSyncAlert")
            if (callServerSyncAPI)
                syncServerCall() //this is called to give confirmation to server that all synced with server
        }
        statusMessage.value = Event(result, "Offline_Check")
    }

    private fun onRetrievePostListStart() {
        //loadingVisibility.value = View.VISIBLE
        statusMessageVal.value = Event("show", "")
    }

    private fun onRetrievePostListFinish() {
        //loadingVisibility.value = View.GONE
        statusMessageVal.value = Event("hide", "")
    }

    private fun onRetrievePostListError(result: Throwable) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
        //  loadingVisibility.value = View.GONE
    }


    private val disposable = CompositeDisposable()


    fun checkCalculatePasswordAPI() {
        //check's password validity
        // auto sync option when premium calculator is changed
        subscription = postApi.checkPWDValidity(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, ""))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onCalculatePasswordValiditySuccess(result) },
                { result -> onCalculatePasswordValidityFailure(result) })
    }

    private fun onCalculatePasswordValiditySuccess(obj: ObjResponse?) {
        Log.e("this is obj response", ">>>>>>>$obj")
        if (obj!!.isServerSync == "false") {
            alertMsg.value = Status.CURRENT_JOB_STATUS
        } else {
            calculatePremium()
        }

    }

    private fun onCalculatePasswordValidityFailure(result: Throwable?) {

    }

    fun calculatePremium() {

/*        subscription = Observable.fromCallable { jobDao.loadSingle("60 Months") }
            .subscribeOn(Schedulers.io()).subscribe({ result ->
                ondataResult(result)
            }, { result -> onRetrievePostListError(result) })*/
        Log.d("Calculate Premium:222", "calculatePremium: "+homeFragInfo.product)
        var zone=homeFragInfo.zone
        Log.d("Calculate Premium:222", "calculatePremium: "+homeFragInfo.zone+"  "+zone)

        subscription = Observable.fromCallable { calculateDao.all(zone.toString()) }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getCalculator().concatMap { apiPostList ->
                        Log.d("api","1222222222222 "+apiPostList.jobs)

                        Log.d("api","1222222222222 "+apiPostList.jobs)

                        val jobs: List<Calculate1> = apiPostList.jobs
                        jobs.forEach { job ->
                            calculateDao.insert(job)
                            Log.d("1222222222222", "calculatePremium646: "+job.productType )
                           // jobDao.insert(job)
                        }
                      //  calculateDao.insertAll(apiPostList)
                        Observable.just(jobs)
                    }
                else
                    Observable.just(dbPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onCalculatePremiumClicked(result) },
                { result -> onRetrievePostListError(result) }
            )
    }

    private fun onCalculatePremiumClicked(result: Any?) {
        // Log.e("result ", ">>>>>>>>$result")
//        Log.d("ProducttType:::",productType);
//        Log.d("11", "insurer :${homeFragInfo.insurer.toString()} ")
//        Log.d("11", "Cate :${homeFragInfo.category.toString()} ")
//        Log.d("11", "insurer :${homeFragInfo.product.toString()} ")
//        Log.d("11", "insurer :${homeFragInfo.gender.toString()} ")
//        Log.d("11", "insurer :${homeFragInfo.funding.toString()} ")

        var loanAmt = homeFragInfo.loanAmt.toString()
        var coverage = homeFragInfo.coverage.toString()
        if (loanAmt.contains(","))
            loanAmt = loanAmt.replace(",", "")

        if (coverage.contains(","))
            coverage = coverage.replace(",", "")
          if(minAge1.isNotEmpty()){
              min_age =minAge1.toInt()

          }else{
              min_age=0
          }
        if(maxAge1.isNotEmpty()){
            max_age =maxAge1.toInt()

        }else{
            max_age=0
        }
        if (validForm(
                loanAmt,
                coverage,
                homeFragInfo.dob.toString(),
                homeFragInfo.age.toString(),
                homeFragInfo.tenure.toString(),
                homeFragInfo.insurer.toString(),
                homeFragInfo.category.toString(),
                homeFragInfo.gender.toString(),
                homeFragInfo.product.toString(),
                homeFragInfo.funding.toString(),
                productType
              //  homeFragInfo.contractno.toString()
          //  ,homeFragInfo.productType.toString()


            )
        ) {
            if (validAge(homeFragInfo.tenure, homeFragInfo.age))
                generatePreminum(result)
        }
    }

    private fun validAge(tenure: String?, age: String?): Boolean {

        val tenureStr: String = tenure.toString().replace("Months", "").trim()
        val tenureInt: Int = tenureStr.toInt()
        //val ageInt: Int = (age?.toInt() ?: 1) * 1
        val ageInt: Int = (age?.toInt() ?: 1) * 12

        val totalAgeInt = tenureInt + ageInt
       if (totalAgeInt > 80 * 12) {
      //  if (totalAgeInt > 80 * 1) {
            status.value = Status.AGE_INVALID_COVER
            return false
        }


        return true

    }

    private fun validForm(
        loanAmt: String,
        coverage: String,
        dob: String,
        age: String,
        tenure: String,
        insurer: String,
        category: String,
        gender:String,
        product: String,
        fundingType:String
       // contractno: String
        ,productTypeVal: String

    ): Boolean {
        status.value = Status.REMOVE_ERRORS
        when {
            insurer.isEmpty()->{
                status.value = Status.INSURER
                return false
            }
            category.isEmpty()->{
                status.value = Status.PRODCAT
                return false
            }
            gender.isEmpty()->{
                status.value = Status.Gender
                return false
            }
            product.isEmpty()->{
                status.value = Status.PRODUCT
                return false
            }
            fundingType.isEmpty()->{
                status.value=Status.FUNDTYPE
            }

            loanAmt.isEmpty() -> {
                status.value = Status.LOAN_EMPTY
                return false
            }
            loanAmt.toDouble() < coverage.toDouble() -> {
                status.value = Status.LOAN_INVALID
                return false
            }
            loanAmt.toDouble() > maxSumInsured1.toInt() -> {
                status.value = Status.LOAN_INVALID1
                return false
            }
            loanAmt.toDouble() < 10000 -> {
                status.value = Status.LOAN_MIN
                return false
            }
            coverage.isEmpty() -> {
                status.value = Status.COVERAGE_EMPTY
                return false
            }
            coverage.toDouble() < 10000 -> {
                status.value = Status.COVERAGE_MIN
                return false
            }
            coverage.toDouble() > maxSumInsured1.toInt() -> {
                status.value = Status.COVERAGE_MIN
                return false
            }
            tenure.isEmpty() -> {
                status.value = Status.TENURE_EMPTY
                return false
            }
            productTypeVal.isEmpty()->{
                status.value=Status.PRODUCT_TYPE_EMPTY 
                return false
            }
            !isTenureMatchedWithArray(tenure) -> {
                status.value = Status.TENURE_MISMATCH
                return false
            }
            dob.isEmpty() -> {
                status.value = Status.DOB_EMPTY
                return false
            }
            age.isEmpty() -> {
                status.value = Status.AGE_EMPTY
                return false
            }
            age.toInt() !in min_age..max_age -> {
                status.value = Status.AGE_INVALID
                return false
            }

//            contractno.isEmpty() ->{
//                status.value=Status.CONTACT_NUMBER_EMPTY
//                return false
//            }
//            !contractno.matches(mobile_regex) ->{
//                status.value=Status.CONTACT_NUMBER
//                return false
//            }
        }
        return true
    }

    private fun isTenureMatchedWithArray(tenure: String): Boolean {
        return tenure in tenureArry
    }

    fun stringToDateConvert(from: String): Date? {
        var myDate: Date? = null
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        try {
            myDate = dateFormat.parse(from)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return myDate
    }

    private fun generatePreminum(result: Any?) {
        Log.d("2222222222222222", "generatePreminum: +result")
        //var jobArry = ArrayList<Job>()
        var premiumCalVersion = ""
        var premiumValue = ""
        var message = ""

        if (result is ArrayList<*>) {//this is from DB
            var calArry = result as ArrayList<Calculate1>
            var zone = false
            var insurer = false
            var prodCat = false
            var product = false
            var fundType = false
            var genderType = false
            //    var isDateinRange = false
//            for (note in calArry) {
//               // jobArry.addAll(note.jobs)
//            }
            var calJob: Calculate1? = null ;
            var calJob1: Calculate1?=null

            var calJobList: MutableList<Calculate1>? = mutableListOf()
             var calcution_available=false;
            var check_premium=false;

            for (value in calArry) {


                /*zone = (value.Zone.isNotEmpty() && (value.Zone.substring(
                    0,
                    value.Zone.indexOf("(")
                ) == homeFragInfo.zone))*/

                zone = (value.Zone).contains(homeFragInfo.zone.toString())
                // zone = true


                try {
                    insurer = (value.Insurer.isNotEmpty() && (value.Insurer.substring(
                        0, value.Insurer.indexOf("(")
                    ) == (homeFragInfo.insurer.toString()
                        .substring(0, value.Insurer.indexOf("(")))))
                    prodCat =
                        (value.ProductCategory.isNotEmpty() && (value.ProductCategory.substring(
                            0, value.ProductCategory.indexOf("(")
                        ) == (homeFragInfo.category
                        )))
                    Log.d("testing", "generatePreminum: "+value.ProductCategory   +"  "+(homeFragInfo.category))
                    Log.d("Type", "generatePreminum: "+value.Type)

                    //todo: uncomment and remove true
                    if(!product) {
                        product = (value.Product.isNotEmpty() && (value.Product.substring(
                            0, value.Product.indexOf("(")
                        ) == (homeFragInfo.product)))
                        Log.d("Product Check", "generatePreminum: ${(value.Product.isNotEmpty())}")
                        Log.d(
                            "Product Check", "generatePreminum: ${
                                value.Product.substring(
                                    0, value.Product.indexOf("(")
                                )
                            }"
                        )
                        Log.d("Product Check", "generatePreminum: ${homeFragInfo.product}")
                    }
                    //product = true

                } catch (e: Exception) {

                }

                fundType =
                    (value.FundingType.isNotEmpty() && (value.FundingType == (homeFragInfo.funding)))
                if(!genderType) {
                    genderType =
                        (value.Gender.isNotEmpty() && (value.Gender == (homeFragInfo.gender)))
                    Log.d(
                        "Product check",
                        "generatePreminum: +" + value.productType + "   " + homeFragInfo.gender
                    )
                }
                /* isDateinRange = if (value.From.isNotEmpty() && value.To.isNotEmpty()) {
                     isWithinRange(
                         Calendar.getInstance().getTime(), //current date
                         stringToDateConvert(value.From), //server from date
                         stringToDateConvert(value.To) //server to date
                     )
                 } else {
                     false
                 }*/

                Log.d("Product  teeteteteteetetetcheck", "generatePreminum: "+value.productType)

                if (zone && insurer && prodCat && product && fundType && genderType) {
                    calcution_available=true
                    if (!value.Type.isNullOrEmpty() &&
                        value.Type.substringBefore("(").trim() == "Construction Equipment") {
                        // your logic here
                        calJob=value
                    }
                    if(!value.Type.isNullOrEmpty() &&
                        value.Type.substringBefore("(").trim() ==productType){
                        calJob1=value
                        Log.d("NML LIMITS","id::"+value.jobId +value.id)
                    }
                    calJobList!!.add(value)
                    premiumCalVersion = value.CalculatorVersion

                  //  break
                }

            }
            Log.d("claList",""+calJobList?.size)
            if(!calcution_available){
                showAlertNoPremium()
            }else

            if (showAlertJobMisMatch(zone, insurer, prodCat, product, fundType, genderType)) {
                if (calJob.toString().isNotEmpty()) {
                    var creatSubJobArry = ArrayList<CreatedSubJobs>()
                    var creatSubJobArry1 = ArrayList<CreatedSubJobs>()


                    if (calJob != null) {
                        creatSubJobArry.addAll(calJob.CreatedSubJobs)

                        for (value in creatSubJobArry) {



                            if(value.MaximumAge!=null&&value.MinimumAge!=null){
                            var minAge: Int = value.MinimumAge.toInt()
                            var maxAge: Int = value.MaximumAge.toInt()
                            var formAge: Int = homeFragInfo.age!!.toInt()
                            var tenure = homeFragInfo.tenure.toString()
                                .substring(0, homeFragInfo.tenure.toString().indexOf(" "))
                            Log.d("Calculation::::", ">>>>>>>>premium ${value.Premium}   Form Age: "+formAge+"  minAge:"+minAge+" maxage:"+maxAge+" tenure:"+tenure+" value Tenure:"+value.Tenure+" ")

                          //  if (premiumCalVersion == value.CalculatorVersion) {
                              Log.d("Form age"," age"+formAge  +" min Age"+minAge +" max age"+maxAge+" trnure"+ tenure +" "+value.Tenure)
                                if (formAge in minAge..maxAge && tenure == value.Tenure?.trim()?.replace(Regex("\\s+"), " ")) {
                                    Log.d("premium for selected55", "11>>>>>>>>${value.Premium}")
                                    premiumValue = value.Premium
                                    check_premium=true
                                    break
                                }else{
                                    Log.d("premium for selected66", "11>>>>>>>>${value.Premium}")

                                    check_premium=false
                                }

                           // }
                        }}

                        if(calJob1!=null){
                            creatSubJobArry1.addAll(calJob1.CreatedSubJobs)

                        }
                        //this is for NML limits check
                        for (value in creatSubJobArry1) {
                            if (value.jobTypeName == "NML Limits") { //NML limit table calc go here
                               if(value.minAge!=null&&value.maxAge!=null){
                                var startAge: Int = value.minAge.toInt()
                                var endAge: Int = value.maxAge.toInt()
                                var formAge: Int = homeFragInfo.age!!.toInt()
                                var coverage = homeFragInfo.coverage.toString()
                                if (coverage.contains(","))
                                    coverage = coverage.replace(",", "")
                                var sumInsured: Int = coverage.toInt()
                                var minsumInsured: Int = value.minSumInsured.toInt()
                                var maxsumInsured: Int = value.maxSumInsured.toInt()

                                var tenure = homeFragInfo.tenure.toString()
                                    .substring(0, homeFragInfo.tenure.toString().indexOf(" "))
                                    .toInt()
                                var minTenure: Int = value.minTenure.toInt()
                                var maxTenure: Int = value.maxTenure.toInt()
                                   Log.d("NML Limits","foem age :"+formAge +" startAge::"+startAge+" endAge::"+endAge+"  tenure"+tenure+" minTenure"+ minTenure+ " max tenure:"+maxTenure+ " min sum::"+minsumInsured+" maxSum:"+maxsumInsured)

                                if (formAge in startAge..endAge && tenure in minTenure..maxTenure) {
                                    if (sumInsured > maxsumInsured) {
                                        premiumValue = 0.0.toInt().toString()
                                        message = value.message
                                        NML_BOOL = true
                                        break
                                    } else {
                                        NML_BOOL = false
                                    }

                                }
                                /*if (formAge in startAge..endAge && sumInsured in minsumInsured..maxsumInsured &&
                                        tenure in minTenure..maxTenure){
                                    premiumValue=0.0.toInt().toString()
                                    message=value.message
                                     NML_BOOL = true
                                     break
                                 } else {
                                     NML_BOOL = false
                                 }*/

                            }}
                       }
                        Log.d("123444", "generatePreminum: "+{premiumValue})
//                        if(!check_premium){
//                            Log.d("check_premium", "generatePreminum: "+check_premium)
//                            showAlertNoPremium()
//                        }

                        if (premiumValue?.isNullOrEmpty() == true) {
                            showAlertNoPremium()
                        } else {

                            /* var QuoteNumber: String,
                         var QuoteGeneratedOn: String,
                         var DeviceId: String,
                         var RMID: String,
                         var UserId: String,
                         var UserName: String,
                         var validUntil: String,
                         var status: String,
                         var medicalsRequired: String,
                         var PreminumPaymentTerm: String,
                         var BenefitType: String,
                         var MembersCovered: String,
                         var Loantype: String*/

                            var RMID: String =
                                SharedPref.read(Constants.PREF_RMID, "").toString()
                            var userID: String =
                                SharedPref.read(Constants.PREF_USERID, "").toString()
                            var fullName: String =
                                SharedPref.read(Constants.PREF_FULL_NAME, "").toString()
                            var quoteNumber: String =
                                (System.currentTimeMillis() / 1000L).toString();
                            var currentDate: String = getCurrentDateForm()
                            var validTillDate: String =
                                getValidTillDate() //30 days added for validtill date
                            var MedicalsReqNML = ""
                            MedicalsReqNML = if (NML_BOOL)
                                "YES"
                            else
                                "NO"


                            var premQuote = PremQuote(
                                homeFragInfo.zone.toString(),
                                homeFragInfo.insurer.toString(),
                                homeFragInfo.category.toString(),
                                homeFragInfo.product.toString(),
                                homeFragInfo.funding.toString(),
                                homeFragInfo.gender.toString(),
                                homeFragInfo.loanAmt.toString(),
                                homeFragInfo.coverage.toString(),
                                homeFragInfo.tenure.toString(),
                                homeFragInfo.dob.toString(),
                                homeFragInfo.age.toString(),
                                homeFragInfo.remarks.toString(),
                                homeFragInfo.contractno.toString(),
                                premiumValue,
                                message,
                                premiumCalVersion,
                                quoteNumber,
                                currentDate,
                                "Android",
                                RMID,
                                userID,
                                fullName,
                                validTillDate,
                                "Active",
                                MedicalsReqNML,
                                "Single premium upfront",
                                "Death cover",
                                "Primary borrower",
                                homeFragInfo.loanType.toString(),
                                productType
                            )
                            premiumCalCallMsg.value = Event(premQuote, "premCal")
                        }
                        Log.d("1111111111111111111", "generatePreminum: ")
                    }
                }
           }
        }

    }

    private fun showAlertNoPremium() {
        alertMsg.value = Status.NO_PREMIUM
    }

    private fun showAlertJobMisMatch(
        zone: Boolean,
        insurer: Boolean,
        prodCat: Boolean,
        product: Boolean,
        fundType: Boolean,
        genderType: Boolean
        // dateinRange: Boolean
    ): Boolean {
        when {

            !zone -> {
               // alertMsg.value = Status.ZONE
                return false
            }
            !insurer -> {
               // alertMsg.value = Status.INSURER
                return false
            }
            !prodCat -> {
             //   alertMsg.value = Status.PRODCAT
                return false
            }


            !genderType -> {
                alertMsg.value = Status.Gender
                return false
            }
            !product -> {
               // alertMsg.value = Status.PRODUCT
                return false
            }
            !fundType -> {
              //  alertMsg.value = Status.FUNDTYPE
                return false
            }
            /*  !dateinRange -> {
                  alertMsg.value = Status.DATERANGE
                  return false
              }*/
        }
        return true
    }
/*
    private fun isWithinRange(todayDate: Date?, fromDate: Date?, toDate: Date?): Boolean {
        return !(todayDate!!.before(fromDate) || todayDate!!.after(toDate))
    }*/

    fun insureSpinText(name: String) {
        homeFragInfo.insurer = name
        /*var category: String?= ""
        category="Zone\$" + homeFragInfo.zone +"~Insurer\$" + name?.substring(0, name?.indexOf("(")!!).toString()
        loadCategory(category)*/
    }
fun productTypeText(name:String){
    Log.d("Ppdhf", "productTypeText: "+name)
    productType=name;
}
    fun setCoverSpinText(s: String) {
        Log.e("cover", ">>>>.$s")
        homeFragInfo.coverage = s
        /*for (i in 0..words.length - 1) {
            val word = Word(words.get(i))
            mDao.insert(word)
        }*/
    }

    fun setGenderSpinText(s: String) {
        homeFragInfo.gender = s
    }

    fun getGenderSpinText(): String? {
        return homeFragInfo.gender
    }


    fun setFundingSpinText(s: String) {
        homeFragInfo.funding = s;
    }

    fun prodCatSpinText(name: String) {
        homeFragInfo.category = name
        /*var productcategory: String?=""
        productcategory="Zone\$" + homeFragInfo.zone +"~Insurer\$"+homeFragInfo.insurer?.substring(0, homeFragInfo.insurer?.indexOf("(")!!).toString()+
                "~Category\$"+ name?.substring(0, name?.indexOf("(")!!).toString()
        loadProducts(productcategory)*/
    }

    fun tenureSpinText(name: String) {
        Log.e("tenure", ">>>>.$name")
        homeFragInfo.tenure = name
    }

    fun loanTypeSpinText(name: String) {
        Log.e("lonatype", ">>>>.$name")
        homeFragInfo.loanType = name
    }

    fun prodSpinText(name: String) {
        homeFragInfo.product = name
    }

    fun zoneSpinText(name: String) {
        Log.e("zone", ">>>>.$name")
        homeFragInfo.zone = name
    }

    fun coverageText(name: String) {
        Log.e("coverage", ">>>>.$name")
        homeFragInfo.coverage = name
    }

    fun onLoneTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("ondateTyped", ">>>>>>>>>>$s")
        homeFragInfo.loanAmt = s.toString()
        homeFragInfo.coverage = s.toString()
    }

    fun onTenureAutoTextEntered(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("tenure", ">>>>>>>>>>$s")
        homeFragInfo.tenure = s.toString()
    }

    fun onCoverageTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("onCoverageTyped", ">>>>>>>>>>$s")
        homeFragInfo.coverage = s.toString()
    }


    fun onRemarksTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("ondateTyped", ">>>>>>>>>>$s")
        homeFragInfo.remarks = s.toString()
    }

    fun onContractNoTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("ondateTyped", ">>>>>>>>>>$s")
        homeFragInfo.contractno = s.toString()
    }

    fun onDateTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("ondateTyped", ">>>>>>>>>>$s")
        homeFragInfo.dob = s.toString()
    }


    private fun isWithinRange(
        time: Date?,
        stringToDateConvert: Date?,
        stringToDateConvert1: Date?
    ): Boolean {
        return (time!!.before(stringToDateConvert) || time!!.after(stringToDateConvert1))
    }

    private fun getValidTillDate(): String {   //we are adding 30 days prior here
        return getValidTillDateForm()

    }

    fun getAge(year: Int, month: Int, day: Int): String? {
        val dob: Calendar = Calendar.getInstance()
        val today: Calendar = Calendar.getInstance()
        dob.set(year, month, day)
        var age: Int = today.get(Calendar.YEAR) - year


        if (today.get(Calendar.MONTH) + 1 < month) {
            age--
        }
        if (today.get(Calendar.MONTH) + 1 == month) {
            if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                age--
            }
        }


        val ageInt = age
        /*val ageInt = age + 1*/
        return ageInt.toString()
    }

    fun addAgeDOBToDTO(dob: String, age: String) {
        homeFragInfo.dob = dob
        homeFragInfo.age = age
    }


    /*override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }
*/

    fun inflatetextValues(loan: String?, remark: String?, contractno: String?) {
        homeFragInfo.loanAmt = loan
        homeFragInfo.remarks = remark
        homeFragInfo.contractno = contractno
    }

    fun deleteDBAndGetServerData() {
        // deleteing entire db and inserting new data
        isFromDBDrop = true // temporary adjust to show alert

        subscription = Observable.fromCallable { insurerDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { prodCatDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { productDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })


        subscription = Observable.fromCallable { tenureDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { loanTypeDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { taxDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { calculateDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })
        subscription = Observable.fromCallable { termsDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })
        getUserZoneUpdate(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL,"").toString())

        //calling this api to refresh entire list

    }

    fun logoutdeleteDBAndGetServerData() {
        // deleteing entire db and inserting new data
        isFromDBDrop = true // temporary adjust to show alert

        subscription = Observable.fromCallable { insurerDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { prodCatDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { productDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })


        subscription = Observable.fromCallable { tenureDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { loanTypeDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { taxDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

        subscription = Observable.fromCallable { calculateDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })
        subscription = Observable.fromCallable { termsDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })

    }

    fun postQuoteAPI(jsonData: String, email: String, userid: String) {
        Log.d("JsonData", "postQuoteAPI: $jsonData")
        subscription = postApi.uploadQuote(
            "CAMPAIGNS",
            "Services",
            "Quotes",
            jsonData,
            email, userid

        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onOfflinePostStart() }
            .doOnTerminate { onOfflinePostStop() }
            .subscribe(
                { result -> onOfflinePostSuccess(result) },
                { result -> onOfflinePostFailure(result) })
    }


    private fun onOfflinePostStart() {
        offlineProgressLayout.value = View.VISIBLE
    }

    private fun onOfflinePostStop() {
        offlineProgressLayout.value = View.GONE
    }

    private fun onOfflinePostSuccess(result: ObjResponse) {
        // postListAdapter.updatePostList(postList)
        statusMessage.value = Event(result, "Offline")
    }

    private fun onOfflinePostFailure(result: Throwable) {
        Log.e("this is offline error", ">>>>>>>>>>>> error $result");
        //statusMessageVal.value = Event("hide", "")
    }

}