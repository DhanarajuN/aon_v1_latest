package com.ongo.aonsecure.model

data class ObjResponse(
    var orgId: Int,
    var UserId: Int,
    var macIdJobId: Int,
    var status: String,
    var mobileNo: String,
    var mobile: String,
    var FullName: String,
    var emailId: String,
    var organisation: String,
    var macId: String,
    var password: String,
    var gender: String,
    var msg: String,
    var IMEI: String,
    var userStatus: String,
    var RMID: String,
    var Zone: String,
    var message: String,
    var isLogin: String,
    var isServerSync: String,
    var DaysToRemaind: String,
    var DaysToExpiry: Int,
    var accessToken:String,
    var isPasswordChanged: String,
    var productTypes: List<String> = emptyList()

)