import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { Review, ReviewStats } from '../../models/review';
import { ReviewService } from '../../services/review.service';
import { InsertReviewDTO } from '../../dtos/review/insert.review.dto';
import { UpdateReviewDTO } from '../../dtos/review/update.review.dto';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class ReviewComponent implements OnInit, OnChanges {
  @Input() productId!: number;

  reviews: Review[] = [];
  reviewStats: ReviewStats | null = null;
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;

  // Form states
  showReviewForm: boolean = false;
  isEditing: boolean = false;
  editingReviewId: number | null = null;

  // New review form
  newReview: InsertReviewDTO = {
    productId: 0,
    rating: 5,
    comment: ''
  };

  // Edit review form
  editReview: UpdateReviewDTO = {
    rating: 5,
    comment: ''
  };

  // UI states
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  hasUserReviewed: boolean = false;

  // Star rating display
  stars: number[] = [1, 2, 3, 4, 5];

  constructor(private reviewService: ReviewService) {}

  ngOnInit(): void {
    this.loadReviews();
    this.loadReviewStats();
    this.checkUserReviewStatus();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId'] && !changes['productId'].firstChange) {
      this.productId = changes['productId'].currentValue;
      this.resetComponent();
      this.ngOnInit();
    }
  }

  resetComponent(): void {
    this.reviews = [];
    this.reviewStats = null;
    this.currentPage = 0;
    this.totalPages = 0;
    this.totalElements = 0;
    this.showReviewForm = false;
    this.isEditing = false;
    this.editingReviewId = null;
    this.hasUserReviewed = false;
  }

  loadReviews(page: number = 0): void {
    this.isLoading = true;
    this.reviewService.getReviewsByProductId(this.productId, page, 10).subscribe({
      next: (response: any) => {
        this.reviews = response.reviews || [];
        this.totalPages = response.totalPages || 0;
        this.totalElements = response.totalElements || 0;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading reviews:', error);
        this.isLoading = false;
      }
    });
  }

  loadReviewStats(): void {
    this.reviewService.getProductReviewStats(this.productId).subscribe({
      next: (stats: ReviewStats) => {
        this.reviewStats = stats;
      },
      error: (error) => {
        console.error('Error loading review stats:', error);
      }
    });
  }

  checkUserReviewStatus(): void {
    this.reviewService.hasUserReviewedProduct(this.productId).subscribe({
      next: (response: any) => {
        this.hasUserReviewed = response.hasReviewed;
      },
      error: (error) => {
        console.error('Error checking review status:', error);
      }
    });
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadReviews(page);
    }
  }

  toggleReviewForm(): void {
    this.showReviewForm = !this.showReviewForm;
    if (!this.showReviewForm) {
      this.resetForms();
    }
  }

  submitReview(): void {
    if (!this.newReview.rating || this.newReview.rating < 1 || this.newReview.rating > 5) {
      alert('Vui lòng chọn số sao từ 1 đến 5!');
      return;
    }

    this.isSubmitting = true;
    this.newReview.productId = this.productId;

    const insertDTO = new InsertReviewDTO(this.newReview);

    this.reviewService.createReview(insertDTO).subscribe({
      next: (response) => {
        alert('Đánh giá của bạn đã được gửi thành công!');
        this.hasUserReviewed = true;
        this.showReviewForm = false;
        this.resetForms();
        this.loadReviews();
        this.loadReviewStats();
        this.isSubmitting = false;
      },
      error: (error) => {
        console.error('Error submitting review:', error);
        alert('Có lỗi xảy ra khi gửi đánh giá: ' + (error.error?.message || error.message));
        this.isSubmitting = false;
      }
    });
  }

  startEditReview(review: Review): void {
    this.isEditing = true;
    this.editingReviewId = review.id;
    this.editReview = {
      rating: review.rating,
      comment: review.comment || ''
    };
    this.showReviewForm = true;
  }

  submitEditReview(): void {
    if (!this.editingReviewId) return;

    if (!this.editReview.rating || this.editReview.rating < 1 || this.editReview.rating > 5) {
      alert('Vui lòng chọn số sao từ 1 đến 5!');
      return;
    }

    this.isSubmitting = true;
    const updateDTO = new UpdateReviewDTO(this.editReview);

    this.reviewService.updateReview(this.editingReviewId, updateDTO).subscribe({
      next: (response) => {
        alert('Đánh giá đã được cập nhật thành công!');
        this.cancelEdit();
        this.loadReviews();
        this.loadReviewStats();
        this.isSubmitting = false;
      },
      error: (error) => {
        console.error('Error updating review:', error);
        alert('Có lỗi xảy ra khi cập nhật đánh giá: ' + (error.error?.message || error.message));
        this.isSubmitting = false;
      }
    });
  }

  deleteReview(reviewId: number): void {
    if (!confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) {
      return;
    }

    this.reviewService.deleteReview(reviewId).subscribe({
      next: (response) => {
        alert('Đánh giá đã được xóa thành công!');
        this.hasUserReviewed = false;
        this.loadReviews();
        this.loadReviewStats();
      },
      error: (error) => {
        console.error('Error deleting review:', error);
        alert('Có lỗi xảy ra khi xóa đánh giá: ' + (error.error?.message || error.message));
      }
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingReviewId = null;
    this.showReviewForm = false;
    this.resetForms();
  }

  private resetForms(): void {
    this.newReview = {
      productId: this.productId,
      rating: 5,
      comment: ''
    };
    this.editReview = {
      rating: 5,
      comment: ''
    };
  }

  // Utility methods
  getStarArray(rating: number): number[] {
    return Array(Math.floor(rating)).fill(0).map((_, i) => i + 1);
  }

  getEmptyStarArray(rating: number): number[] {
    return Array(Math.max(0, 5 - Math.ceil(rating))).fill(0).map((_, i) => i + 1);
  }

  trackByReviewId(index: number, review: Review): number {
    return review.id;
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
  }

  getRatingPercentage(rating: number): number {
    if (!this.reviewStats || this.reviewStats.reviewCount === 0) return 0;
    const ratingData = this.reviewStats.ratingDistribution?.find(r => r.rating === rating);
    return ratingData ? (ratingData.count / this.reviewStats.reviewCount) * 100 : 0;
  }

  getRatingCount(rating: number): number {
    if (!this.reviewStats || !this.reviewStats.ratingDistribution) return 0;
    const ratingData = this.reviewStats.ratingDistribution.find(r => r.rating === rating);
    return ratingData ? ratingData.count : 0;
  }

  canUserModifyReview(review: Review): boolean {
    // You might want to implement proper user authentication check here
    // For now, just return true for demonstration
    return true;
  }

  getButtonText(): string {
    if (this.isEditing) {
      return 'Chỉnh sửa đánh giá';
    } else if (this.hasUserReviewed) {
      return 'Bạn đã đánh giá';
    } else {
      return 'Viết đánh giá';
    }
  }

  getFormTitle(): string {
    return this.isEditing ? 'Chỉnh sửa đánh giá' : 'Viết đánh giá của bạn';
  }

  getRatingInputName(): string {
    return this.isEditing ? 'edit-rating' : 'new-rating';
  }

  getCommentInputName(): string {
    return this.isEditing ? 'edit-comment' : 'new-comment';
  }

  getCurrentRating(): number {
    return this.isEditing ? this.editReview.rating : this.newReview.rating;
  }

  getCurrentComment(): string {
    return this.isEditing ? this.editReview.comment || '' : this.newReview.comment || '';
  }

  getSubmitButtonText(): string {
    if (this.isSubmitting) {
      return 'Đang gửi...';
    } else if (this.isEditing) {
      return 'Cập nhật';
    } else {
      return 'Gửi đánh giá';
    }
  }
}
