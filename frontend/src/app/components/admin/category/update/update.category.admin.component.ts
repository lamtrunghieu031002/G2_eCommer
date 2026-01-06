import { Component, OnInit } from '@angular/core';
import { Category } from '../../../../models/category';
import { CategoryService } from '../../../../services/category.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UpdateCategoryDTO } from '../../../../dtos/category/update.category.dto';
import { ApiResponse } from 'src/app/responses/api.response';
import { ToastService } from 'src/app/services/toast.service';


@Component({
  selector: 'app-detail.category.admin',
  templateUrl: './update.category.admin.component.html',
  styleUrls: ['./update.category.admin.component.scss']
})

export class UpdateCategoryAdminComponent implements OnInit {
  categoryId: number;
  updatedCategory: Category;
  
  constructor(
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService
  
  ) {
    this.categoryId = 0;    
    this.updatedCategory = {} as Category;  
  }

  ngOnInit(): void {    
    this.route.paramMap.subscribe(params => {
      this.categoryId = Number(params.get('id'));
      this.getCategoryDetails();
    });
    
  }
  
  getCategoryDetails(): void {
    this.categoryService.getDetailCategory(this.categoryId).subscribe({
      next: (apiResponse: ApiResponse) => {        
        this.updatedCategory = { ...apiResponse.data };                        
      },
      complete: () => {
        
      },
      error: (error: any) => {
        
      }
    });     
  }
  updateCategory() {
    // Implement your update logic here
    const updateCategoryDTO: UpdateCategoryDTO = {
      name: this.updatedCategory.name,      
    };
    this.categoryService.updateCategory(this.updatedCategory.id, updateCategoryDTO).subscribe({
      next: (response: any) => {  
      },
      complete: () => {
        this.toastService.showToast({
          error: null,
          defaultMsg: 'Cập nhật danh mục thành công',
          title: 'Thành Công'
        });
        this.router.navigate(['/admin/categories']);     
      },
      error: (error: any) => {
        console.error('Error fetching categorys:', error);
      }
    });  
  }  
}
