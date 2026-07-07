import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Coupon } from '../models/coupon';
import { UpdateCouponDTO } from '../dtos/coupon/update.coupon.dto';
import { InsertCouponDTO } from '../dtos/coupon/insert.coupon.dto';
import { ApiResponse } from '../responses/api.response';

@Injectable({
  providedIn: 'root'
})
export class CouponService {

  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) { }

  getCoupons(keyword: string = '', active: boolean = true, page: number = 0, limit: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('limit', limit.toString());

    if (keyword) {
      params = params.set('keyword', keyword);
    }
    params = params.set('active', active.toString());

    return this.http.get<any>(`${environment.apiBaseUrl}/coupons`, { params });
  }

  getCouponById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiBaseUrl}/coupons/${id}`);
  }

  deleteCoupon(id: number): Observable<any> {
    return this.http.delete(`${this.apiBaseUrl}/coupons/${id}`, { responseType: 'text' });
  }

  updateCoupon(id: number, updatedCoupon: UpdateCouponDTO): Observable<any> {
    return this.http.put<any>(`${this.apiBaseUrl}/coupons/${id}`, updatedCoupon);
  }

  insertCoupon(insertCouponDTO: InsertCouponDTO): Observable<any> {
    return this.http.post(`${this.apiBaseUrl}/coupons`, insertCouponDTO);
  }
}
