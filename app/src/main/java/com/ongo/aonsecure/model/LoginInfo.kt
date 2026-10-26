package com.ongo.aonsecure.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

data class LoginInfo(
    private var _userName: String,
    private var _password: String,
    private var _termsChecked: Boolean = false
) : BaseObservable() {

    var userName: String
        @Bindable get() = _userName
        set(value) {
            _userName = value
            notifyPropertyChanged(BR.userName)
        }

    var password: String
        @Bindable get() = _password
        set(value) {
            _password = value
            notifyPropertyChanged(BR.password)
        }

    var termsChecked: Boolean
        @Bindable get() = _termsChecked
        set(value) {
            _termsChecked = value
            notifyPropertyChanged(BR.termsChecked)
        }
}
