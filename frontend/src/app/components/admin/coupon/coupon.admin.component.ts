import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Coupon } from '../../../models/coupon';
import { CouponService, CouponListResponse } from '../../../services/coupon.service';

@Component({
  selector: 'app-coupon-admin',
  templateUrl: './coupon.admin.component.html',
  styleUrls: ['./coupon.admin.component.scss']
})
export class CouponAdminComponent implements OnInit {

  coupons: Coupon[] = [];
  totalPages: number = 0;
  totalElements: number = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  keyword: string = '';
  activeFilter: boolean = true;

  constructor(
    private couponService: CouponService,
    private router: Router
  ) {}

  ngOnInit() {
    this.getCoupons();
  }

  getCoupons() {
    this.couponService.getAllCoupons(this.keyword, this.activeFilter, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: CouponListResponse) => {
          this.coupons = response.coupons;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
        },
        error: (error: any) => {
          console.error('Error fetching coupons:', error);
          alert('Có lỗi xảy ra khi tải danh sách phiếu giảm giá!');
        }
      });
  }

  onSearch() {
    this.currentPage = 0;
    this.getCoupons();
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.getCoupons();
  }

  onActiveFilterChange() {
    this.currentPage = 0;
    this.getCoupons();
  }

  insertCoupon() {
    this.router.navigate(['/admin/coupons/insert']);
  }

  updateCoupon(couponId: number) {
    this.router.navigate(['/admin/coupons/update', couponId]);
  }

  deleteCoupon(coupon: Coupon) {
    const confirmation = window.confirm('Bạn có chắc chắn muốn xóa coupon này?');
    if (confirmation) {
      this.couponService.deleteCoupon(coupon.id).subscribe({
        next: () => {
          alert('Xóa phiếu giảm giá thành công!');
          this.getCoupons();
        },
        error: (error: any) => {
          alert(error.error || 'Có lỗi xảy ra khi xóa phiếu giảm giá!');
          console.error('Error deleting coupon:', error);
        }
      });
    }
  }

  validateCoupon(coupon: Coupon) {
    // Test validate với order amount = 100000 (100k VND)
    this.couponService.validateCoupon(coupon.code, 100000).subscribe({
      next: (response: any) => {
        if (response.valid) {
          alert(`Phiếu giảm giá hợp lệ! Giảm ${response.coupon.discountValue} ${response.coupon.discountType === 'PERCENT' ? '%' : 'VND'}`);
        } else {
          alert(`Phiếu giảm giá không hợp lệ: ${response.message}`);
        }
      },
      error: (error: any) => {
        alert('Có lỗi xảy ra khi kiểm tra phiếu giảm giá!');
        console.error('Error validating coupon:', error);
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('vi-VN');
  }
}
