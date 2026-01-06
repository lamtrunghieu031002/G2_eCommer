import { IsNumber } from 'class-validator';

export class CartItemDTO {
    @IsNumber()
    variant_id: number;

    @IsNumber()
    quantity: number;
    

    constructor(data: any) {
        this.variant_id = data.product_id;
        this.quantity = data.quantity;
    }
}
