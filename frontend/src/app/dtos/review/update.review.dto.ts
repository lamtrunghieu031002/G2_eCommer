import {
    IsNumber,
    IsString,
    IsOptional,
    Min,
    Max,
} from 'class-validator';

export class UpdateReviewDTO {
    @IsNumber()
    @Min(1)
    @Max(5)
    rating: number;

    @IsOptional()
    @IsString()
    comment?: string;

    constructor(data: any) {
        this.rating = data.rating;
        this.comment = data.comment;
    }
}



