export interface KakaoFriendOption {
  offset?: number;
  limit?: number;
  order?: 'asc' | 'desc';
}

export interface KakaoScope {
  agreed: boolean;
  displayName: string;
  id: string;
  revocable: boolean;
  type: string;
  using: boolean;
}

export enum KakaoOAuthTokenStatus {
  LOGIN_NEEDED = 'LOGIN_NEEDED',
  ERROR = 'ERROR',
  SUCCEED = 'SUCCEED',
}

export interface KakaoToken {
  accessToken: string;
  refreshToken: string;
}

export interface CapacitorKakaoPlugin {
  /** initialize only for web. */
  initializeKakao(options: {
    appKey: string;
    webKey: string;
  }): Promise<{ status: KakaoOAuthTokenStatus }>;
  /** kakao web login */
  kakaoWebLogin(options: { redirectUri: string; scopes?: string[] }): void;
  /** kakao login */
  kakaoLogin(): Promise<KakaoToken>;
  /** kakao logout */
  kakaoLogout(): Promise<void>;
  /** disconnect link with app */
  kakaoUnlink(): Promise<void>;
  /** send kakao link (only default kakao link) */
  shareDefault(options: {
    title: string;
    description: string;
    imageUrl: string;
    imageLinkUrl: string;
    buttonTitle: string;
    imageWidth?: number;
    imageHeight?: number;
  }): Promise<void>;
  /** get kakao user info */
  getUserInfo(): Promise<{ value: any }>;
  /** get kakao friend list data */
  getFriendList(options?: KakaoFriendOption): Promise<{ value: any }>;
  /** get additional scopes  */
  loginWithNewScopes(scopes?: string[]): Promise<void>;
  /** get user agreed scopes */
  getUserScopes(): Promise<{ value: KakaoScope[] }>;
}
