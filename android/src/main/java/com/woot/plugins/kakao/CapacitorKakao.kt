package com.woot.plugins.kakao

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.share.model.SharingResult
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.Link
import java.util.ArrayList
import com.google.gson.Gson
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.common.util.KakaoCustomTabsClient
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

    fun shareDefault(call: PluginCall) {
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
        val feed = FeedTemplate(content, null, null, buttons)
        if (ShareClient.instance.isKakaoTalkSharingAvailable(activity)) {
            ShareClient.instance
                .shareDefault(
                    activity,
                    feed
                ) { shareResult: SharingResult?, error: Throwable? ->
                    if (error != null) {
                        call.reject("kakao link failed")
                    } else if (shareResult != null) {
                        activity.startActivity(shareResult.intent)
                    }
                    call.resolve()
                }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            // 웹 공유 예시 코드
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(feed)
            var shareResult = true
            // CustomTabs으로 웹 브라우저 열기

            // 1. CustomTabsServiceConnection 지원 브라우저 열기
            // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
            try {
                KakaoCustomTabsClient.openWithDefault(activity, sharerUrl)
                call.resolve()
            } catch(e: UnsupportedOperationException) {
                // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
                shareResult = false
            }

            // 2. CustomTabsServiceConnection 미지원 브라우저 열기
            // ex) 다음, 네이버 등
            try {
                KakaoCustomTabsClient.open(activity, sharerUrl)
                call.resolve()
            } catch (e: ActivityNotFoundException) {
                // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
                shareResult = false
            }
        }
    }

    fun getUserInfo(call: PluginCall) {
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
                call.reject(error.toString());
            }
            else if (user != null) {
                Log.i(TAG, "사용자 정보 요청 성공" +
                        "\n회원번호: ${user.id}" +
                        "\n이메일: ${user.kakaoAccount?.email}" +
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                        "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
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
        // 카카오톡 친구 목록 가져오기 (기본)
        TalkApiClient.instance.friends(
            offset = offset, limit = limit, order = order
        ) { friends, error ->
            if (error != null) {
                Log.e(TAG, "카카오톡 친구 목록 가져오기 실패", error)
                call.reject("카카오톡 친구 목록 가져오기 실패 : " + error.toString())
            }
            else if (friends != null) {
                Log.i(TAG, "카카오톡 친구 목록 가져오기 성공 \n${friends.elements?.joinToString("\n")}")
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
            Log.d(TAG, "사용자에게 추가 동의를 받아야 합니다.")

            UserApiClient.instance.loginWithNewScopes(activity, scopes) { token, error ->
                if (error != null) {
                    Log.e(TAG, "사용자 추가 동의 실패", error)
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
                Log.e(TAG, "동의 정보 확인 실패", error)
                call.reject("동의 정보 확인 실패" + error.toString())
            }else if (scopeInfo != null) {
                Log.i(TAG, "동의 정보 확인 성공\n 현재 가지고 있는 동의 항목 $scopeInfo")
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
