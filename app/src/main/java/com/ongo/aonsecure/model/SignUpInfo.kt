package com.ongo.aonsecure.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class SignUpInfo : BaseObservable() {
    @get:Bindable
    var userName: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.userName)
        }

    @get:Bindable
    var mobileNo: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.mobileNo)
        }

    @get:Bindable
    var emailID: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.emailID)
        }

    @get:Bindable
    var rmid: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.rmid)
        }

    @get:Bindable
    var deviceType: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.deviceType)
        }

    @get:Bindable
    var imei: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.imei)
        }

}