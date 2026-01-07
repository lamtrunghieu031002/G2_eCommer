import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CouponService } from '../../../../services/coupon.service';
import { InsertCouponDTO } from '../../../../dtos/coupon/insert.coupon.dto';

@Component({
  selector: 'app-insert-coupon-admin',
  templateUrl: './insert.coupon.admin.component.html',
  styleUrls: ['./insert.coupon.admin.component.scss']
})
export class InsertCouponAdminComponent {

  couponDTO: InsertCouponDTO = {
    code: '',
    name: '',
    description: '',
    discountType: 'PERCENT',
    discountValue: 0,
    minimumOrderAmount: 0,
    maximumDiscount: 0,
    startDate: '',
    endDate: '',
    active: true,
    usageLimit: undefined
  };

  constructor(
    private couponService: CouponService,
    private router: Router
  ) {}

  onSubmit() {
    // Validate form
    if (!this.validateForm()) {
      return;
    }

    // Convert dates to ISO format
    const startDate = new Date(this.couponDTO.startDate);
    const endDate = new Date(this.couponDTO.endDate);

    const submitDTO: InsertCouponDTO = {
      ...this.couponDTO,
      startDate: startDate.toISOString(),
      endDate: endDate.toISOString()
    };

    this.couponService.createCoupon(submitDTO).subscribe({
      next: (coupon) => {
        alert('Thêm phiếu giảm giá thành công!');
        this.router.navigate(['/admin/coupons']);
      },
      error: (error) => {
        console.error('Error creating coupon:', error);
        alert(error.error || 'Có lỗi xảy ra khi thêm phiếu giảm giá!');
      }
    });
  }

  validateForm(): boolean {
    if (!this.couponDTO.code.trim()) {
      alert('Vui lòng nhập mã phiếu giảm giá!');
      return false;
    }

    if (!this.couponDTO.name.trim()) {
      alert('Vui lòng nhập tên phiếu giảm giá!');
      return false;
    }

    if (this.couponDTO.discountValue <= 0) {
      alert('Giá trị giảm phải lớn hơn 0!');
      return false;
    }

    if (this.couponDTO.discountType === 'PERCENT' && this.couponDTO.discountValue > 100) {
      alert('Giảm phần trăm không được vượt quá 100%!');
      return false;
    }

    if (!this.couponDTO.startDate) {
      alert('Vui lòng chọn ngày bắt đầu!');
      return false;
    }

    if (!this.couponDTO.endDate) {
      alert('Vui lòng chọn ngày kết thúc!');
      return false;
    }

    const startDate = new Date(this.couponDTO.startDate);
    const endDate = new Date(this.couponDTO.endDate);

    if (startDate >= endDate) {
      alert('Ngày kết thúc phải sau ngày bắt đầu!');
      return false;
    }

    if (startDate <= new Date()) {
      alert('Ngày bắt đầu phải trong tương lai!');
      return false;
    }

    return true;
  }

  onCancel() {
    if (confirm('Bạn có chắc chắn muốn hủy? Dữ liệu sẽ không được lưu.')) {
      this.router.navigate(['/admin/coupons']);
    }
  }

  onDiscountTypeChange() {
    // Reset discount value when type changes
    this.couponDTO.discountValue = 0;
  }
}
