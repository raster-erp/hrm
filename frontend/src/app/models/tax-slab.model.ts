export interface TaxSlabRequest {
  regime: string;
  financialYear: string;
  slabFrom: number;
  slabTo: number | null;
  rate: number;
  description: string;
}

export interface TaxSlabResponse {
  id: number;
  regime: string;
  financialYear: string;
  slabFrom: number;
  slabTo: number | null;
  rate: number;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
