package com.ongo.aonsecure.activity.ui.help

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.model.ObjResponse
import com.ongo.aonsecure.network.PostApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HelpViewModel : BaseViewModel() {

    /*private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text*/

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    val offlineProgressLayout: MutableLiveData<Int> = MutableLiveData()

    private val statusMessage = MutableLiveData<Event<Any, String>>()
    val message: LiveData<Event<Any, String>>
        get() = statusMessage

    private val statusMessageVal = MutableLiveData<Event<Any, String>>()
    val messageVal: LiveData<Event<Any, String>>
        get() = statusMessageVal

    fun getUserStatus(email: String){
        subscription=postApi.userStatus(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onOfflinePostStart() }
            .doOnTerminate { onOfflinePostStop() }
            .subscribe(
                { result -> onStatusSuccess(result) },
                { result -> onStatusFailure(result) })
    }

    private fun onStatusSuccess(result: Any) {
        // SharedPref.write(Constants.PREF_USER_STATUS, result?.status.toString())
        statusMessageVal.value= Event(result,"userstatus")
    }

    private fun onStatusFailure(result: Throwable) {

    }



    fun postQuoteAPI(jsonData: String, email: String, userid: String) {
        Log.d("jsonData:", "postQuoteAPI:$jsonData ")
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
        statusMessage.value = Event(result, "Offline")
        Log.d("offlineCje", "onOfflinePostSuccess: $")
    }

    private fun onOfflinePostFailure(result: Throwable) {
        Log.e("this is offline error", ">>>>>>>>>>>> error $result");
        //statusMessageVal.value = Event("hide", "")
    }

}