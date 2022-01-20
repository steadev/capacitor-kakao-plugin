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
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.talk.model.Friend
import com.kakao.sdk.talk.model.FriendOrder
import com.kakao.sdk.talk.model.Order
import org.json.JSONArray

val gson = Gson()

class CapacitorKakao(var activity: AppCompatActivity) {
    fun kakaoLogin(call: PluginCall) {
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
                        val ret = JSObject()
                        ret.put("value", oAuthToken.accessToken)
                        call.resolve(ret)
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
                        ret.put("value", oAuthToken.accessToken)
                        call.resolve(ret)
                    } else {
                        call.reject("no_data")
                    }
                }
        }
    }

    fun kakaoLogout(call: PluginCall) {
        UserApiClient.instance
            .logout { error: Throwable? ->
                if (error != null) {
                    call.reject()
                } else {
                    call.resolve()
                }
            }
    }

    fun kakaoUnlink(call: PluginCall) {
        UserApiClient.instance
            .unlink { error: Throwable? ->
                if (error != null) {
                    call.reject()
                } else {
                    call.resolve()
                }
            }
    }

    fun sendLinkFeed(call: PluginCall) {
        val link = Link(call.getString("imageLinkUrl"), call.getString("imageLinkUrl"), null, null)
        val content = Content(call.getString("title")!!, call.getString("imageUrl")!!, link, call.getString("description"))
        val buttons = ArrayList<Button>()
        buttons.add(Button(call.getString("buttonTitle")!!, link))
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
                    call.reject("scopes agree failed")
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

    companion object {
        private const val TAG = "CapacitorKakao"
    }
}