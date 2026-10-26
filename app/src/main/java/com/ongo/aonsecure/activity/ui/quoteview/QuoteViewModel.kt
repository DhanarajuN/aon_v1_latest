package com.ongo.aonsecure.activity.ui.quoteview

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
import com.ongo.aonsecure.utils.SharedPref
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import pdf.app.library.utils.Constants
import javax.inject.Inject

class QuoteViewModel(
    private val insurerDao: InsurerDao,
    private val prodCatDao: ProdCatDao,
    private val productDao: ProductDao,
    private val zoneDao: ZoneDao,
    private val tenureDao: TenureDao,
    private val loanTypeDao: LoanTypeDao,
    private val taxDao: TaxDao,
    private val calculateDao: CalculateDao,
    private val jobDao: JobDao,
   // private val productTypeDao: ProductTypeDao
) : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable

    var isFromDBDrop: Boolean = false
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val offlineProgressLayout: MutableLiveData<Int> = MutableLiveData()


    private val taxValue = MutableLiveData<Event<Any, String>>()
    val taxvalue: LiveData<Event<Any, String>> get() = taxValue

    private val status = SingleLiveEvent<Status>()
    fun getStatus(): LiveData<Status> {
        return status
    }

    private val statusMessageVal = MutableLiveData<Event<String, String>>()
    val messageVal: LiveData<Event<String, String>>
        get() = statusMessageVal

    private val statusMessage = MutableLiveData<Event<Any, String>>()
    val message: LiveData<Event<Any, String>>
        get() = statusMessage


    //checking user zone everytime after user status
    fun getUserZone() {
        subscription =
            postApi.getUserZone(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRetrievePostListStart() }
                //.doOnTerminate { onRetrievePostListFinish() }
                .subscribe(
                    { result -> onZoneChangeSuccess(result) },
                    { result -> onZoneChangeFailure(result) })
    }

    private fun onZoneChangeSuccess(result: ObjResponse) {
        statusMessage.value = Event(result, "userzone")
        checkCalculatePasswordAPI()
    }

    private fun onZoneChangeFailure(result: Throwable) {
        Log.e("this is obj response", ">>>>>>>$result")
    }


    fun getUserStatus() {
        subscription =
            postApi.userStatus(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRetrievePostListStart() }
                // .doOnTerminate { onRetrievePostListFinish() }
                .subscribe(
                    { result -> onStatusSuccess(result) },
                    { result -> onStatusFailure(result) })
    }

    private fun onStatusSuccess(result: Any) {
        statusMessage.value = Event(result, "userstatus")
        getUserZone()
    }

    private fun onStatusFailure(result: Throwable) {
        Log.e("this is obj response", ">>>>>>>$result")
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

    private fun onCalculatePasswordValiditySuccess(obj: ObjResponse) {
        Log.e("this is obj response", ">>>>>>>$obj")
            statusMessage.value= Event(obj,"calculate")
    }

    private fun onCalculatePasswordValidityFailure(result: Throwable?) {

    }

    fun postQuoteAPI(jsonData: JSONObject, email: String, userid: String) {
        Log.d("jsonData:", "postQuoteAPI:$jsonData ")

        subscription = postApi.uploadQuote(
            "CAMPAIGNS",
            "Services",
            "Quotes",
            jsonData.toString(),
            email, userid

        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                { result -> onRetrievePostListError(result) })
    }

    fun postQuoteAPI1(jsonData: String, email: String, userid: String) {
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
                { result -> onRetrievePostListSuccess(result) },
                { result -> onRetrievePostListError(result) })
    }
    private fun onOfflinePostStart() {
        offlineProgressLayout.value = View.VISIBLE
    }

    private fun onOfflinePostStop() {
        offlineProgressLayout.value = View.GONE
    }
    private fun onRetrievePostListStart() {
        statusMessageVal.value = Event("show", "")
        loadingVisibility.value = View.VISIBLE
    }

    private fun onRetrievePostListFinish() {
        statusMessageVal.value = Event("hide", "")
        loadingVisibility.value = View.GONE
    }

    private fun onRetrievePostListSuccess(result: ObjResponse) {
        //postListAdapter.updatePostList(postList)
        Log.e("this is obj response", ">>>>>>>$result")
        //if(result.status == "1") {
        status.value = Status.QUOTE_SUCCESS
        // }

    }

    private fun onRetrievePostListError(error: Throwable) {
        Log.e("this is obj error", ">>>>>>>$error")
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
}