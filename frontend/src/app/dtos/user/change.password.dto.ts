export class ChangePasswordDTO {

    current_password: string;

    new_password: string;
    confirm_password: string;   
    
    constructor(data: any) {
        this.confirm_password = data.currentPassword;
        this.new_password = data.newPassword;
        this.current_password = data.currentPassword;        
    }
}