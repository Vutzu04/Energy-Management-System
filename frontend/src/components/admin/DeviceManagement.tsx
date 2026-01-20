import { useState, useEffect } from 'react';
import { deviceService } from '../../services/deviceService';
import { Device } from '../../types';
import './CRUD.css';

export default function DeviceManagement() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingDevice, setEditingDevice] = useState<Device | null>(null);
  const [formData, setFormData] = useState({ name: '', maximumConsumptionValue: 0 });

  useEffect(() => {
    loadDevices();
  }, []);

  const loadDevices = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await deviceService.getAllDevices();
      setDevices(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load devices');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingDevice(null);
    setFormData({ name: '', maximumConsumptionValue: 0 });
    setShowModal(true);
  };

  const handleEdit = (device: Device) => {
    setEditingDevice(device);
    setFormData({
      name: device.name,
      maximumConsumptionValue: device.maximumConsumptionValue,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      if (editingDevice) {
        await deviceService.updateDevice(editingDevice.id, formData);
      } else {
        await deviceService.createDevice(formData);
      }
      setShowModal(false);
      loadDevices();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this device?')) return;
    setError('');
    try {
      await deviceService.deleteDevice(id);
      loadDevices();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete device');
    }
  };

  return (
    <div className="crud-container">
      <div className="crud-header">
        <h2>Device Management</h2>
        <button onClick={handleCreate} className="create-button">Create Device</button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <div className="table-container">
          <table className="crud-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Maximum Consumption Value</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {devices.length === 0 ? (
                <tr>
                  <td colSpan={4} className="empty-message">No devices found</td>
                </tr>
              ) : (
                devices.map((device) => (
                  <tr key={device.id}>
                    <td>{device.id}</td>
                    <td>{device.name}</td>
                    <td>{device.maximumConsumptionValue}</td>
                    <td>
                      <button onClick={() => handleEdit(device)} className="edit-button">
                        Edit
                      </button>
                      <button onClick={() => handleDelete(device.id)} className="delete-button">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editingDevice ? 'Edit Device' : 'Create Device'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Name</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Maximum Consumption Value</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.maximumConsumptionValue}
                  onChange={(e) =>
                    setFormData({ ...formData, maximumConsumptionValue: parseFloat(e.target.value) })
                  }
                  required
                  min="0"
                />
              </div>
              <div className="modal-actions">
                <button type="submit" className="submit-button">
                  {editingDevice ? 'Update' : 'Create'}
                </button>
                <button type="button" onClick={() => setShowModal(false)} className="cancel-button">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

