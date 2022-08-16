//
//  RNKakaoLogins.swift
//  RNKakaoLogins
//
//  Created by hyochan on 2021/03/18.
//  Copyright © 2021 Facebook. All rights reserved.
//

import Foundation

import KakaoSDKUser
import KakaoSDKCommon
import KakaoSDKLink
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

@objc(RNKakaoLogins)
class RNKakaoLogins: NSObject {

    public override init() {
        let appKey: String? = Bundle.main.object(forInfoDictionaryKey: "KAKAO_APP_KEY") as? String
        KakaoSDK.initSDK(appKey: appKey!)
    }
    
    @objc(initializeKakao:rejecter:)
    func initializeKakao(_ resolve: @escaping RCTPromiseResolveBlock,
                    rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            self.tokenAvailability { (status: TokenStatus) in
                resolve([
                    "status": status.rawValue
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

   @objc
   static func requiresMainQueueSetup() -> Bool {
     return true
   }

   @objc(isKakaoTalkLoginUrl:)
   public static func isKakaoTalkLoginUrl(url:URL) -> Bool {

       let appKey = try? KakaoSDK.shared.appKey();

       if (appKey != nil) {
           return AuthApi.isKakaoTalkLoginUrl(url)
       }
       return false
   }

    @objc(handleOpenUrl:)
    public static func handleOpenUrl(url:URL) -> Bool {
       return AuthController.handleOpenUrl(url: url)
    }

    @objc(login:resolver:rejecter:)
    func login(_ serviceTerms: NSArray?, resolver resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            // 카카오톡 설치 여부 확인
            // if kakaotalk app exists, login with app. else, login with web
            if (UserApi.isKakaoTalkLoginAvailable()) {
                if serviceTerms == nil {
                    UserApi.shared.loginWithKakaoTalk {(oauthToken, error) in
                        self.handleKakaoLoginResponse(resolve: resolve, reject: reject, oauthToken: oauthToken, error: error)
                    }
                } else {
                    UserApi.shared.loginWithKakaoTalk(serviceTerms: serviceTerms as? [String], completion: {(oauthToken, error) in
                        self.handleKakaoLoginResponse(resolve: resolve, reject: reject, oauthToken: oauthToken, error: error)
                    })
                }
            }
            else{
                if serviceTerms == nil {
                    UserApi.shared.loginWithKakaoAccount {(oauthToken, error) in
                        self.handleKakaoLoginResponse(resolve: resolve, reject: reject, oauthToken: oauthToken, error: error)
                    }
                } else {
                    UserApi.shared.loginWithKakaoAccount(serviceTerms: serviceTerms as? [String], completion: {(oauthToken, error) in
                        self.handleKakaoLoginResponse(resolve: resolve, reject: reject, oauthToken: oauthToken, error: error)
                    })
                }
            }
        }
    }
    
    @objc(loginWithNewScopes:resolver:rejecter:)
    func loginWithNewScopes(_ scopes: NSArray, resolver resolve: @escaping RCTPromiseResolveBlock,
                                         rejecter reject: @escaping RCTPromiseRejectBlock) ->  Void {
        DispatchQueue.main.async {
            if scopes.count == 0  {
                resolve("empty scopes")
                return
            }
            
            var scopeParam = [String]()
            for scope in scopes {
                scopeParam.append(scope as! String)
            }

            print(scopeParam)
            //필요한 scope으로 토큰갱신을 한다.
            UserApi.shared.loginWithKakaoAccount(scopes: scopeParam) { (oauthToken, error) in
                self.handleKakaoLoginResponse(resolve: resolve, reject: reject, oauthToken: oauthToken, error: error)
            }
        }
    }
    
    private func handleKakaoLoginResponse(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock, oauthToken: OAuthToken?, error: Error?) -> Void {
        if let error = error {
            reject("RNKakaoLogins", error.localizedDescription, nil)
        }
        else {
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss";
            resolve([
                "accessToken": oauthToken?.accessToken ?? "",
                "refreshToken": oauthToken?.refreshToken ?? "" as Any,
                "idToken": oauthToken?.idToken ?? "",
                "accessTokenExpiresAt": dateFormatter.string(from: oauthToken!.expiredAt),
                "refreshTokenExpiresAt": dateFormatter.string(from: oauthToken!.refreshTokenExpiredAt),
                "scopes": oauthToken?.scopes ?? "",
            ])
        }
    }

//
    @objc(logout:rejecter:)
    func logout(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.logout {(error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve("Successfully logged out")
                }
            }
        }
    }

    @objc(unlink:rejecter:)
    func unlink(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.unlink {(error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve("Successfully unlinked")
                }
            }
        }
    }

    @objc(getAccessToken:rejecter:)
    func getAccessToken(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.accessTokenInfo {(accessTokenInfo, error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve([
                        "accessToken": TokenManager.manager.getToken()?.accessToken as Any,
                        "expiresIn": accessTokenInfo?.expiresIn as Any,
                    ])
                }
            }
        }
    }

