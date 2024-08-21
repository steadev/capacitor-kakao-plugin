import Foundation
import Capacitor

import KakaoSDKUser
import KakaoSDKCommon
import KakaoSDKShare
import KakaoSDKTemplate
import KakaoSDKAuth
import KakaoSDKTalk

extension Encodable {
    
    var toDictionary : [String: Any]? {
        guard let object = try? JSONEncoder().encode(self) else { return nil }
        guard let dictionary = try? JSONSerialization.jsonObject(with: object, options: []) as? [String:Any] else { return nil }
        return dictionary
    }
}

enum TokenStatus: String {
    case LOGIN_NEEDED
    case ERROR
    case SUCCEED
}

@objc public class CapacitorKakao: NSObject {

    @objc public func initializeKakao(_ call: CAPPluginCall) -> Void {
        tokenAvailability { (status: TokenStatus) in
            call.resolve([
                "status": status.rawValue
            ])
        }
    }

    @objc public func kakaoLogin(_ call: CAPPluginCall) -> Void {
        let serviceTerms = call.getArray("serviceTerms", String.self) ?? nil
        // 카카오톡 설치 여부 확인
        // if kakaotalk app exists, login with app. else, login with web
        if (UserApi.isKakaoTalkLoginAvailable()) {
            if serviceTerms == nil {
                UserApi.shared.loginWithKakaoTalk {(oauthToken, error) in
                    self.handleKakaoLoginResponse(call, oauthToken: oauthToken, error: error)
                }
            } else {
                UserApi.shared.loginWithKakaoTalk(serviceTerms: serviceTerms, completion: {(oauthToken, error) in
                    self.handleKakaoLoginResponse(call, oauthToken: oauthToken, error: error)
                })
            }
        }
        else{
            if serviceTerms == nil {
                UserApi.shared.loginWithKakaoAccount {(oauthToken, error) in
                    self.handleKakaoLoginResponse(call, oauthToken: oauthToken, error: error)
                }
            } else {
                UserApi.shared.loginWithKakaoAccount(serviceTerms: serviceTerms, completion: {(oauthToken, error) in
                    self.handleKakaoLoginResponse(call, oauthToken: oauthToken, error: error)
                })
            }
            
        }
    }
    
    private func handleKakaoLoginResponse(_ call: CAPPluginCall, oauthToken: OAuthToken?, error: Error?) -> Void {
        if error != nil {
            call.reject("error")
        }
        else {
            call.resolve([
                "accessToken": oauthToken?.accessToken ?? "",
                "refreshToken": oauthToken?.refreshToken ?? ""
            ])
        }
    }
    
    @objc public func kakaoLogout(_ call: CAPPluginCall) -> Void {

        UserApi.shared.logout {(error) in
            if let error = error {
                print(error)
                call.reject("error")
            }
            else {
                call.resolve()
            }
        }
    }
    
    
    
