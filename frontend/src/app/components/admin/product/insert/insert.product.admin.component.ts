import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OnInit } from '@angular/core';
import { InsertProductDTO } from '../../../../dtos/product/insert.product.dto';
import { Category } from '../../../../models/category';
import { CategoryService } from '../../../../services/category.service';
import { ProductService } from '../../../../services/product.service';
import { ProductVariantService } from 'src/app/services/product.variant.service';
import { ApiResponse } from 'src/app/responses/api.response';
@Component({
  selector: 'app-insert.product.admin',
  templateUrl: './insert.product.admin.component.html',
  styleUrls: ['./insert.product.admin.component.scss']
})
export class InsertProductAdminComponent implements OnInit {
  insertProductDTO: InsertProductDTO = {
    name: '',
    price: 0,
    description: '',
    category_id: 1,
    images: [],
    variants: []
  };
  categories: Category[] = []; // Dữ liệu động từ categoryService
  constructor(    
    private route: ActivatedRoute,
    private router: Router,
    private categoryService: CategoryService,    
    private productService: ProductService,   
    private variantService: ProductVariantService 
  ) {
    
  } 
  ngOnInit() {
    this.getCategories(1, 100)
  } 
  getCategories(page: number, limit: number) {
    this.categoryService.getCategories(page, limit).subscribe({
      next: (apiresponse: ApiResponse) => {
        
        this.categories = apiresponse.data;
      },
      complete: () => {
        
      },
      error: (error: any) => {
        console.error('Error fetching categories:', error);
      }
    });
  }
  onFileChange(event: any) {
    // Retrieve selected files from input element
    const files = event.target.files;
    // Limit the number of selected files to 5
    if (files.length > 5) {
      alert('Please select a maximum of 5 images.');
      return;
    }
    // Store the selected files in the newProduct object
    this.insertProductDTO.images = files;
  }

  insertProduct() {    
    this.productService.insertProduct(this.insertProductDTO).subscribe({
      next: (response) => {
        
        const productId = response.id; // Assuming the response contains the newly created product's ID
        if (this.insertProductDTO.images.length > 0) {
          
          this.productService.uploadImages(productId, this.insertProductDTO.images).subscribe({
            next: (imageResponse) => {
              
              // Handle the uploaded images response if needed              
              console.log('Images uploaded successfully:', imageResponse);
              // Navigate back to the previous page
              this.router.navigate(['../'], { relativeTo: this.route });
            },
            error: (error) => {
              // Handle the error while uploading images
              alert(error.error)
              console.error('Error uploading images:', error);
            }
          })          
        }

        if (this.insertProductDTO.variants.length > 0) {
          this.insertProductDTO.variants.forEach(variant => {
            this.variantService.insertVariants(productId, variant).subscribe({
              next: (response) => {
                console.log(`Thêm biến thể "${variant}" thành công!`, response);
              }, error:( error) => {
                alert(error.error)
                console.log('Error:', error)
              }
            })
          })
        }
      },
      error: (error) => {
        
        // Handle error while inserting the product
        alert(error.error)
        console.error('Error inserting product:', error);
      }
    });    
  }

  addVariant() {
    this.insertProductDTO.variants.push({ variant: '', stock: 0});
  }
}
