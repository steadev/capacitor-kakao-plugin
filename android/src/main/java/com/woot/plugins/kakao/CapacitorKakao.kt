package com.woot.plugins.kakao

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.link.model.LinkResult
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.Link
import java.util.ArrayList
import com.google.gson.Gson
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.talk.model.Order
import org.json.JSONArray

val gson = Gson()

enum class TokenStatus {
    LOGIN_NEEDED,
    ERROR,
    SUCCEED
}


class CapacitorKakao(var activity: AppCompatActivity) {
    fun initializeKakao(call: PluginCall) {
        tokenAvailability() { status: TokenStatus ->
            val ret = JSObject()
            ret.put("status", status.toString())
            call.resolve(ret)
        }
    }

    fun kakaoLogin(call: PluginCall) {
        var serviceTermsParam = call.getArray("serviceTerms");
        serviceTermsParam = if (serviceTermsParam === null || serviceTermsParam.length() === 0) null else serviceTermsParam;

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            if (serviceTermsParam != null) {
                UserApiClient.instance.loginWithKakaoTalk(
                    activity,
                    serviceTerms = serviceTermsParam.toList<String>()
                ) { oAuthToken: OAuthToken?, error: Throwable? ->
                    handleKakaoLoginResponse(call, oAuthToken, error)
                }    
            } else {
                UserApiClient.instance.loginWithKakaoTalk(
                    activity
                ) { oAuthToken: OAuthToken?, error: Throwable? ->
                    handleKakaoLoginResponse(call, oAuthToken, error)
                }
            }
        } else {
            if (serviceTermsParam != null) {
                UserApiClient.instance.loginWithKakaoAccount(
                    activity,
                    serviceTerms = serviceTermsParam.toList<String>()
                ) { oAuthToken: OAuthToken?, error: Throwable? ->
                    handleKakaoLoginResponse(call, oAuthToken, error)
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(
                    activity
                ) { oAuthToken: OAuthToken?, error: Throwable? ->
                    handleKakaoLoginResponse(call, oAuthToken, error)
                }
            }
        }
    }

