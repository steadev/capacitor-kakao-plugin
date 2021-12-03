import { WebPlugin } from '@capacitor/core';
import * as Kakao from './assets/kakao-sdk';
import type { CapacitorKakaoPlugin } from './definitions';

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
      const KakaoSdk: any = Kakao;
      KakaoSdk.Auth.login({
        success: function (authObj: any) {
          let { access_token } = authObj;
          resolve({ value: access_token });
        },
        fail: function (err: any) {
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

      const KakaoSdk: any = Kakao;
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

      const KakaoSdk: any = Kakao;
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
      const KakaoSdk: any = Kakao;
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
    return { value: null };
  }
  async getFriendList(options?: {
    offset?: number;
    limit?: number;
    order?: 'asc' | 'desc',
    friendOrder?: 'FAVORITE' | 'NICKNAME'
  }): Promise<{ value: any }> {
    if (options) {
      // do something
    }
    return { value: [] };
  }

  async loginWithNewScopes(scopes?: string): Promise<void> {
    if (scopes) {
      // do something
    }
    return;
  }

  async getUserScopes(): Promise<{ value: string[]}> {
    return { value: [] };
  }
}
