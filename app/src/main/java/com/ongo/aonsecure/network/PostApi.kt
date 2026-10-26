package com.ongo.aonsecure.network

import com.ongo.aonsecure.model.*
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * The interface which provides methods to get result of webservices
 */
interface PostApi {
    /**
     * Get the list of the pots from the API
     */

    @GET("/Services/getMasters?type=Calculator")
    fun getCalculator(): Observable<Calculate>

    //http://13.235.83.12:8081/services/getMasters?type=PasswordConfiguration
    @GET("/Services/getMasters?type=PasswordConfiguration")
    fun getPasswordConfig(): Observable<PasswordConfig>

    @GET("/Services/getMasters?type=LoanType")
    fun getLoanType(): Observable<LoanType>

    @GET("/Services/getMasters?type=Tenure")
    fun getTenure(): Observable<Tenure>

    //13.235.83.12:8081/Services/getMasters?type=ZoneInsurer&propKey=Zone&keyWord=Zone C&status=Active
    @GET("/Services/getMasters?type=Insurer&propKey=Zone&status=Active")
    fun getInsurer(
      //  @Query("keyWord") zoneType: String?
    ): Observable<Insurer>

    @GET("/Services/getMasters?type=Zone")
    fun getZones(): Observable<Zone>

    @GET("api/public/jobs/moduleconstants?type=ModuleConstants&mallId=3")
    fun checkTerms(): Observable<TermsConfig>
    @GET("/Services/getMasters?type=ProductCategories&status=Active")
    fun getProdCate(@Query("mallId") zoneName: String?
    ): Observable<ProdCat>

    @GET("/Services/getMasters?type=Products&status=Active")
    fun getProducts(@Query("mallId")zonename: String?): Observable<Product>

    @GET("/Services/getMasters?type=Product Type&status=Active")
    fun getProductType(@Query("mallId")zonename: String?): Observable<ProductType>

    /*@GET("/Services/getMasters?type=Products")
    fun getInsurers(
        @Query("mallId") mallId:String?,
        @Query("keyWord") keyword:String?,
        @Query("propKey") propKey:String?
    ): Observable<Insurer>*/

    @GET("/MobileAPIs/getTax?")
    fun getTax(
        @Query("date") date: String
    ): Observable<Tax>

    //&userEmailId=testfive@gmail.com&dt=DEVICES&devicetype=Android&mobileNo=9999123456&password=123456&DeviceId=f39696d60a008a66&DeviceName=Android
  //with out
    //  @POST("/MobileAPIs/regAndloyaltyAPI")
    @POST("/LoginController/regAndloyaltyAPI")

    fun registerUser(
        @Query("orgId") orgId: String?,
        @Query("Name") name: String?,
        @Query("userEmailId") userEmailId: String?,
        @Query("dt") dt: String?,
        @Query("mobileNo") mobileNo: String?,
        @Query("devicetype") deviceType: String?,
        @Query("IMEI") imei: String?,
        @Query("RMID") rmid: String?,
        @Query("RegDate") regDate: String,
        @Query("isLogin") loginBool: String
    ): Observable<ObjResponse>

  //with out security  @GET("/MobileApis/loginConsumerForOrg")
  @GET("/LoginController/loginConsumerForOrg")
    fun loginuser(
        @Query("orgId") orgId: String?,
        @Query("email") userEmailId: String?,
        @Query("password") password: String?,
        @Query("IMEI") imei: String?
    ): Observable<ObjResponse>

    //http://localhost:8081/MobileAPIs/postedJobs?userId=3&consumerEmail=tst@mail.com&type=Quote&json={list:[{Name:test,Age:21}]}&dt=CAMPAIGNS&category=Services
    @POST("/MobileAPIs/postedJobs")
    open fun uploadQuote(
        @Query("dt") dt: String?,
        @Query("category") category: String?,
        @Query("type") type: String?,
        @Query("json") json: String?,
        @Query("consumerEmail") consumerEmail: String?,
        @Query("userId") userId: String?
    ): Observable<ObjResponse>

    @POST("/PublicApis/publicPostedJobs")

    fun reg(
        @Query("dt") dt: String?,
        @Query("consumerEmail") deviceType: String?,
        @Query("type") type:  String?,
        @Query("RMID") rmid: String?,
        @Query("json") json: String,
        @Query("category") category: String
    ): Observable<ObjResponse>
    //http://13.235.83.12:8081/Services/getMasters?type=Quotes&mallId=3
    @GET("/Services/getMasters")
    fun quoteList(
        @Query("type") orgId: String?,
        @Query("consumerId") userId: String?
    ): Observable<QuoteList>

    // http://localhost:8081/mobileApis/changeRequest?userEmailId=SRIRAM.BADETI@GMAIL.COM&newIMEI=12345678910
    // (--- or ---)
    //http://localhost:8081/mobileApis/changeRequest?mobileNo=9848177506&newIMEI=123456789

 // with out  //@POST("/mobileApis/changeRequest")
    @POST("/LoginController/changeRequest")
    fun changeRequest(
        @Query("userEmailId") userEmailId: String?,
        @Query("mobileNo") mobileNo: String?,
        @Query("RMID") rmid: String?,
        @Query("newIMEI") imei: String?
    ): Observable<ObjResponse>

    //http://localhost:8081/MobileApis/changePasswordValue?email=finaltest1@gmail.com&currentPassword=s11M9FGv&newPassword=12345678&newrecheckPassword=12345678
   // @POST("/MobileApis/changePasswordValue")
    @POST("/LoginController/changePasswordValue")
    fun changePassword(
        @Query("email") email: String?,
        @Query("currentPassword") currentPassword: String?,
        @Query("newPassword") newPassword: String?,
        @Query("newrecheckPassword") newrecheckPassword: String?,
        @Query("isLogin") loginBool: String,
        @Query("isChangePassword") isPassword:String
    ): Observable<ObjResponse>

    //http://localhost:8081/MobileApis/checkUserbyEmail?email=finaltest1@gmail.com&RMID=333330090
    //@POST("/MobileApis/checkUserbyEmail")
    @POST("/LoginController/checkUserbyEmail")
    fun forgotPassword(
        @Query("email") email: String?,
        @Query("RMID") rmID: String?
    ): Observable<ObjResponse>

    @GET("/MobileAPIs/getStatus?")
    fun userStatus(@Query("email") email: String?
    ):Observable<StatusDto>

    @GET("/Mobileapis/currentZone?")
    fun getUserZone(@Query("email") email: String?
    ):Observable<ObjResponse>
   @GET( "/LoginController/loginrefresh?")
   fun tokenRefresh(@Query("token")token:String?
   ):Observable<ObjResponse>

    //http://13.235.83.12:8081/mobileapis/CheckPWDValidity?email=sriram.badeti@gmail.com
    @GET("/mobileapis/CheckPWDValidity")
    fun checkPWDValidity(
        @Query("email") email: String?
    ): Observable<ObjResponse>

    //http://13.235.83.12:8081/mobileapis/serverStatus?email=sriram.badeti@gmail.com&serverSyncValue=true
    @GET("/mobileapis/serverStatus")
    fun syncServerUpdateCall(
        @Query("email") email: String?,
        @Query("serverSyncValue") isSyncServer: String?
    ): Observable<ObjResponse>

}