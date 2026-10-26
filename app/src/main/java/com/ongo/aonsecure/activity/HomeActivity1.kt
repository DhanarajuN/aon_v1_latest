package com.ongo.aonsecure.activity

import android.Manifest
import androidx.activity.addCallback
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ongo.aonsecure.BuildConfig
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.ui.help.HelpFragment
import com.ongo.aonsecure.activity.ui.home.HomeFragment1
import com.ongo.aonsecure.activity.ui.profile.ProfileFragment
import com.ongo.aonsecure.activity.ui.quote_list.QuoteListFragmnet
import com.ongo.aonsecure.activity.ui.quoteview.QuoteFragment
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.databinding.ActivityHome1Binding
import com.ongo.aonsecure.utils.FragmentCallback
import com.ongo.aonsecure.viewmodel.MainActivityViewModel

class HomeActivity1 : BaseCompatActivity() , FragmentCallback {
    private var instantFrag: HomeFragment1? = null
    private var instaFrag: ProfileFragment?  = null
    private var instFrag: HelpFragment? = null
    private var quoteListInstaFrag: QuoteListFragmnet?=null
    private var quoteFragment: QuoteFragment?=null
    private lateinit var binding: ActivityHome1Binding
    private lateinit var viewModel: MainActivityViewModel

    private var contentFrame = 0
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBackHandler()
       // setContentView(R.layout.activity_home1)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_home1
        )
        contentFrame = R.id.container
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        binding.viewModel = viewModel
        initDeclareFunc()
    }
    private fun initDeclareFunc() {
        val quoteListFragmnet = HomeFragment1.newInstance()
        openFragment(quoteListFragmnet)
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.toolbar1);
        window.statusBarColor = resources.getColor(R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        val editFormImageView = toolbar.findViewById<ImageView>(R.id.back_press_iv)
        val title = toolbar.findViewById<TextView>(R.id.title_tv)
        title.setText("Create New Quote")
        // Set an OnClickListener for the ImageView
        editFormImageView.setOnClickListener {
            onBackPressed()
        }
//        if (Build.VERSION.SDK_INT >= 21) {
//            val window = this.window
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.statusBarColor = this.resources.getColor(R.color.black)
//        }
       //
    // requestPermission()


    }
    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(this) {
            setResult(RESULT_BACK_PRESSED)
            finish()
        }
    }
    companion object {
        const val RESULT_BACK_PRESSED = 123
        private const val REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 102

    }
    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(contentFrame)
    }
    override fun onDataSent(data: String?) {
        TODO("Not yet implemented")
        Log.e("navigating to ", ">>>>$data")
        if(data.equals("callQuotePage")){ //this is to call quotelist frag once the quote is submitted
            val intent = Intent(mContext, MainActivity::class.java)
            startActivity(intent)
        }
        if(data.equals("callHomeFrag")){
            val intent = Intent(mContext, HomeActivity1::class.java)
            startActivity(intent)
        }
    }
    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(fragment.tag)
        transaction.commit()
    }






    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }else if (requestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Proceed with your file access logic
            } else {
                Toast.makeText(this, "Manage external storage permission not granted.", Toast.LENGTH_SHORT).show()
            }
    }}

    fun addQuoteFrag(
        fragment: Fragment
    ) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.addToBackStack(fragment.tag)
        transaction.commit()


    }
    //this will be get called whenever broadcast receiver network status change is called
    fun callInternetCheck1(isInternet: Boolean){
        Log.d("5555555", "onNetworkConnectionChanged22: $isInternet   ")

       quoteListInstaFrag?.autoSyncCallForOfflineCHeck(isInternet)
        quoteFragment?.autoSyncCallForOfflineCHeck(isInternet)
        instantFrag?.autoSyncCallForOfflineCHeck(isInternet)
        instaFrag?.autoSyncCallForOfflineCHeckProfile(isInternet)
        instFrag?.autoSyncCallForOfflineCHeckHelp(isInternet)

    }
    private fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (hasManageExternalStoragePermission()) {
                // Proceed with your file access logic
            } else {
                Toast.makeText(this, "Manage external storage permission not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        if(fragment is HomeFragment1)
            instantFrag = fragment
        if(fragment is ProfileFragment)
            instaFrag = fragment
        if (fragment is HelpFragment)
            instFrag = fragment
        if(fragment is QuoteListFragmnet)
            quoteListInstaFrag=fragment
        if(fragment is QuoteFragment)
            quoteFragment=fragment

    }


    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Log.d("5555555", "onNetworkConnectionChanged: $isConnected")
        showMessage(isConnected)
    }
    private fun showMessage(isConnected: Boolean) {
        Log.d("5555555", "onNetworkConnectionChanged1: $isConnected")

        isInternetConnected = isConnected
        if(this is HomeActivity1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (this).callInternetCheck1(isConnected)
            }
    }
}