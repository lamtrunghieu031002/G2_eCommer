import { Component, OnInit } from '@angular/core';
import { ApiResponse } from '../responses/api.response';
import { HttpErrorResponse } from '@angular/common/http';
import { OrderService } from '../services/order.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastService } from '../services/toast.service';
import { CartService } from '../services/cart.service';

@Component({
  selector: 'app-payment-callback',
  templateUrl: './payment-callback.component.html', // Tách riêng HTML
  styleUrls: ['./payment-callback.component.scss']
})
export class PaymentCallbackComponent implements OnInit { 

  loading: boolean = true;
  paymentSuccess: boolean = false;

  constructor(
    private orderService: OrderService,
    private activatedRoute: ActivatedRoute,
    private toastService: ToastService,
    private cartService: CartService,
    private router: Router

  ){

  }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      
      const vnp_ResponseCode = params['vnp_ResponseCode']; // Mã phản hồi từ VNPay
      const orderId:number = Number(params['vnp_TxnRef']); // Mã đơn hàng (nếu bạn truyền vào khi tạo URL thanh toán)
      
      if (vnp_ResponseCode === '00') {
        // Thanh toán thành công
        this.handlePaymentSuccess(orderId);
      } else {
        // Thanh toán thất bại
        this.handlePaymentFailure();
      }
    });
  }

  handlePaymentSuccess(orderId: number): void {    

    this.orderService.updateOrderStatus(orderId, 'pending').subscribe({
      next: (response: ApiResponse) => {
        this.loading = false;
        this.paymentSuccess = true;

        this.toastService.showToast({
          error: null,
          defaultMsg: 'Thanh toán thành công!',
          title: 'Thành Công'
        });

        setTimeout(() => {
          
          this.cartService.clearCart();
          this.router.navigate(['/']);
        }, 3000);
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.paymentSuccess = false;
        this.toastService.showToast({
          error: error,
          defaultMsg: 'Lỗi khi cập nhật trạng thái đơn hàng',
          title: 'Lỗi'
        });
      }
    });
  }

  handlePaymentFailure(): void {
    this.loading = false;
    this.paymentSuccess = false;
    this.toastService.showToast({
      error: null,
      defaultMsg: 'Thanh toán thất bại. Vui lòng thử lại.',
      title: 'Lỗi'
    });
    // Chuyển hướng về trang thanh toán hoặc trang chủ
    setTimeout(() => {
      this.router.navigate(['/checkout']);
    }, 3000);
  }
}