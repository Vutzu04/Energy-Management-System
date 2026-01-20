import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { deviceService } from '../services/deviceService';
import { Device } from '../types';
import EnergyChart from './energy/EnergyChart';
import { ChatInterface } from './chat/ChatInterface';
import './ClientDashboard.css';

export default function ClientDashboard() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedDeviceId, setSelectedDeviceId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'devices' | 'chat'>('devices');
  const navigate = useNavigate();

  useEffect(() => {
    loadMyDevices();
  }, []);

  const loadMyDevices = async () => {
    setLoading(true);
    setError('');
    try {
      console.log('Loading my devices...');
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      console.log('Token available:', !!token);
      console.log('Username:', username);
      
      const data = await deviceService.getMyDevices();
      console.log('Devices loaded:', data);
      setDevices(data);
    } catch (err: any) {
      console.error('Error loading devices:', err);
      console.error('Error response:', err.response?.data);
      setError(err.response?.data?.message || 'Failed to load devices');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div className="client-dashboard">
      <header className="client-header">
        <h1>Dashboard</h1>
        <div className="header-actions">
          <span className="username">Welcome, {authService.getUsername() || 'Client'}</span>
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </header>

      {/* Tab Navigation */}
      <div className="tab-navigation">
        <button
          className={`tab-button ${activeTab === 'devices' ? 'active' : ''}`}
          onClick={() => setActiveTab('devices')}
        >
          📱 My Devices
        </button>
        <button
          className={`tab-button ${activeTab === 'chat' ? 'active' : ''}`}
          onClick={() => setActiveTab('chat')}
        >
          💬 Support Chat
        </button>
      </div>

      <main className="client-main">
        {/* Devices Tab */}
        {activeTab === 'devices' && (
          <>
            {error && <div className="error-message">{error}</div>}

            {loading ? (
              <div className="loading">Loading your devices...</div>
            ) : (
              <>
                <h2>📱 Your Devices</h2>
                {devices.length === 0 ? (
                  <div className="empty-state">
                    <p>No devices are currently associated with your account.</p>
                  </div>
                ) : (
                  <>
                    <div className="devices-grid">
                      {devices.map((device) => (
                        <div
                          key={device.id}
                          className={`device-card ${selectedDeviceId === device.id ? 'selected' : ''}`}
                          onClick={() => setSelectedDeviceId(device.id)}
                        >
                          <div className="device-id">ID: {device.id.substring(0, 8)}...</div>
                          <h3 className="device-name">{device.name}</h3>
                          <div className="device-info">
                            <span className="info-label">Max Consumption:</span>
                            <span className="info-value">{device.maximumConsumptionValue} kWh</span>
                          </div>
                          {selectedDeviceId === device.id && (
                            <div className="selected-badge">✓ Selected</div>
                          )}
                        </div>
                      ))}
                    </div>

                    {selectedDeviceId && (
                      <>
                        <h2 style={{ marginTop: '40px' }}>⚡ Energy Consumption</h2>
                        <EnergyChart
                          deviceId={selectedDeviceId}
                          deviceName={devices.find(d => d.id === selectedDeviceId)?.name || ''}
                        />
                      </>
                    )}
                  </>
                )}
              </>
            )}
          </>
        )}

        {/* Chat Tab */}
        {activeTab === 'chat' && (
          <ChatInterface />
        )}
      </main>
    </div>
  );
}

