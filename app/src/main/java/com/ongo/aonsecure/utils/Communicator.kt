package com.ongo.aonsecure.utils

import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel

class Communicator : BaseViewModel() {

    val message = MutableLiveData<Any>()

    fun setMsgCommunicator(msg: String) {
        message.setValue(msg)
    }
}