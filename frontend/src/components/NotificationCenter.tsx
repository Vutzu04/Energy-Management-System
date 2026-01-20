import React, { useEffect, useState } from 'react';
import { websocketService } from '../services/websocketService';
import './NotificationCenter.css';

export interface Notification {
  type: string;
  deviceId?: string;
  deviceName?: string;
  consumption?: number;
  threshold?: number;
  severity?: string;
  message?: string;
  timestamp?: string;
}

interface NotificationCenterProps {
  maxNotifications?: number;
  autoRemoveDelay?: number;
}

/**
 * NotificationCenter Component
 * Displays real-time notifications from WebSocket service
 * Handles overconsumption alerts and system notifications
 */
export const NotificationCenter: React.FC<NotificationCenterProps> = ({
  maxNotifications = 5,
  autoRemoveDelay = 8000,
}) => {
  const [notifications, setNotifications] = useState<(Notification & { id: string })[]>([]);
  const [connected, setConnected] = useState(false);
  const [subscriptionId, setSubscriptionId] = useState<string | null>(null);

  useEffect(() => {
    // Connect to WebSocket
    websocketService
      .connect(
        () => {
          console.log('✅ NotificationCenter: WebSocket connected');
          setConnected(true);

          // Subscribe to notifications
          const subId = websocketService.subscribeToNotifications((notification) => {
            addNotification(notification);
          });
          setSubscriptionId(subId);
        },
        (error) => {
          console.error('❌ NotificationCenter: Connection error:', error);
          setConnected(false);
        }
      )
      .catch((error) => {
        console.error('❌ NotificationCenter: Failed to connect:', error);
        setConnected(false);
      });

    return () => {
      if (subscriptionId) {
        websocketService.unsubscribe(subscriptionId);
      }
      websocketService.disconnect();
    };
  }, []);

  const addNotification = (notification: Notification) => {
    const id = `notif-${Date.now()}-${Math.random()}`;
    const newNotif = { ...notification, id };

    setNotifications((prev) => {
      // Keep only the latest notifications
      const updated = [newNotif, ...prev].slice(0, maxNotifications);
      return updated;
    });

    // Auto-remove after delay
    setTimeout(() => {
      removeNotification(id);
    }, autoRemoveDelay);
  };

  const removeNotification = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const getSeverityClass = (severity?: string): string => {
    switch (severity?.toUpperCase()) {
      case 'CRITICAL':
        return 'severity-critical';
      case 'HIGH':
        return 'severity-high';
      case 'MEDIUM':
        return 'severity-medium';
      default:
        return 'severity-low';
    }
  };

  return (
    <div className="notification-center">
      {/* Connection Status */}
      <div className={`connection-indicator ${connected ? 'connected' : 'disconnected'}`}>
        <span className="indicator-dot"></span>
        <span className="indicator-text">
          {connected ? '🟢 Connected' : '🔴 Disconnected'}
        </span>
      </div>

      {/* Notifications List */}
      <div className="notifications-list">
        {notifications.length === 0 ? (
          <div className="no-notifications">
            <p>No notifications</p>
          </div>
        ) : (
          notifications.map((notif) => (
            <div
              key={notif.id}
              className={`notification-item ${notif.type?.toLowerCase()} ${getSeverityClass(
                notif.severity
              )}`}
            >
              {/* Overconsumption Alert */}
              {notif.type === 'OVERCONSUMPTION' && (
                <>
                  <div className="notification-header">
                    <span className="notification-icon">⚠️</span>
                    <span className="notification-title">Overconsumption Alert</span>
                    <span className={`severity-badge ${getSeverityClass(notif.severity)}`}>
                      {notif.severity || 'MEDIUM'}
                    </span>
                  </div>
                  <div className="notification-content">
                    <p className="device-name">
                      <strong>{notif.deviceName}</strong>
                    </p>
                    <p className="consumption-info">
                      Consumption: <span className="value">{notif.consumption?.toFixed(2)} kWh</span>
                      {notif.threshold && (
                        <>
                          {' '}
                          / Threshold: <span className="threshold">{notif.threshold?.toFixed(2)} kWh</span>
                        </>
                      )}
                    </p>
                  </div>
                </>
              )}

              {/* System Notification */}
              {notif.type === 'SYSTEM' && (
                <>
                  <div className="notification-header">
                    <span className="notification-icon">ℹ️</span>
                    <span className="notification-title">System Notification</span>
                  </div>
                  <div className="notification-content">
                    <p>{notif.message}</p>
                  </div>
                </>
              )}

              {/* Alert Notification */}
              {notif.type === 'ALERT' && (
                <>
                  <div className="notification-header">
                    <span className="notification-icon">🔔</span>
                    <span className="notification-title">Alert</span>
                    <span className={`severity-badge ${getSeverityClass(notif.severity)}`}>
                      {notif.severity || 'MEDIUM'}
                    </span>
                  </div>
                  <div className="notification-content">
                    <p>{notif.message}</p>
                  </div>
                </>
              )}

              {/* Default Notification */}
              {!['OVERCONSUMPTION', 'SYSTEM', 'ALERT'].includes(notif.type || '') && (
                <>
                  <div className="notification-header">
                    <span className="notification-icon">📬</span>
                    <span className="notification-title">{notif.type}</span>
                  </div>
                  <div className="notification-content">
                    <p>{notif.message}</p>
                  </div>
                </>
              )}

              {/* Timestamp */}
              {notif.timestamp && (
                <div className="notification-footer">
                  <small>{new Date(notif.timestamp).toLocaleTimeString()}</small>
                </div>
              )}

              {/* Close Button */}
              <button
                className="close-btn"
                onClick={() => removeNotification(notif.id)}
                aria-label="Close notification"
              >
                ×
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default NotificationCenter;
