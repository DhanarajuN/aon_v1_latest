package com.ongo.aonsecure.activity.ui.quoteview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.model.TaxDao
import com.ongo.aonsecure.network.PostApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TaxViewModel(private var taxDao: TaxDao) : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

    private val taxValue = MutableLiveData<Event<Any, String>>()

    private val statusMessageVal = MutableLiveData<Event<String, String>>()
    val messageVal: LiveData<Event<String, String>>
        get() = statusMessageVal

   val taxvalue: LiveData<Event<Any, String>> get() = taxValue

    fun getTax(date: String) {
        subscription = Observable.fromCallable { taxDao.all }
            .flatMap { dbPostList ->
                if (dbPostList.isEmpty())
                    postApi.getTax(date).concatMap { apiPostList ->
                        taxDao.insertAll(apiPostList)
                        Observable.just(apiPostList)
                    }
                else
                    Observable.just(dbPostList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
           // .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onTaxSuccess(result) },
                { result -> onTaxFailure(result) })
    }

   /* private fun onRetrievePostListFinish() {
        statusMessageVal.value = Event("hide", "")
    }*/

    private fun onRetrievePostListStart() {
        statusMessageVal.value = Event("show", "")
    }

    private fun onTaxSuccess(result: Any) {
        Log.e("Result",">>>>>>>$result")
        taxValue.value= Event(result,"taxval")
        statusMessageVal.value = Event("hide", "")
    }

    private fun onTaxFailure(result: Throwable?) {

    }

}
