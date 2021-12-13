# capacitor-kakao-plugin


Referenced [capacitor3-kakao-login](https://github.com/efluvi/capacitor3-kakao-login)


## Provided Functions

- [login](https://developers.kakao.com/docs/latest/ko/kakaologin/common#login)
- [logout](https://developers.kakao.com/docs/latest/ko/kakaologin/common#logout)
- [unlink](https://developers.kakao.com/docs/latest/ko/kakaologin/common#link-and-unlink)
- [link](https://developers.kakao.com/docs/latest/ko/message/common)
- [getting user info](https://developers.kakao.com/docs/latest/ko/kakaologin/common#user-info)
- [getting friends info](https://developers.kakao.com/docs/latest/ko/kakaotalk-social/common)
- [login with new scopes](https://developers.kakao.com/docs/latest/ko/kakaologin/common#additional-consent)
- [getting user scopes](https://developers.kakao.com/docs/latest/ko/kakaologin/js#check-consent)



## Install

```shell
npm install capacitor-kakao-login
npx cap sync
```



## Settings

### Android

- Set AndroidManifest.xml

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="io.ionic.starter">
  ...
  <!-- For Kakao Link (only if targeting Android 11) -->
+ <queries>
+   <package android:name="com.kakao.talk" />
+ </queries>
  ...
  <application 
    android:name=".GlobalApplication"
    ... >
+   <meta-data
+       android:name="com.kakao.sdk.AppKey"
+       android:value="@string/kakao_app_key" />
    <!-- For Login -->
+    <activity 
+       android:name="com.kakao.sdk.auth.AuthCodeHandlerActivity"
+       android:exported="true">
+       <intent-filter>
+           <action android:name="android.intent.action.VIEW" />
+           <category android:name="android.intent.category.DEFAULT" />
+           <category android:name="android.intent.category.BROWSABLE" />
+           <data android:host="oauth"
+                   android:scheme="kakao{NATIVE_APP_KEY}" />
+       </intent-filter>
+   </activity>
    <activity
            android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|smallestScreenSize|screenLayout|uiMode"
            android:name="io.ionic.starter.MainActivity"
            android:label="@string/title_activity_main"
            android:theme="@style/AppTheme.NoActionBarLaunch"
            android:launchMode="singleTask">
      ...
      <!-- For Kakao Link -->
+     <intent-filter>
+       <action android:name="android.intent.action.VIEW" />
+       <category android:name="android.intent.category.DEFAULT" />
+       <category android:name="android.intent.category.BROWSABLE" />
+       <data
+             android:host="@string/kakaolink_host"
+             android:scheme="@string/kakao_scheme" />
+     </intent-filter>
      ...
    </activity>
  </application>
</manifest>
```

- Set Kakao Repository to `build.gradle`

```shell
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://devrepo.kakao.com/nexus/content/groups/public/' }
    }
}
```

- Add Kakao Initialization

```java
package io.ionic.starter;
import android.app.Application;
import com.woot.plugins.kakao.CapacitorKakaoPlugin;

public class GlobalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CapacitorKakaoLoginPlugin.initKakaoSdk(this,getString(R.string.kakao_app_key));
    }
}
```

- Add kakao string variables

```xml
<string name="kakao_app_key">{NATIVE_APP_KEY}</string>
<string name="kakao_scheme">kakao{NATIVE_APP_KEY}</string>
<string name="kakaolink_host">kakaolink</string>
```



### IOS

- Add kakao values and schemes to `info.plist`

```xml
<dict>
  <array>
		<dict>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>kakao{NATIVE_APP_KEY}</string>
				<string>io.ionic.starter</string>
			</array>
		</dict>
	</array>
  
	<key>KAKAO_APP_KEY</key>
	<string>{NATIVE_APP_KEY}</string>
	<key>LSApplicationQueriesSchemes</key>
	<array>
		<string>kakao{NATIVE_APP_KEY}</string>
		<string>kakaokompassauth</string>
		<string>storykompassauth</string>
		<string>kakaolink</string>
		<string>storylink</string>
		<string>kakaotalk</string>
		<string>kakaotalk-5.9.7</string>
		<string>kakaostory-2.9.0</string>
  </array>
</dic>
```

- Add initial kakao codes to `AppDelegate.swift`

```swift
import UIKit
import Capacitor
import KakaoSDKAuth
import KakaoSDKCommon

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
  
  ...
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
    
    		// Initialize Kakao
+       let key = Bundle.main.infoDictionary?["KAKAO_APP_KEY"] as? String
+       KakaoSDK.initSDK(appKey: key!)
        return true
  }
  
  ...
  
  func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        // Called when the app was launched with a url. Feel free to add additional processing here,
        // but if you want the App API to support tracking app url opens, make sure to keep this call
    
    		// Need for Login with KakaoTalk
+       if (AuthApi.isKakaoTalkLoginUrl(url)) {
+           return AuthController.handleOpenUrl(url: url)
+       }
        
        return ApplicationDelegateProxy.shared.application(app, open: url, options: options)
  }
  
  ...
}
```



## APIs

- `initializeKakao(options)`
- `kakaoLogin()`
- `kakaoLogout()`
- `kakaoUnlink()`
- `sendLinkFeed(...)`
- `getUserInfo()`
- `getFriendList(...)`
- `loginWithNewScopes(...)`
- `getUserScopes()`



### initializeKakao(options) => Promise<void>

---

It is only for web implementation. If you want to use kakao functions in web environment, use this at the start of the app.

<b>Parameter</b>

```json
{
  webKey: {kakao_web_key}
}
```



### kakaoLogin() => Promise<{ value: string; }>

---

If user has kakaotalk app, `login with kakaotalk`. If not, `login with kakaoAccount`.
And the return value(access_token) doesn't needed in general. Kakao SDK automatically manage access_token and refresh_token

<b>Return</b>

```json
{
  value: {kakao_access_token}
}
```



### kakaoLogout() => Promise<void>

---

This function is used to log out the currently logged in user. 
Logout expires the token so that Kakao API calls can no longer be made with that access token.



### kakaoUnlink() => Promise<void>

---

This function is called to disconnect the app from the user.



### sendLinkFeed(options) => Promise<void>

---

This is a function to send a KakaoTalk message by composing a message in JSON format according to the template you want to use.

<b>Parameter</b>

```json
{ 
  title: string;
  description: string;
  imageUrl: string;
  imageLinkUrl: string;
  buttonTitle: string;
}
```



### getUserInfo() => Promise<{ value: any }>

---

Retrieves the information of the currently logged in user.

<b>Return</b>

see link below for detail return value
[Kakao user info Response](https://developers.kakao.com/docs/latest/ko/kakaologin/common#user-info)



### getFriendList(options) => Promise<{ value: any }>

---

Receives KakaoTalk friend information connected to the currently logged-in user's Kakao account.

@ISSUE
There is an error in KakaoTalk sdk (`Android`, `Javascript`). There is another option, `friendOrder`, but it doen't implemented in those platforms.

<b>Parameter</b>

```json
{
  offset?: number;
  limit?: number;
  order?: 'asc' | 'desc';
}
```

<b>Return</b>

```json
[
  {
    id: number;	// 회원번호
    uuid: string;	// 친구마다 고유한 값을 가지는 참고용 코드(Code) - 카카오톡 메시지 전송 시 사용
    favorite?: boolean;	// 해당 친구 즐겨찾기 여부
    profileNickname?: string; // 프로필 닉네임
    profileThumbnailImage?:	string;	// 프로필 썸네일(Thumbnail) 이미지. HTTPS만 지원
    allowedMsg?: boolean; // 메시지 차단 여부
	}
]
```



### loginWithNewScopes(scopes: string[]) => Promise<void>

---

This is a function that requests consent for items that the user does not agree to on the consent screen when logging in to Kakao for the first time, but must additionally agree to while using the service.

<b>Parameter</b>

scopes can find in your Kakao console
`https://developers.kakao.com/console/app/{your_kakao_app_id}/product/login/scope`



### getUserScopes() => Promise<{ value: KakaoScope[] }>

---

Retrieves the detailed information list of consent items that the user has agreed to.

<b>Return</b>

```json
[
  {
    agreed: boolean;
    displayName: string;
    id: string;
    revocable: boolean;
    type: string;
    using: boolean;
  }
]
```

