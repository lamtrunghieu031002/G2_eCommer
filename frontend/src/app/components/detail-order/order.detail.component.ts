import { Component, OnInit } from '@angular/core';
import { OrderResponse } from '../../responses/order/order.response';
import { environment } from '../../../environments/environment';
import { OrderDetail } from '../../models/order.detail';
import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';
import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { OrderService } from 'src/app/services/order.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiResponse } from 'src/app/responses/api.response';
import { ToastService } from 'src/app/services/toast.service';
@Component({
    selector: 'app-order-detail',
    templateUrl: './order.detail.component.html',
    styleUrls: ['./order.detail.component.scss'],
})
export class OrderDetailComponent implements OnInit {  
  orderResponse: OrderResponse = {
    id: 0, // Hoặc bất kỳ giá trị số nào bạn muốn
    user_id: 0,
    fullname: '',
    phone_number: '',
    email: '',
    note: '',
    order_date: new Date(),
    status: '',
    total_money: 0, // Hoặc bất kỳ giá trị số nào bạn muốn
    shipping_method: '',
    shipping_address: '',
    shipping_date: new Date(),
    payment_method: '',
    order_details: [] // Một mảng rỗng
  };  
    
  constructor(
    private orderService: OrderService,
    private router: Router,
    private toastService: ToastService,
    private location: Location,
    private route: ActivatedRoute
  ){
  }

  ngOnInit(): void {
    this.getOrderDetails();
  }
  
  getOrderDetails(): void {
    
    const orderId = Number(this.route.snapshot.paramMap.get('orderId'));
    this.orderService.getOrderById(orderId).subscribe({
      next: (apiResponse: ApiResponse) => {    
        const response = apiResponse.data;    
        this.orderResponse = {
          ...response,
          order_details: response.order_details.map((detail: OrderDetail) => ({
            ...detail,
            thumbnail: detail.thumbnail
              ? `${environment.apiBaseUrl}/products/images/${detail.thumbnail}`
              : 'assets/images/no-image.png' // Ảnh mặc định nếu không có
          }))
        };
      },
      complete: () => {
        ;        
      },
      error: (error: HttpErrorResponse) => {
        ;
        console.error(error?.error?.message ?? '');
      } 
    });
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

  cancelOrder (status: string) {

    let confirmed = "";
    let message = "";
    if( status === 'returned'){
      confirmed = "Bạn có chắc chắn muốn hoàn hàng!";
      message = "Hoàn hàng thành công!"
    } else {
      confirmed = "Bạn có chắc chắn huỷ đơn hàng!";
      message = "Huỷ đơn thành công!"
    }

    if(confirm(confirmed)){
      this.orderService.updateOrderStatus(this.orderResponse.id, status).subscribe({
        next: (response: ApiResponse) => {
          this.toastService.showToast({
            error: null,
            defaultMsg: message,
            title: 'Thành Công'
          });
          this.location.back()
        },
        error: (error: HttpErrorResponse) => {
          this.toastService.showToast({
            error: error,
            defaultMsg: 'Lỗi',
            title: 'Lỗi'
          });
        }
      });
    }
  }
}

