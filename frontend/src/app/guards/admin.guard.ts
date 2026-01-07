import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, CanActivateFn, CanMatchFn, Route, UrlSegment } from '@angular/router';
import { Router } from '@angular/router'; // Đảm bảo bạn đã import Router ở đây.
import { inject } from '@angular/core';
import { UserService } from '../services/user.service';
import { UserResponse } from '../responses/user/user.response';
import { TokenService } from '../services/token.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard {
  userResponse?:UserResponse | null;
  constructor(
    private tokenService: TokenService, 
    private router: Router,
    private userService:UserService 
  ) {}

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const isTokenExpired = this.tokenService.isTokenExpired();
    const isUserIdValid = this.tokenService.getUserId() > 0;
    this.userResponse = this.userService.getUserResponseFromLocalStorage();
    // const isAdmin = this.userResponse?.role.name === 'admin';
    const role = this.tokenService.getRoleFromToken();
    const isAdmin = role === 'ADMIN';
    
    if (!isTokenExpired && isUserIdValid && isAdmin ) {
      return true;
    } else {
      // Nếu không authenticated, trả về trang login:
      this.router.navigate(['/login']);
      return false;
    }
  }  
}

export const AdminGuardFn: CanActivateFn & CanMatchFn = (
  route: ActivatedRouteSnapshot | Route, 
  state: RouterStateSnapshot | UrlSegment[]
): boolean => {
  return inject(AdminGuard).canActivate(route as any, state as any);
};
