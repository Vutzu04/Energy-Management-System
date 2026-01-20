import { useEffect, useState } from 'react';
import axios from 'axios';
import './ChatInterface.css';

interface ChatMessage {
  id?: string;
  userId: string;
  username: string;
  message: string;
  response?: string;
  responseType?: string;
  createdAt?: string;
  responseTime?: string;
}

export const ChatInterface = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [userId, setUserId] = useState<string>('');
  const [username, setUsername] = useState<string>('');
  const [userRole, setUserRole] = useState<string>('');
  const [respondingToId, setRespondingToId] = useState<string | null>(null);
  const [responseText, setResponseText] = useState('');
  const [respondingLoading, setRespondingLoading] = useState(false);

  // Initialize user info from localStorage
  useEffect(() => {
    const storedUserId = localStorage.getItem('userId');
    const storedUsername = localStorage.getItem('username');
    const storedRole = localStorage.getItem('role');
    
    if (storedUserId && storedUsername) {
      setUserId(storedUserId);
      setUsername(storedUsername);
      setUserRole(storedRole || '');
      
      // Load chat based on role
      if (storedRole === 'Administrator') {
        loadPendingAdminMessages();
      } else {
        loadChatHistory(storedUserId);
      }
    }

    // Auto-refresh every 3 seconds to check for new responses from admin
    const interval = setInterval(() => {
      if (storedUserId && storedUsername && storedRole !== 'Administrator') {
        loadChatHistory(storedUserId);
      } else if (storedRole === 'Administrator') {
        loadPendingAdminMessages();
      }
    }, 3000);

    return () => clearInterval(interval);
  }, []);

  const loadChatHistory = async (uid: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8086/api/chat/history/${uid}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
      );
      
      if (response.data && response.data.messages) {
        setMessages(response.data.messages);
      }
    } catch (error) {
      console.error('Error loading chat history:', error);
    }
  };

  const loadPendingAdminMessages = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        'http://localhost:8086/api/chat/admin/pending',
        { headers: { 'Authorization': `Bearer ${token}` } }
      );
      
      if (response.data && response.data.messages) {
        setMessages(response.data.messages);
        console.log('📨 Loaded ' + response.data.messages.length + ' pending messages');
        
        // DEBUG: Log each message to see its responseType
        response.data.messages.forEach((msg, idx) => {
          console.log(`Message ${idx}:`, {
            username: msg.username,
            responseType: msg.responseType,
            response: msg.response,
            id: msg.id
          });
        });
      }
    } catch (error) {
      console.error('Error loading pending messages:', error);
    }
  };

  const sendMessage = async () => {
    if (!inputText.trim()) return;
    if (!userId || !username) {
      alert('Please log in first');
      return;
    }

    const newMessage: ChatMessage = {
      userId,
      username,
      message: inputText
    };

    setLoading(true);

    try {
      const token = localStorage.getItem('token');
      
      // Send message to customer support service
      const response = await axios.post(
        'http://localhost:8086/api/chat/send',
        newMessage,
        { headers: { 'Authorization': `Bearer ${token}` } }
      );

      // Simulate receiving response (in real scenario, would come via WebSocket)
      if (response.data.status === 'success') {
        // Add user message to chat
        setMessages(prev => [...prev, {
          ...newMessage,
          createdAt: new Date().toISOString()
        }]);
        
        // Reload history to get the response
        setTimeout(() => {
          loadChatHistory(userId);
        }, 500);
      }

      setInputText('');
    } catch (error) {
      console.error('Error sending message:', error);
      alert('Failed to send message');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const respondToMessage = async (messageId: string) => {
    if (!responseText.trim()) return;

    setRespondingLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `http://localhost:8086/api/chat/admin/respond/${messageId}`,
        { response: responseText },
        { headers: { 'Authorization': `Bearer ${token}` } }
      );

      if (response.data.status === 'success') {
        console.log('✅ Admin response sent successfully');
        setResponseText('');
        setRespondingToId(null);
        
        // Reload pending messages
        setTimeout(() => {
          loadPendingAdminMessages();
        }, 500);
      }
    } catch (error) {
      console.error('Error sending response:', error);
      alert('Failed to send response');
    } finally {
      setRespondingLoading(false);
    }
  };

  const handleResponseKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      respondToMessage(respondingToId || '');
    }
  };

  if (!userId) {
    return (
      <div className="chat-container">
        <div className="chat-message system-message">
          Please log in to use the chat feature
        </div>
      </div>
    );
  }

  // Admin View - Shows pending messages from clients
  if (userRole === 'Administrator') {
    return (
      <div className="chat-wrapper">
        <div className="chat-header">
          <h2>📨 Customer Support - Admin Panel</h2>
          <p className="user-info">Logged in as: <strong>{username}</strong> (Administrator)</p>
        </div>

        <div className="chat-container">
          {messages.length === 0 ? (
            <div className="chat-empty">
              <p>✅ No pending messages</p>
              <p className="info-text">All client inquiries have been resolved!</p>
            </div>
          ) : (
            <>
              <div className="admin-stats">
                <span className="stat-badge">{messages.length} pending messages</span>
              </div>
              {messages.map((msg, idx) => {
                const isPending = msg.responseType === 'FORWARDED_TO_ADMIN' && msg.responseType !== 'ADMIN_RESPONSE';
                const isResponding = respondingToId === msg.id;
                const isResponded = msg.responseType === 'ADMIN_RESPONSE';

                return (
                  <div key={idx} className="admin-message-group">
                    {/* Client Message */}
                    <div className="chat-message client-message">
                      <div className="message-header">
                        <strong>👤 {msg.username}</strong>
                        <span className="message-time">
                          {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString() : ''}
                        </span>
                      </div>
                      <div className="message-content">{msg.message}</div>
                      <div className="client-id">ID: {msg.userId?.substring(0, 8)}...</div>
                    </div>

                    {/* Pending Status */}
                    {isPending && (
                      <div className="chat-message admin-note">
                        <div className="message-header">
                          <strong>⚠️ Status</strong>
                          <span className="badge admin-pending-badge">Awaiting Response</span>
                        </div>
                      </div>
                    )}

                    {/* Response Input Section */}
                    {isPending && isResponding && (
                      <div className="admin-response-input">
                        <textarea
                          value={responseText}
                          onChange={(e) => setResponseText(e.target.value)}
                          onKeyPress={handleResponseKeyPress}
                          placeholder="Type your response to the client..."
                          disabled={respondingLoading}
                          rows={3}
                          autoFocus
                        />
                        <div className="response-buttons">
                          <button
                            onClick={() => respondToMessage(msg.id || '')}
                            disabled={respondingLoading || !responseText.trim()}
                            className="send-button"
                          >
                            {respondingLoading ? '⏳ Sending...' : '📤 Send Response'}
                          </button>
                          <button
                            onClick={() => {
                              setRespondingToId(null);
                              setResponseText('');
                            }}
                            className="cancel-button"
                          >
                            ✕ Cancel
                          </button>
                        </div>
                      </div>
                    )}

                    {/* Respond Button */}
                    {isPending && !isResponding && (
                      <button
                        onClick={() => setRespondingToId(msg.id || '')}
                        className="respond-button"
                      >
                        💬 Respond to Client
                      </button>
                    )}

                    {/* Admin Response Already Sent */}
                    {isResponded && (
                      <div className="chat-message admin-response-message">
                        <div className="message-header">
                          <strong>✅ Your Response</strong>
                          <span className="badge admin-response-badge">Responded</span>
                          <span className="message-time">
                            {msg.responseTime ? new Date(msg.responseTime).toLocaleTimeString() : ''}
                          </span>
                        </div>
                        <div className="message-content">{msg.response}</div>
                      </div>
                    )}
                  </div>
                );
              })}
            </>
          )}
        </div>
      </div>
    );
  }

  // Client View - Shows their own messages and responses
  return (
    <div className="chat-wrapper">
      <div className="chat-header">
        <h2>💬 Customer Support Chat</h2>
        <p className="user-info">Logged in as: <strong>{username}</strong></p>
      </div>

      <div className="chat-container">
        {messages.length === 0 ? (
          <div className="chat-empty">
            <p>👋 Start a conversation with our support team</p>
            <p className="info-text">Try asking about energy consumption, billing, or account help</p>
          </div>
        ) : (
          messages.map((msg, idx) => (
            <div key={idx}>
              {/* User message */}
              <div className="chat-message user-message">
                <div className="message-header">
                  <strong>You</strong>
                  <span className="message-time">
                    {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString() : ''}
                  </span>
                </div>
                <div className="message-content">{msg.message}</div>
              </div>

              {/* Support response */}
              {msg.response && (
                <div className="chat-message support-message">
                  <div className="message-header">
                    <strong>🤖 Support System</strong>
                    {msg.responseType === 'RULE_BASED' && (
                      <span className="badge rule-badge">Rule-Based</span>
                    )}
                    {msg.responseType === 'FORWARDED_TO_ADMIN' && (
                      <span className="badge admin-badge">Forwarded</span>
                    )}
                    <span className="message-time">
                      {msg.responseTime ? new Date(msg.responseTime).toLocaleTimeString() : ''}
                    </span>
                  </div>
                  <div className="message-content">{msg.response}</div>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      <div className="chat-input-area">
        <textarea
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Type your message... (Press Enter to send)"
          disabled={loading}
          rows={3}
        />
        <button
          onClick={sendMessage}
          disabled={loading || !inputText.trim()}
          className="send-button"
        >
          {loading ? '⏳ Sending...' : '📤 Send'}
        </button>
      </div>
    </div>
  );
};

