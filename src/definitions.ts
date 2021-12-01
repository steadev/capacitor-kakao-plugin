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
  getFriendList(options: {
    offset?: number;
    limit?: number;
    order?: 'asc' | 'desc',
    friendOrder?: 'FAVORITE' | 'NICKNAME'
  }): Promise<{ value: any }>;
}
