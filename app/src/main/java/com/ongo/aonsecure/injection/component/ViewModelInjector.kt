package com.ongo.aonsecure.injection.component

import com.ongo.aonsecure.activity.ui.filter.FilterActivityViewModel
import com.ongo.aonsecure.activity.ui.help.HelpViewModel
import com.ongo.aonsecure.activity.ui.home.HomeViewModel
import com.ongo.aonsecure.activity.ui.profile.ProfileViewModel
import com.ongo.aonsecure.activity.ui.quote_list.QuoteListViewModel
import com.ongo.aonsecure.activity.ui.quoteview.QuoteViewModel
import com.ongo.aonsecure.injection.module.NetworkModule
import com.ongo.aonsecure.viewmodel.*
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters.
 */
@Singleton
@Component(modules = [(NetworkModule::class)])
interface ViewModelInjector {
    /**
     * Injects required dependencies into the specified MainActivityViewModel.
     * @param mainActivityViewModel MainActivityViewModel in which to inject the dependencies
     */
    fun inject(mainActivityViewModel: MainActivityViewModel)
    fun inject(splashScreenViewModel: SplashScreenViewModel)
    fun inject(loginActivityViewModel: LoginActivityViewModel)
    fun inject(signUpActivityViewModel: SignUpActivityViewModel)
    fun inject(homeViewModel: HomeViewModel)
    fun inject(quoteViewModel: QuoteViewModel)
    fun inject(profileViewModel: ProfileViewModel)
    fun inject(helpViewModel: HelpViewModel)
    fun inject(quoteListViewModel: QuoteListViewModel)
    fun inject(changeRequestActivityViewModel: ChangeRequestActivityViewModel)
    fun inject(filterActivityViewModel: FilterActivityViewModel)
    fun inject(changePasswordViewModel: ChangePasswordViewModel)
    fun inject(forgotPasswordViewModel: ForgotPasswordViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun networkModule(networkModule: NetworkModule): Builder
    }
}