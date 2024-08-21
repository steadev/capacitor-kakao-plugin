package com.woot.plugins.kakao

import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.kakao.sdk.common.KakaoSdk.init
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.PluginMethod
import com.getcapacitor.PluginCall

@CapacitorPlugin(name = "CapacitorKakao")
class CapacitorKakaoPlugin : Plugin() {
    private lateinit var implementation: CapacitorKakao
    override fun load() {
        super.load()
        implementation = CapacitorKakao(activity)
    }

    @PluginMethod
    fun initializeKakao(call: PluginCall) {
        implementation.initializeKakao(call)
    }

    @PluginMethod
    fun kakaoLogin(call: PluginCall) {
        implementation.kakaoLogin(call)
    }

    @PluginMethod
    fun kakaoLogout(call: PluginCall) {
        implementation.kakaoLogout(call)
    }

    @PluginMethod
    fun kakaoUnlink(call: PluginCall) {
        implementation.kakaoUnlink(call)
    }

    @PluginMethod
    fun shareDefault(call: PluginCall) {
        implementation.shareDefault(call)
    }

    @PluginMethod
    fun getUserInfo(call: PluginCall) {
        implementation.getUserInfo(call)
    }

    @PluginMethod
    fun getFriendList(call: PluginCall) {
        implementation.getFriendList(call)
    }

    @PluginMethod
    fun loginWithNewScopes(call: PluginCall) {
        implementation.loginWithNewScopes(call)
    }

    @PluginMethod
    fun getUserScopes(call: PluginCall) {
        implementation.getUserScopes(call)
    }

    companion object {
        //SDK 초기화
        @JvmStatic
        fun initKakaoSdk(context: Context?, key: String?) {
            init(context!!, key!!)
        }
    }
}
