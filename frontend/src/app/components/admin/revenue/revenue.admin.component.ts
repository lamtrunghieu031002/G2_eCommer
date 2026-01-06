import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Route, Router } from '@angular/router';
import { UserResponse } from '../../../responses/user/user.response';
import { ApiResponse } from '../../../responses/api.response';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { UserService } from 'src/app/services/user.service';
import { ProductService } from 'src/app/services/product.service';

@Component({
  selector: 'app-revenue-admin',
  templateUrl: './revenue.admin.component.html',
  styleUrls: ['./revenue.admin.component.scss']
})
export class RevenueAdminComponent implements OnInit {
  productStats: any[] = [];
  totalPages: number = 0;
  page: number = 0;
  limit: number = 12;
  currentPage: number = 0;
  relatedLimit: number = 18;

  startDate: string = '';
  endDate: string = '';

  constructor(
    private http: HttpClient,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    // Khởi tạo với hôm nay và 7 ngày trước
    // const today = new Date();
    // const lastWeek = new Date();
    // lastWeek.setDate(today.getDate() - 7);

    // this.startDate = lastWeek.toISOString().slice(0, 10);
    // this.endDate = today.toISOString().slice(0, 10);

    this.page = 0;
    this.fetchProductStats();
  }

  fetchProductStats(isNewFilter: boolean = false): void {
    
    if (isNewFilter) {
      this.productStats = [];
      this.page = 0;
    }
    
    this.productService.getProductStat(this.startDate, this.endDate, this.page, this.relatedLimit).subscribe({
      next: (res: ApiResponse) => {
        console.log('totalPages: ', this.totalPages)
        this.productStats = [...this.productStats, ...res.data.products]; 
        this.totalPages = res.data.totalPages;
      },
      error: (err) => {
        alert('Không thể tải thống kê.');
        console.error(err);
      }
    });
  }

  loadMore() {
    this.page++; 
    this.fetchProductStats();
  }
  
}
