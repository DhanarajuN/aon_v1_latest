package com.ongo.aonsecure.base

import androidx.lifecycle.ViewModel
import com.ongo.aonsecure.activity.ui.filter.FilterActivityViewModel
import com.ongo.aonsecure.activity.ui.help.HelpViewModel
import com.ongo.aonsecure.activity.ui.home.HomeViewModel
import com.ongo.aonsecure.activity.ui.profile.ProfileViewModel
import com.ongo.aonsecure.activity.ui.quote_list.QuoteListViewModel
import com.ongo.aonsecure.activity.ui.quoteview.QuoteViewModel
import com.ongo.aonsecure.injection.component.DaggerViewModelInjector
import com.ongo.aonsecure.injection.component.ViewModelInjector
import com.ongo.aonsecure.injection.module.NetworkModule
import com.ongo.aonsecure.viewmodel.*

/**
 * we will be using this class for dependency injection only
 */
abstract class BaseViewModel : ViewModel() {
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(NetworkModule)
        .build()

    init {
        inject()
    }

    /**
     * Injects the required dependencies
     */
    private fun inject() {
        when (this) {
            is MainActivityViewModel -> injector.inject(this)
            is SplashScreenViewModel -> injector.inject(this)
            is LoginActivityViewModel -> injector.inject(this)
            is SignUpActivityViewModel -> injector.inject(this)
            is HomeViewModel -> injector.inject(this)
            is QuoteViewModel -> injector.inject(this)
            is ProfileViewModel -> injector.inject(this)
            is HelpViewModel -> injector.inject(this)
            is QuoteListViewModel -> injector.inject(this)
            is ChangeRequestActivityViewModel ->injector.inject(this)
            is FilterActivityViewModel -> injector.inject(this)
            is ChangePasswordViewModel ->injector.inject(this)
            is ForgotPasswordViewModel -> injector.inject(this)

        }
    }
}