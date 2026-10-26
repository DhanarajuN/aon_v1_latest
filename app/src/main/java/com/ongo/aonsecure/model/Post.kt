package com.ongo.aonsecure.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id


@Entity
data class Post(

    @Id var ids: Long = 0,
    var id: Int?,
    var QuoteGeneratedOn: String?,
    var Insurer: String?,
    var ProductCategory: String?,
    var FundingType: String?,
    var Product: String?,
    var medicals: String?,
    var Gender: String?,
    var Name: String?,
    var DateofBirth: String?,
    var Coverage: String?,
    var Age: String?,
    var Tenure: String?,
    var Premium: String?,
    var Tax: String?,
    var Total: String?,
    var DeviceID: String?,
    var RMID: String?,
    var UserID: String?,
    var UserName: String?,
    var CalculatorVersion: String?,
    var ValidUntil: String?,
    var Status: String?,
    var InsurerName: String?,
    var LoanAmount: String?,
    var MedicalsRequired: String?,
    var PremiumPaymentTerm: String?,
    var BenefitType: String?,
    var MembersCovered: String?,
    var NetPremium: String?,
    var TotalPremium: String?,
    var QuoteNo: String?,
    var Zone: String?,
    var LoanType: String?,
    var Remarks: String?,
    var ContractNo: String?
)