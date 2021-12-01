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
    private var implementation: CapacitorKakao? = null
    override fun load() {
        super.load()
        implementation = CapacitorKakao(activity)
    }

    @PluginMethod
    fun initializeKakao(call: PluginCall) {
        val ret = JSObject()
        ret.put("value", "done")
        call.resolve(ret)
    }

    @PluginMethod
    fun kakaoLogin(call: PluginCall?) {
        implementation!!.kakaoLogin(call!!)
    }

    @PluginMethod
    fun kakaoLogout(call: PluginCall?) {
        implementation!!.kakaoLogout(call!!)
    }

    @PluginMethod
    fun kakaoUnlink(call: PluginCall?) {
        implementation!!.kakaoUnlink(call!!)
    }

    @PluginMethod
    fun sendLinkFeed(call: PluginCall?) {
        implementation!!.sendLinkFeed(call!!)
    }

    companion object {
        //SDK 초기화
        @JvmStatic
        fun initKakaoSdk(context: Context?, key: String?) {
            init(context!!, key!!)
        }
    }
}