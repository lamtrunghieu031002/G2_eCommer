import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product } from '../models/product';
import { ProductVariant } from '../models/product.variants';
import { VariantResponse } from '../responses/variant/variant.response';
import { InsertVariantDTO } from '../dtos/variant/insert.variant.dto';
import { ApiResponse } from '../responses/api.response';

@Injectable({
  providedIn: 'root'
})
export class ProductVariantService {
  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) { }

  getVariantsByProductId(productId: number): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(`${this.apiBaseUrl}/variants/${productId}`);
  }

  getVariantsByIds(variantIds: number[]): Observable<ApiResponse> {
    const params = new HttpParams().set('ids', variantIds.join(','));
    return this.http.get<ApiResponse>(`${this.apiBaseUrl}/variants/by-ids`, { params });
  }

  insertVariants(productId: number, insertVariantDTO: InsertVariantDTO): Observable<ApiResponse> {
    // Add a new product variant
    return this.http.post<ApiResponse>(`${this.apiBaseUrl}/variants/${productId}`, insertVariantDTO);
  }

  deleteVariants(variantId: number): Observable<ApiResponse> {
    // Add a new product variant
    return this.http.delete<ApiResponse>(`${this.apiBaseUrl}/variants/${variantId}`);
  }

}
