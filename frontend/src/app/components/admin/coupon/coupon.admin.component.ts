import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Coupon } from '../../../models/coupon';
import { CouponService } from '../../../services/coupon.service';

@Component({
  selector: 'app-coupon-admin',
  templateUrl: './coupon.admin.component.html',
  styleUrls: ['./coupon.admin.component.scss']
})
export class CouponAdminComponent implements OnInit {
  coupons: Coupon[] = [];
  keyword: string = '';
  activeFilter: boolean = true;
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;

  constructor(
    private couponService: CouponService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.getCoupons(0, 100);
  }

  getCoupons(page: number, limit: number) {
    this.couponService.getCoupons(this.keyword, this.activeFilter, page, limit).subscribe({
      next: (response: any) => {
        this.coupons = response.coupons || [];
        this.totalPages = response.totalPages || 0;
        this.totalElements = response.totalElements || 0;
      },
      complete: () => {
      },
      error: (error: any) => {
        console.error('Error fetching coupons:', error);
      }
    });
  }

  insertCoupon() {
    this.router.navigate(['/admin/coupons/insert']);
  }

  updateCoupon(couponId: number) {
    this.router.navigate(['/admin/coupons/update', couponId]);
  }

  deleteCoupon(coupon: Coupon) {
    const confirmation = window.confirm('Bạn có chắc chắn muốn xóa mã giảm giá này?');
    if (confirmation) {
      this.couponService.deleteCoupon(coupon.id).subscribe({
        next: (response: string) => {
          alert('Xóa mã giảm giá thành công!');
          // Reload danh sách thay vì reload toàn bộ trang
          this.getCoupons(this.currentPage, 100);
        },
        complete: () => {},
        error: (error: any) => {
          console.error('Error deleting coupon:', error);
          // Kiểm tra loại lỗi và hiển thị thông báo phù hợp
          if (error.status === 404) {
            alert('Coupon không tồn tại hoặc đã được xóa!');
          } else if (error.status === 403) {
            alert('Bạn không có quyền xóa mã giảm giá này!');
          } else if (error.error && typeof error.error === 'string') {
            alert('Lỗi: ' + error.error);
          } else if (error.message) {
            alert('Lỗi: ' + error.message);
          } else {
            alert('Có lỗi xảy ra khi xóa mã giảm giá!');
          }
        }
      });
    }
  }

  onSearch() {
    this.currentPage = 0;
    this.getCoupons(this.currentPage, 100);
  }

  onFilterChange() {
    this.currentPage = 0;
    this.getCoupons(this.currentPage, 100);
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
  }

  formatCurrency(amount: number): string {
    if (amount === null || amount === undefined) return '';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }

  getDiscountTypeText(type: string): string {
    return type === 'PERCENT' ? 'Phần trăm (%)' : 'Cố định (VND)';
  }
}