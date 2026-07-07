import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CouponService } from '../../../../services/coupon.service';
import { UpdateCouponDTO } from '../../../../dtos/coupon/update.coupon.dto';

@Component({
  selector: 'app-update-coupon-admin',
  templateUrl: './update.coupon.admin.component.html',
  styleUrls: ['./update.coupon.admin.component.scss']
})
export class UpdateCouponAdminComponent implements OnInit {

  couponId: number = 0;
  coupon: any = {};
  updateCouponDTO: UpdateCouponDTO = {
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
    usageLimit: 0
  };

  discountTypes = [
    { value: 'PERCENT', label: 'Phần trăm (%)' },
    { value: 'FIXED', label: 'Cố định (VND)' }
  ];

  constructor(
    private couponService: CouponService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.couponId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.couponId) {
      this.loadCoupon();
    }
  }

  loadCoupon() {
    this.couponService.getCouponById(this.couponId).subscribe({
      next: (response) => {
        this.coupon = response;
        // Populate the update DTO
        this.updateCouponDTO = {
          code: this.coupon.code,
          name: this.coupon.name,
          description: this.coupon.description || '',
          discountType: this.coupon.discountType,
          discountValue: this.coupon.discountValue,
          minimumOrderAmount: this.coupon.minimumOrderAmount || 0,
          maximumDiscount: this.coupon.maximumDiscount || 0,
          startDate: this.formatDateTimeForInput(new Date(this.coupon.startDate)),
          endDate: this.formatDateTimeForInput(new Date(this.coupon.endDate)),
          active: this.coupon.active,
          usageLimit: this.coupon.usageLimit || 0
        };
      },
      complete: () => {},
      error: (error) => {
        console.error('Error loading coupon:', error);
        alert('Không thể tải thông tin mã giảm giá!');
        this.router.navigate(['/admin/coupons']);
      }
    });
  }

  onSubmit() {
    // Validate required fields
    if (!this.updateCouponDTO.code || !this.updateCouponDTO.name || !this.updateCouponDTO.discountValue) {
      alert('Vui lòng điền đầy đủ thông tin bắt buộc!');
      return;
    }

    // Validate dates
    const startDate = new Date(this.updateCouponDTO.startDate);
    const endDate = new Date(this.updateCouponDTO.endDate);

    if (endDate <= startDate) {
      alert('Ngày kết thúc phải sau ngày bắt đầu!');
      return;
    }

    // Validate percent discount
    if (this.updateCouponDTO.discountType === 'PERCENT' && (this.updateCouponDTO.discountValue < 1 || this.updateCouponDTO.discountValue > 100)) {
      alert('Giá trị giảm phần trăm phải từ 1% đến 100%!');
      return;
    }

    // Ensure datetime format is complete (add seconds if missing)
    const formattedCoupon = {
      ...this.updateCouponDTO,
      startDate: this.ensureFullDateTimeFormat(this.updateCouponDTO.startDate),
      endDate: this.ensureFullDateTimeFormat(this.updateCouponDTO.endDate)
    };

    // Create DTO
    const updateDTO = new UpdateCouponDTO(formattedCoupon);

    this.couponService.updateCoupon(this.couponId, updateDTO).subscribe({
      next: (response) => {
        alert('Cập nhật mã giảm giá thành công!');
        this.router.navigate(['/admin/coupons']);
      },
      complete: () => {},
      error: (error) => {
        console.error('Error updating coupon:', error);
        alert('Có lỗi xảy ra khi cập nhật mã giảm giá: ' + error.error);
      }
    });
  }

  onCancel() {
    this.router.navigate(['/admin/coupons']);
  }

  onDiscountTypeChange() {
    // Reset discount value when type changes
    this.updateCouponDTO.discountValue = 0;
  }

  private formatDateTimeForInput(date: Date): string {
    return date.toISOString().slice(0, 16); // Format for datetime-local input
  }

  private ensureFullDateTimeFormat(dateTimeString: string): string {
    // If the string doesn't have seconds, add :00
    if (dateTimeString.length === 16) { // Format: yyyy-MM-ddTHH:mm
      return dateTimeString + ':00';
    }
    return dateTimeString;
  }

  formatCurrency(amount: number): string {
    if (amount === null || amount === undefined) return '';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }
}