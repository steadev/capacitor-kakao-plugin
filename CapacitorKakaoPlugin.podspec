require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))
kakao_sdk_version = "2.22.5"

Pod::Spec.new do |s|
  s.name = 'CapacitorKakaoPlugin'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['repository']['url']
  s.author = package['author']
  s.source = { :git => package['repository']['url'], :tag => s.version.to_s }
  s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
  s.ios.deployment_target  = '13.0'
  s.dependency 'Capacitor'
  s.dependency 'KakaoSDKCommon', kakao_sdk_version
  s.dependency 'KakaoSDKAuth', kakao_sdk_version
  s.dependency 'KakaoSDKUser', kakao_sdk_version
  s.dependency 'KakaoSDKTalk', kakao_sdk_version
  s.dependency 'KakaoSDKShare', kakao_sdk_version
  s.dependency 'KakaoSDKTemplate', kakao_sdk_version
  s.swift_version = '5.1'
end