    @objc(getProfile:rejecter:)
    func getProfile(_ resolve: @escaping RCTPromiseResolveBlock,
               rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UserApi.shared.me() {(user, error) in
                if let error = error {
                    reject("RNKakaoLogins", error.localizedDescription, nil)
                }
                else {
                    resolve([
                        "id": user?.id as Any,
                        "name": user?.kakaoAccount?.name as Any,
                        "email": user?.kakaoAccount?.email as Any,
                        "nickname": user?.kakaoAccount?.profile?.nickname as Any,
                        "profileImageUrl": user?.kakaoAccount?.profile?.profileImageUrl?.absoluteString as Any,
                        "thumbnailImageUrl": user?.kakaoAccount?.profile?.thumbnailImageUrl?.absoluteString as Any,
                        "phoneNumber": user?.kakaoAccount?.phoneNumber as Any,
                        "ageRange": user?.kakaoAccount?.ageRange?.rawValue as Any,
                        "birthday": user?.kakaoAccount?.birthday as Any,
                        "birthdayType": user?.kakaoAccount?.birthdayType as Any,
                        "birthyear": user?.kakaoAccount?.birthyear as Any,
                        "gender": user?.kakaoAccount?.gender?.rawValue as Any,
                        "isEmailValid": user?.kakaoAccount?.isEmailValid as Any,
                        "isEmailVerified": user?.kakaoAccount?.isEmailVerified as Any,
                        "isKorean": user?.kakaoAccount?.isKorean as Any,
                        "ageRangeNeedsAgreement": user?.kakaoAccount?.ageRangeNeedsAgreement as Any,
                        "birthdayNeedsAgreement": user?.kakaoAccount?.birthdayNeedsAgreement as Any,
                        "birthyearNeedsAgreement": user?.kakaoAccount?.birthyearNeedsAgreement as Any,
                        "emailNeedsAgreement": user?.kakaoAccount?.emailNeedsAgreement as Any,
                        "genderNeedsAgreement": user?.kakaoAccount?.genderNeedsAgreement as Any,
                        "isKoreanNeedsAgreement": user?.kakaoAccount?.isKoreanNeedsAgreement as Any,
                        "phoneNumberNeedsAgreement": user?.kakaoAccount?.phoneNumberNeedsAgreement as Any,
                        "profileNeedsAgreement": user?.kakaoAccount?.profileNeedsAgreement as Any,
                    ])
                }
            }
        }
    }
    
    @objc(sendLinkFeed:resolver:rejecter:)
    func sendLinkFeed(_ data: [String: Any], resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let title = data["title"] ?? ""
            let description = data["description"] ?? ""
            let imageUrl = data["imageUrl"] ?? ""
            let imageLinkUrl = data["imageLinkUrl"] ?? ""
            let buttonTitle = data["buttonTitle"] ?? ""
            let imageWidth: Int? = data["imageWidth"] as! Int?
            let imageHeight: Int? = data["imageHeight"] as! Int?

                
                
            let link = Link(webUrl: URL(string:imageLinkUrl as! String),
                            mobileWebUrl: URL(string:imageLinkUrl as! String))

            let button = Button(title: buttonTitle as! String, link: link)
            let content = Content(title: title as! String,
                                  imageUrl: URL(string:imageUrl as! String)!,
                                  imageWidth: imageWidth,
                                  imageHeight: imageHeight,
                                  description: description as? String,
                                  link: link)
            let feedTemplate = FeedTemplate(content: content, social: nil, buttons: [button])

            //메시지 템플릿 encode
            if let feedTemplateJsonData = (try? SdkJSONEncoder.custom.encode(feedTemplate)) {

            //생성한 메시지 템플릿 객체를 jsonObject로 변환
                if let templateJsonObject = SdkUtils.toJsonObject(feedTemplateJsonData) {
                    LinkApi.shared.defaultLink(templateObject:templateJsonObject) {(linkResult, error) in
                        if let error = error {
                            print(error)
                            reject("RNKakaoLogins", error.localizedDescription, nil)
                        }
                        else {

                            //do something
                            guard let linkResult = linkResult else { return }
                            UIApplication.shared.open(linkResult.url, options: [:], completionHandler: nil)
                            
                            resolve("succeed")
                        }
                    }
                }
            }
        }
    }
}
