import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatbotState } from '../../models/chatbot.model';

@Component({
  selector: 'app-chatbot-button',
  template: `
    <button 
      class="chatbot-button"
      [class.has-unread]="chatbotState.hasUnreadMessages"
      (click)="toggleChatbot()"
      [attr.aria-label]="'Mở chatbot'"
      title="Chat với chúng tôi">
      
      <!-- Icon chat -->
      <i class="fa fa-comments" *ngIf="!chatbotState.isOpen"></i>
      <i class="fa fa-times" *ngIf="chatbotState.isOpen"></i>
      
      <!-- Badge số tin nhắn chưa đọc -->
      <span 
        class="unread-badge" 
        *ngIf="chatbotState.hasUnreadMessages && !chatbotState.isOpen">
        {{ chatbotState.unreadCount }}
      </span>
      
      <!-- Pulse animation -->
      <span class="pulse-ring" *ngIf="chatbotState.hasUnreadMessages && !chatbotState.isOpen"></span>
    </button>
  `,
  styleUrls: ['./chatbot-button.component.scss']
})
export class ChatbotButtonComponent implements OnInit, OnDestroy {
  
  chatbotState: ChatbotState = {
    isOpen: false,
    isMinimized: false,
    isTyping: false,
    hasUnreadMessages: false,
    unreadCount: 0
  };
  
  private stateSubscription?: Subscription;

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit(): void {
    // Subscribe to chatbot state changes
    this.stateSubscription = this.chatbotService.chatbotState$.subscribe(
      state => {
        this.chatbotState = state;
      }
    );
  }

  ngOnDestroy(): void {
    if (this.stateSubscription) {
      this.stateSubscription.unsubscribe();
    }
  }

  /**
   * Toggle mở/đóng chatbot window
   */
  toggleChatbot(): void {
    this.chatbotService.toggleChatbot();
  }
}
