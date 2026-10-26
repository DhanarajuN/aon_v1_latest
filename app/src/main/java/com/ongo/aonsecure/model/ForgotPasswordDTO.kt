package com.ongo.aonsecure.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class ForgotPasswordDTO : BaseObservable() {

    @get:Bindable
    var emailID: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.emailID)
        }

    @get:Bindable
    var rmID: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.rmID)
        }
}