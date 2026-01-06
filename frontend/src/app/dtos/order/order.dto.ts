export class OrderDTO {
  user_id: number;

  fullname: string;

  email: string;

  phone_number: string;
  
  shipping_address: string;
  
  status: string;

  note: string;
  
  total_money?: number;

  shipping_method: string;

  order_date: Date;

  payment_method: string;

  vnp_txn_ref?: string;
  cart_items: { variant_id: number, quantity: number }[]; // Thêm cart_items để lưu thông tin giỏ hàng

  constructor(data: any) {
    this.user_id = data.user_id;
    this.fullname = data.fullname;
    this.email = data.email;
    this.status = data.status;
    this.phone_number = data.phone_number;
    this.shipping_address = data.shipping_address;
    this.note = data.note;
    this.order_date = data.order_date;
    this.total_money = data.total_money;
    this.shipping_method = data.shipping_method;
    this.payment_method = data.payment_method;
    this.cart_items = data.cart_items;
  }
}

