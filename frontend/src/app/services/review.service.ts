import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Review, ReviewStats } from '../models/review';
import { UpdateReviewDTO } from '../dtos/review/update.review.dto';
import { InsertReviewDTO } from '../dtos/review/insert.review.dto';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) { }

  // Tạo review mới
  createReview(insertReviewDTO: InsertReviewDTO): Observable<any> {
    return this.http.post(`${this.apiBaseUrl}/reviews`, insertReviewDTO);
  }

  // Cập nhật review
  updateReview(reviewId: number, updateReviewDTO: UpdateReviewDTO): Observable<any> {
    return this.http.put(`${this.apiBaseUrl}/reviews/${reviewId}`, updateReviewDTO);
  }

  // Xóa review
  deleteReview(reviewId: number): Observable<any> {
    return this.http.delete(`${this.apiBaseUrl}/reviews/${reviewId}`);
  }

  // Lấy reviews theo product ID với pagination
  getReviewsByProductId(productId: number, page: number = 0, limit: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('limit', limit.toString());

    return this.http.get(`${this.apiBaseUrl}/reviews/product/${productId}`, { params });
  }

  // Lấy thống kê review của sản phẩm
  getProductReviewStats(productId: number): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.apiBaseUrl}/reviews/product/${productId}/stats`);
  }

  // Lấy reviews của user hiện tại
  getUserReviews(page: number = 0, limit: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('limit', limit.toString());

    return this.http.get(`${this.apiBaseUrl}/reviews/user`, { params });
  }

  // Kiểm tra user đã review sản phẩm chưa
  hasUserReviewedProduct(productId: number): Observable<any> {
    return this.http.get(`${this.apiBaseUrl}/reviews/check/${productId}`);
  }
}

