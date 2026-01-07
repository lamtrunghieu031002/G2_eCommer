export interface Review {
    id: number;
    userId: number;
    userName: string;
    productId: number;
    rating: number; // 1-5 stars
    comment: string;
    createdAt: string;
    updatedAt: string;
}

export interface ReviewStats {
    averageRating: number;
    reviewCount: number;
    ratingDistribution: Array<{
        rating: number;
        count: number;
    }>;
}



