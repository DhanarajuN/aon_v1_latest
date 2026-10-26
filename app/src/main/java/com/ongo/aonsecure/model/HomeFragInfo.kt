package com.ongo.aonsecure.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class HomeFragInfo : BaseObservable() {

    @get:Bindable
    var insurer: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.insurer)
        }

    @get:Bindable
    var zone: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.zone)
        }

    @get:Bindable
    var category: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.category)
        }

    @get:Bindable
    var
            product: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.product)
        }

    @get:Bindable
    var funding: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.funding)
        }

    @get:Bindable
    var gender: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.gender)
        }

    @get:Bindable
    var loanAmt: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.loanAmt)
        }

    @get:Bindable
    var coverage: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.coverage)
        }

    @get:Bindable
    var tenure: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.tenure)
        }

    @get:Bindable
    var dob: String? = ""
    set(value) {
        field = value
        notifyPropertyChanged(BR.dob)
    }

    @get:Bindable
    var age: String? = ""
    set(value) {
        field = value
        notifyPropertyChanged(BR.age)
    }

    @get:Bindable
    var loanType: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.loanType)
        }

//    @get:Bindable
//    var productType: String? = ""
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.productType)
//        }

    @get:Bindable
    var remarks: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.remarks)
        }
    @get:Bindable
    var contractno: String? = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.contractno)
        }

}