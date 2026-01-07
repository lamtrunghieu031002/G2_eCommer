import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatMessage, ProductSuggestion } from '../../models/chatbot.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-chatbot-window',
  templateUrl: './chatbot-window.component.html',
  styleUrls: ['./chatbot-window.component.scss'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateY(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateY(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateY(100%)', opacity: 0 }))
      ])
    ]),
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('200ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ])
  ]
})
export class ChatbotWindowComponent implements OnInit, OnDestroy, AfterViewChecked {
  
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  
  // State
  isOpen = false;
  isTyping = false;
  messages: (ChatMessage & { products?: ProductSuggestion[] })[] = [];
  userInput = '';
  
  // Quick suggestions
  quickSuggestions = [
    'Tìm điện thoại giá rẻ',
    'Laptop cho sinh viên',
    'Tai nghe bluetooth tốt'
  ];
  
  private stateSubscription?: Subscription;
  private shouldScrollToBottom = false;

  constructor(
    private chatbotService: ChatbotService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to chatbot state
    this.stateSubscription = this.chatbotService.chatbotState$.subscribe(
      state => {
        const wasOpen = this.isOpen;
        this.isOpen = state.isOpen;
        this.isTyping = state.isTyping;
        
        // ✅ CHỈ LOAD LỊCH SỬ LẦN ĐẦU TIÊN MỞ CHATBOT
        if (this.isOpen && !wasOpen && this.messages.length === 0) {
          this.loadLocalHistory();
        }
      }
    );
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    if (this.stateSubscription) {
      this.stateSubscription.unsubscribe();
    }
  }

  /**
   * Load lịch sử chat từ localStorage
   */
  loadLocalHistory(): void {
    const history = this.chatbotService.getLocalHistory();
    if (history.messages && history.messages.length > 0) {
      this.messages = history.messages;
      this.shouldScrollToBottom = true;
    }
  }

  /**
   * Gửi tin nhắn
   */
  sendMessage(event: Event): void {
    event.preventDefault();
    
    const message = this.userInput.trim();
    if (!message || this.isTyping) {
      return;
    }

    // ✅ 1. Tạo tin nhắn USER
    const userMessage: ChatMessage = {
      message: message,
      sender: 'USER',
      createdAt: new Date()
    };
    
    // ✅ 2. Thêm vào UI ngay lập tức
    this.messages.push(userMessage);
    
    // ✅ 3. Lưu vào localStorage
    this.chatbotService.addMessageToHistory(userMessage);
    
    this.userInput = '';
    this.shouldScrollToBottom = true;

    // ✅ 4. Gọi API
    this.chatbotService.sendMessage(message).subscribe({
      next: (response) => {
        // Thêm tin nhắn bot vào UI (đã được lưu localStorage trong service)
        this.messages.push({
          message: response.message,
          sender: 'BOT',
          createdAt: new Date(response.timestamp),
          products: response.products
        });
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Error sending message:', error);
        // Thêm error message
        const errorMessage: ChatMessage = {
          message: '❌ Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại!',
          sender: 'BOT',
          createdAt: new Date()
        };
        this.messages.push(errorMessage);
        this.chatbotService.addMessageToHistory(errorMessage);
        this.shouldScrollToBottom = true;
      }
    });
  }

  /**
   * Gửi tin nhắn nhanh từ suggestions
   */
  sendQuickMessage(suggestion: string): void {
    this.userInput = suggestion;
    this.sendMessage(new Event('submit'));
  }

  /**
   * Đóng chat window
   */
  closeChat(): void {
    this.chatbotService.toggleChatbot();
  }

  /**
   * Xóa lịch sử chat
   */
  clearChat(): void {
    if (confirm('Bạn có chắc muốn xóa toàn bộ lịch sử chat?')) {
      this.messages = [];
      this.chatbotService.clearSession();
      this.chatbotService.clearLocalHistory();
    }
  }

  /**
   * Scroll to bottom của messages container
   */
  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        const element = this.messagesContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  /**
   * Format message text (hỗ trợ HTML basic)
   */
  formatMessage(message: string): string {
    if (!message) return '';
    
    // Convert line breaks to <br>
    let formatted = message.replace(/\n/g, '<br>');
    
    // Convert **bold** to <strong>
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    
    // Convert *italic* to <em>
    formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');
    
    return formatted;
  }

  /**
   * Format thời gian
   */
  formatTime(date?: Date): string {
    if (!date) return '';
    
    const messageDate = new Date(date);
    const now = new Date();
    const diffMs = now.getTime() - messageDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} giờ trước`;
    
    return messageDate.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format giá tiền
   */
  formatPrice(price: number): string {
    if (price >= 1000000) {
      return `${(price / 1000000).toFixed(1)} triệu`;
    }
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }

  /**
   * Lấy URL hình ảnh sản phẩm
   */
  getProductImage(thumbnail?: string): string {
    if (!thumbnail) {
      return 'assets/noImage.png';  // ✅ ẢNH MẶC ĐỊNH KHI KHÔNG CÓ THUMBNAIL
    }
    
    // Nếu là URL đầy đủ thì return luôn
    if (thumbnail.startsWith('http')) {
      return thumbnail;
    }
    
    // Nếu là relative path thì ghép với backend URL
    return `${environment.apiBaseUrl.replace('/api/v1', '')}/uploads/${thumbnail}`;
  }

  /**
   * Handle error khi load hình ảnh thất bại
   * ✅ HIỂN THỊ ẢNH 404 KHI LOAD LỖI
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    
    // Nếu đã là ảnh 404 rồi thì không làm gì nữa (tránh loop vô hạn)
    if (img.src.includes('404.png')) {
      return;
    }
    
    // Hiển thị ảnh 404
    img.src = 'assets/404.png';  // ✅ ẢNH HIỂN THỊ KHI LOAD LỖI
  }

  /**
   * Xem chi tiết sản phẩm
   */
  viewProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
    // Đóng chatbot khi chuyển trang
    this.closeChat();
  }
}
