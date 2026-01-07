import { Product } from "./product";
import {Order} from './order'
export interface OrderDetail {
    id: number;
    order_id: number; 
    variant_id: number;
    product_name: string;
    variant_name: string;
    thumbnail: string;
    price: number;
    number_of_products: number;
    total_money: number;
    color?: string;
}