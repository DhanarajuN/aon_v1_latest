package com.ongo.aonsecure.activity

import android.Manifest
import androidx.activity.addCallback
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ongo.aonsecure.BuildConfig
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.ui.help.HelpFragment
import com.ongo.aonsecure.activity.ui.home.HomeFragment
import com.ongo.aonsecure.activity.ui.home.HomeFragment1
import com.ongo.aonsecure.activity.ui.profile.ProfileFragment
import com.ongo.aonsecure.activity.ui.profile.ProfileViewModel
import com.ongo.aonsecure.activity.ui.quote_list.QuoteListFragmnet
import com.ongo.aonsecure.activity.ui.quoteview.QuoteFragment
import com.ongo.aonsecure.base.BaseCompatActivity
import com.ongo.aonsecure.database.ObjectBox
import com.ongo.aonsecure.databinding.ActivityMainBinding
import com.ongo.aonsecure.model.OfflineJson
import com.ongo.aonsecure.model.OfflineJson_
import com.ongo.aonsecure.utils.FragmentCallback
import com.ongo.aonsecure.utils.PREF_PASSWORD
import com.ongo.aonsecure.utils.PREF_USERNAME
import com.ongo.aonsecure.utils.SharedPref
import com.ongo.aonsecure.viewmodel.MainActivityViewModel
import io.objectbox.Box
//import kotlinx.android.synthetic.main.toolbar.*
import pdf.app.library.utils.Constants

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : BaseCompatActivity(), FragmentCallback {
    private lateinit var profileViewModel: ProfileViewModel
    private var instantFrag: HomeFragment1? = null
    private var instaFrag: ProfileFragment? = null
    private var instFrag: HelpFragment? = null
    private var quoteListInstaFrag: QuoteListFragmnet? = null
    private var quoteFragment: QuoteFragment? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private val REQUEST_CODE_BACK_PRESSED = 2
    private var offlineBox: Box<OfflineJson> = ObjectBox.boxStore.boxFor(OfflineJson::class.java)
    private var offlineDBCount: Long = 0
    var userstatus: String? = ""
    private var contentFrame = 0
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBackHandler()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Check for force update

        val quoteListFragmnet = QuoteListFragmnet.newInstance()
        openFragment(quoteListFragmnet)

        contentFrame = R.id.container
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        binding.viewModel = viewModel

        initDeclareFunc()
    }
    private fun initFireBase() {
        Log.d("FirebaseInit", "Initializing Firebase")

        val firebaseDatabase = FirebaseDatabase.getInstance("https://cli-premium-calculator-default-rtdb.firebaseio.com/")
        Log.d("FirebaseInit", "Firebase Database Instance Obtained")

        val databaseReference = firebaseDatabase.getReference("Apps").child("Android").child("Version")
        Log.d("FirebaseInit", "Database Reference: $databaseReference")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FirebaseInit", "onDataChange triggered")

                if (dataSnapshot.value != null) {
                    var fireVersion: Long = -1
                    try {
                        fireVersion = dataSnapshot.value as Long
                        Log.d("FirebaseInit", "Firebase Version: $fireVersion")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (fireVersion > getVersionCode()) {
                        if (!isFinishing && !isDestroyed) {
                            showForceUpdateDialog()
                        }
                        Log.e("FirebaseInit", "New version available: $fireVersion")
                    } else {
                        Log.d("FirebaseInit", "App is up to date")
                    }
                } else {
                    Log.d("FirebaseInit", "No data found at reference")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseInit", "onCancelled: ${databaseError.message}")
            }
        }

        databaseReference.addValueEventListener(valueEventListener)
    }

    private fun getVersionCode(): Int {
        var versionCode: Int = -1
        try {
            val pInfo = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
            versionCode = pInfo.versionCode
            Log.d("FirebaseInit", "$versionCode")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode
    }

    private fun showForceUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Required")
        builder.setMessage("A new version of the app is available. Please update to continue.")
        builder.setCancelable(false)
        builder.setPositiveButton("Update") { dialog, which ->
            redirectToPlayStore()
        }
        builder.setOnDismissListener {
            redirectToPlayStore()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun redirectToPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$packageName")
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initDeclareFunc() {
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()?.setCustomView(R.layout.toolbar)
        window.statusBarColor = resources.getColor(R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val editFormImageView = toolbar.findViewById<ImageView>(R.id.editForm_iv)
        val drawable: Drawable = editFormImageView.getDrawable()
        val color = ContextCompat.getColor(this, R.color.white)

        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        editFormImageView.setOnClickListener {
            val intent = Intent(mContext, HomeActivity1::class.java)
            startActivityForResult(intent, REQUEST_CODE_BACK_PRESSED)
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navigationView1.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_quote_list -> {
                    val intent = Intent(mContext, HomeActivity1::class.java)
                    startActivityForResult(intent, REQUEST_CODE_BACK_PRESSED)
                }
                R.id.nav_profile -> {
                    val intent = Intent(mContext, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_help -> {
                    val intent = Intent(mContext, HelpActivity::class.java)
                    startActivity(intent)
                }
                R.id.logout_button -> {
                    if (offlineBox.all.size > 0)
                        userStatusChanged()
                    else
                        com.ongo.aonsecure.utils.alertDialog(
                            this,
                            "Alert!",
                            "Want to logout. Are you sure?",
                            false,
                            "Yes",
                            "No"
                        )
                }
                R.id.nav_changePassword -> {
                    changePassword()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.navigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { it ->
            when (it.itemId) {
                R.id.nav_home -> {
                    if (getCurrentFragment() !is HomeFragment) {

                        val homeFragment = HomeFragment.newInstance()
                        openFragment(homeFragment)
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_quote_list -> {
                    if (getCurrentFragment() !is QuoteListFragmnet) {
                        if (!checkStoragePermissions()) {
                            requestPermission()
                        }
                        val quoteListFragmnet = QuoteListFragmnet.newInstance()
                        openFragment(quoteListFragmnet)
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_profile -> {
                    if (getCurrentFragment() !is ProfileFragment) {
                        val profileFragment = ProfileFragment.newInstance()
                        openFragment(profileFragment)
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_help -> {
                    if (getCurrentFragment() !is HelpFragment) {
                        val helpFragment = HelpFragment.newInstance()
                        openFragment(helpFragment)
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

     //   binding.navigationView.selectedItemId = R.id.home

        val quoteListFragmnet = QuoteListFragmnet.newInstance()
        openFragment(quoteListFragmnet)
        initFireBase()
        if (!checkStoragePermissions()) {
            requestPermission()
        }
    }

    fun changePassword() {
        val intent = Intent(mContext, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    private fun userStatusChanged() {
        var checkInternetConnection = checkInternet()
        if (checkInternetConnection)
            if (offlineBox.all.size > 0) {
                offlineDBCount = offlineBox.all.size.toLong()
                var postList: List<OfflineJson> =
                    offlineBox.query().equal(OfflineJson_.ids, offlineDBCount).build().find()

                if (postList.isNotEmpty()) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )

                    var jsonData = postList[0].jsonData
                    profileViewModel.postQuoteAPI(
                        jsonData.toString(),
                        SharedPref.read(Constants.PREF_LOGGED_IN_EMAIL, "").toString(),
                        "3"
                    )

                } else {
                    if (userstatus == "In Active") {
                        com.ongo.aonsecure.utils.alertDialog(
                            this,
                            "Alert!",
                            "Your profile has been made inactive, please contact Admin Support",
                            false,
                            "okay",
                            ""
                        )
                    } else {
                        alertDialogForOfflineSync(
                            "Alert!",
                            "Offline quotes available. Kindly check with internet connection to sync?",
                            false,
                            "Yes",
                            "No"
                        )
                    }
                }
            }
    }

    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(this) {
            val quoteListFragment = QuoteListFragmnet.newInstance()
            openFragment(quoteListFragment)
            if (getCurrentFragment() is QuoteListFragmnet) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun addQuoteFrag(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.addToBackStack(fragment.tag)
        transaction.commit()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        instantFrag = null
        instaFrag = null
        instFrag = null

        if (fragment is QuoteFragment)
            fragment.setFragmentCallback(this)
        if (fragment is ProfileFragment)
            fragment.setFragmentCallback(this)
        if (fragment is HelpFragment)
            fragment.setFragmentCallback(this)
        if (fragment is HomeFragment1)
            instantFrag = fragment
        if (fragment is ProfileFragment)
            instaFrag = fragment
        if (fragment is HelpFragment)
            instFrag = fragment
        if (fragment is QuoteListFragmnet)
            quoteListInstaFrag = fragment
        if (fragment is QuoteFragment)
            quoteFragment = fragment

    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(contentFrame)
    }

    override fun onDataSent(data: String?) {
        Log.e("navigating to ", ">>>>$data")
        if (data.equals("callQuotePage")) {
            val quoteListFragment = QuoteListFragmnet.newInstance()
            openFragment(quoteListFragment)
            binding.navigationView.selectedItemId = R.id.nav_quote_list
        }
        if (data.equals("callHomeFrag")) {
            binding.navigationView.selectedItemId = R.id.nav_home
        }
    }

    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }


    fun showStoragePermissionDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Storage Permission")
        alertDialogBuilder.setMessage("Please enable storage permissions to access this feature.")
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            startActivity(intent)
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and above → No storage permission needed
//callgenePdf()
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6–9 → Ask for legacy storage permissions
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                111
            )
        }
        else {
            // Belo
        }}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BACK_PRESSED) {
            if (resultCode == HomeActivity1.RESULT_BACK_PRESSED) {
                val quoteListFragment = QuoteListFragmnet.newInstance()
                openFragment(quoteListFragment)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        }
    }

    private fun alertDialogForOfflineSync(
        title: String,
        msg: String,
        cancellable: Boolean,
        positive: String,
        negative: String
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(positive) { dialogInterface, which ->
            var rememberMeUserName = SharedPref.read(PREF_USERNAME, "")
            var rememberMePassword = SharedPref.read(PREF_PASSWORD, "")
            SharedPref.write(Constants.PREF_LOGGED_IN, "")
            SharedPref.clearPref()
            SharedPref.write(PREF_USERNAME, rememberMeUserName)
            SharedPref.write(PREF_PASSWORD, rememberMePassword)
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
        builder.setNegativeButton(negative) { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(cancellable)
        alertDialog.show()
    }

    fun callInternetCheck(isInternet: Boolean) {
        quoteListInstaFrag?.autoSyncCallForOfflineCHeck(isInternet)
        quoteFragment?.autoSyncCallForOfflineCHeck(isInternet)
        instantFrag?.autoSyncCallForOfflineCHeck(isInternet)
        instaFrag?.autoSyncCallForOfflineCHeckProfile(isInternet)
        instFrag?.autoSyncCallForOfflineCHeckHelp(isInternet)
    }
}
