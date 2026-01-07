import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CouponService } from '../../../../services/coupon.service';
import { Coupon } from '../../../../models/coupon';
import { UpdateCouponDTO } from '../../../../dtos/coupon/update.coupon.dto';

@Component({
  selector: 'app-update-coupon-admin',
  templateUrl: './update.coupon.admin.component.html',
  styleUrls: ['./update.coupon.admin.component.scss']
})
export class UpdateCouponAdminComponent implements OnInit {

  couponId: number = 0;
  coupon: Coupon | null = null;
  couponDTO: UpdateCouponDTO = {
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

  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private couponService: CouponService
  ) {}

  ngOnInit() {
    this.couponId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.couponId) {
      this.loadCoupon();
    }
  }

  loadCoupon() {
    this.couponService.getCouponById(this.couponId).subscribe({
      next: (coupon: Coupon) => {
        this.coupon = coupon;
        this.populateForm();
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading coupon:', error);
        alert('Không thể tải thông tin phiếu giảm giá!');
        this.router.navigate(['/admin/coupons']);
      }
    });
  }

  populateForm() {
    if (!this.coupon) return;

    this.couponDTO = {
      code: this.coupon.code,
      name: this.coupon.name,
      description: this.coupon.description || '',
      discountType: this.coupon.discountType,
      discountValue: this.coupon.discountValue,
      minimumOrderAmount: this.coupon.minimumOrderAmount || 0,
      maximumDiscount: this.coupon.maximumDiscount || 0,
      startDate: this.formatDateForInput(this.coupon.startDate),
      endDate: this.formatDateForInput(this.coupon.endDate),
      active: this.coupon.active,
      usageLimit: this.coupon.usageLimit
    };
  }

  formatDateForInput(dateString: string): string {
    const date = new Date(dateString);
    // Format for datetime-local input (YYYY-MM-DDTHH:mm)
    return date.toISOString().slice(0, 16);
  }

  onSubmit() {
    if (!this.validateForm()) {
      return;
    }

    // Convert dates to ISO format
    const startDate = new Date(this.couponDTO.startDate);
    const endDate = new Date(this.couponDTO.endDate);

    const submitDTO: UpdateCouponDTO = {
      ...this.couponDTO,
      startDate: startDate.toISOString(),
      endDate: endDate.toISOString()
    };

    this.couponService.updateCoupon(this.couponId, submitDTO).subscribe({
      next: (coupon) => {
        alert('Cập nhật phiếu giảm giá thành công!');
        this.router.navigate(['/admin/coupons']);
      },
      error: (error) => {
        console.error('Error updating coupon:', error);
        alert(error.error || 'Có lỗi xảy ra khi cập nhật phiếu giảm giá!');
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

    return true;
  }

  onCancel() {
    if (confirm('Bạn có chắc chắn muốn hủy? Các thay đổi sẽ không được lưu.')) {
      this.router.navigate(['/admin/coupons']);
    }
  }

  onDiscountTypeChange() {
    // Reset discount value when type changes
    this.couponDTO.discountValue = 0;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }
}