    private fun handleKakaoLoginResponse(call: PluginCall, oAuthToken: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Log.e(TAG, "login fail : ", error)
            call.reject(error.toString())
        } else if (oAuthToken != null) {
            Log.i(TAG, "login success : " + oAuthToken.accessToken)
            val ret = JSObject()
            ret.put("accessToken", oAuthToken.accessToken)
            ret.put("refreshToken", oAuthToken.refreshToken)
            call.resolve(ret)
        } else {
            call.reject("no_data")
        }
    }

    fun kakaoLogout(call: PluginCall) {
        UserApiClient.instance
            .logout { error: Throwable? ->
                if (error != null) {
                    call.reject("logout failed")
                } else {
                    call.resolve()
                }
            }
    }

    fun kakaoUnlink(call: PluginCall) {
        UserApiClient.instance
            .unlink { error: Throwable? ->
                if (error != null) {
                    call.reject("unlink failed")
                } else {
                    call.resolve()
                }
            }
    }

    fun sendLinkFeed(call: PluginCall) {
        val imageLinkUrl = call.getString("imageLinkUrl")
        val imageUrl: String = if (call.getString("imageUrl") === null) "" else call.getString("imageUrl")!!
        val title: String = if (call.getString("title") === null) "" else call.getString("title")!!
        val description = call.getString("description")
        val buttonTitle: String = if (call.getString("buttonTitle") === null) "" else call.getString("buttonTitle")!!
        val imageWidth: Int? = call.getInt("imageWidth")
        val imageHeight: Int? = call.getInt("imageHeight")
        
        val link = Link(imageLinkUrl, imageLinkUrl, null, null)
        val content = Content(title, imageUrl, link, description, imageWidth, imageHeight)
        val buttons = ArrayList<Button>()
        buttons.add(Button(buttonTitle, link))
        val feed = FeedTemplate(content, null, buttons)
        LinkClient.instance
            .defaultTemplate(
                    activity,
                    feed
            ) { linkResult: LinkResult?, error: Throwable? ->
                if (error != null) {
                    call.reject("kakao link failed: " + error.toString())
                } else if (linkResult != null) {
                    activity.startActivity(linkResult.intent)
                }
                call.resolve()
            }
    }

    fun getUserInfo(call: PluginCall) {
        // ????????? ?????? ?????? (??????)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "????????? ?????? ?????? ??????", error)
                call.reject(error.toString());
            }
            else if (user != null) {
                Log.i(TAG, "????????? ?????? ?????? ??????" +
                        "\n????????????: ${user.id}" +
                        "\n?????????: ${user.kakaoAccount?.email}" +
                        "\n?????????: ${user.kakaoAccount?.profile?.nickname}" +
                        "\n???????????????: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
                val userJsonData = JSObject(gson.toJson(user).toString())
                val ret = JSObject()
                ret.put("value", userJsonData)
                call.resolve(ret)
            }
        }
    }

    fun getFriendList(call: PluginCall) {
        val offset = call.getInt("offset")
        val limit = call.getInt("limit")
        var order = Order.ASC
        if (call.getString("order")?.toUpperCase() == "DESC") {
            order = Order.DESC
        }
        // ???????????? ?????? ?????? ???????????? (??????)
        TalkApiClient.instance.friends(
            offset = offset, limit = limit, order = order
        ) { friends, error ->
            if (error != null) {
                Log.e(TAG, "???????????? ?????? ?????? ???????????? ??????", error)
                call.reject("???????????? ?????? ?????? ???????????? ?????? : " + error.toString())
            }
            else if (friends != null) {
                Log.i(TAG, "???????????? ?????? ?????? ???????????? ?????? \n${friends.elements?.joinToString("\n")}")
                val friendList = ArrayList<JSObject>()
                if (friends.elements != null) {
                    for (friend in friends.elements!!) {
                        friendList.add(JSObject(gson.toJson(friend).toString()))
                    }
                }
                val jsonArray = JSONArray(friendList)
                val ret = JSObject()
                ret.put("value", jsonArray)
                call.resolve(ret);
            }
        }
    }
    
    fun loginWithNewScopes(call: PluginCall) {
        var scopes = mutableListOf<String>()
        val tobeAgreedScopes = call.getArray("scopes").toList<String>()
        for (scope in tobeAgreedScopes) {
            scopes.add(scope)
        }
        if (scopes.count() > 0) {
            Log.d(TAG, "??????????????? ?????? ????????? ????????? ?????????.")

            UserApiClient.instance.loginWithNewScopes(activity, scopes) { token, error ->
                if (error != null) {
                    Log.e(TAG, "????????? ?????? ?????? ??????", error)
                    // call.reject("scopes agree failed")
                    if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                        UserApiClient.instance
                            .loginWithKakaoTalk(
                                    activity
                            ) { oAuthToken: OAuthToken?, error: Throwable? ->
                                if (error != null) {
                                    Log.e(TAG, "login fail : ", error)
                                    call.reject(error.toString())
                                } else if (oAuthToken != null) {
                                    Log.i(TAG, "login success : " + oAuthToken.accessToken)
                                    loginWithNewScopes(call)
                                } else {
                                    call.reject("no_data")
                                }
                            }
                    } else {
                        UserApiClient.instance
                            .loginWithKakaoAccount(
                                    activity
                            ) { oAuthToken: OAuthToken?, error: Throwable? ->
                                if (error != null) {
                                    Log.e(TAG, "login fail : ", error)
                                    call.reject(error.toString())
                                } else if (oAuthToken != null) {
                                    Log.i(TAG, "login success : " + oAuthToken.accessToken)
                                    val ret = JSObject()
                                    ret.put("accessToken", oAuthToken.accessToken)
                                    ret.put("refreshToken", oAuthToken.refreshToken)
                                    call.resolve(ret)
                                } else {
                                    call.reject("no_data")
                                }
                            }
                    }
                } else {
                    Log.d(TAG, "allowed scopes: ${token!!.scopes}")
                    call.resolve()
                }
            }
        } else {
            call.resolve()
        }
    }

    fun getUserScopes(call: PluginCall) {
        UserApiClient.instance.scopes { scopeInfo, error->
            if (error != null) {
                Log.e(TAG, "?????? ?????? ?????? ??????", error)
                call.reject("?????? ?????? ?????? ??????" + error.toString())
            }else if (scopeInfo != null) {
                Log.i(TAG, "?????? ?????? ?????? ??????\n ?????? ????????? ?????? ?????? ?????? $scopeInfo")
                val scopeList = JSArray()
                if (scopeInfo.scopes != null) {
                    for (scope in scopeInfo.scopes!!) {
                        scopeList.put(scope)
                    }
                }
                val ret = JSObject()
                ret.put("value", scopeList)
                call.resolve(ret);
            }
        }
    }

    private fun tokenAvailability(callback: (TokenStatus) -> Unit) {
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError() == true) {
                        callback(TokenStatus.LOGIN_NEEDED)
                    }
                    else {
                        callback(TokenStatus.ERROR)
                    }
                }
                else {
                    callback(TokenStatus.SUCCEED)
                }
            }
        }
        else {
            callback(TokenStatus.LOGIN_NEEDED)
        }
    }

    companion object {
        private const val TAG = "CapacitorKakao"
    }
}
