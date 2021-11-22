export interface CapacitorKakaoPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
