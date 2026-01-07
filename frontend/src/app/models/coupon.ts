export interface Coupon {
  id: number;
  code: string;
  name: string;
  description: string;
  discountType: string;
  discountValue: number;
  minimumOrderAmount: number;
  maximumDiscount: number;
  startDate: string;
  endDate: string;
  active: boolean;
  usageLimit: number;
  usedCount: number;
  createdAt: string;
  updatedAt: string;
}
