import { registerPlugin } from '@capacitor/core';

import type { CapacitorKakaoPlugin } from './definitions';

const CapacitorKakao = registerPlugin<CapacitorKakaoPlugin>('CapacitorKakao', {
  web: () => import('./web').then(m => new m.CapacitorKakaoWeb()),
});

export * from './definitions';
export { CapacitorKakao };
