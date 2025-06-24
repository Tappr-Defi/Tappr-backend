# Tappr Backend API Documentation

## Authentication Endpoints

Base URL: `/api/v1/auth`

### 1. Register User
**POST** `/api/v1/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "12345",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "validationErrors": {
    "email": "email address cannot be empty",
    "password": "password cannot be empty"
  },
  "path": "/api/v1/auth"
}
```

### 2. Login User
**POST** `/api/v1/auth/login`

Authenticate a user and get access tokens.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "MERCHANT",
  "loggedIn": true,
  "userId": "12345"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid credentials",
  "path": "/api/v1/auth"
}
```

### 3. Logout User
**POST** `/api/v1/auth/logout`

Log out a user.

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "User logged out successfully",
  "isLoggedIn": false
}
```

### 4. Health Check
**GET** `/api/v1/auth/health`

Check if the authentication service is running.

**Response (200 OK):**
```
Auth service is running
```

## User Profile Endpoints

Base URL: `/api/v1/user`

### 1. Get User Profile
**GET** `/api/v1/user/profile`

Get the current user's profile information.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "id": "12345",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "role": "MERCHANT",
  "kycVerified": false,
  "createdAt": "2024-01-01T12:00:00"
}
```

### 2. Refresh Token
**POST** `/api/v1/user/refresh-token`

Refresh an expired access token.

**Headers:**
```
Authorization: Bearer <refresh_token>
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Token refreshed successfully"
}
```

### 3. Validate Token
**POST** `/api/v1/user/validate-token`

Validate if a token is still valid.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "valid": true,
  "email": "john.doe@example.com",
  "role": "MERCHANT"
}
```

## Error Handling

All endpoints return consistent error responses:

**Error Response Structure:**
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "validationErrors": {
    "field": "error message"
  },
  "path": "/api/v1/auth"
}
```

## HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data or business logic error
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected server error

## Authentication

Most endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

The JWT token contains the user's email and role information and expires after a configured time period.

## CORS

CORS is configured to allow all origins for development. In production, update the `WebConfig` class to specify your frontend domain.

## Rate Limiting

Currently not implemented. Consider adding rate limiting for production use.