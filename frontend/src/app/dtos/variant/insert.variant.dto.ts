import {
    IsString, 
    IsNotEmpty,     
} from 'class-validator';

export class InsertVariantDTO {
    
    @IsString()
    @IsNotEmpty()
    variant: string;

    stock: number;
    
    constructor(data: any) {
        this.variant = data.variant;
        this.stock = data.category_id;
    }
}