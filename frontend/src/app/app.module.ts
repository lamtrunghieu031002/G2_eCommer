import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

// Routing
import { AppRoutingModule } from './app-routing.module';

// Components
import { AppComponent } from './app/app.component';
import { HomeComponent } from './components/home/home.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { DetailProductComponent } from './components/detail-product/detail-product.component';
import { OrderComponent } from './components/order/order.component';
import { OrderUserComponent } from './components/order_user/order.user.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { UserProfileComponent } from './components/user-profile/user.profile.component';
import { OrderDetailComponent } from './components/detail-order/order.detail.component';
import { ChangePasswordComponent } from './components/change_password/change.password.component';
import { ReviewComponent } from './components/review/review.component';

// Chatbot Components
import { ChatbotButtonComponent } from './components/chatbot-button/chatbot-button.component';
import { ChatbotWindowComponent } from './components/chatbot-window/chatbot-window.component';

// Modules
import { AdminModule } from './components/admin/admin.module';

// Interceptors
import { TokenInterceptor } from './interceptors/token.interceptor';

@NgModule({
  declarations: [    
    AppComponent,
    HomeComponent, 
    HeaderComponent,
    FooterComponent, 
    DetailProductComponent, 
    OrderComponent, 
    OrderUserComponent, 
    LoginComponent, 
    RegisterComponent, 
    UserProfileComponent,
    OrderDetailComponent, 
    ChangePasswordComponent,
    // CHATBOT COMPONENTS
    ChatbotButtonComponent,
    ChatbotWindowComponent,
    AppComponent,
    OrderDetailComponent,
    ChangePasswordComponent,
    ReviewComponent 
    //admin    
    //AdminComponent,
    //OrderAdminComponent,
    //ProductAdminComponent,
    //CategoryAdminComponent,
    //DetailOrderAdminComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,    
    AppRoutingModule,    
    NgbModule,        
    AdminModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true,
    },
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
