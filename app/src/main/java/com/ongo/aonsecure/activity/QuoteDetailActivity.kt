package com.ongo.aonsecure.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ongo.aonsecure.R
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.ActivityQuoteDetailBinding
import com.ongo.aonsecure.model.Post
import com.ongo.aonsecure.model.Post_
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import kotlin.math.ceil

class QuoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuoteDetailBinding
    var quoteBox: Box<Post> = ObjectBox.boxStore.boxFor(Post::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_quote_detail
        )
        val quoteNo: String? = intent.getStringExtra("quoteNo")
        if (quoteNo?.isNotEmpty()!!) {
            intiViews(quoteNo)
        }

        binding.backPress.setOnClickListener {
            finish()
        }

    }

    private fun intiViews(quoteNo: String) {
        //check and retrieve data from here objectbox database
        var postList: List<Post> = quoteBox.query()
            .equal(Post_.QuoteNo, quoteNo, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .find()


        if (postList.isNotEmpty()) {
            var insurer = ""
            insurer = if (postList[0].Insurer!!.contains("(")) {
                postList[0].Insurer?.substring(
                    0,
                    postList[0].Insurer!!.indexOf("(")
                ).toString()
            } else {
                postList[0].Insurer.toString()
            }
            var category=""
            category=if (postList[0].ProductCategory!!.contains("(")){
                postList[0].ProductCategory?.substring(0, postList[0].ProductCategory!!.indexOf("(")).toString()
            }else{
                postList[0].ProductCategory.toString()
            }
            binding.insurerText.text = insurer
            binding.quoteText.text = postList[0].QuoteNo ?: ""
            binding.generatedText.text = postList[0].QuoteGeneratedOn ?: ""
            binding.validText.text = postList[0].ValidUntil ?: ""
            binding.rmidText.text = postList[0].RMID ?: ""
            binding.calcVersionText.text = postList[0].CalculatorVersion ?: ""
            binding.deviceIdText.text = postList[0].DeviceID ?: ""
            binding.useridText.text = postList[0].UserID ?: ""
            binding.userNameText.text = postList[0].UserName ?: ""
          //  binding.statusText.text = postList[0].Status ?: ""
            binding.categoryText.text = category
            binding.zoneText.text = postList[0].Zone ?: ""
            binding.fundText.text = postList[0].FundingType ?: ""
            binding.prodcutText.text = postList[0].Product ?: ""
            binding.loanText.text = postList[0].LoanType ?: ""
           // binding.coverageText.text = postList[0].Coverage?.toString()
            binding.genderText.text = postList[0].Gender ?: ""
            binding.dobText.text = postList[0].DateofBirth ?: ""
            binding.ageText.text = postList[0].Age?.toString() + " Years"
            binding.tenureText.text = postList[0].Tenure ?: ""
         //   binding.loanAmtText.text = postList[0].LoanAmount?.toString()
            binding.medicalReqText.text = postList[0].MedicalsRequired ?: ""
            binding.benefitTypeText.text = postList[0].BenefitType ?: ""
            binding.membersCoveredText.text = postList[0].MembersCovered ?: ""
            binding.paymentTermText.text = postList[0].PremiumPaymentTerm ?: ""
            binding.remarkText.text = postList[0].Remarks ?: ""
            binding.contractnoText.text = postList[0].ContractNo ?: ""
           // binding.premiumText.text = postList[0].NetPremium?./*toDouble()?.roundToInt().*/toString()
         //   binding.taxText.text = postList[0].Tax?./*toDouble()?.roundToInt().*/toString()
         //   binding.totalText.text = postList[0].Total?./*toDouble()?.roundToInt().*/toString()

            if (postList[0].LoanAmount?.contains(" INR")!! && postList[0].NetPremium?.contains(" INR")!! &&
                postList[0].Tax?.contains(" INR")!! && postList[0].Total?.contains(" INR")!! &&
                postList[0].Coverage?.contains(" INR")!!) {

                binding.loanAmtText.text = postList[0].LoanAmount?.substring(0, postList[0].LoanAmount?.indexOf(" INR")!!) + " INR"
                binding.premiumText.text = postList[0].NetPremium?.substring(0, postList[0].NetPremium?.indexOf(" INR")!!) + " INR"
                binding.taxText.text = postList[0].Tax?.substring(0, postList[0].Tax?.indexOf(" INR")!!) + " INR"
                binding.totalText.text = postList[0].Total?.substring(0, postList[0].Total?.indexOf(" INR")!!) + " INR"
                binding.coverageText.text = postList[0].Coverage?.substring(0, postList[0].Coverage?.indexOf(" INR")!!) + " INR"

            } else{
                binding.loanAmtText.text = postList[0].LoanAmount?.toString()+ " INR"
                binding.premiumText.text = postList[0].NetPremium + " INR"
                binding.taxText.text = postList[0].Tax + " INR"
                binding.totalText.text = postList[0].Total + " INR"
                binding.coverageText.text = postList[0].Coverage?.toString() + " INR"
            }
        }
    }
}
