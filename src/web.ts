import { WebPlugin } from '@capacitor/core';
import * as Kakao from './assets/kakao-sdk';
import type { CapacitorKakaoPlugin, KakaoScope } from './definitions';

const KakaoSdk: any = Kakao;

export class CapacitorKakaoWeb extends WebPlugin implements CapacitorKakaoPlugin {
  webKey: any;

  initializeKakao(options: { appKey: string, webKey: string }) {
    return new Promise<{ value: string }>(resolve => {
      if (this.webKey === undefined) {
        this.webKey = options.webKey;
      }
      Kakao.init(this.webKey);
      resolve({ value: 'done' });
    });
  }

  //웹 카카오 로그인
  kakaoLogin() {
    return new Promise<{ value: string }>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }
      KakaoSdk.Auth.login({
        success: (authObj: any) => {
          const { access_token } = authObj;
          this.setAccessToken(access_token);
          resolve({ value: access_token });
        },
        fail: (err: any) => {
          console.error(err);
          reject(err);
        },
      });
    });
  }

  //웹 로그아웃
  kakaoLogout() {
    return new Promise<{ value: string }>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }

      KakaoSdk.Auth.logout();
      resolve({ value: 'done' });
    });
  }

  //unlink
  kakaoUnlink() {
    return new Promise<{ value: string }>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }

      KakaoSdk.API.request({
        url: '/v1/user/unlink',
        success: function (response: any) {
          console.log(response);
          resolve({ value: 'done' });
        },
        fail: function (error: any) {
          console.log(error);
          reject(error);
        },
      });
    });
  }

  //message
  sendLinkFeed(options: {
    title: string;
    description: string;
    imageUrl: string;
    imageLinkUrl: string;
    buttonTitle: string;
  }) {
    return new Promise<{ value: string }>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }
      KakaoSdk.Link.sendDefault({
        objectType: 'feed',
        content: {
          title: options.title,
          description: options.description,
          imageUrl: options.imageUrl,
          link: {
            mobileWebUrl: options.imageLinkUrl,
          },
        },
        buttons: [
          {
            title: options.buttonTitle,
            link: {
              mobileWebUrl: options.imageLinkUrl,
            },
          },
        ],
        callback: function () {
          resolve({ value: 'done' });
        },
      });
    });
  }

  async getUserInfo(): Promise<{ value: any }> {
    return new Promise((resolve, reject) => {
      KakaoSdk.API.request({
        url: '/v2/user/me',
        success: (result: any) => {
          resolve({ value: result });
        },
        fail: (error: any) => {
          reject(error);
        }
      });
    })
  }
  async getFriendList(options?: {
    offset?: number;
    limit?: number;
    order?: 'asc' | 'desc',
    friendOrder?: 'FAVORITE' | 'NICKNAME'
  }): Promise<{ value: any }> {
    return new Promise((resolve, reject) => {
      KakaoSdk.API.request({
        url: '/v1/api/talk/friends',
        data: options,
        success: (result: any) => {
          resolve({ value: result });
        },
        fail: (error: any) => {
          reject(error);
        }
      });
    })
  }

  async loginWithNewScopes(scopes?: string[]): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }
      KakaoSdk.Auth.login({
        scope: scopes ? scopes.join(',') : undefined,
        success: (authObj: any) => {
          const { access_token } = authObj;
          this.setAccessToken(access_token);
          resolve();
        },
        fail: (err: any) => {
          console.error(err);
          reject(err);
        },
      });
    });
  }

  async getUserScopes(): Promise<{ value: KakaoScope[]}> {
    return new Promise((resolve, reject) => {
      KakaoSdk.API.request({
        url: '/v2/user/scopes',
        success: (result: any) => {
          resolve({ value: result.scopes as KakaoScope[] });
        },
        fail: (error: any) => {
          reject(error);
        }
      });
    });
  }

  private setAccessToken(token: string): void {
    const KakaoSdk: any = Kakao;
    KakaoSdk.setAccessToken(token);
  }
}
