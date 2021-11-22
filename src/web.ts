import { WebPlugin } from '@capacitor/core';

import type { CapacitorKakaoPlugin } from './definitions';

export class CapacitorKakaoWeb
  extends WebPlugin
  implements CapacitorKakaoPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
