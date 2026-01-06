import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { ChangePasswordDTO } from 'src/app/dtos/user/change.password.dto';
import { ApiResponse } from 'src/app/responses/api.response';
import { ToastService } from 'src/app/services/toast.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-change-password',
  templateUrl: './change.password.component.html',
  styleUrls: ['./change.password.component.scss']
})

export class ChangePasswordComponent {
  changePasswordForm: FormGroup;
  showChangePassword: boolean = false;
  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private toastService: ToastService,
    private router: Router,
  ) {
    this.changePasswordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(3)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(3)]],
    }, { validators: this.passwordsMatchValidator });
    

  }
  passwordsMatchValidator(form: AbstractControl) {
    const { newPassword, confirmPassword } = form.value;
    return newPassword && confirmPassword && newPassword === confirmPassword
      ? null
      : { passwordMismatch: true };
  }
  
  
  changePassword() {
    let userId = this.userService.getUserResponseFromLocalStorage()?.id;
    if (this.changePasswordForm.valid && userId ) {
      const requestDTO: ChangePasswordDTO = {
        current_password: this.changePasswordForm.get('currentPassword')?.value,
        new_password: this.changePasswordForm.get('newPassword')?.value,
        confirm_password: this.changePasswordForm.get('confirmPassword')?.value
      };

      this.userService.changePassword(userId, requestDTO).subscribe({
        next: (response: ApiResponse) => {
          this.toastService.showToast({
            error: null,
            defaultMsg: "Đổi mật khẩu thành công, vui lòng đăng nhập lại!",
            title: 'Thành Công'
          });
          this.router.navigate(['/login']);
        },
        error: (error) => {
          console.error('Đổi mật khẩu thất bại', error);
        }
      });
      console.log('Đổi mật khẩu thành công', this.changePasswordForm.value);
    } else {
      this.changePasswordForm.markAllAsTouched();
    }
  }

  
  
}

