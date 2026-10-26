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
import com.ongo.aonsecure.model.ChangePasswordDTO
import com.ongo.aonsecure.model.ObjResponse
import com.ongo.aonsecure.model.PasswordConfig
import com.ongo.aonsecure.network.PostApi
import com.ongo.aonsecure.utils.PREF_USER_PASSWORD_EXPIRED
import com.ongo.aonsecure.utils.SharedPref
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class ChangePasswordViewModel : BaseViewModel() {

    val navEventSample = NavLiveEvent()
    private val status = SingleLiveEvent<Status>()
    fun getStatus(): LiveData<Status> {
        return status
    }
    var changePasswordDTO = ChangePasswordDTO()

    var passwordLength = 0
    var upperCaseLimit = 0
    var specialCharactersLimit = 0
    var notAllowedChars = ""

    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

    private val statusMessage = MutableLiveData<Event<Any, String>>()


    val message: LiveData<Event<Any, String>>
        get() = statusMessage

    fun onOldPasswordTyped(s: CharSequence, start: Int, befor: Int, count: Int) {

        changePasswordDTO.oldPassword = s.toString()
    }

    fun onNewPasswordTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        changePasswordDTO.newPassword = s.toString()
    }

    fun onConfirmPasswordTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        changePasswordDTO.confirmPassword = s.toString()
    }

    fun onEmailTyped(s: CharSequence, start: Int, befor: Int, count: Int) {
        changePasswordDTO.emailID = s.toString()
    }

    init {
        getPasswordConfigurationAPI()
    }

    fun onSubmitClicked() {
        if (validateValues()) {
            changePasswordAPI()
        }
    }

    private fun validateValues(): Boolean {
        status.value = Status.REMOVE_ERRORS
        if (changePasswordDTO.emailID?.isEmpty()!!) {
            status.value = Status.EMPTY_EMAIL
            return false
        } else if (!isEmailValid(changePasswordDTO.emailID!!)) {
            status.value = Status.INVALID_EMAIL
            return false
        } else if (changePasswordDTO.oldPassword?.isEmpty()!!) {
            status.value = Status.OLD_PASSWORD_EMPTY
            return false
        } else if (changePasswordDTO.newPassword?.isEmpty()!!) {
            status.value = Status.NEW_PASSWORD_EMPTY
            return false
        }
        /*else if (!isValidPassword(changePasswordDTO.newPassword)) { //ToDo: as per brd we have removed this..No NUMBER VALIDATION
            status.value = Status.NEW_PASSWORD_INVALID
            return false
        }*/
        else if (changePasswordDTO.newPassword!!.contains(notAllowedChars, ignoreCase = false)) {
            status.value = Status.NOT_ALLOWED_SPECIAL_CHARS
            return false
        } else if (charsCount(changePasswordDTO.newPassword!!) != null) {
            var charCountvalue = charsCount(changePasswordDTO.newPassword!!)
            if (charCountvalue.upper < upperCaseLimit) {
                status.value = Status.UPPERCASE_INVALID
                return false
            } else if (charCountvalue.specialChars < specialCharactersLimit) {
                status.value = Status.SPECIAL_CHARS_INVALID
                return false
            }
        }
        if (changePasswordDTO.newPassword?.length!! < passwordLength) {
            status.value = Status.PASSWORD_LENGTH_INVALID
            return false
        }
        if (changePasswordDTO.confirmPassword?.isEmpty()!!) {
            status.value = Status.CONFIRM_PASSWORD_EMPTY
            return false
        } else if (changePasswordDTO.confirmPassword.toString() != changePasswordDTO.newPassword.toString()) {
            status.value = Status.CONFIRM_PASSWORD_MISMATCH
            return false
        }
        return true
    }

    private fun changePasswordAPI() {
        subscription = postApi.changePassword(
            changePasswordDTO.emailID,
            changePasswordDTO.oldPassword,
            changePasswordDTO.newPassword,
            changePasswordDTO.confirmPassword,
            "True",
            "True"
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
            SharedPref.write(PREF_USER_PASSWORD_EXPIRED,"")
            status.value = Status.SUCCESS
        } else if (obj.status == "-1") {
            Log.e("this is obj failed", ">>>>>>>$obj")
            // status.value = Status.ERROR
            statusMessage.value = Event(obj.msg, "")
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    data class CharCount(var upper: Int, var lower: Int, var number: Int, var specialChars: Int)

    private fun charsCount(password: String?): CharCount {

        var upper = 0
        var lower = 0
        var number = 0
        var special = 0

        for (element in password!!) {
            when (element) {
                in 'A'..'Z' -> upper++
                in 'a'..'z' -> lower++
                in '0'..'9' -> number++
                else -> special++
            }
        }

        return CharCount(upper, lower, number, special)

    }

    private fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    private fun getPasswordConfigurationAPI() {
        subscription = postApi.getPasswordConfig()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                { result -> onRetrievePostListError(result) })
    }

    private fun onRetrievePostListSuccess(response: PasswordConfig) {
        //postListAdapter.updatePostList(postList)

        if (response.status == 1) {
            Log.e("this is obj pass config", ">>>>>>>$response.")
            passwordLength = response.jobs[0].LengthOfPassword.toInt()
            upperCaseLimit = response.jobs[0].UpperCase.toInt()
            specialCharactersLimit = response.jobs[0].SpecialCharacter.toInt()
            notAllowedChars = response.jobs[0].NotAllowed

            //sending the value to view for setting the note value
            statusMessage.value = Event(response, "")

        } else if (response.status == -1) {
            Log.e("this is obj failed", ">>>>>>> -1 $response")
            //status.value = Status.ERROR
            //status.value = Status.ERROR
            //statusMessage.value = Event(response.jobs.get(0)., "")
            //TODO: Remove this if api works properly
            //launchActivityEventSample()
        }

    }

    private fun onRetrievePostListError(error: Throwable) {
        Log.e("this is obj error", ">>>>>>>$error")
        status.value = Status.ERROR
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
}