package com.ongo.aonsecure.activity.ui.quote_list

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.SingleLiveEvent
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.BASE_URL
import com.ongo.aonsecure.utils.SharedPref
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pdf.app.library.utils.Constants
import java.util.*
import javax.inject.Inject

class QuoteListViewModel(
    private val insurerDao: InsurerDao,
    private val prodCatDao: ProdCatDao,
    private val productDao: ProductDao,
    private val tenureDao: TenureDao,
    private val loanTypeDao: LoanTypeDao,
    private val taxDao: TaxDao,
    private val calculateDao: CalculateDao,
  //  private val productTypeDao: ProductTypeDao
) : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi

    // val postListAdapter: PostListAdapter = PostListAdapter()
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

    private val alertMsg = SingleLiveEvent<Status>()
    fun getAlertMsg(): LiveData<Status> {
        return alertMsg
    }

    private lateinit var subscription: Disposable

    private val statusMessage = MutableLiveData<Event<Any, String>>()
    private val premiumCalCallMsg = MutableLiveData<Event<Any, String>>()
    val premiumcal: LiveData<Event<Any, String>> get() = premiumCalCallMsg

    private val noInternetCall = MutableLiveData<Event<Any, String>>()
    val noInternet: LiveData<Event<Any, String>> get() = noInternetCall
    val message: LiveData<Event<Any, String>>
        get() = statusMessage


    private val statusMessageVal = MutableLiveData<Event<String, String>>()
    private val zonesMessage = MutableLiveData<Event<Any, String>>()
    val zone: LiveData<Event<Any, String>>
        get() = zonesMessage
    private val passwordMessage = MutableLiveData<Event<Any, String>>()
    val password: LiveData<Event<Any, String>>
        get() = passwordMessage
    val messageVal: LiveData<Event<String, String>>
        get() = statusMessageVal
    var callServerSyncAPI: Boolean = false
    var isFromDBDrop: Boolean = false
    val offlineProgressLayout: MutableLiveData<Int> = MutableLiveData()


/*    init {
        loadPosts()
    }*/

    override fun onCleared() {
        super.onCleared()
        if (this::subscription.isInitialized)
            subscription.dispose()
    }


    fun getUserStatus(email: String){
        subscription=postApi.userStatus(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onStatusSuccess(result) },
                { result -> onStatusFailure(result) })
    }

    private fun onStatusSuccess(result: Any) {
        // SharedPref.write(Constants.PREF_USER_STATUS, result?.status.toString())
        premiumCalCallMsg.value= Event(result,"userstatus")
    }

    private fun onStatusFailure(result: Throwable) {

    }


    fun loadPosts() {
        subscription = postApi.quoteList(
            "Quotes",
            SharedPref.read(Constants.PREF_USERID, "")
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                { result -> onRetrievePostListError(result) })
    }





    private fun onRetrievePostListSuccess(postList: QuoteList) {
        //  Log.e("quotelist", ">>>>>>>${postList}")
         //  postListAdapter.updatePostList(postList)

        premiumCalCallMsg.value = Event(postList, "adapter")
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


    private fun onFirstStatusFailure(result: Throwable) {
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result")
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
        if((obj.isPasswordChanged=="False")){
            passwordMessage.value = Event(obj, "passwordChange")
        }
        if ((obj.isServerSync=="False") || obj.isServerSync.isEmpty()) { //server auto sync
            callServerSyncAPI = true
            deleteDBAndGetServerData()
        } else if (obj.isServerSync=="True") { //from db load data
            callServerSyncAPI = false
            loadInsurer()
        }
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
        loadTenure()
    }

    private fun onInsurerFailure(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
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
        loadLoanType()
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
        statusMessage.value = Event(result, "Product")
        loadTax()
    }


    private fun onProductPostListError(result: Throwable?) {
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
        subscription = Observable.fromCallable { calculateDao.all(SharedPref.read(Constants.PREF_ZONE, "").toString()) }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getCalculator().concatMap { apiPostList ->
                        Log.d("api", "1222222222222 ${apiPostList}")

                        val jobs: List<Calculate1> = apiPostList.jobs
                        jobs.forEach { job ->
                            Log.d("api", "ProductType from API: " + job.productType  )  // Log the ProductType value from API

                            calculateDao.insert(job)
                            Log.d("1222222222222", "calculatePremium 434: "+job.productType   +job.Type)
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
    private fun onCategoryError(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
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


        //calling this api to refresh entire list
        loadInsurer()
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

    }
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
        }

    }

    private fun onCalculatePasswordValidityFailure(result: Throwable?) {

    }

}