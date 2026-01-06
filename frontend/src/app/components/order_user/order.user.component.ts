import { Component, inject, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { Router } from '@angular/router';
import { OrderResponse } from '../../responses/order/order.response';
import { environment } from '../../../environments/environment';
import { OrderDetail } from '../../models/order.detail';
import { UserService } from 'src/app/services/user.service';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-order-user',
  templateUrl: './order.user.component.html',
  styleUrls: ['./order.user.component.scss']
})
export class OrderUserComponent implements OnInit {

  orderResponses: OrderResponse[] = [];
  selectedStatus: string = "";
  keyword: string = "";
  constructor(
    private orderService: OrderService,
    private router: Router,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.getOrderByUser(this.keyword, this.selectedStatus);
  }

  getOrderByUser(keyword: string, status: string): void {
    this.orderService.getOrdersByUser(keyword, status).subscribe({
      next: (apiResponse: ApiResponse) => {
        const responses = apiResponse.data;
        this.orderResponses = responses.map((response: { order_details: OrderDetail[]; }) => ({
          ...response,
          order_details: response.order_details.map((detail: OrderDetail) => ({
            ...detail,
            thumbnail: detail.thumbnail
              ? `${environment.apiBaseUrl}/products/images/${detail.thumbnail}`
              : 'assets/images/no-image.png' // Ảnh mặc định nếu không có
          }))
        }));
        console.log(this.selectedStatus);
      },
      error: (error: any) => console.error('Error fetching orders:', error)
    });
  }

  searchOrders(): void {
    this.getOrderByUser(this.keyword, this.selectedStatus);
  }

  filterOrders(status: string) {
    this.selectedStatus = status;
    this.keyword = "";
    this.getOrderByUser(this.keyword, this.selectedStatus);
  }

  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      'pending': 'Chờ duyệt',
      'shipping': 'Đang vận chuyển',
      'processing': 'Chờ lấy hàng',
      'delivered': 'Hoàn thành',
      'cancelled': 'Đã hủy',
      'returned': 'Hoàn hàng'
    };
    return statusMap[status] || 'Không xác định';
  }

  selectOrder(orderId: number) {
    // console.log('orderId', orderId);
    this.router.navigate(['/orders/user', orderId])
  }




}

