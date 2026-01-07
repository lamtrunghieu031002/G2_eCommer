import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Product } from '../../../../models/product';
import { Category } from '../../../../models/category';
import { ProductService } from '../../../../services/product.service';
import { CategoryService } from '../../../../services/category.service';
import { environment } from '../../../../../environments/environment';
import { ProductImage } from '../../../../models/product.image';
import { UpdateProductDTO } from '../../../../dtos/product/update.product.dto';
import { ProductVariantService } from 'src/app/services/product.variant.service';
import { ToastService } from 'src/app/services/toast.service';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-detail.product.admin',
  templateUrl: './update.product.admin.component.html',
  styleUrls: ['./update.product.admin.component.scss']
})

export class UpdateProductAdminComponent implements OnInit {
  productId: number;
  product: Product;
  updatedProduct: Product;
  categories: Category[] = []; // Dữ liệu động từ categoryService
  currentImageIndex: number = 0;
  images: File[] = [];

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router,
    private categoryService: CategoryService,  
    private productVariantService: ProductVariantService ,
    private toastService: ToastService,
  ) {
    this.productId = 0;
    this.product = {} as Product;
    this.updatedProduct = {} as Product;  
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.productId = Number(params.get('id'));
      
    });
    this.getCategories(1, 100);
    this.getProductDetails();
    console.log(this.currentImageIndex);
    console.log(this.product);
    
  }
  getCategories(page: number, limit: number) {
    this.categoryService.getCategories(page, limit).subscribe({
      next: (apiresponse: ApiResponse) => {
        
        this.categories = apiresponse.data;
      },
      complete: () => {
        ;
      },
      error: (error: any) => {
        console.error('Error fetching categories:', error);
      }
    });
  }
  getProductDetails(): void {
    this.productService.getDetailProduct(this.productId).subscribe({
      next: (apiResponse: ApiResponse) => {
        this.product = apiResponse.data;
        this.updatedProduct = { ...apiResponse.data };                
        this.updatedProduct.product_images.forEach((product_image:ProductImage) => {
          product_image.image_url = `${environment.apiBaseUrl}/products/images/${product_image.image_url}`;
        
        });
        this.currentImageIndex = this.updatedProduct.product_images.findIndex(
          (product_image) => 
            this.updatedProduct.thumbnail === product_image.image_url.split("images/")[1]
        );
      },
      complete: () => {
        
      },
      error: (error: any) => {
        this.toastService.showToast({
          error: error,
          defaultMsg: 'Lỗi tải chi tiết sản phẩm',
          title: 'Lỗi Hệ Thống'
        });
      }
    });     
  }
  updateProduct() {
    const updateProductDTO: UpdateProductDTO = {
      name: this.updatedProduct.name,
      price: this.updatedProduct.price,
      description: this.updatedProduct.description,
      category_id: this.updatedProduct.category_id,
      thumbnail: this.updatedProduct.product_images[this.currentImageIndex].image_url.split("images/")[1]
    };
    this.productService.updateProduct(this.product.id, updateProductDTO).subscribe({
      next: (response: any) => {  
                
      },
      complete: () => {
        this.toastService.showToast({
          error: null,
          defaultMsg: 'Cập nhật sản phẩm thành công!',
          title: 'Thành Công'
        });
        this.router.navigate(['/admin/products']);        
      },
      error: (error: any) => {
        this.toastService.showToast({
          error: 'Cập nhật thất bại',
          defaultMsg: '',
          title: 'Lỗi'
        });
        console.error('Error fetching products:', error);
      }
    });  
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
  onFileChange(event: any) {
    // Retrieve selected files from input element
    const files = event.target.files;
    // Limit the number of selected files to 5
    if (files.length > 6) {
      alert('Please select a maximum of 6 images.');
      return;
    }
    // Store the selected files in the newProduct object
    this.images = files;
    this.productService.uploadImages(this.productId, this.images).subscribe({
      next: (imageResponse) => {
        
        // Handle the uploaded images response if needed              
        console.log('Images uploaded successfully:', imageResponse);
        this.images = [];       
        // Reload product details to reflect the new images
        this.getProductDetails(); 
      },
      error: (error) => {
        // Handle the error while uploading images
        alert(error.error)
        console.error('Error uploading images:', error);
      }
    })
  }
  deleteImage(productImage: ProductImage) {
    if (confirm('Are you sure you want to remove this image?')) {
      // Call the removeImage() method to remove the image   
      this.productService.deleteProductImage(productImage.id).subscribe({
        next:(productImage: ProductImage) => {
          location.reload();          
        },        
        error: (error) => {
          // Handle the error while uploading images
          alert(error.error)
          console.error('Error deleting images:', error);
        }
      });
    }   
  }

  removeVariant(variantId: number | undefined) {
    // this.updatedProduct.product_variants.splice(index, 1);
    if (!variantId) {
      // Chỉ cần xoá khỏi UI (mảng product_variants)
      this.updatedProduct.product_variants = this.updatedProduct.product_variants.filter(v => v.id !== variantId);
      return;
    }

    this.productVariantService.deleteVariants(variantId).subscribe({
      next:() => {
         
         this.updatedProduct.product_variants = this.updatedProduct.product_variants.filter(v => v.id !== variantId);
      }, complete: () => {
        this.toastService.showToast({
          error: null,
          defaultMsg: 'Xoá thành công!',
          title: 'Thành Công'
        }); 
        this.router.navigate(['/admin/products/update', this.productId]);
      },      
      error: (error) => {
        // Handle the error while uploading images
        alert(error.error)
        console.error('Error deleting variant:', error);
      }

    });
    
  }

  addVariant() {
    this.updatedProduct.product_variants.push({ id: 0, variant: '', stock: 0, product_id: this.productId });
  }

  saveVariants() {
    const newVariants = this.updatedProduct.product_variants.filter(variant => !variant.id || variant.id === 0);

    if (!newVariants.length) {
      alert("Không có biến thể mới để lưu!");
      return;
    }

    const insertRequests = newVariants.map(variant => {
      const insertVariantDTO = {
        variant: variant.variant,
        stock: variant.stock
      };
      return this.productVariantService.insertVariants(this.productId, insertVariantDTO);
    });
  
    forkJoin(insertRequests).subscribe({
      next: (responses) => {
        
      },
      error: (error) => {
        console.error("Lỗi khi thêm biến thể:", error);
        alert("Đã xảy ra lỗi khi thêm biến thể.");
      }
    });
    window.location.reload();
    alert("Biến thể mới đã được lưu!");
  }
}
