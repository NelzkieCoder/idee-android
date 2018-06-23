package io.mindhouse.idee.ui.auth

import com.facebook.login.LoginResult
import io.mindhouse.idee.ExceptionHandler
import io.mindhouse.idee.data.AuthorizeRepository
import io.mindhouse.idee.data.model.User
import io.mindhouse.idee.di.qualifier.IOScheduler
import io.mindhouse.idee.ui.base.BaseViewModel
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by kmisztal on 23/06/2018.
 *
 * @author Krzysztof Misztal
 */

class AuthViewModel @Inject constructor(
        private val authorizeRepository: AuthorizeRepository,
        @IOScheduler private val ioScheduler: Scheduler,
        private val exceptionHandler: ExceptionHandler
) : BaseViewModel<AuthViewState>() {

    override val initialState = AuthViewState(false, authorizeRepository.isLoggedIn, null)

    fun onFacebookToken(fbToken: LoginResult) {
        postState(AuthViewState(true, false, null))

        val disposable = authorizeRepository.signInWithFacebook(fbToken.accessToken)
                .subscribeOn(ioScheduler)
                .subscribeBy(
                        onSuccess = ::onLoggedIn,
                        onError = ::onError
                )
        addDisposable(disposable)
    }

    private fun onLoggedIn(user: User) {
        postState(AuthViewState(false, true, null))
    }

    private fun onError(throwable: Throwable) {
        val message = exceptionHandler.getErrorMessage(throwable)
        postState(AuthViewState(false, false, message))
    }
}