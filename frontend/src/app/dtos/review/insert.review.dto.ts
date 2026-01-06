import {
    IsNumber,
    IsString,
    IsOptional,
    Min,
    Max,
    IsNotEmpty,
} from 'class-validator';

export class InsertReviewDTO {
    @IsNumber()
    @Min(1)
    productId: number;

    @IsNumber()
    @Min(1)
    @Max(5)
    rating: number;

    @IsOptional()
    @IsString()
    comment?: string;

    constructor(data: any) {
        this.productId = data.productId;
        this.rating = data.rating;
        this.comment = data.comment;
    }
}

