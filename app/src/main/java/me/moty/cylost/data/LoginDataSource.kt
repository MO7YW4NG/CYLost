package me.moty.cylost.data

import com.google.gson.Gson
import me.moty.cylost.data.model.LoggedInUser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String, callback: (Result<LoggedInUser>) -> Unit) {
        try {
            val url = "https://myself.cycu.edu.tw/auth/myselfLogin"
            val body =
                FormBody.Builder().add("UserNm", username).add("UserPasswd", password).build()
            val req = Request.Builder().post(body).url(url).build()
            OkHttpClient().newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback(Result.Error(IOException("Error logging in", e)))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code != 200) {
                        callback(Result.Error(IOException("Error logging in code " + response.code)))
                        return
                    }
                    if (response.body == null) {
                        callback(Result.Error(IOException("Error ")))
                        return
                    }
                    val json = response.body?.string()
                    if (json.equals("伺服器執行錯誤(i)", true)) {
                        callback(Result.Error(IOException("Error ")))
                        return
                    }
                    val info = Gson().fromJson(json, LoginInfo::class.java)
                    if (info.login_YN == "N") {
                        callback(Result.Error(IOException("Error logging in code " + info.d_Message_C)))
                        return
                    }
                    callback(Result.Success(LoggedInUser(username)))
                }

            })
        } catch (e: Throwable) {
            callback(Result.Error(IOException("Error logging in", e)))
        }
    }

    class LoginInfo {
        lateinit var d_Message_C: String
        lateinit var done_YN: String
        lateinit var login_YN: String
    }

    fun logout() {

    }
}