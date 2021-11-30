package com.woot.plugins.kakao;

import android.content.Context;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.link.LinkClient;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.user.UserApiClient;
import java.util.ArrayList;

@CapacitorPlugin(name = "CapacitorKakao")
public class CapacitorKakaoPlugin extends Plugin {

    private CapacitorKakao implementation;

    //SDK 초기화
    public static void initKakaoSdk(Context context, String key) {
        KakaoSdk.init(context, key);
    }

    @Override
    public void load() {
        super.load();
        implementation = new CapacitorKakao(getActivity());
    }

    @PluginMethod
    public void initializeKakao(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", "done");
        call.resolve(ret);
    }

    @PluginMethod
    public void kakaoLogin(PluginCall call) {
        implementation.kakaoLogin(call);
    }

    @PluginMethod
    public void kakaoLogout(PluginCall call) {
        implementation.kakaoLogout(call);
    }

    @PluginMethod
    public void kakaoUnlink(PluginCall call) {
        implementation.kakaoUnlink(call);
    }

    @PluginMethod
    public void sendLinkFeed(PluginCall call) {
        implementation.sendLinkFeed(call);
    }
}