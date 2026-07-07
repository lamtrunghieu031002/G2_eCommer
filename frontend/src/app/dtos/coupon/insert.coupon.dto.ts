import {
    IsString,
    IsNotEmpty,
    IsEnum,
    IsNumber,
    IsOptional,
    IsDateString,
    IsBoolean,
    Min,
    Max,
} from 'class-validator';

export class InsertCouponDTO {
    @IsString()
    @IsNotEmpty()
    code: string;

    @IsString()
    @IsNotEmpty()
    name: string;

    @IsOptional()
    @IsString()
    description?: string;

    @IsString()
    @IsNotEmpty()
    @IsEnum(['PERCENT', 'FIXED'])
    discountType: string;

    @IsNumber()
    @Min(0.01)
    discountValue: number;

    @IsOptional()
    @IsNumber()
    @Min(0)
    minimumOrderAmount?: number;

    @IsOptional()
    @IsNumber()
    @Min(0)
    maximumDiscount?: number;

    @IsDateString()
    startDate: string;

    @IsDateString()
    endDate: string;

    @IsOptional()
    @IsBoolean()
    active?: boolean;

    @IsOptional()
    @IsNumber()
    @Min(1)
    usageLimit?: number;

    constructor(data: any) {
        this.code = data.code;
        this.name = data.name;
        this.description = data.description;
        this.discountType = data.discountType;
        this.discountValue = data.discountValue;
        this.minimumOrderAmount = data.minimumOrderAmount;
        this.maximumDiscount = data.maximumDiscount;
        this.startDate = data.startDate;
        this.endDate = data.endDate;
        this.active = data.active !== undefined ? data.active : true;
        this.usageLimit = data.usageLimit;
    }
}