    @objc public func kakaoUnlink(_ call: CAPPluginCall) -> Void {

        UserApi.shared.unlink {(error) in
            if let error = error {
                print(error)
                call.reject("error")
            }
            else {
                call.resolve()
            }
        }
    }
    
    
    @objc public func shareDefault(_ call: CAPPluginCall) -> Void {

        let title = call.getString("title") ?? ""
        let description = call.getString("description") ?? ""
        let imageUrl = call.getString("imageUrl") ?? ""
        let imageLinkUrl = call.getString("imageLinkUrl") ?? ""
        let buttonTitle = call.getString("buttonTitle") ?? ""
        let imageWidth: Int? = call.getInt("imageWidth")
        let imageHeight: Int? = call.getInt("imageHeight")

        
        
        let link = Link(webUrl: URL(string:imageLinkUrl),
                        mobileWebUrl: URL(string:imageLinkUrl))

        let button = Button(title: buttonTitle, link: link)
        let content = Content(title: title,
                              imageUrl: URL(string:imageUrl)!,
                              imageWidth: imageWidth,
                              imageHeight: imageHeight,
                              description: description,
                              link: link)
        let feedTemplate = FeedTemplate(content: content, social: nil, buttons: [button])

        //메시지 템플릿 encode
        if let feedTemplateJsonData = (try? SdkJSONEncoder.custom.encode(feedTemplate)) {

        //생성한 메시지 템플릿 객체를 jsonObject로 변환
            if let templateJsonObject = SdkUtils.toJsonObject(feedTemplateJsonData) {
                if ShareApi.isKakaoTalkSharingAvailable() {
                    ShareApi.shared.shareDefault(templateObject:templateJsonObject) {(linkResult, error) in
                        if let error = error {
                            print(error)
                            call.reject("error")
                        }
                        else {

                            //do something
                            guard let linkResult = linkResult else { return }
                            UIApplication.shared.open(linkResult.url, options: [:], completionHandler: nil)
                            
                            call.resolve()
                        }
                    }
                } else {
                    call.reject("not implemented")
                    // 카카오톡 미설치: 웹 공유 사용 권장
                    // Custom WebView 또는 디폴트 브라우져 사용 가능
                    // 웹 공유 예시 코드
//                        if let url = ShareApi.shared.makeDefaultUrl(templateObject: templateJsonObject) {
//
//                            self.safariViewController = SFSafariViewController(url: url)
//                            self.safariViewController?.modalTransitionStyle = .crossDissolve
//                            self.safariViewController?.modalPresentationStyle = .overCurrentContext
//                            self.present(self.safariViewController!, animated: true) {
//                                print("웹 present success")
//                            }
//                        }
                }
            }
        }
    }

    @objc public func getUserInfo(_ call: CAPPluginCall) -> Void {
        UserApi.shared.me() {(user, error) in
            if let error = error {
                print(error)
                call.reject("me() failed.")
            }
            else {
                print("me() success.")
                call.resolve([
                    "value": user?.toDictionary as Any
                ])
            }
        }
    }

    @objc public func getFriendList(_ call: CAPPluginCall) -> Void {
        let offset = call.getInt("offset")
        let limit = call.getInt("limit")
        var order = Order.Asc
        if (call.getString("order")?.uppercased() == "DESC") {
            order = Order.Desc
        }
        TalkApi.shared.friends (
            offset:offset, limit:limit, order: order
        ) {(friends, error) in
            if let error = error {
                print(error)
                call.reject("getFriendList() failed.")
            }
            else {
                print("getFriendList() success")
                let friendList = friends?.toDictionary
                call.resolve([
                    "value": (friendList != nil) ? friendList!["elements"] as Any : [] as Any
                ])
            }
        }
    }

    @objc public func loginWithNewScopes(_ call: CAPPluginCall) ->  Void {
        var scopes = [String]()
        guard let tobeAgreedScopes = call.getArray("scopes", String.self) else {
            call.reject("scopes agree failed")
            return
        }
        for scope in tobeAgreedScopes {
            scopes.append(scope)
        }
            
        if scopes.count == 0  {
            call.resolve()
            return
        }

        //필요한 scope으로 토큰갱신을 한다.
        UserApi.shared.loginWithKakaoAccount(scopes: scopes) { (_, error) in
            if let error = error {
                print(error)
                call.reject("scopes agree failed")
            }
            else {
                call.resolve()
            }

        }
    }

    @objc public func getUserScopes(_ call: CAPPluginCall) -> Void {
        UserApi.shared.scopes() { (scopeInfo, error) in
            if error != nil {
                call.reject("get kakao user scope failed : ")
            }
            else {
                let scopeInfoDict = scopeInfo?.toDictionary
                call.resolve([
                    "value": (scopeInfoDict != nil) ? scopeInfoDict!["scopes"] as Any : [] as Any
                ])
            }
        }
    }

    private func tokenAvailability(completion: @escaping ((TokenStatus) -> Void)) -> Void {
        if (AuthApi.hasToken()) {
            UserApi.shared.accessTokenInfo { (_, error) in
                if let error = error {
                    if let sdkError = error as? SdkError, sdkError.isInvalidTokenError() == true  {
                        completion(TokenStatus.LOGIN_NEEDED)
                    }
                    else {
                        //기타 에러
                        completion(TokenStatus.ERROR)
                    }
                }
                else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                    completion(TokenStatus.SUCCEED)
                }
            }
        }
        else {
            completion(TokenStatus.LOGIN_NEEDED)
        }
    }
}
