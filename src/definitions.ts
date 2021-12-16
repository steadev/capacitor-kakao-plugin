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
export interface CapacitorKakaoPlugin {
  /** initialize only for web. */
  initializeKakao(options: { appKey: string; webKey: string; }): Promise<void>;
  /** kakao login */
  kakaoLogin(): Promise<{ value: string }>;
  /** kakao logout */
  kakaoLogout(): Promise<void>;
  /** disconnect link with app */
  kakaoUnlink(): Promise<void>;
  /** send kakao link (only default kakao link) */
  sendLinkFeed(options: {
    title: string;
    description: string;
    imageUrl: string;
    imageLinkUrl: string;
    buttonTitle: string;
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
