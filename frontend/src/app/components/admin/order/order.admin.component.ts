import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { OrderResponse } from '../../../responses/order/order.response';
import { OrderService } from '../../../services/order.service';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-order-admin',
  templateUrl: './order.admin.component.html',
  styleUrls: ['./order.admin.component.scss']
})
export class OrderAdminComponent implements OnInit{  
  orders: OrderResponse[] = [];
  currentPage: number = 0;
  itemsPerPage: number = 10;
  pages: number[] = [];
  totalPages:number = 0;
  keyword:string = "";
  visiblePages: number[] = [];

  constructor(
    private orderService: OrderService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location
  ) {

  }
  ngOnInit(): void {
    
    this.currentPage = 0;
    //this.currentPage = Number(localStorage.getItem('currentOrderAdminPage')) || 0; 
    this.getAllOrders(this.keyword, this.currentPage, this.itemsPerPage);
  }
  searchOrders() {
    this.currentPage = 0;
    this.itemsPerPage = 10;
    //Mediocre Iron Wallet
    
    this.getAllOrders(this.keyword.trim(), this.currentPage, this.itemsPerPage);
  }
  getAllOrders(keyword: string, page: number, limit: number) {
    
    this.orderService.getAllOrders(keyword, page, limit).subscribe({
      next: (apiResponse: ApiResponse) => {
                
        console.log(page);
        const response = apiResponse.data;
        this.orders = response.orders;
        this.totalPages = response.totalPages;
        this.visiblePages = this.generateVisiblePageArray(this.currentPage, this.totalPages);
      },
      complete: () => {
        ;
      },
      error: (error: any) => {
        ;
        console.error('Error fetching products:', error);
      }
    });    
  }
  onPageChange(page: number) {
    ;
    this.currentPage = page < 0 ? 0 : page;
    //localStorage.setItem('currentOrderAdminPage', String(this.currentPage));         
    this.getAllOrders(this.keyword, this.currentPage, this.itemsPerPage);
  }

  generateVisiblePageArray(currentPage: number, totalPages: number): number[] {
    const maxVisiblePages = 5;
    const halfVisiblePages = Math.floor(maxVisiblePages / 2);

    let startPage = Math.max(currentPage - halfVisiblePages, 1);
    let endPage = Math.min(startPage + maxVisiblePages - 1, totalPages);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(endPage - maxVisiblePages + 1, 1);
    }

    return new Array(endPage - startPage + 1).fill(0)
        .map((_, index) => startPage + index);
  }

  deleteOrder(id:number) {
    const confirmation = window
      .confirm('Are you sure you want to delete this order?');
    if (confirmation) {
      
      this.orderService.deleteOrder(id).subscribe({
        next: (response: any) => {
           
          location.reload();          
        },
        complete: () => {
          ;          
        },
        error: (error: any) => {
          ;
          console.error('Error fetching products:', error);
        }
      });    
    }
  }
  viewDetails(order:OrderResponse) {
    
    this.router.navigate(['/admin/orders', order.id]);
  }
  
}