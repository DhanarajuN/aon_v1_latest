package com.ongo.aonsecure.injection

import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ongo.aonsecure.activity.ui.filter.FilterActivityViewModel
import com.ongo.aonsecure.activity.ui.help.HelpViewModel
import com.ongo.aonsecure.activity.ui.home.HomeViewModel
import com.ongo.aonsecure.activity.ui.profile.ProfileViewModel
import com.ongo.aonsecure.activity.ui.quote_list.QuoteListViewModel
import com.ongo.aonsecure.activity.ui.quoteview.QuoteViewModel
import com.ongo.aonsecure.database.NoteDatabase
import com.ongo.aonsecure.viewmodel.LoginActivityViewModel
import com.ongo.aonsecure.viewmodel.SplashScreenViewModel

class ViewModelFactory(
    private val activity: Fragment,application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity.requireContext(), NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.taxDao(),
                db.calculateDao(),
                db.termsDao(),
                //db.productTypeDao(),
                db.jobDao()

                ) as T
        }

        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity.requireContext(), NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return QuoteViewModel(db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.zoneDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.taxDao(),
                db.calculateDao(),
                db.jobDao(),
                //db.productTypeDao()

            ) as T
        }
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity.requireContext(), NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel() as T
        }

        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            val db = Room.databaseBuilder(activity.requireContext(), NoteDatabase::class.java, "zone")
                .build()
            @Suppress("UNCHECKED_CAST")
            return HelpViewModel() as T
        }
        if (modelClass.isAssignableFrom(QuoteListViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity.requireContext(), NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return QuoteListViewModel(db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.taxDao(),
                db.calculateDao(),
              //  db.productTypeDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}

class ViewModelFactoryActivity(private val activity: Activity,application: Application) : ViewModelProvider.AndroidViewModelFactory(application) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity, NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.taxDao(),
                db.calculateDao(),
                db.termsDao(),
               // db.productTypeDao(),
                db.jobDao()
            ) as T
        }
        if (modelClass.isAssignableFrom(FilterActivityViewModel::class.java)) {
            val db = Room.databaseBuilder(activity, NoteDatabase::class.java, "zone").build()
            @Suppress("UNCHECKED_CAST")
            return FilterActivityViewModel(
                db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.calculateDao(),
                db.jobDao(),
               // db.productTypeDao()
            ) as T
        }
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            val db = Room.databaseBuilder(activity, NoteDatabase::class.java, "zone")
                .build()
            @Suppress("UNCHECKED_CAST")
            return HelpViewModel() as T
        }
        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity, NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return QuoteViewModel(db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.zoneDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.taxDao(),
                db.calculateDao(),
                db.jobDao(),
               // db.productTypeDao()

            ) as T
        }
        if (modelClass.isAssignableFrom(LoginActivityViewModel::class.java)){

            val db =
                Room.databaseBuilder(activity, NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return LoginActivityViewModel(db.termsDao()) as T
        }
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity, NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel() as T
        }
        if (modelClass.isAssignableFrom(SplashScreenViewModel::class.java)) {
            val db =
                Room.databaseBuilder(activity, NoteDatabase::class.java, "zone")
                    .build()
            @Suppress("UNCHECKED_CAST")
            return SplashScreenViewModel(
                db.insurerDao(),
                db.prodCatDao(),
                db.prodctDao(),
                db.zoneDao(),
                db.tenureDao(),
                db.loanTypeDao(),
                db.taxDao(),
                db.calculateDao(),
               // db.productTypeDao(),
                db.jobDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


