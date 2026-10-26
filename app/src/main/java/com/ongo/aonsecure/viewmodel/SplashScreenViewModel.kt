package com.ongo.aonsecure.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.SharedPref
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pdf.app.library.utils.Constants
import javax.inject.Inject

class SplashScreenViewModel(
    private val insurerDao: InsurerDao,
    private val prodCatDao: ProdCatDao,
    private val productDao: ProductDao,
    private val zoneDao: ZoneDao,
    private val tenureDao: TenureDao,
    private val loanTypeDao: LoanTypeDao,
    private val taxDao: TaxDao,
    private val calculateDao: CalculateDao,
 //   private val productTypeDao: ProductTypeDao,
    private val jobDao: JobDao ) : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    var isFromDBDrop: Boolean = false

    private val status = MutableLiveData<Event<Any, String>>()

    val userStatus: LiveData<Event<Any, String>> get() = status


    fun getUserStatus(email: String){
        subscription=postApi.userStatus(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //.doOnSubscribe { onRetrievePostListStart() }
            //  .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onStatusSuccess(result) },
                { result -> onStatusFailure(result) })
    }

    private fun onStatusSuccess(result: Any) {
        // SharedPref.write(Constants.PREF_USER_STATUS, result?.status.toString())
        status.value= Event(result,"userstatus")
        getUserZone(SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL,"").toString())
    }


    fun getUserZone(email: String) {
        subscription = postApi.getUserZone(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> onStatusSuccess(result) },
                { result -> onStatusFailure(result) })
    }

    private fun onStatusSuccess(result: ObjResponse) {
        status.value = Event(result, "userzone")

    }

    private fun onStatusFailure(result: Throwable) {

        Log.i(">>>", result.localizedMessage)
    }

    private fun onRetrievePostListError(result: Throwable) {
        // statusMessageVal.value = Event("hide", "")
        Log.e("this is SplashViewModel", ">>>>>>>>>>>> error $result");
        //  loadingVisibility.value = View.GONE
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
      //  (this as SplashScreenActivity).splash()
    }

}