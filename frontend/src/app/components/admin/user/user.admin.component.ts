import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Route, Router } from '@angular/router';
import { UserResponse } from '../../../responses/user/user.response';
import { ApiResponse } from '../../../responses/api.response';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-user-admin',
  templateUrl: './user.admin.component.html',
  styleUrls: ['./user.admin.component.scss']
})
export class UserAdminComponent implements OnInit {
  users: UserResponse[] = [];
  currentPage: number = 0;
  itemsPerPage: number = 12;
  pages: number[] = [];
  totalPages: number = 0;
  visiblePages: number[] = [];
  keyword: string = "";
  localStorage?: Storage;

  constructor(
    private userService: UserService,
    private router: Router,
  ) {

  }
  ngOnInit(): void {
    this.currentPage = Number(this.localStorage?.getItem('currentUserAdminPage')) || 0;
    this.getUsers(this.keyword, this.currentPage, this.itemsPerPage);
  }

  searchUsers() {
    this.currentPage = 0;
    this.itemsPerPage = 10;
    this.getUsers(this.keyword.trim(), this.currentPage, this.itemsPerPage);
  }

  getUsers(keyword: string, page: number, limit: number) {
    this.userService.getUsers({ keyword, page, limit }).subscribe({
      next: (apiResponse: ApiResponse) => {
        
        const response = apiResponse.data
        this.users = response.users;
        this.totalPages = response.totalPages;
        this.visiblePages = this.generateVisiblePageArray(this.currentPage, this.totalPages);
      },
      complete: () => {
        // Handle complete event
        
      },
      error: (error: any) => {
        // this.toastService.showToast({
        //   error: error,
        //   defaultMsg: 'Lỗi tải danh sách người dùng',
        //   title: 'Lỗi Tải Dữ Liệu'
        // });
        console.error('Error fetching products:', error)
      }
    });
  }

  generateVisiblePageArray(currentPage: number, totalPages: number): number[] {
    const maxVisiblePages = 5;
    const halfVisiblePages = Math.floor(maxVisiblePages / 2);

    let startPage = Math.max(currentPage - halfVisiblePages, 1);
    let endPage = Math.min(startPage + maxVisiblePages - 1, totalPages);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(endPage - maxVisiblePages + 1, 1);
    }

    return new Array(endPage - startPage + 1).fill(0)
      .map((_, index) => startPage + index);
  }

  onPageChange(page: number) {
    this.currentPage = page < 0 ? 0 : page;
    this.localStorage?.setItem('currentUserAdminPage', String(this.currentPage));
    this.getUsers(this.keyword, this.currentPage, this.itemsPerPage);
  }
  // Hàm xử lý sự kiện khi thêm mới user
  // insertUser() {
  //   
  //   // Điều hướng đến trang detail-user với userId là tham số
  //   this.router.navigate(['/admin/users/insert']);
  // } 

  // // Hàm xử lý sự kiện khi 1 user được bấm vào
  // updateUser(userId: number) {
  //   
  //   // Điều hướng đến trang detail-user với userId là tham số
  //   this.router.navigate(['/admin/users/update', userId]);
  // }  

  trackByUserId(index: number, user: any): number {
    return user.id;
  }
  // resetPassword(userId: number) {
  //   this.userService.resetPassword(userId).subscribe({
  //     next: (apiResponse: ApiResponse) => {
  //       console.error('Block/unblock user successfully');
  //       //location.reload();
  //     },
  //     complete: () => {
  //       // Handle complete event
  //     },
  //     error: (error: HttpErrorResponse) => {
  //       this.toastService.showToast({
  //         error: error,
  //         defaultMsg: 'Lỗi reset mật khẩu',
  //         title: 'Lỗi Bảo Mật'
  //       });
  //     }
  //   });
  // }

  // toggleUserStatus(user: UserResponse) {
  //   let confirmation: boolean;
  //   if (user.is_active) {
  //     confirmation = window.confirm('Are you sure you want to block this user?');
  //   } else {
  //     confirmation = window.confirm('Are you sure you want to enable this user?');
  //   }

  //   if (confirmation) {
  //     const params = {
  //       userId: user.id,
  //       enable: !user.is_active
  //     };

  //     this.userService.toggleUserStatus(params).subscribe({
  //       next: (response: any) => {
  //         console.error('Block/unblock user successfully');
  //         location.reload();
  //       },
  //       complete: () => {
  //         // Handle complete event
  //       },
  //       error: (error: HttpErrorResponse) => {
  //         this.toastService.showToast({
  //           error: error,
  //           defaultMsg: 'Lỗi thay đổi trạng thái người dùng',
  //           title: 'Lỗi Hệ Thống'
  //         });
  //       }
  //     });
  //   }      
  // }
}
