package com.ongo.aonsecure.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR


class ChangePasswordDTO : BaseObservable() {

    @get:Bindable
    var emailID: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.emailID)
        }

    @get:Bindable
    var oldPassword: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.oldPassword)
        }

    @get:Bindable
    var newPassword: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.newPassword)
        }

    @get:Bindable
    var confirmPassword: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.confirmPassword)
        }

    @get:Bindable
    var rmid: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.rmid)
        }
}