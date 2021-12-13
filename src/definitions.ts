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
  initializeKakao(options: { appKey: string; webKey: string; }): Promise<void>;
  kakaoLogin(): Promise<{ value: string }>;
  kakaoLogout(): Promise<void>;
  kakaoUnlink(): Promise<void>;
  sendLinkFeed(options: {
    title: string;
    description: string;
    imageUrl: string;
    imageLinkUrl: string;
    buttonTitle: string;
  }): Promise<void>;
  getUserInfo(): Promise<{ value: any }>;
  getFriendList(options?: KakaoFriendOption): Promise<{ value: any }>;
  loginWithNewScopes(scopes?: string[]): Promise<void>;
  getUserScopes(): Promise<{ value: KakaoScope[] }>;
}
