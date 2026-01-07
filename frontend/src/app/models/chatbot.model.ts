/**
 * Chatbot Models & Interfaces
 * Tương thích 100% với Backend API (Spring Boot)
 */

/**
 * Interface cho tin nhắn đơn lẻ
 */
export interface ChatMessage {
  id?: number;
  message: string;
  sender: 'USER' | 'BOT';
  createdAt?: Date;
  productIds?: string; // "1,2,3" - danh sách ID sản phẩm
}

/**
 * Interface cho phiên chat
 */
export interface ChatSession {
  id?: number;
  sessionId: string;
  userId?: number;
  createdAt?: Date;
  updatedAt?: Date;
  isActive: boolean;
  messages?: ChatMessage[];
}

/**
 * Interface cho sản phẩm gợi ý từ bot
 */
export interface ProductSuggestion {
  id: number;
  name: string;
  price: number;
  thumbnail?: string;
  description?: string;
  reason?: string; // Lý do AI gợi ý sản phẩm này
}

/**
 * Interface cho request gửi lên API
 * POST /api/v1/chatbot/chat
 */
export interface ChatRequest {
  session_id?: string; // null nếu là chat mới
  message: string;
  user_id?: number; // null nếu là guest
}

/**
 * Interface cho response từ API
 */
export interface ChatResponse {
  session_id: string;
  message: string; // Câu trả lời từ bot
  timestamp: Date;
  products?: ProductSuggestion[]; // Danh sách sản phẩm gợi ý
  message_type: 'text' | 'product_list' | 'history' | 'comparison';
}

/**
 * Interface cho Response wrapper từ backend
 */
export interface ChatApiResponse {
  status: string;
  message: string;
  data: ChatResponse;
}

/**
 * Interface cho trạng thái chatbot UI
 */
export interface ChatbotState {
  isOpen: boolean;
  isMinimized: boolean;
  isTyping: boolean;
  hasUnreadMessages: boolean;
  unreadCount: number;
}

/**
 * Interface cho lịch sử chat (lưu localStorage)
 */
export interface ChatHistory {
  sessionId: string;
  messages: ChatMessage[];
  lastUpdated: Date;
}

/**
 * Enum cho các loại tin nhắn
 */
export enum MessageType {
  TEXT = 'text',
  PRODUCT_LIST = 'product_list',
  HISTORY = 'history',
  COMPARISON = 'comparison'
}

/**
 * Enum cho người gửi
 */
export enum MessageSender {
  USER = 'USER',
  BOT = 'BOT'
}
