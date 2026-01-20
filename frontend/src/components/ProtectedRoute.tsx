import { Navigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { UserRole } from '../types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: UserRole;
}

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole) {
    const userRole = authService.getRole();
    if (userRole !== requiredRole) {
      // Redirect based on actual role
      if (userRole === 'Administrator') {
        return <Navigate to="/admin" replace />;
      } else {
        return <Navigate to="/client" replace />;
      }
    }
  }

  return <>{children}</>;
}

