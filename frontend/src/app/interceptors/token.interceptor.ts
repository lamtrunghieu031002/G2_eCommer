import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TokenService } from '../services/token.service';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
    constructor(private tokenService: TokenService) { }

    intercept(
        req: HttpRequest<any>,
        next: HttpHandler): Observable<HttpEvent<any>> {
        
        // ✅ FIX: Bỏ qua token cho TẤT CẢ chatbot endpoints
        if (req.url.includes('/chatbot/')) {
            console.log('🤖 Chatbot request - Skipping token', req.url);
            return next.handle(req);
        }
        
        // ✅ FIX: Nếu có header X-Skip-Auth thì bỏ qua
        if (req.headers.has('X-Skip-Auth')) {
            console.log('⚠️ Skip-Auth header detected', req.url);
            const newReq = req.clone({
                headers: req.headers.delete('X-Skip-Auth')
            });
            return next.handle(newReq);
        }
        
        // Xử lý bình thường cho các request khác
        const token = this.tokenService.getToken();
        if (token) {
            req = req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`,
                },
            });
        }
        return next.handle(req);
    }

}