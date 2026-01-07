export interface UpdateCouponDTO {
  code: string;
  name: string;
  description: string;
  discountType: string; // 'PERCENT' | 'FIXED'
  discountValue: number;
  minimumOrderAmount?: number;
  maximumDiscount?: number;
  startDate: string; // ISO format
  endDate: string; // ISO format
  active: boolean;
  usageLimit?: number;
}
