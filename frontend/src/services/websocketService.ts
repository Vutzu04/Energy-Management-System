import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';

/**
 * WebSocket Service for real-time notifications and updates
 * Handles STOMP protocol over WebSocket for receiving:
 * - Overconsumption alerts from monitoring service
 * - System notifications
 * - Chat messages (forwarded by customer support service)
 */

type NotificationCallback = (notification: any) => void;
type ChatCallback = (message: any) => void;

class WebSocketService {
  private stompClient: Client | null = null;
  private isConnecting = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  /**
   * Connect to WebSocket server
   * @param onConnect Callback when connection is established
   * @param onError Callback on connection error
   */
  connect(onConnect?: () => void, onError?: (error: any) => void): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.stompClient?.connected) {
        console.log('✅ WebSocket already connected');
        onConnect?.();
        resolve();
        return;
      }

      if (this.isConnecting) {
        console.log('⏳ WebSocket connection in progress...');
        return;
      }

      this.isConnecting = true;

      try {
        const socket = new SockJS('http://localhost:8085/ws');
        this.stompClient = new Client({
          webSocketFactory: () => socket,
          connectHeaders: {
            login: 'guest',
            passcode: 'guest',
          },
          debug: (str) => {
            console.log('[WebSocket Debug]', str);
          },
          reconnectDelay: this.reconnectDelay,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });

        this.stompClient.onConnect = () => {
          console.log('✅ WebSocket connected successfully');
          this.isConnecting = false;
          this.reconnectAttempts = 0;
          onConnect?.();
          resolve();
        };

        this.stompClient.onStompError = (frame) => {
          console.error('❌ STOMP error:', frame);
          this.isConnecting = false;
          const error = new Error(`STOMP error: ${frame.body}`);
          onError?.(error);
          reject(error);
        };

        this.stompClient.onWebSocketError = (error) => {
          console.error('❌ WebSocket error:', error);
          this.isConnecting = false;
          onError?.(error);
          reject(error);
        };

        this.stompClient.activate();
      } catch (error) {
        console.error('❌ Error creating WebSocket connection:', error);
        this.isConnecting = false;
        onError?.(error);
        reject(error);
      }
    });
  }

  /**
   * Subscribe to overconsumption notifications
   * @param callback Function to call when notification is received
   * @returns Subscription ID for unsubscribing
   */
  subscribeToNotifications(callback: NotificationCallback): string | null {
    if (!this.stompClient?.connected) {
      console.error('❌ WebSocket not connected. Cannot subscribe to notifications.');
      return null;
    }

    const subscription = this.stompClient.subscribe('/topic/notifications', (message: IMessage) => {
      try {
        const notification = JSON.parse(message.body);
        console.log('📨 Received notification:', notification);
        callback(notification);
      } catch (error) {
        console.error('❌ Error parsing notification:', error);
      }
    });

    console.log('✅ Subscribed to /topic/notifications');
    return subscription.id;
  }

  /**
   * Subscribe to chat messages for a specific user
   * @param userId User ID to receive chat messages for
   * @param callback Function to call when chat message is received
   * @returns Subscription ID for unsubscribing
   */
  subscribeToChat(userId: string, callback: ChatCallback): string | null {
    if (!this.stompClient?.connected) {
      console.error('❌ WebSocket not connected. Cannot subscribe to chat.');
      return null;
    }

    const subscription = this.stompClient.subscribe(`/user/${userId}/queue/chat`, (message: IMessage) => {
      try {
        const chatMessage = JSON.parse(message.body);
        console.log('💬 Received chat message:', chatMessage);
        callback(chatMessage);
      } catch (error) {
        console.error('❌ Error parsing chat message:', error);
      }
    });

    console.log(`✅ Subscribed to chat messages for user ${userId}`);
    return subscription.id;
  }

  /**
   * Unsubscribe from a topic
   * @param subscriptionId The subscription ID returned by subscribe methods
   */
  unsubscribe(subscriptionId: string | null): void {
    if (subscriptionId && this.stompClient?.connected) {
      this.stompClient.unsubscribe(subscriptionId);
      console.log('✅ Unsubscribed from topic');
    }
  }

  /**
   * Send a message to a topic (for testing or future features)
   * @param destination Topic destination
   * @param body Message body
   */
  send(destination: string, body: any): void {
    if (!this.stompClient?.connected) {
      console.error('❌ WebSocket not connected. Cannot send message.');
      return;
    }

    try {
      this.stompClient.publish({
        destination,
        body: JSON.stringify(body),
      });
      console.log('✅ Message sent to', destination);
    } catch (error) {
      console.error('❌ Error sending message:', error);
    }
  }

  /**
   * Check if WebSocket is connected
   */
  isConnected(): boolean {
    return this.stompClient?.connected ?? false;
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect(): void {
    if (this.stompClient?.connected) {
      this.stompClient.deactivate();
      console.log('✅ WebSocket disconnected');
    }
  }
}

// Export singleton instance
export const websocketService = new WebSocketService();
