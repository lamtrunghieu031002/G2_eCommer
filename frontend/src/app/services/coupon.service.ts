import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Coupon } from '../models/coupon';
import { InsertCouponDTO } from '../dtos/coupon/insert.coupon.dto';
import { UpdateCouponDTO } from '../dtos/coupon/update.coupon.dto';

export interface CouponListResponse {
  coupons: Coupon[];
  totalPages: number;
  totalElements: number;
}

export interface CouponValidateResponse {
  valid: boolean;
  coupon?: Coupon;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class CouponService {

  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) { }

  getAllCoupons(keyword: string = '', active: boolean = true, page: number = 0, limit: number = 10): Observable<CouponListResponse> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('active', active.toString())
      .set('page', page.toString())
      .set('limit', limit.toString());

    return this.http.get<CouponListResponse>(`${this.apiBaseUrl}/coupons`, { params });
  }

  getCouponById(id: number): Observable<Coupon> {
    return this.http.get<Coupon>(`${this.apiBaseUrl}/coupons/${id}`);
  }

  createCoupon(couponDTO: InsertCouponDTO): Observable<Coupon> {
    return this.http.post<Coupon>(`${this.apiBaseUrl}/coupons`, couponDTO);
  }

  updateCoupon(id: number, couponDTO: UpdateCouponDTO): Observable<Coupon> {
    return this.http.put<Coupon>(`${this.apiBaseUrl}/coupons/${id}`, couponDTO);
  }

  deleteCoupon(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/coupons/${id}`);
  }

  validateCoupon(code: string, orderAmount: number): Observable<CouponValidateResponse> {
    const params = new HttpParams().set('orderAmount', orderAmount.toString());
    return this.http.get<CouponValidateResponse>(`${this.apiBaseUrl}/coupons/validate/${code}`, { params });
  }
}
