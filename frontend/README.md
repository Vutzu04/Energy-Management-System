# Device Management Frontend

Frontend application built with React + TypeScript for the Device Management System.

## Features

- **Authentication**: Login with JWT token management
- **Admin Dashboard**: 
  - User CRUD operations
  - Device CRUD operations
  - Device-User Association
- **Client Dashboard**: View associated devices

## Prerequisites

- Node.js 18+ and npm/yarn

## Installation

```bash
cd frontend
npm install
```

## Configuration

The API base URL can be configured via environment variable:

Create a `.env` file:
```
VITE_API_BASE_URL=http://localhost:8080
```

If not set, it defaults to `http://localhost:8080`.

## Running the Application

```bash
npm run dev
```

The application will start on `http://localhost:3000`.

## API Endpoints Expected

The frontend expects the following API endpoints:

### Authentication
- `POST /api/auth/login` - Login endpoint

### User Management (Admin only)
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Device Management
- `GET /api/devices` - Get all devices (Admin)
- `GET /api/devices/my-devices` - Get user's devices (Client)
- `GET /api/devices/{id}` - Get device by ID
- `POST /api/devices` - Create device
- `PUT /api/devices/{id}` - Update device
- `DELETE /api/devices/{id}` - Delete device

### Association (Admin only)
- `POST /api/associations` - Associate device to user
- `DELETE /api/associations/{userId}/{deviceId}` - Remove association

## Authentication

All API requests (except login) include the JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

The token is stored in localStorage and automatically included in all requests via axios interceptors.

## Project Structure

```
src/
├── components/
│   ├── admin/
│   │   ├── UserManagement.tsx
│   │   ├── DeviceManagement.tsx
│   │   ├── Association.tsx
│   │   └── CRUD.css
│   ├── Login.tsx
│   ├── AdminDashboard.tsx
│   ├── ClientDashboard.tsx
│   └── ProtectedRoute.tsx
├── services/
│   ├── api.ts
│   ├── authService.ts
│   ├── userService.ts
│   ├── deviceService.ts
│   └── associationService.ts
├── types/
│   └── index.ts
├── App.tsx
└── main.tsx
```

