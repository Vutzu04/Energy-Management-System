import { useState, useEffect } from 'react';
import { userService } from '../../services/userService';
import { deviceService } from '../../services/deviceService';
import { associationService, AssociationResponse } from '../../services/associationService';
import { User, Device } from '../../types';
import './CRUD.css';

export default function Association() {
  const [users, setUsers] = useState<User[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [associations, setAssociations] = useState<AssociationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedDeviceId, setSelectedDeviceId] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [usersData, devicesData, associationsData] = await Promise.all([
        userService.getAllUsers(),
        deviceService.getAllDevices(),
        associationService.getAllAssociations(),
      ]);
      setUsers(usersData);
      setDevices(devicesData);
      setAssociations(associationsData);
      
      console.log('Loaded associations:', associationsData);
    } catch (err: any) {
      console.error('Error loading data:', err);
      setError(err.response?.data?.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleAssociate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!selectedUserId || !selectedDeviceId) {
      setError('Please select both user and device');
      return;
    }

    // Find the selected user's username
    const selectedUser = users.find(u => u.id === selectedUserId);
    const username = selectedUser?.username || '';

    console.log('Associating device:', { userId: selectedUserId, deviceId: selectedDeviceId, username });

    try {
      await associationService.associateDeviceToUser({
        userId: selectedUserId,
        deviceId: selectedDeviceId,
        username: username  // Include username for easier lookups
      } as any);
      
      setSuccess('Device successfully associated with user!');
      setSelectedUserId('');
      setSelectedDeviceId('');
      
      // Reload associations from backend
      const updatedAssociations = await associationService.getAllAssociations();
      setAssociations(updatedAssociations);
      console.log('Associations updated:', updatedAssociations);
    } catch (err: any) {
      console.error('Association error:', err.response?.data);
      setError(err.response?.data?.error || err.response?.data?.message || 'Failed to associate device');
    }
  };

  const handleRemoveAssociation = async (userId: string, deviceId: string) => {
    if (!window.confirm('Remove this association?')) return;
    
    try {
      await associationService.removeAssociation(userId, deviceId);
      setSuccess('Association removed successfully!');
      
      // Reload associations from backend
      const updatedAssociations = await associationService.getAllAssociations();
      setAssociations(updatedAssociations);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to remove association');
    }
  };

  return (
    <div className="crud-container">
      <div className="crud-header">
        <h2>Device-User Association</h2>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message" style={{marginBottom: '15px'}}>{success}</div>}

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <>
          <div style={{marginBottom: '30px', padding: '20px', backgroundColor: '#f5f5f5', borderRadius: '8px'}}>
            <h3>Create New Association</h3>
            <form onSubmit={handleAssociate} style={{display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap'}}>
              <select
                id="user-select"
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
                required
                style={{
                  flex: 1,
                  minWidth: '200px',
                  padding: '10px 12px',
                  borderRadius: '6px',
                  border: '2px solid #6366f1',
                  fontSize: '14px',
                  backgroundColor: '#fff',
                  cursor: 'pointer',
                  transition: 'border-color 0.2s',
                  color: selectedUserId ? '#000' : '#999',
                }}
                onFocus={(e) => e.target.style.borderColor = '#4f46e5'}
                onBlur={(e) => e.target.style.borderColor = '#6366f1'}
              >
                <option value="" style={{ color: '#999' }}>Select User</option>
                {users.map((user) => (
                  <option key={user.id} value={user.id} style={{ color: '#000' }}>
                    {user.username}
                  </option>
                ))}
              </select>

              <select
                id="device-select"
                value={selectedDeviceId}
                onChange={(e) => setSelectedDeviceId(e.target.value)}
                required
                style={{
                  flex: 1,
                  minWidth: '200px',
                  padding: '10px 12px',
                  borderRadius: '6px',
                  border: '2px solid #6366f1',
                  fontSize: '14px',
                  backgroundColor: '#fff',
                  cursor: 'pointer',
                  transition: 'border-color 0.2s',
                  color: selectedDeviceId ? '#000' : '#999',
                }}
                onFocus={(e) => e.target.style.borderColor = '#4f46e5'}
                onBlur={(e) => e.target.style.borderColor = '#6366f1'}
              >
                <option value="" style={{ color: '#999' }}>Select Device</option>
                {devices.map((device) => (
                  <option key={device.id} value={device.id} style={{ color: '#000' }}>
                    {device.name}
                  </option>
                ))}
              </select>

              <button type="submit" className="submit-button">
                Associate
              </button>
            </form>
          </div>

          <div>
            <h3>All Associations</h3>
            {associations.length === 0 ? (
              <div className="empty-message" style={{padding: '20px', textAlign: 'center', color: '#666'}}>
                No associations yet
              </div>
            ) : (
              <div className="table-container">
                <table className="crud-table">
                  <thead>
                    <tr>
                      <th>User</th>
                      <th>Device</th>
                      <th>Max Consumption</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {associations.map((assoc, idx) => {
                      const user = users.find(u => u.id === assoc.userId);
                      return (
                        <tr key={idx}>
                          <td>{user?.username || assoc.userId}</td>
                          <td>{assoc.deviceName}</td>
                          <td>{assoc.maximumConsumptionValue}</td>
                          <td>
                            <button 
                              onClick={() => handleRemoveAssociation(assoc.userId, assoc.deviceId)}
                              style={{
                                padding: '8px 16px',
                                borderRadius: '6px',
                                border: 'none',
                                backgroundColor: '#ef4444',
                                color: '#fff',
                                fontSize: '14px',
                                fontWeight: '500',
                                cursor: 'pointer',
                                transition: 'all 0.2s ease',
                              }}
                              onMouseEnter={(e) => {
                                e.currentTarget.style.backgroundColor = '#dc2626';
                                e.currentTarget.style.transform = 'translateY(-2px)';
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(239, 68, 68, 0.3)';
                              }}
                              onMouseLeave={(e) => {
                                e.currentTarget.style.backgroundColor = '#ef4444';
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = 'none';
                              }}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

