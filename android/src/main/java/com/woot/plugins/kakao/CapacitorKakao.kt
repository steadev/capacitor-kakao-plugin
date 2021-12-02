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
                    null
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
                    null
                }
        }
    }

    fun kakaoLogout(call: PluginCall) {
        UserApiClient.instance
            .logout { error: Throwable? ->
                val ret = JSObject()
                ret.put("value", "done")
                call.resolve(ret)
                null
            }
    }

    fun kakaoUnlink(call: PluginCall) {
        UserApiClient.instance
            .unlink { error: Throwable? ->
                val ret = JSObject()
                ret.put("value", "done")
                call.resolve(ret)
                null
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
                val ret = JSObject()
                ret.put("value", "done")
                call.resolve(ret)
                null
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
        // 카카오톡 친구 목록 가져오기 (기본)
        TalkApiClient.instance.friends { friends, error ->
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

    companion object {
        private const val TAG = "CapacitorKakao"
    }
}