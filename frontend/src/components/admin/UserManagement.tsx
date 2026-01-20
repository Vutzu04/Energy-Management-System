import { useState, useEffect } from 'react';
import { userService } from '../../services/userService';
import { User } from '../../types';
import './CRUD.css';

export default function UserManagement() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({ username: '', password: '', role: 'Client' as const });

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingUser(null);
    setFormData({ username: '', password: '', role: 'Client' });
    setShowPassword(false);
    setShowModal(true);
  };

  const handleEdit = (user: User) => {
    setEditingUser(user);
    setFormData({ username: user.username, password: '', role: 'Client' });
    setShowPassword(false);
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      if (editingUser) {
        // For updates, send the new values
        // Password is only updated if provided (not empty)
        const updateData: any = {
          username: formData.username  // Always send the (possibly new) username
        };
        if (formData.password) {  // Only include password if user provided a new one
          updateData.password = formData.password;
        }
        console.log('Updating user:', editingUser.id, 'with data:', updateData);
        await userService.updateUser(editingUser.id, updateData);
        console.log('User updated successfully');
      } else {
        // For creating, send all required fields
        const createData = {
          username: formData.username,
          password: formData.password,
          role: formData.role || 'Client'
        };
        console.log('Creating user with data:', createData);
        await userService.createUser(createData);
        console.log('User created successfully');
      }
      setShowModal(false);
      loadUsers();
    } catch (err: any) {
      console.error('Error in handleSubmit:', err.response?.data);
      setError(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;
    setError('');
    try {
      await userService.deleteUser(id);
      loadUsers();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete user');
    }
  };

  return (
    <div className="crud-container">
      <div className="crud-header">
        <h2>User Management</h2>
        <button onClick={handleCreate} className="create-button">Create User</button>
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
                <th>Username</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={3} className="empty-message">No users found</td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>
                      <button onClick={() => handleEdit(user)} className="edit-button">
                        Edit
                      </button>
                      <button onClick={() => handleDelete(user.id)} className="delete-button">
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
        <div className="modal-overlay" onClick={() => {
          setShowModal(false);
          setShowPassword(false);
        }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editingUser ? 'Edit User' : 'Create User'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Password {editingUser && '(leave empty to keep current)'}</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    required={!editingUser}
                    style={{ paddingRight: '40px', width: '100%', boxSizing: 'border-box' }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                      position: 'absolute',
                      right: '10px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer',
                      fontSize: '18px',
                      padding: '0 5px'
                    }}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? '👁️' : '👁️‍🗨️'}
                  </button>
                </div>
              </div>
              {!editingUser && (
                <div className="form-group">
                  <label>Role</label>
                  <select
                    value={formData.role}
                    onChange={(e) => setFormData({ ...formData, role: e.target.value as any })}
                  >
                    <option value="Client">Client</option>
                    <option value="Administrator">Administrator</option>
                  </select>
                </div>
              )}
              <div className="modal-actions">
                <button type="submit" className="submit-button">
                  {editingUser ? 'Update' : 'Create'}
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

