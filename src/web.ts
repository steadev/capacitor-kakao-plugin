import { WebPlugin } from '@capacitor/core';
import camelCase from 'lodash.camelcase';
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
          const { accessToken } = this.adaptCamelCase(authObj);
          this.setAccessToken(accessToken);
          resolve({ value: accessToken });
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
          resolve({ value: this.adaptCamelCase(result) });
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
          resolve({ value: this.adaptCamelCase(result) });
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
          resolve({ value: this.adaptCamelCase(result.scopes) as KakaoScope[] });
        },
        fail: (error: any) => {
          reject(error);
        }
      });
    });
  }

  private setAccessToken(token: string): void {
    KakaoSdk.Auth.setAccessToken(token);
  }

  private transform<T, V>(targetObject: T | any | object): V | any {
    if (Array.isArray(targetObject)) {
      return (targetObject as T[]).map(value => this.transform<T, V>(value)) as any;
    } else if (!this.isObject(targetObject)) {
      return targetObject;
    }

    const transformed: any = {};
    const keys = Object.keys(targetObject);

    for (const key of keys) {
      const value = targetObject[key];
      const transformedKey = camelCase(key);

      if (Array.isArray((value))) {
        transformed[transformedKey] = value.map(v => this.transform(v));
      } else if (this.isObject(value)) {
        transformed[transformedKey] = this.transform(value);
      } else {
        transformed[transformedKey] = value;
      }
    }

    return transformed as V;
  }

  private adaptCamelCase<T, V>(response: T): V {
    return this.transform<T, V>(response);
  }

  private isObject(value: any): boolean {
    const type = typeof value;
  
    return !!value && (type === 'object');
  }
}
