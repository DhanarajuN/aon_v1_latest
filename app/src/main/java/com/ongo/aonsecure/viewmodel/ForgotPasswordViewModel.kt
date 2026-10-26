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
import com.ongo.aonsecure.model.ForgotPasswordDTO
import com.ongo.aonsecure.model.ObjResponse
import com.ongo.aonsecure.network.PostApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

class ForgotPasswordViewModel : BaseViewModel() {
    val navEventSample = NavLiveEvent()
    private val status = SingleLiveEvent<Status>()
    fun getStatus(): LiveData<Status> {
        return status
    }
    var forgotPasswordDTO = ForgotPasswordDTO()

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

    private val statusMessage = MutableLiveData<Event<Any, String>>()

    val message: LiveData<Event<Any, String>>
        get() = statusMessage

    fun onEmailTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        forgotPasswordDTO.emailID = s.toString()
    }

    fun onRMIDTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        forgotPasswordDTO.rmID = s.toString()
    }


    fun onSubmitClicked() {
        /**** clearing error on every button click ****/
        status.value = Status.REMOVE_ERRORS
        if (validateCredentials(
                forgotPasswordDTO.emailID.toString(),
                forgotPasswordDTO.rmID.toString()
            )
        ) {
            changePasswordAPI()
        }
        //launchActivityEventSample()
    }

    private fun changePasswordAPI() {
        subscription = postApi.forgotPassword(
            forgotPasswordDTO.emailID,
            forgotPasswordDTO.rmID
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

    private fun onRetrievePostListError(error: Throwable) {
        Log.e("this is obj error", ">>>>>>>$error")
    }

    private fun onRetrievePostListSuccess(obj: ObjResponse) {
        //postListAdapter.updatePostList(postList)
        Log.e("this is obj response", ">>>>>>>$obj")
        if (obj.status == "1") {
            launchActivityEventSample()
            status.value = Status.SUCCESS
        } else if (obj.status == "-1") {
            Log.e("this is obj failed", ">>>>>>>$obj")
            // status.value = Status.ERROR
            statusMessage.value = Event(obj.msg, "")
        }
    }

    private fun launchActivityEventSample() {
        val bundle = Bundle()
        bundle.putString("arg1", "value1")
        navEventSample.value =
            ActivityEvent(
                LoginActivity::class.java,
                bundle
            )
    }

    private fun validateCredentials(
        emailID: String,
        rmID: String
    ): Boolean {
        if (emailID.isEmpty()) {
            status.value = Status.EMPTY_EMAIL
            return false
        } else if (!isEmailValid(emailID)) {
            status.value = Status.INVALID_EMAIL
            return false
        } else if (rmID.isEmpty()) {
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

}