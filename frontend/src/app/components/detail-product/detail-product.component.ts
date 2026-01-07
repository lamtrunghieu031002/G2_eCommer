import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Product } from '../../models/product';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { environment } from '../../../environments/environment';
import { ProductImage } from '../../models/product.image';
import { ToastService } from 'src/app/services/toast.service';
import { AuthGuard } from 'src/app/guards/auth.guard';
import { ProductVariant } from 'src/app/models/product.variants';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-detail-product',
  templateUrl: './detail-product.component.html',
  styleUrls: ['./detail-product.component.scss']
})

export class DetailProductComponent implements OnInit {
  product?: Product;
  productId: number = 0;
  currentImageIndex: number = 0;
  quantity: number = 1;
  isPressedAddToCart: boolean = false;
  thumbnailId: number = 0;
  variants: any[] = [];
  selectedVariantId: number | null = null;
  selectedVariant: ProductVariant | null = null; 
  relatedProducts: Product[] = [];
  relatedLimit: number = 18;
  currentPage: number = 0;
  categoryId: number = 0;
  totalPages:number = 0;
  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private authGuard: AuthGuard,
  ) {

  }
  ngOnInit() {
    // Lấy productId từ URL      
    const idParam = this.activatedRoute.snapshot.paramMap.get('id');
    
    //this.cartService.clearCart();
    if (idParam !== null) {
      this.productId = +idParam;
    }
    if (!isNaN(this.productId)) {
      this.productService.getDetailProduct(this.productId).subscribe({
        next: (apiresponse: ApiResponse) => {
          // Lấy danh sách ảnh sản phẩm và thay đổi URL
          
          const response = apiresponse.data;
          if (response.product_images && response.product_images.length > 0) {
            response.product_images.forEach((product_image: ProductImage, index: number) => {
              product_image.image_url = `${environment.apiBaseUrl}/products/images/${product_image.image_url}`;
            });
          }
          
          this.product = response
          if (this.product && this.product.product_images && this.product.thumbnail) {
            // Tìm index của ảnh có URL chứa thumbnail
            this.thumbnailId = this.product.product_images.findIndex(
              (img: ProductImage) => img.image_url.includes(this.product!.thumbnail)
            );

          }
          console.log(this.productId)
          
          if(this.product) {
            this.variants = this.product?.product_variants
            this.categoryId = this.product?.category_id
            this.getRelatedProduct(this.productId, this.product.category_id, this.currentPage, this.relatedLimit)
          }
          // console.log(this.variants);
          this.showImage(this.thumbnailId);
          
        },
        complete: () => {
          ;
        },
        error: (error: any) => {
          ;
          console.error('Error fetching detail:', error);
        }
      });
    

    } else {
      console.error('Invalid productId:', idParam);
    }


  }

  getRelatedProduct(productId: number, categoryId: number, page: number, limit: number) {
    if(this.product){
      this.productService.getProductsByCategoryId(productId, categoryId, page, limit).subscribe({
        next: (apiresponse: ApiResponse) => {    
          const response = apiresponse.data;
          this.relatedProducts = [
            ...(this.relatedProducts || []),
            ...response.products.map((product: any) => ({
              ...product,
              thumbnail: `${environment.apiBaseUrl}/products/images/${product.thumbnail}`
            }))
          ];
          this.totalPages = response.totalPages;
        },
        complete: () => {
          
        },
        error: (error: any) => {
          console.log("Error: ", error)
        }
      
      })
    }
    
  }

  showImage(index: number): void {
    
    if (this.product && this.product.product_images &&
      this.product.product_images.length > 0) {
      // Đảm bảo index nằm trong khoảng hợp lệ        
      if (index < 0) {
        index = 0;
      } else if (index >= this.product.product_images.length) {
        index = this.product.product_images.length - 1;
      }
      // Gán index hiện tại và cập nhật ảnh hiển thị
      this.currentImageIndex = index;
    }
  }

  selectVariant(variantId: number) {
    this.selectedVariantId = this.selectedVariant?.id === variantId ? null : variantId;
  }


  thumbnailClick(index: number) {
    
    // Gọi khi một thumbnail được bấm
    this.currentImageIndex = index; // Cập nhật currentImageIndex
  }
  nextImage(): void {
    
    this.showImage(this.currentImageIndex + 1);
  }

  previousImage(): void {
    
    this.showImage(this.currentImageIndex - 1);
  }
  addToCart(): void {
    
    this.isPressedAddToCart = true;
    if (!this.authGuard.canActivate(null as any, null as any)) {
      return;
    }
    if (!this.selectedVariantId && this.variants.length > 0) {
      this.toastService.showToast({
        error: 'Vui lòng chọn một biến thể sản phẩm trước khi thêm vào giỏ hàng!',
        defaultMsg: '',
        title: 'Lỗi'
      });
      return;
    }
    if (this.product && this.selectedVariantId) {
      this.cartService.addToCart(this.selectedVariantId, this.quantity);
      this.toastService.showToast({
        error: null,
        defaultMsg: 'Thêm vào giỏ hàng thành công!',
        title: 'Thành Công'
      });
    } 
    else {
      // Xử lý khi product là null
      console.error('Không thể thêm sản phẩm vào giỏ hàng.');
    }
  }

  increaseQuantity(): void {
    
    this.quantity++;
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }
  getTotalPrice(): number {
    if (this.product) {
      return this.product.price * this.quantity;
    }
    return 0;
  }
  buyNow(): void {
    if (this.isPressedAddToCart == false) {
      this.addToCart();
    }
    this.router.navigate(['/orders']);
  }

  onProductClick(productId: number) {
    ;
    // Điều hướng đến trang detail-product với productId là tham số
    window.location.href = `/products/${productId}`;
    // this.router.navigate(['/products', productId]);
  }

  loadMoreRelated() {
    this.currentPage++; 
    this.getRelatedProduct(this.productId, this.categoryId, this.currentPage, this.relatedLimit)
  }
}
