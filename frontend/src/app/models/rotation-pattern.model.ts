export interface RotationPatternRequest {
  name: string;
  description: string;
  rotationDays: number;
  shiftSequence: string;
}

export interface RotationPatternResponse {
  id: number;
  name: string;
  description: string;
  rotationDays: number;
  shiftSequence: string;
  createdAt: string;
  updatedAt: string;
}
