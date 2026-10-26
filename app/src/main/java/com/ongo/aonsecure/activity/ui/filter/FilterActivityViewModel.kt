package com.ongo.aonsecure.activity.ui.filter

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.SingleLiveEvent
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.SharedPref
import com.ongo.aonsecure.utils.tenureArry
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pdf.app.library.utils.Constants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class FilterActivityViewModel (
    private val insurerDao: InsurerDao,
    private val prodCatDao: ProdCatDao,
    private val productDao: ProductDao,
    private val tenureDao: TenureDao,
    private val loanTypeDao: LoanTypeDao,
    private val calculateDao: CalculateDao,
    private val jobDao: JobDao,
   // private val productTypeDao: ProductTypeDao
) : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    val homeFragInfo = HomeFragInfo()
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

    private val statusMessage = MutableLiveData<Event<Any, String>>()
    val message: LiveData<Event<Any, String>>
        get() = statusMessage

    private val premiumCalCallMsg = MutableLiveData<Event<Any, String>>()
    private var NML_BOOL: Boolean = false

    private val statusMessageVal = MutableLiveData<Event<String, String>>()

    val messageVal: LiveData<Event<String, String>>
        get() = statusMessageVal


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
        loadInsurer()
    }

    fun setCoverSpinText(s: String) {
        Log.e("cover", ">>>>.$s")
        homeFragInfo.coverage = s
    }

    fun setGenderSpinText(s: String) {
        homeFragInfo.gender = s
    }

    fun setFundingSpinText(s: String) {
        homeFragInfo.funding = s;
    }

    fun prodCatSpinText(name: String) {
        homeFragInfo.category = name
    }

    fun tenureSpinText(name: String) {
        Log.e("tenure", ">>>>.$name")
        homeFragInfo.tenure = name
    }

    fun prodSpinText(name: String) {
        homeFragInfo.product = name
    }

    fun insureSpinText(name: String) {
        homeFragInfo.insurer = name
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

    fun loanTypeSpinText(name: String) {
        homeFragInfo.loanType=name.toString()
    }


    fun onTenureAutoTextEntered(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("tenure", ">>>>>>>>>>$s")
        homeFragInfo.tenure = s.toString()
    }

    fun onCoverageTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("onCoverageTyped", ">>>>>>>>>>$s")
        homeFragInfo.coverage = s.toString()
    }


    fun onContractNoTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("ondateTyped", ">>>>>>>>>>$s")
        homeFragInfo.contractno = s.toString()
    }

    fun onDateTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        Log.e("ondateTyped", ">>>>>>>>>>$s")
        homeFragInfo.dob = s.toString()
    }

    private val disposable = CompositeDisposable()

    private fun ondataResult(result: Job) {
        Log.e("result is", ">>>>>>>>>>$result")
    }

    private fun onQueryResult(result: List<List<Job>>) {

    }

    private fun onCalculatePremiumClicked(result: Any?) {

    }

    private fun isTenureMatchedWithArray(tenureStr: String): Boolean {
        return tenureStr in tenureArry
    }

    fun stringToDateConvert(date: String): Date? {
        var myDate: Date? = null
        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        try {
            myDate = dateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return myDate
    }

    private fun isWithinRange(todayDate: Date?, fromDate: Date?, toDate: Date?): Boolean {
        return !(todayDate!!.before(fromDate) || todayDate!!.after(toDate))
    }

    fun getAge(year: Int, month: Int, day: Int): String? {
        val dob: Calendar = Calendar.getInstance()
        val today: Calendar = Calendar.getInstance()
        dob.set(year, month, day)
        var age: Int = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        val ageInt = age
        return ageInt.toString()
    }

    fun addAgeDOBToDTO(dob: String, age: String) {
        homeFragInfo.dob = dob
        homeFragInfo.age = age
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

    private fun onCategoryError(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
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
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onProductSuccess(result) },
                { result -> onProductPostListError(result) }
            )

    }

    private fun onProductSuccess(result: Any) {
        statusMessage.value = Event(result, "Product")
    }


    private fun onProductPostListError(result: Throwable?) {
        statusMessageVal.value = Event("hide", "")
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
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
        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
        statusMessageVal.value = Event("hide", "")
    }

    /*override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }*/

    fun inflatetextValues(loan: String?, remark: String?) {
        homeFragInfo.loanAmt = loan
        homeFragInfo.remarks = remark
    }

    fun deleteDBAndGetServerData() {
        //deleteing entire db and inserting new data

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

        subscription = Observable.fromCallable { calculateDao.deleteAllNotes() }
            .subscribeOn(Schedulers.io()).subscribe({ result ->

            }, { result -> onRetrievePostListError(result) })


        //calling this api to refresh entire list
        //loadInsurer()
    }



}