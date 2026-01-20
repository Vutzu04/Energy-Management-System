import { useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { useNavigate } from 'react-router-dom';
import UserManagement from './admin/UserManagement';
import DeviceManagement from './admin/DeviceManagement';
import Association from './admin/Association';
import EnergyChart from './energy/EnergyChart';
import { ChatInterface } from './chat/ChatInterface';
import './AdminDashboard.css';

type ActiveSection = 'users' | 'devices' | 'association' | 'charts' | 'chat';

export default function AdminDashboard() {
  const [activeSection, setActiveSection] = useState<ActiveSection>('users');
  const navigate = useNavigate();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div className="admin-dashboard">
      <header className="admin-header">
        <h1>Admin Dashboard</h1>
        <div className="header-actions">
          <span className="username">Welcome, {authService.getUsername() || 'Admin'}</span>
          <button onClick={handleLogout} className="logout-button">Logout</button>
        </div>
      </header>

      <div className="admin-content">
        <nav className="admin-nav">
          <button
            className={`nav-button ${activeSection === 'users' ? 'active' : ''}`}
            onClick={() => setActiveSection('users')}
          >
            User CRUD
          </button>
          <button
            className={`nav-button ${activeSection === 'devices' ? 'active' : ''}`}
            onClick={() => setActiveSection('devices')}
          >
            Device CRUD
          </button>
          <button
            className={`nav-button ${activeSection === 'association' ? 'active' : ''}`}
            onClick={() => setActiveSection('association')}
          >
            Asociere
          </button>
          <button
            className={`nav-button ${activeSection === 'charts' ? 'active' : ''}`}
            onClick={() => setActiveSection('charts')}
          >
            📊 Energy Charts
          </button>
          <button
            className={`nav-button ${activeSection === 'chat' ? 'active' : ''}`}
            onClick={() => setActiveSection('chat')}
          >
            💬 Support Chat
          </button>
        </nav>

        <main className="admin-main">
          {activeSection === 'users' && <UserManagement />}
          {activeSection === 'devices' && <DeviceManagement />}
          {activeSection === 'association' && <Association />}
          {activeSection === 'charts' && <AdminEnergyCharts />}
          {activeSection === 'chat' && <ChatInterface />}
        </main>
      </div>
    </div>
  );
}

function AdminEnergyCharts() {
  const [selectedDeviceId, setSelectedDeviceId] = useState<string | null>(null);
  const [selectedDeviceName, setSelectedDeviceName] = useState<string | null>(null);
  const [devices, setDevices] = useState<Array<{ id: string; name: string }>>([]);
  const [loadingDevices, setLoadingDevices] = useState(true);

  // Load devices on mount
  useEffect(() => {
    const loadDevices = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await fetch('http://localhost:8080/api/devices', {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
          const data = await response.json();
          setDevices(data);
          // Auto-select first device
          if (data.length > 0) {
            setSelectedDeviceId(data[0].id);
            setSelectedDeviceName(data[0].name);
          }
        }
      } catch (err) {
        console.error('Error loading devices:', err);
      } finally {
        setLoadingDevices(false);
      }
    };

    loadDevices();
  }, []);

  return (
    <div className="energy-charts-section">
      <div className="charts-header">
        <h2>📊 Device Energy Consumption Analysis</h2>
        <p>View real-time energy consumption data for your devices</p>
      </div>

      {loadingDevices ? (
        <div className="loading-devices">Loading devices...</div>
      ) : devices.length === 0 ? (
        <div className="no-devices">No devices available. Create a device first!</div>
      ) : (
        <>
          <div className="device-selector">
            <label htmlFor="device-select">Select Device:</label>
            <select
              id="device-select"
              value={selectedDeviceId || ''}
              onChange={(e) => {
                const deviceId = e.target.value;
                const device = devices.find(d => d.id === deviceId);
                setSelectedDeviceId(deviceId);
                setSelectedDeviceName(device?.name || '');
              }}
              className="device-select-dropdown"
            >
              <option value="">-- Select a device --</option>
              {devices.map((device) => (
                <option key={device.id} value={device.id}>
                  {device.name}
                </option>
              ))}
            </select>
          </div>

          {selectedDeviceId && selectedDeviceName && (
            <EnergyChart
              key={selectedDeviceId}
              deviceId={selectedDeviceId}
              deviceName={selectedDeviceName}
            />
          )}
        </>
      )}
    </div>
  );
}

