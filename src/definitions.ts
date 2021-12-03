export interface KakaoScope {
  agreed: boolean;
  displayName: string;
  id: string;
  revocable: boolean;
  type: string;
  using: boolean;
}
export interface CapacitorKakaoPlugin {
  initializeKakao(options: { appKey: string; webKey: string; }): Promise<{ value: string }>;
  kakaoLogin(): Promise<{ value: string }>;
  kakaoLogout(): Promise<{ value: string }>;
  kakaoUnlink(): Promise<{ value: string }>;
  sendLinkFeed(options: {
    title: string;
    description: string;
    imageUrl: string;
    imageLinkUrl: string;
    buttonTitle: string;
  }): Promise<{ value: string }>;
  getUserInfo(): Promise<{ value: any }>;
  getFriendList(options?: {
    offset?: number;
    limit?: number;
    order?: 'asc' | 'desc',
    friendOrder?: 'FAVORITE' | 'NICKNAME'
  }): Promise<{ value: any }>;
  loginWithNewScopes(scopes?: string[]): Promise<void>;
  getUserScopes(): Promise<{ value: KakaoScope[] }>;
}
