package com.woot.plugins.kakao

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
        val link = Link(call.getString("image_link_url"), call.getString("image_link_url"), null, null)
        val content = Content(call.getString("title")!!, call.getString("image_url")!!, link, call.getString("description"))
        val buttons = ArrayList<Button>()
        buttons.add(Button(call.getString("button_title")!!, link))
        val feed = FeedTemplate(content, null, buttons)
        LinkClient.instance
                .defaultTemplate(
                        activity,
                        feed
                ) { linkResult: LinkResult?, error: Throwable? ->
                    if (error != null) {
                    } else if (linkResult != null) {
                        activity.startActivity(linkResult.intent)
                    }
                    val ret = JSObject()
                    ret.put("value", "done")
                    call.resolve(ret)
                    null
                }
    }

    companion object {
        private const val TAG = "CapacitorKakao"
    }
}