package com.ongo.aonsecure.model

import android.os.Parcel
import android.os.Parcelable

data class PremQuote(
    var zone: String,
    var insurer: String,
    var prodCat: String,
    var product: String,
    var fundType: String,
    var gender: String,
    var loanAmount: String,
    var coverage: String,
    var tenure: String,
    var dob: String,
    var age: String,
    var remarks: String,
    var contractno: String,
    var preminum: String,
    var message: String,
    var CalculatorVersion: String,
    var QuoteNumber: String,
    var QuoteGeneratedOn: String,
    var DeviceId: String,
    var RMID: String,
    var UserId: String,
    var UserName: String,
    var validUntil: String,
    var status: String,
    var medicalsRequired: String,
    var PreminumPaymentTerm: String,
    var BenefitType: String,
    var MembersCovered: String,
    var Loantype: String,
    var type:String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()

        )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(zone)
        dest.writeString(insurer)
        dest.writeString(prodCat)
        dest.writeString(product)
        dest.writeString(fundType)
        dest.writeString(gender)
        dest.writeString(loanAmount)
        dest.writeString(coverage)
        dest.writeString(tenure)
        dest.writeString(dob)
        dest.writeString(age)
        dest.writeString(remarks)
        dest.writeString(contractno)
        dest.writeString(preminum)
        dest.writeString(CalculatorVersion)
        dest.writeString(QuoteNumber)
        dest.writeString(QuoteGeneratedOn)
        dest.writeString(DeviceId)
        dest.writeString(RMID)
        dest.writeString(UserId)
        dest.writeString(UserName)
        dest.writeString(validUntil)
        dest.writeString(status)
        dest.writeString(medicalsRequired)
        dest.writeString(PreminumPaymentTerm)
        dest.writeString(BenefitType)
        dest.writeString(MembersCovered)
        dest.writeString(Loantype)
        dest.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PremQuote> {
        override fun createFromParcel(parcel: Parcel): PremQuote {
            return PremQuote(parcel)
        }

        override fun newArray(size: Int): Array<PremQuote?> {
            return arrayOfNulls(size)
        }
    }
}
