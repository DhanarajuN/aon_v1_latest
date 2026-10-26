package com.ongo.aonsecure.viewmodel

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.activity.LoginActivity
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.events.ActivityEvent
import com.ongo.aonsecure.id.Event
import com.ongo.aonsecure.id.NavLiveEvent
import com.ongo.aonsecure.id.SingleLiveEvent
import com.ongo.aonsecure.id.Status
import com.ongo.aonsecure.model.ObjResponse
import com.ongo.aonsecure.model.SignUpInfo
import com.ongo.aonsecure.network.PostApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

class ChangeRequestActivityViewModel : BaseViewModel() {

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable

    private val statusMessage = MutableLiveData<Event<String, String>>()

    val message: LiveData<Event<String, String>>
        get() = statusMessage

    private val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    private val status = SingleLiveEvent<Status>()


    private val signUpInfo = SignUpInfo()
    val navEventSample = NavLiveEvent()

    fun getStatus(): LiveData<Status> {
        return status
    }

    fun onUserNameTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        signUpInfo.userName = s.toString()
    }

    fun onMobileNumberTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        signUpInfo.mobileNo = s.toString()
    }

    fun onEmailTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        signUpInfo.emailID = s.toString()
    }

    fun onRMIDTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        signUpInfo.rmid = s.toString()
    }

    fun onDeviceTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
       signUpInfo.deviceType = s.toString()
    }
    fun setDeviceType(s: String) {
        signUpInfo.deviceType = s
    }


    fun onImeiTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        signUpInfo.imei = s.toString()
    }

    fun submitButtonClick() {
        Log.e("username & password", ">??? ${signUpInfo.userName}, ${signUpInfo.mobileNo} ")
        /**** clearing error on every button click ****/
        status.value = Status.REMOVE_ERRORS
        if (validateCredentials(
                signUpInfo.emailID.toString(),
                signUpInfo.mobileNo.toString(),
                signUpInfo.userName.toString(),
                signUpInfo.rmid.toString()
            )
        ) {
            postChangeRequestAPI()
        }
        //launchActivityEventSample()
    }

    private fun validateCredentials(
        emailID: String,
        mobile: String,
        name: String,
        rmid: String
    ): Boolean {
        /*if (name.isEmpty()) {
            status.value = Status.EMPTY_FIRST_NAME
            return false
        } else*/ if (mobile.isEmpty()) {
            status.value = Status.EMPTY_MOBILE
            return false
        } else if (!isValidMobile(mobile)) {
            status.value = Status.INVALID_MOBILE
            return false
        } else if (emailID.isEmpty()) {
            status.value = Status.EMPTY_EMAIL
            return false
        } else if (!isEmailValid(emailID)) {
            status.value = Status.INVALID_EMAIL
            return false
        } else if (rmid.isEmpty()) {
            status.value = Status.EMPTY_RMID
            return false
        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val expression = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\\\S+\$).{4,}\$";
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }

    private fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length == 10
        } else false
    }

    private fun postChangeRequestAPI() {
        subscription = postApi.changeRequest(
            signUpInfo.emailID,
            signUpInfo.mobileNo,
            signUpInfo.rmid,
            signUpInfo.imei
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                { result -> onRetrievePostListError(result) })
    }

    private fun onRetrievePostListStart() {
        loadingVisibility.value = View.VISIBLE
    }

    private fun onRetrievePostListFinish() {
        loadingVisibility.value = View.GONE
    }

    private fun onRetrievePostListSuccess(obj: ObjResponse) {
        //postListAdapter.updatePostList(postList)
        Log.e("this is obj response", ">>>>>>>$obj")
        if (obj.status == "1") {
            launchActivityEventSample()
            status.value = Status.SUCCESS
        } else if (obj.status == "-1") {
            Log.e("this is obj failed", ">>>>>>>$obj")
            //status.value = Status.ERROR
            statusMessage.value = Event(obj.msg, "")
        }
    }

    private fun onRetrievePostListError(error: Throwable) {
        Log.e("this is obj error", ">>>>>>>$error")
    }

    /**
     * Launch an Activity via the NavLiveEvent. This will leverage the ActivityEvent.
     * This event takes in the Activity class and data so that the Observer
     * can simply launch the Activity without having to contain logic of its own.
     */
    private fun launchActivityEventSample() {
        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        navEventSample.value =
            ActivityEvent(
                LoginActivity::class.java,
                bundle
            )
    }

    fun setImei(deviceId: String?) {
        Log.e("devide imei", ">>>>>>>${signUpInfo.imei}")
        signUpInfo.imei = deviceId
    }

}