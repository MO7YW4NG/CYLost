package me.moty.cylost.ui.login

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.moty.cylost.MainActivity
import me.moty.cylost.R
import me.moty.cylost.data.LoginRepository
import me.moty.cylost.data.Result
import me.moty.cylost.data.model.LoggedInUser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class LoginViewModel(private var loginRepository: LoginRepository) : ViewModel() {

    private val loginFormState = MutableLiveData<LoginFormState>()
    private val loginResult = MutableLiveData<LoginResult>()

    public fun login(stu: String, pwd: String, checked: Boolean) {
        loginRepository.login(stu, pwd) { result ->
            if (result is Result.Success) {
                val data: LoggedInUser = result.data
                loginResult.postValue(LoginResult(LoggedInUserView(data.stuId)))
                if (checked) {
                    val edit = MainActivity.sharedPreferences.edit()
                    edit.putBoolean("remember", true)
                    edit.putString("stuid", stu)
                    edit.putString("pwd", pwd)
                    edit.apply()
                } else {
                    if (MainActivity.sharedPreferences.contains("remember")) {
                        val edit = MainActivity.sharedPreferences.edit()
                        edit.remove("remember")
                        edit.remove("stuid")
                        edit.remove("pwd")
                        edit.apply()
                    }
                }
            } else {
                loginResult.postValue(LoginResult(error = R.string.login_failed))
            }

        }
    }

    public fun logout() {
        loginRepository.logout()
        loginResult.postValue(LoginResult())
        loginFormState.value = LoginFormState()
    }

    public fun getRepo(): LoginRepository {
        return loginRepository
    }

    public fun getLoginFromState(): LiveData<LoginFormState> {
        return loginFormState
    }

    public fun getLoginResult(): LiveData<LoginResult> {
        return loginResult
    }

    public fun loginDataChanged(stuId: String, pwd: String) {
        if (!isStuIdValid(stuId))
            loginFormState.value = LoginFormState(R.string.invalid_username, null)
        else if (!isPwdValid(pwd))
            loginFormState.value = LoginFormState(null, R.string.invalid_password)
        else
            loginFormState.value = LoginFormState(isDataValid = true)

    }

    private fun isStuIdValid(stuId: String): Boolean {
        return stuId.trim().isNotEmpty()
    }

    private fun isPwdValid(pwd: String): Boolean {
        return pwd.trim().length > 5
    }

    public fun isLogged(): Boolean {
        return loginRepository.isLoggedIn
    }

}