import { Component, ViewChild, OnInit } from '@angular/core';
import { LoginDTO } from '../../dtos/user/login.dto';
import { UserService } from '../../services/user.service';
import { TokenService } from '../../services/token.service';
import { RoleService } from '../../services/role.service'; // Import RoleService
import { ActivatedRoute, Router } from '@angular/router';
import { NgForm } from '@angular/forms';
import { LoginResponse } from '../../responses/user/login.response';
import { Role } from '../../models/role'; // Đường dẫn đến model Role
import { UserResponse } from '../../responses/user/user.response';
import { CartService } from '../../services/cart.service';
import { ApiResponse } from 'src/app/responses/api.response';
import { ToastService } from 'src/app/services/toast.service';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit{
  @ViewChild('loginForm') loginForm!: NgForm;

  /*
  //Login user1
  phoneNumber: string = '0903428409';
  password: string = '30011211';

  //Login admin
  phoneNumber: string = '11223344';
  password: string = '11223344';

  */
  phoneNumber: string = '33445566';
  password: string = '123456789';
  showPassword: boolean = false;

  roles: Role[] = []; // Mảng roles
  rememberMe: boolean = true;
  //selectedRole: Role | undefined; // Biến để lưu giá trị được chọn từ dropdown
  userResponse?: UserResponse
  loginError: string = '';

  onPhoneNumberChange() {
    console.log(`Phone typed: ${this.phoneNumber}`);
    //how to validate ? phone must be at least 6 characters
  }
  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private userService: UserService,
    private tokenService: TokenService,
    private roleService: RoleService,
    private cartService: CartService,
  ) { }

  ngOnInit() {
    // Gọi API lấy danh sách roles và lưu vào biến roles
    
    this.roleService.getRoles().subscribe({      
      next: (roles: Role[]) => { // Sử dụng kiểu Role[]
        
        this.roles = roles;
        //this.selectedRole = roles.length > 0 ? roles[0] : undefined;
      },
      complete: () => {
        
      },  
      error: (error: any) => {
        
        console.error('Error getting roles:', error);
      }
    });
  }
  createAccount() {
    
    // Chuyển hướng người dùng đến trang đăng ký (hoặc trang tạo tài khoản)
    this.router.navigate(['/register']); 
  }
  login() {
    const loginDTO: LoginDTO = {
      phone_number: this.phoneNumber,
      password: this.password,
    };
    
    this.userService.login(loginDTO).subscribe({
      next: (apiResponse: ApiResponse) => {
        const { token } = apiResponse.data;
        
        if (this.rememberMe) {          
          this.tokenService.setToken(token);
          
          this.userService.getUserDetail(token).subscribe({
            next: (response: any) => {
              this.userResponse = {
                ...response,
                date_of_birth: new Date(response.date_of_birth),
              };    
              this.userService.saveUserResponseToLocalStorage(this.userResponse);
              
              // ✅ FIX: So sánh không phân biệt HOA/thường
              const roleName = this.userResponse?.role.name?.toLowerCase();
              
              console.log('User role:', roleName); // Debug log
              
              if (roleName === 'admin') {
                console.log('Redirecting to admin...');
                this.router.navigate(['/admin']);    
              } else if (roleName === 'user') {
                console.log('Redirecting to home...');
                this.router.navigate(['/']);                      
              } else {
                // Fallback nếu role không khớp
                console.warn('Unknown role:', roleName);
                this.router.navigate(['/']);
              }
            },
            complete: () => {
              this.cartService.refreshCart();
            },
            error: (error: any) => {
              console.error('Error getting user detail:', error);
              this.loginError = "Không thể lấy thông tin người dùng.";
            }
          });
        }                        
      },
      complete: () => {
        console.log('Login request completed');
      },
      error: (error: any) => {
        console.error('Login error:', error);
        this.loginError = "Đăng nhập thất bại. Vui lòng thử lại.";
      }
    });
  }
  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}
