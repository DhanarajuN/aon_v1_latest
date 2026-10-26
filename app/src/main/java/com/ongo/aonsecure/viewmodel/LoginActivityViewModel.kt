package com.ongo.aonsecure.viewmodel

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.activity.*
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.events.ActivityEvent
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.NavLiveEvent
import com.ongo.aonsecure.id.SingleLiveEvent
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.PREF_USER_PASSWORD_EXPIRED
import com.ongo.aonsecure.utils.SharedPref
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pdf.app.library.utils.Constants
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivityViewModel(
    private val termsDao: TermsDao) : BaseViewModel() {

    private val loginInfo = LoginInfo("", "")
    val navEventSample = NavLiveEvent()
    private val status = SingleLiveEvent<Status>()
    var termsLink="";

    val loginEventSample = NavLiveEvent()
    private val status_login = SingleLiveEvent<Status>()
    var generatedIMEI: String? = ""

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val isButtonEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

    private val statusMessage = MutableLiveData<Event<String, String>>()
    private val statusMessage1 = MutableLiveData<Event<Any, String>>()
    val terms: LiveData<Event<Any, String>>
        get() = statusMessage1

    val message: LiveData<Event<String, String>>
        get() = statusMessage

    fun getStatus(): LiveData<Status> {
        return status
    }

    fun setUserName(name: String) {
        loginInfo.userName = name
    }

    fun setPassword(password: String) {
        loginInfo.password = password
    }
    fun setTerms(boolean: Boolean){
        //loginInfo.isTermsChecked=boolean
       // Log.d("ttttttt", "setTerms: ${loginInfo.isTermsChecked}")
    }

    fun onUserNameTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        loginInfo.userName = s.toString()
    }

    fun onPasswordTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        loginInfo.password = s.toString()
    }
    fun onTermsCheckedChanged(isChecked: Boolean) {
        loginInfo.termsChecked = isChecked
        // Do any additional processing based on the checked state
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun submitButtonClick() {
        Log.e("username & password", ">??? ${loginInfo.userName}, ${loginInfo.password} ${loginInfo.termsChecked}")

            //calling next screen
            status.value = Status.REMOVE_ERRORS
            // launchActivityEventSample() //TODO:  uncomment below and delete this line
            if (validateCredentials(
                    loginInfo.userName.toString(),
                    loginInfo.password.toString()
                   // loginInfo.termsChecked
                )
            ) {

                if (SharedPref.read(PREF_USER_PASSWORD_EXPIRED, "")?.isNotEmpty()!!) {
                    var password = SharedPref.read(PREF_USER_PASSWORD_EXPIRED, "").toString()
                    if (password == "passwordexpired") {
                        changePassword()
                    } else {
                        if (isButtonEnabled.value == true) {
                            // Disable the button to prevent multiple clicks
                            isButtonEnabled.value = false
                        loginAPI()}
                    }
                } else {
                    if (isButtonEnabled.value == true) {
                        // Disable the button to prevent multiple clicks
                        isButtonEnabled.value = false
                        loginAPI()}
                }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun signupButtonClick() {
        //   Log.e("username & password", ">??? ${loginInfo.userName}, ${loginInfo.password} ")
        //calling next screen
        signUpIntentEvent()
    }
fun termsConditionsClick(){
    val bundle = Bundle()
    if (!termsLink.isNullOrEmpty()){
    bundle.putString("URL", termsLink)
    navEventSample.value =
        ActivityEvent(
            WebActivity::class.java,
            bundle
        )
    }
}

    fun changeDeviceClick() {
        //Change handset/device request screen
        changeDeviceIntentEvent()
    }

//     fun termsEnanble() {
//        subscription = Observable.fromCallable { termsDao.all }
//            .flatMap { dbPostList ->
//                if (dbPostList.isEmpty()) {
//                    postApi.checkTerms().concatMap { apiPostlist ->
//                        termsDao.insertAll(apiPostlist)
//                        Observable.just(apiPostlist)
//                    }
//                } else {
//                    Observable.just(dbPostList)
//                }
//            }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                { result -> onTermsSuccess(result) },
//                { result -> onTermsError(result) })
//
//    }
//    private fun onTermsSuccess(result: Any) {
//
//        statusMessage1.value = Event(result, "Terms")
//        Log.d("testing", "terms: "+result)
//    }
//
//    private fun onTermsError(result: Throwable?) {
//        //statusMessageVal.value = Event("hide", "")
//        Log.d("testing1", "terms: "+result)
//
//        Log.e("this is homeviewmodel", ">>>>>>>>>>>> error $result");
//    }
fun getTermsApi() {
        subscription = postApi.checkTerms()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess1(result) },
                { result -> onRetrievePostListError1(result) })
    }

    private fun onRetrievePostListSuccess1(response: TermsConfig) {
        //postListAdapter.updatePostList(postList)

        if (response.status == 1) {
            Log.d("this is obj pass config", ">>>>>>>$response.")
            var terms=response.jobs[0].IS_TERMS_ENABLE
            termsLink=response.jobs[0].TERMS_LINK


            //sending the value to view for setting the note value
            statusMessage1.value = Event(response, "")

        } else if (response.status == -1) {
            Log.d("this is obj failed", ">>>>>>> -1 $response")
            //status.value = Status.ERROR
            //status.value = Status.ERROR
            //statusMessage.value = Event(response.jobs.get(0)., "")
            //TODO: Remove this if api works properly
            //launchActivityEventSample()
        }

    }

    private fun onRetrievePostListError1(error: Throwable) {
        Log.e("this is obj error", ">>>>>>>$error")
      //  status.value = Status.ERROR
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loginAPI() {

        subscription = postApi.loginuser(
            "3",
            loginInfo.userName,
            loginInfo.password,
            generatedIMEI
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                { result -> onRetrievePostListError(result) })
    }

    fun setImei(deviceId: String?) {
        generatedIMEI = deviceId
    }

    private fun onRetrievePostListStart() {
        loadingVisibility.value = View.VISIBLE

    }

    private fun onRetrievePostListFinish() {
        isButtonEnabled.value = true
        loadingVisibility.value = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onRetrievePostListSuccess(obj: ObjResponse) {
        //postListAdapter.updatePostList(postList)

        Log.e("this is obj response", ">>>>>>>$obj")
        if (obj.status == "1") {

            SharedPref.write(Constants.PREF_USERID, obj.UserId.toString())
            SharedPref.write(Constants.PREF_FULL_NAME, obj.FullName)
            SharedPref.write(Constants.PREF_MOBILE, obj.mobileNo)
            SharedPref.write(Constants.PREF_IMEI, obj.IMEI)
            SharedPref.write(Constants.PREF_ZONE, obj.Zone)
            SharedPref.write(Constants.PREF_RMID, obj.RMID)
            SharedPref.write(Constants.PREF_TOKEN,obj.accessToken)
            //    SharedPref.write(Constants.PREF_USER_STATUS, obj.userStatus)

            if (obj.isLogin.isEmpty() || obj.isLogin == "False")
                changePassword() //force user to change password
            else
                launchActivityEventSample()
        } else if (obj.status == "-1") {
            Log.e("this is obj failed", ">>>>>>> -1 $obj")
            //status.value = Status.ERROR
            //status.value = Status.ERROR
            statusMessage.value = Event(obj.msg, "")
            //TODO: Remove this if api works properly
            //launchActivityEventSample()
        }

    }

    private fun onRetrievePostListError(error: Throwable) {
        Log.e("this is obj error", ">>>>>>>$error")
        status.value = Status.ERROR
    }

    private fun validateCredentials(
        emailID: String,
        password: String
       // terms:Boolean
    ): Boolean {
        if (emailID.isEmpty()) {
            status.value = Status.EMPTY_EMAIL
            return false
        } else if (!isEmailValid(emailID)) {
            status.value = Status.INVALID_EMAIL
            return false
        } else if (password.isEmpty()) {
            status.value = Status.EMPTY_PASSWORD
            return false
        } else if (password.length < 6) {
            status.value = Status.INVALID_PASSWORD
            return false}
//        }else if(!terms){
//            status.value=Status.TERMS
//            return false
//        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }


    /**
     * Launch an Activity via the NavLiveEvent. This will leverage the ActivityEvent.
     * This event takes in the Activity class and data so that the Observer
     * can simply launch the Activity without having to contain logic of its own.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun launchActivityEventSample() {

        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        loginEventSample.value = ActivityEvent(MainActivity::class.java, bundle)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun signUpIntentEvent() {
        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        navEventSample.value =
            ActivityEvent(
                SignUpActivity::class.java,
                bundle
            )
    }

    private fun changeDeviceIntentEvent() {
        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        navEventSample.value =
            ActivityEvent(
                ChangeRequestActivity::class.java,
                bundle
            )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changePassword() {
        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        navEventSample.value =
            ActivityEvent(
                ChangePasswordActivity::class.java,
                bundle
            )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun forgotPassword() {
        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        navEventSample.value =
            ActivityEvent(
                ForgotPasswordActivity::class.java,
                bundle
            )
    }
}