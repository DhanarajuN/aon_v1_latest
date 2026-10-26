package com.ongo.aonsecure.adapter

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.ongo.aonsecure.BuildConfig
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.QuoteDetailActivity
import com.ongo.aonsecure.model.Post
import com.ongo.aonsecure.pdf_gen.TemplatePDF
import com.ongo.aonsecure.utils.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
class CustomAdapter(val userList: List<Post>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post_adap, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList[position])

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(post: Post) {
            val quote_text = itemView.findViewById(R.id.quote_text) as TextView
            quote_text.text = post.QuoteNo
            val generatedText = itemView.findViewById(R.id.generated_text) as TextView
            generatedText.text = post.QuoteGeneratedOn?.split(" ")?.first()

            // val valid_text = itemView.findViewById(R.id.valid_text) as TextView
           // valid_text.text = post.Zone
            val coverage_text = itemView.findViewById(R.id.coverage_text) as TextView
               coverage_text.text = post.Coverage?.toString() + " INR"
            val tenure_text = itemView.findViewById(R.id.tenure_text) as TextView
            tenure_text.text = post.Tenure
            val product_text = itemView.findViewById(R.id.product_text) as TextView
            product_text.text = post.Product
            val medical_req_text = itemView.findViewById(R.id.medical_req_text) as TextView
            medical_req_text.text = post.MedicalsRequired+" Medical Req"
           // val premium_text = itemView.findViewById(R.id.premium_text) as TextView
            val total_text = itemView.findViewById(R.id.total_text) as TextView


            if (post.NetPremium?.contains(" INR")!! && post.TotalPremium?.contains(" INR")!!
                && post.Coverage?.contains(" INR")!!
            ) {
              /*  premium_text.text =
                    post.NetPremium?.substring(0, post.NetPremium?.indexOf(" INR")!!) + " INR"*/
                total_text.text =
                    post.TotalPremium?.substring(0, post.TotalPremium?.indexOf(" INR")!!) + " INR"
                coverage_text.text =
                    post.Coverage?.substring(0, post.Coverage?.indexOf(" INR")!!) + " INR"
            } else {
             //   premium_text.text = post.NetPremium?.toDouble()?.roundToInt().toString() + " INR"
                total_text.text = post.TotalPremium?.toDouble()?.roundToInt().toString() + " INR"
                coverage_text.text = post.Coverage?.toString() + " INR"
            }

            val active_status = itemView.findViewById(R.id.active_status) as TextView
            //active inactive date comparator
          //  compareDate(active_status, post.ValidUntil)

           // var view_detail = itemView.findViewById(R.id.view_detail) as TextView
            var share_button = itemView.findViewById(R.id.share_button1) as Button
            var download_pdf = itemView.findViewById(R.id.download_pdf1) as Button

            itemView.setOnClickListener {
                Log.e("this is post", ">>>>>>>>${post}")
                var intent = Intent(itemView.context, QuoteDetailActivity::class.java)
                intent.putExtra("quoteNo", post.QuoteNo)
                itemView.context.startActivity(intent)
            }

            share_button.setOnClickListener {
                pdfView(itemView, post, true)
            }

            download_pdf.setOnClickListener {
                if (!isNotificationEnabled(itemView.context)) {
                    showNotificationPermissionDialog(itemView.context)
                    //requestNotificationPermission(itemView.context)
                }
//                else if(!checkStoragePermissions()){
//                    Toast.makeText(itemView.context, "storage permission denied", Toast.LENGTH_LONG).show()
//                }
                pdfView(itemView, post, false)
            }


        }
        fun checkStoragePermissions(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                Environment.isExternalStorageManager()
            } else {
                //Below android 11
                val write: Int =
                    ContextCompat.checkSelfPermission(itemView.context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val read: Int =
                    ContextCompat.checkSelfPermission(itemView.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
            }
        }

        private fun requestPermission() {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                Log.d("22233", "requestPermission: "+checkStoragePermissions())
                if (!checkStoragePermissions()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    itemView.context.startActivity(intent)
                }else{
                   // pdfView()
                }
            }else
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /*requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        111
                    )*/
                } else {
                 //   pdfView()
                    //startActivity(new Intent(PdfCreationActivity.this, PdfCreate.class));
                }
        }
        private fun isNotificationEnabled(context: Context): Boolean {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }
        fun showNotificationPermissionDialog(context: Context) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle("Notification Permission")
            alertDialogBuilder.setMessage("Please enable notification permissions to receive updates.")
            alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
                // Open the notification settings screen
                requestNotificationPermission(context)
            }
            alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
                // Handle cancel button click if needed
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        private   fun requestNotificationPermission(context: Context) {
            val intent = Intent()
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", context.packageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                }
                else -> {
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:" + context.packageName)
                }
            }
            context.startActivity(intent)
        }
        private fun pdfView(itemView: View, post: Post, showSharePop: Boolean) {

            /**
             * How to USE FONT....
             */
            // val urName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED)

            /*   var mProgressBarHandler = ProgressBarHandler(itemView.context) // In onCreate
                mProgressBarHandler.show()*/

            var templatePDF = TemplatePDF(itemView.context)
            templatePDF.openDocument(post?.QuoteNo)
            // templatePDF.addBorder()

            /*  templatePDF.addMetaData("clientes", "Ventas", "Marines")
              templatePDF.addTitles("Teorema-Sistemas", "Comprovante de venda", "Rafael Reynoud Benetti", "R$ 60,00")*/
            templatePDF.addBorder()
            templatePDF
            // templatePDF.addImage(getByteArry())
            templatePDF.addImage(getByteArry(itemView.context))
            var arry = arrayOf("      ", "Quote Number", "Generated On")
            // val clients: ArrayList<Array<String>>? = null
            val clients = ArrayList<Array<String>>()
            templatePDF.createTable(arry, "fSubTitle", clients)

            var arry1 = arrayOf(
                "      ",
                post?.QuoteNo,
                post?.QuoteGeneratedOn

                /*post?.ValidUntil,*/
               /* "Active"*/
            )
            val clients1: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry1, "", clients1)
            templatePDF.lineSeperator()

            var arry2 = arrayOf("User ID", "User Name", "FID")
            val clients2: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry2, "fSubTitle", clients2)

            var arry3 = arrayOf(post?.UserID, post?.UserName, post?.RMID)
            val clients3: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry3, "", clients3)


            var arry4 = arrayOf("Insurer Name", "Product Category", "Product")
            val clients4: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry4, "fSubTitle", clients4)

            var insurer = ""
            insurer = if (post.Insurer!!.contains("(")) {
                post.Insurer?.substring(0, post.Insurer!!.indexOf("(")).toString()
            } else {
                post.Insurer.toString()
            }
            var category = ""
            category = if (post.ProductCategory!!.contains("(")) {
                post.ProductCategory?.substring(0, post.ProductCategory!!.indexOf("(")).toString()
            } else {
                post.ProductCategory.toString()
            }

            var arry5 = arrayOf(insurer, category, post?.Product)
            val clients5: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry5, "", clients5)

            var arry6 = arrayOf("Funding Type", "Gender", "Date Of Birth")
            val clients6: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry6, "fSubTitle", clients6)

            var arry7 = arrayOf(post?.FundingType, post?.Gender, post?.DateofBirth)
            val clients7: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry7, "", clients7)


            var arry8 = arrayOf("Age", "Loan Amount", "Sum Insured")
            val clients8: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry8, "fSubTitle", clients8)

            var arry9 =
                arrayOf(post?.Age, post?.LoanAmount, post?.Coverage)
            val clients9: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry9, "", clients9)

            var arry10 = arrayOf("Tenure", "Medicals Required", "Calculator Version")
            val clients10: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry10, "fSubTitle", clients10)

            var arry11 = arrayOf(post?.Tenure, post?.MedicalsRequired, post?.CalculatorVersion)
            val clients11: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry11, "", clients11)

            var arry12 = arrayOf("Premium Payment Term", "Loan Type", "Benefit Type")
            val clients12: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry12, "fSubTitle", clients12)

            var arry13 = arrayOf(
                post?.PremiumPaymentTerm,
                post?.LoanType,
                post?.BenefitType
            )
            val clients13: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry13, "", clients13)

            var arry14 = arrayOf("Zone","Remarks", "Contract Number")
            val clients14: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry14, "fSubTitle", clients14)

            var remarks: String = if (post?.Remarks != null && post?.Remarks!!.isNotEmpty())
                post?.Remarks!!
            else
                " -------- "
            var contractno: String = if (post?.ContractNo != null && post?.ContractNo!!.isNotEmpty())
                post?.ContractNo!!
            else
                " -------- "

            var zone: String = ""
            if (post?.Zone != null && post?.Zone?.isNotEmpty()!!)
                zone = post?.Zone!!
            else
                zone = "--------"

            var arry15 = arrayOf( zone,remarks,contractno)
            val clients15: ArrayList<Array<String>>? = null
            templatePDF.createTable(arry15, "", clients15)

            var arry16 = arrayOf("Net Premium", "Tax", "Total Premium")
            val clients16: ArrayList<Array<String>>? = null
            templatePDF.tableWithBackgroundColor(arry16, "fSubTitle", clients16)

            var netPremium = ""
            var tax = ""
            var total = ""

            if (post.NetPremium?.contains(" INR")!! && post.Tax?.contains(" INR")!!
                && post.Total?.contains(" INR")!!) {
                netPremium = post.NetPremium?.substring(0, post.NetPremium?.indexOf(" INR")!!) + " INR"
                tax = post.Tax?.substring(0, post.Tax?.indexOf(" INR")!!) + " INR"
                total = post.Total?.substring(0, post.Total?.indexOf(" INR")!!) + " INR"
            } else {
                netPremium = post.NetPremium?.toDouble()?.toString() + " INR"
                tax = post.Tax?.toDouble()?.toString() + " INR"
                total =post.Total?.toDouble()?.let { ceil(it)?.toInt().toString() } + " INR"

            }

            /*post.NetPremium?.toString(),
                post.Tax?.toString()/*.toDouble()?.toInt()?*/,
                post.Total?.toString()/*.toDouble()?.toInt()?*/*/

            var arry17 = arrayOf(netPremium,tax,total)
            val clients17: ArrayList<Array<String>>? = null
            templatePDF.tableWithBackgroundColor(arry17, "", clients17)

            templatePDF.addSubTitleParagraph("Terms & Conditions", "")
            templatePDF.addvalueParagraph("* This is just an indicative Premium calculation and should not be considered as an official document/final policy document in any sense")
            templatePDF.addvalueParagraph("* Premium quoted is including GST of 18%")
           // templatePDF.addvalueParagraph("* Final Policy issuance & Acceptance of Risk is the decision of the Insurance company (subject to all relevant documents & terms) and CNH Industrial Capital (India) Pvt Ltd does not hold any liability/made responsible for any such decisions")
            templatePDF.addvalueParagraph("* Final Policy issuance & Acceptance of Risk is the decision of the Insurance company (subject to all relevant documents & terms) and Aon India Insurance Brokers Private Limited does not hold any liability/made responsible for any such decisions")
            templatePDF.addvalueParagraph(" ")

            templatePDF.addSubTitleParagraph(
                "This is a system generated document and does not require any signature",
                "center"
            )

            templatePDF.closeDocument()

            // mProgressBarHandler.hide()

            if (showSharePop) {
               // if (isExternalStorageWritable()) {
                    templatePDF.viewPDF(itemView.context)
               // }
            } else {
                alertDialog(
                    itemView,
                    "Alert!",
                    "Quote downloaded successfully. Check in downloads folder ",
                    false,
                    "Okay",
                    "",
                    post?.QuoteNo



                )
                callNotificationForQuoteGenerated(post?.QuoteNo)
            }
        }



        private fun callNotificationForQuoteGenerated(quoteNo: String?) {
            val intent = Intent(itemView.context, MessageService::class.java)
            intent.putExtra(MessageService.QUOTE_NUMBER, quoteNo)
            intent.putExtra(MessageService.MESSAGE_EXTRA, "Quote downloaded successfully.Click to view Pdf")
            itemView.context.startService(intent)
        }

        fun alertDialog(
            itemView: View,
            title: String,
            msg: String,
            cancellable: Boolean,
            positive: String,
            negative: String,
            quoteNo: String?
        ) {
            val builder = AlertDialog.Builder(itemView.context)
            //set title for alert dialog
            builder.setTitle(title)
            //set message for alert dialog
            builder.setMessage(msg)
            builder.setIcon(R.drawable.ic_vec_upload_success)
            //performing positive action
            builder.setPositiveButton(positive) { dialogInterface, which ->
                retrivePdf(quoteNo)

            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(cancellable)
            alertDialog.show()
        }
        private fun retrivePdf(quoteNo: String?) {
            if (!quoteNo.isNullOrEmpty()) {

                // Correct PDF Path (App private Documents folder)
                val file = File(
                    itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "$quoteNo.pdf"
                )

                if (!file.exists()) {
                    Toast.makeText(itemView.context, "PDF not found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Correct URI for FileProvider
                val uri = FileProvider.getUriForFile(
                    itemView.context,
                    itemView.context.packageName + ".provider",
                    file
                )

                // Open PDF
                val openFile = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)   // 🔥 REQUIRED when context is not an Activity
                }

                try {
                    itemView.context.startActivity(openFile)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(itemView.context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                }
            }
        }


        private fun compareDate(activeStatus: TextView, validUntil: String?) {
            try {
                val current = getCurrentDateForm()
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
                val quoteValidDate = sdf.parse(validUntil.toString())
                val currentDate = sdf.parse(current)

                if (currentDate.before(quoteValidDate)) {
                    activeStatus.text = "Active"
                    activeStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.active_dot,
                        0,
                        0,
                        0
                    );
                } else {
                    activeStatus.text = "In Active"
                    activeStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.inactive_dot,
                        0,
                        0,
                        0
                    );
                }

            } catch (e: DateTimeException) {
                Log.e("exp date", ">>>>>>>>>${e.localizedMessage}")
            }

        }
    }


}