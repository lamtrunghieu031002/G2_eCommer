import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { UserService } from '../../services/user.service';

import { Router } from '@angular/router';
import { TokenService } from '../../services/token.service';
import { UserResponse } from '../../responses/user/user.response';
import { Category } from 'src/app/models/category';
import { CategoryService } from 'src/app/services/category.service';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit{
  userResponse?:UserResponse | null;
  isPopoverOpen = false;
  activeNavItem: number = 0;
  categories: Category[] = []; // Dữ liệu động từ categoryService
  selectedCategoryId: number  = 0; // Giá trị category được chọn
  keyword:string = "";
  @Output() searchEvent = new EventEmitter<{ keyword: string, categoryId: number }>();

  constructor(
    private userService: UserService,       
    private tokenService: TokenService,    
    private router: Router,
    private categoryService: CategoryService,
  ) {
    
   }
  ngOnInit() {
    this.userResponse = this.userService.getUserResponseFromLocalStorage();    
    this.getCategories(0,100);
  }  

  togglePopover(event: Event): void {
    event.preventDefault();
    this.isPopoverOpen = !this.isPopoverOpen;
  }

  handleItemClick(index: number): void {
    //alert(`Clicked on "${index}"`);
    if(index === 0) {
      this.router.navigate(['/user-profile']);                      
    } else if (index == 1) {
      this.router.navigate(['/orders/user']);
    }
    else if (index === 2) {
      this.userService.removeUserFromLocalStorage();
      this.tokenService.removeToken();
      this.userResponse = this.userService.getUserResponseFromLocalStorage();    
    }
    this.isPopoverOpen = false; // Close the popover after clicking an item    
  }

  
  setActiveNavItem(index: number) {    
    this.activeNavItem = index;
    //alert(this.activeNavItem);
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

  searchProducts() {
    this.searchEvent.emit({ keyword: this.keyword, categoryId: this.selectedCategoryId });  
  }
}
