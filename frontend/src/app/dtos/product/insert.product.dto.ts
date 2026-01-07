import {
    IsString, 
    IsNotEmpty, 
    IsPhoneNumber,     
} from 'class-validator';
import { InsertVariantDTO } from '../variant/insert.variant.dto';

export class InsertProductDTO {
    @IsPhoneNumber()
    name: string;

    price: number;

    @IsString()
    @IsNotEmpty()
    description: string;

    category_id: number;
    images: File[] = [];
    variants: InsertVariantDTO[] = [];
    
    constructor(data: any) {
        this.name = data.name;
        this.price = data.price;
        this.description = data.description;
        this.category_id = data.category_id;
    }
}