export type HolidayType = 'PUBLIC' | 'REGIONAL' | 'OPTIONAL' | 'COMPANY';

export interface HolidayRequest {
  name: string;
  date: string;
  type: HolidayType;
  region?: string;
  description?: string;
}

export interface HolidayResponse {
  id: number;
  name: string;
  date: string;
  type: HolidayType;
  region: string | null;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
