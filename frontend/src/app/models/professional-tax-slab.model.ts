export interface ProfessionalTaxSlabRequest {
  state: string;
  slabFrom: number;
  slabTo: number | null;
  monthlyTax: number;
  februaryTax: number | null;
}

export interface ProfessionalTaxSlabResponse {
  id: number;
  state: string;
  slabFrom: number;
  slabTo: number | null;
  monthlyTax: number;
  februaryTax: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
