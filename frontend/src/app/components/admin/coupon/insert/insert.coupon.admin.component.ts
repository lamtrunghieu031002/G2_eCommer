import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CouponService } from '../../../../services/coupon.service';
import { InsertCouponDTO } from '../../../../dtos/coupon/insert.coupon.dto';

@Component({
  selector: 'app-insert-coupon-admin',
  templateUrl: './insert.coupon.admin.component.html',
  styleUrls: ['./insert.coupon.admin.component.scss']
})
export class InsertCouponAdminComponent implements OnInit {

  coupon: InsertCouponDTO = {
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
    private router: Router
  ) {}

  ngOnInit(): void {
    // Không gán mặc định ngày bắt đầu và kết thúc
    // Để trống cho người dùng tự chọn
  }

  onSubmit() {
    // Validate required fields
    if (!this.coupon.code || !this.coupon.name || !this.coupon.discountValue) {
      alert('Vui lòng điền đầy đủ thông tin bắt buộc!');
      return;
    }

    // Validate dates
    if (!this.coupon.startDate || !this.coupon.endDate) {
      alert('Vui lòng chọn ngày bắt đầu và ngày kết thúc!');
      return;
    }

    const startDate = new Date(this.coupon.startDate);
    const endDate = new Date(this.coupon.endDate);
    const now = new Date();

    if (startDate <= now) {
      alert('Ngày bắt đầu phải trong tương lai!');
      return;
    }

    if (endDate <= startDate) {
      alert('Ngày kết thúc phải sau ngày bắt đầu!');
      return;
    }

    // Validate percent discount
    if (this.coupon.discountType === 'PERCENT' && (this.coupon.discountValue < 1 || this.coupon.discountValue > 100)) {
      alert('Giá trị giảm phần trăm phải từ 1% đến 100%!');
      return;
    }

    // Ensure datetime format is complete (add seconds if missing)
    const formattedCoupon = {
      ...this.coupon,
      startDate: this.ensureFullDateTimeFormat(this.coupon.startDate),
      endDate: this.ensureFullDateTimeFormat(this.coupon.endDate)
    };

    // Create DTO
    const insertDTO = new InsertCouponDTO(formattedCoupon);

    this.couponService.insertCoupon(insertDTO).subscribe({
      next: (response) => {
        alert('Thêm mã giảm giá thành công!');
        this.router.navigate(['/admin/coupons']);
      },
      complete: () => {},
      error: (error) => {
        console.error('Error creating coupon:', error);
        alert('Có lỗi xảy ra khi thêm mã giảm giá: ' + error.error);
      }
    });
  }

  onCancel() {
    this.router.navigate(['/admin/coupons']);
  }

  onDiscountTypeChange() {
    // Reset discount value when type changes
    this.coupon.discountValue = 0;
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
}
