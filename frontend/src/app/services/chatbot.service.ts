import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { 
  ChatRequest, 
  ChatResponse, 
  ChatApiResponse,
  ChatbotState,
  ChatHistory,
  ChatMessage
} from '../models/chatbot.model';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiBaseUrl = environment.apiBaseUrl;
  private chatbotUrl = `${this.apiBaseUrl}/chatbot`;
  
  // BehaviorSubject để quản lý trạng thái chatbot
  private chatbotStateSubject = new BehaviorSubject<ChatbotState>({
    isOpen: false,
    isMinimized: false,
    isTyping: false,
    hasUnreadMessages: false,
    unreadCount: 0
  });
  
  public chatbotState$ = this.chatbotStateSubject.asObservable();
  
  // Session ID hiện tại
  private currentSessionId: string | null = null;
  
  // LocalStorage keys
  private readonly SESSION_KEY = 'chatbot_session_id';
  private readonly HISTORY_KEY = 'chatbot_history';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {
    this.loadSessionFromStorage();
  }

  /**
   * Lấy trạng thái chatbot hiện tại
   */
  getChatbotState(): ChatbotState {
    return this.chatbotStateSubject.value;
  }

  /**
   * Cập nhật trạng thái chatbot
   */
  updateChatbotState(state: Partial<ChatbotState>): void {
    const currentState = this.chatbotStateSubject.value;
    this.chatbotStateSubject.next({ ...currentState, ...state });
  }

  /**
   * Toggle mở/đóng chatbot
   */
  toggleChatbot(): void {
    const currentState = this.getChatbotState();
    this.updateChatbotState({ 
      isOpen: !currentState.isOpen,
      hasUnreadMessages: false,
      unreadCount: 0
    });
  }

  /**
   * Tạo session mới
   * POST /api/v1/chatbot/session/new
   */
  createNewSession(): Observable<string> {
    const userId = this.tokenService.getUserId();
    const params: any = userId > 0 ? { userId: userId.toString() } : {};
    
    return this.http.post<ChatApiResponse>(
      `${this.chatbotUrl}/session/new`,
      null,
      { params }
    ).pipe(
      map((response: any) => {
        const sessionId = response.data as string;
        this.currentSessionId = sessionId;
        this.saveSessionToStorage(sessionId);
        return sessionId;
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Gửi tin nhắn tới chatbot
   * POST /api/v1/chatbot/chat
   */
  sendMessage(message: string): Observable<ChatResponse> {
    // Bật typing indicator
    this.updateChatbotState({ isTyping: true });

    // Lấy session_id hiện tại hoặc null nếu chưa có
    const sessionId = this.currentSessionId;
    const userId = this.tokenService.getUserId();

    const request: ChatRequest = {
      session_id: sessionId || undefined,
      message: message,
      user_id: userId > 0 ? userId : undefined
    };

    // ✅ GỬI REQUEST - Token được gửi tự động bởi TokenInterceptor
    return this.http.post<ChatApiResponse>(
      `${this.chatbotUrl}/chat`,
      request
    ).pipe(
      map(response => {
        // Tắt typing indicator
        this.updateChatbotState({ isTyping: false });
        
        const chatResponse = response.data;
        
        // Lưu session_id nếu chưa có
        if (chatResponse.session_id && !this.currentSessionId) {
          this.currentSessionId = chatResponse.session_id;
          this.saveSessionToStorage(chatResponse.session_id);
        }
        
        // ✅ CHỈ LƯU TIN NHẮN BOT (User message đã được lưu ở component)
        this.addMessageToHistory({
          message: chatResponse.message,
          sender: 'BOT',
          createdAt: new Date(chatResponse.timestamp),
          productIds: chatResponse.products?.map(p => p.id).join(',')
        });
        
        return chatResponse;
      }),
      catchError(error => {
        this.updateChatbotState({ isTyping: false });
        return this.handleError(error);
      })
    );
  }

  /**
   * Lấy lịch sử chat
   * GET /api/v1/chatbot/history/{sessionId}
   */
  getChatHistory(sessionId?: string): Observable<ChatResponse> {
    const id = sessionId || this.currentSessionId;
    
    if (!id) {
      return throwError(() => new Error('No session ID available'));
    }

    return this.http.get<ChatApiResponse>(
      `${this.chatbotUrl}/history/${id}`
    ).pipe(
      map(response => response.data),
      catchError(this.handleError)
    );
  }

  /**
   * Health check
   * GET /api/v1/chatbot/health
   */
  healthCheck(): Observable<any> {
    return this.http.get<ChatApiResponse>(`${this.chatbotUrl}/health`).pipe(
      map(response => response.data),
      catchError(this.handleError)
    );
  }

  /**
   * Lưu session ID vào localStorage
   */
  private saveSessionToStorage(sessionId: string): void {
    localStorage.setItem(this.SESSION_KEY, sessionId);
  }

  /**
   * Load session ID từ localStorage
   */
  private loadSessionFromStorage(): void {
    const sessionId = localStorage.getItem(this.SESSION_KEY);
    if (sessionId) {
      this.currentSessionId = sessionId;
    }
  }

  /**
   * Xóa session hiện tại
   */
  clearSession(): void {
    this.currentSessionId = null;
    localStorage.removeItem(this.SESSION_KEY);
    localStorage.removeItem(this.HISTORY_KEY);
  }

  /**
   * Lấy session ID hiện tại
   */
  getCurrentSessionId(): string | null {
    return this.currentSessionId;
  }

  /**
   * Lưu tin nhắn vào localStorage history
   * PUBLIC method để component có thể gọi
   */
  addMessageToHistory(message: ChatMessage): void {
    const history = this.getLocalHistory();
    history.messages.push(message);
    history.lastUpdated = new Date();
    localStorage.setItem(this.HISTORY_KEY, JSON.stringify(history));
  }

  /**
   * Lấy lịch sử từ localStorage
   */
  getLocalHistory(): ChatHistory {
    const stored = localStorage.getItem(this.HISTORY_KEY);
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        return {
          sessionId: parsed.sessionId || this.currentSessionId || '',
          messages: parsed.messages || [],
          lastUpdated: new Date(parsed.lastUpdated)
        };
      } catch (e) {
        console.error('Error parsing chat history:', e);
      }
    }
    
    return {
      sessionId: this.currentSessionId || '',
      messages: [],
      lastUpdated: new Date()
    };
  }

  /**
   * Xóa lịch sử local
   */
  clearLocalHistory(): void {
    localStorage.removeItem(this.HISTORY_KEY);
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi khi kết nối với chatbot';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Lỗi: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Lỗi ${error.status}: ${error.error?.message || error.message}`;
    }
    
    console.error('Chatbot Service Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  /**
   * Kiểm tra xem có tin nhắn chưa đọc không
   */
  markMessagesAsRead(): void {
    this.updateChatbotState({
      hasUnreadMessages: false,
      unreadCount: 0
    });
  }

  /**
   * Đánh dấu có tin nhắn mới
   */
  addUnreadMessage(): void {
    const currentState = this.getChatbotState();
    this.updateChatbotState({
      hasUnreadMessages: true,
      unreadCount: currentState.unreadCount + 1
    });
  }
}
