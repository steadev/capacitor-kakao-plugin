import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorKakaoPlugin)
public class CapacitorKakaoPlugin: CAPPlugin {
    private let implementation = CapacitorKakao()

    //네이티브는 app delegate에서 초기화함
    @objc func initializeKakao(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.initializeKakao(call)
        }
    }
    
    @objc func kakaoLogin(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.kakaoLogin(call)
        }
    }
    
    @objc func kakaoLogout(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.kakaoLogout(call)
        }
        
    }
    
    @objc func kakaoUnlink(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.kakaoUnlink(call)
        }
    }
    
    @objc func shareDefault(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.shareDefault(call)
        }
    }

    @objc func getUserInfo(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.getUserInfo(call)
        }
    }
    @objc func getFriendList(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.getFriendList(call)
        }
    }

    @objc func loginWithNewScopes(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.loginWithNewScopes(call)
        }
    }

    @objc func getUserScopes(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.implementation.getUserScopes(call)
        }
    }
}
