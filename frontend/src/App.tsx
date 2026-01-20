import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { authService } from './services/authService';
import Login from './components/Login';
import AdminDashboard from './components/AdminDashboard';
import ClientDashboard from './components/ClientDashboard';
import ProtectedRoute from './components/ProtectedRoute';
import NotificationCenter from './components/NotificationCenter';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      {/* Global Notification Center for real-time alerts */}
      <NotificationCenter maxNotifications={5} autoRemoveDelay={8000} />
      
      <Routes>
        <Route
          path="/login"
          element={
            authService.isAuthenticated() ? (
              authService.getRole() === 'Administrator' ? (
                <Navigate to="/admin" replace />
              ) : (
                <Navigate to="/client" replace />
              )
            ) : (
              <Login />
            )
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="Administrator">
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/client"
          element={
            <ProtectedRoute requiredRole="Client">
              <ClientDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

