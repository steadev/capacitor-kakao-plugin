import { WebPlugin } from '@capacitor/core';
import camelCase from 'lodash.camelcase';
import snake_case from 'lodash.snakecase';
import * as Kakao from './assets/kakao-sdk';
import {
  CapacitorKakaoPlugin,
  KakaoFriendOption,
  KakaoOAuthTokenStatus,
  KakaoScope,
  KakaoToken,
} from './definitions';

const KakaoSdk: any = Kakao;

export class CapacitorKakaoWeb
  extends WebPlugin
  implements CapacitorKakaoPlugin {
  webKey: any;

  initializeKakao(options: {
    appKey: string;
    webKey: string;
  }): Promise<{ status: KakaoOAuthTokenStatus }> {
    return new Promise<{ status: KakaoOAuthTokenStatus }>((resolve, reject) => {
      if (options.webKey === undefined) {
        reject({ status: KakaoOAuthTokenStatus.LOGIN_NEEDED });
      }
      if (this.webKey === undefined) {
        this.webKey = options.webKey;
      }
      Kakao.init(this.webKey);
      resolve({ status: KakaoOAuthTokenStatus.SUCCEED });
    });
  }

  //웹 카카오 로그인
  kakaoLogin() {
    return new Promise<KakaoToken>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialized');
      }
      KakaoSdk.Auth.login({
        success: (authObj: any) => {
          const { accessToken, refreshToken } = ResponseAdapter.adapt(
            authObj,
          ) as { accessToken: string; refreshToken: string };
          this.setAccessToken(accessToken);
          resolve({ accessToken, refreshToken });
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
    return new Promise<void>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }

      KakaoSdk.Auth.logout();
      resolve();
    });
  }

  //unlink
  kakaoUnlink() {
    return new Promise<void>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialized');
      }

      KakaoSdk.API.request({
        url: '/v1/user/unlink',
        success: (response: any) => {
          console.log(response);
          resolve();
        },
        fail: (error: any) => {
          console.log(error);
          reject(error);
        },
      });
    });
  }

  //message
  shareDefault(options: {
    title: string;
    description: string;
    imageUrl: string;
    imageLinkUrl: string;
    buttonTitle: string;
    imageWidth?: number;
    imageHeight?: number;
  }) {
    return new Promise<void>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialized');
      }
      KakaoSdk.Link.sendDefault({
        objectType: 'feed',
        content: {
          title: options.title,
          description: options.description,
          imageUrl: options.imageUrl,
          imageWidth: options.imageWidth,
          imageHeight: options.imageHeight,
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
        callback: () => {
          resolve();
        },
      });
    });
  }

  async getUserInfo(): Promise<{ value: any }> {
    return new Promise((resolve, reject) => {
      KakaoSdk.API.request({
        url: '/v2/user/me',
        success: (result: any) => {
          resolve({ value: ResponseAdapter.adapt(result) });
        },
        fail: (error: any) => {
          reject(error);
        },
      });
    });
  }
  async getFriendList(options?: KakaoFriendOption): Promise<{ value: any }> {
    return new Promise((resolve, reject) => {
      KakaoSdk.API.request({
        url: '/v1/api/talk/friends',
        data: options ? RequestAdapter.adapt(options) : undefined,
        success: (result: any) => {
          if (Array.isArray(result?.elements)) {
            resolve({ value: ResponseAdapter.adapt(result.elements) });
          } else {
            reject();
          }
        },
        fail: (error: any) => {
          reject(error);
        },
      });
    });
  }

  async loginWithNewScopes(scopes?: string[]): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      if (!this.webKey) {
        reject('kakao_sdk_not_initialzed');
      }
      KakaoSdk.Auth.login({
        scope: scopes ? scopes.join(',') : undefined,
        success: (authObj: any) => {
          const { accessToken } = ResponseAdapter.adapt(authObj) as {
            accessToken: string;
          };
          this.setAccessToken(accessToken);
          resolve();
        },
        fail: (err: any) => {
          console.error(err);
          reject(err);
        },
      });
    });
  }

  async getUserScopes(): Promise<{ value: KakaoScope[] }> {
    return new Promise((resolve, reject) => {
      KakaoSdk.API.request({
        url: '/v2/user/scopes',
        success: (result: any) => {
          resolve({
            value: ResponseAdapter.adapt(result.scopes) as KakaoScope[],
          });
        },
        fail: (error: any) => {
          reject(error);
        },
      });
    });
  }

  private setAccessToken(token: string): void {
    KakaoSdk.Auth.setAccessToken(token);
  }
}

const isObject = (value: any): boolean => {
  const type = typeof value;

  return !!value && type === 'object';
};

/**
 * JSON 형식의 데이터를 snake_case -> camelCase 로 전환해주는 어댑터입니다.
 */
export class ResponseAdapter {
  private static transform<T, V>(targetObject: T | any | object): V | any {
    if (Array.isArray(targetObject)) {
      return (targetObject as T[]).map(value =>
        this.transform<T, V>(value),
      ) as any;
    } else if (!isObject(targetObject)) {
      return targetObject;
    }

    const transformed: any = {};
    const keys = Object.keys(targetObject);

    for (const key of keys) {
      const value = targetObject[key];
      const transformedKey = camelCase(key);

      if (Array.isArray(value)) {
        transformed[transformedKey] = value.map(v => this.transform(v));
      } else if (isObject(value)) {
        transformed[transformedKey] = this.transform(value);
      } else {
        transformed[transformedKey] = value;
      }
    }

    return transformed as V;
  }

  static adapt<T, V>(response: T): V {
    return this.transform<T, V>(response);
  }
}

/**
 * JSON 형식의 데이터를 camelCase -> snake_case 로 전환해주는 어댑터입니다.
 */
export class RequestAdapter {
  private static transform<T, V>(targetObject: T | any | object): V | any {
    if (Array.isArray(targetObject)) {
      return (targetObject as T[]).map(value =>
        this.transform<T, V>(value),
      ) as any;
    } else if (!isObject(targetObject)) {
      return targetObject;
    }

    const transformed: any = {};
    const keys = Object.keys(targetObject);

    for (const key of keys) {
      const value = targetObject[key];
      const transformedKey = snake_case(key);

      if (Array.isArray(value)) {
        transformed[transformedKey] = value.map(v => this.transform(v));
      } else if (isObject(value)) {
        transformed[transformedKey] = this.transform(value);
      } else {
        transformed[transformedKey] = value;
      }
    }

    return transformed as V;
  }

  static adapt<T, V>(requestBody: T): V {
    return this.transform<T, V>(requestBody);
  }
}
