# Authentication Service Workflow

## 1. Registration Flow
1. User submits registration (email, password, fullname)
2. System validates:
   - Password matches retyped password
   - Email not already registered
3. System creates user:
   - Generates unique username from fullname
   - Encodes password
   - Sets account as disabled
4. Creates verification token
5. Sends verification email
6. Logs registration action

## 2. Account Verification Flow
1. User clicks verification link from email
2. System validates:
   - Signature is valid
   - Token exists and matches
3. System enables user account
4. Deletes verification token
5. Sends welcome email
6. Logs verification action

## 3. Login Flow
1. User submits login credentials
2. System authenticates using AuthenticationManager
3. Validates:
   - User exists
   - Account is verified (enabled)
   - Account is not locked
4. Generates:
   - Access token
   - Refresh token
5. Stores refresh token
6. Returns tokens to user

## 4. Password Reset Flow
1. User requests password reset
2. System:
   - Validates user exists
   - Generates reset token
   - Sends reset email
3. User submits new password with token
4. System:
   - Validates token signature
   - Checks token expiration
   - Updates password
   - Deletes used token
   - Logs action

## 5. Token Management
1. Refresh Token:
   - Used to get new access token
   - Validates expiration
   - Returns new access token
2. Logout:
   - Invalidates refresh token
   - Removes from database

## Security Features
- Password encryption
- Token expiration
- Email verification
- Signature validation
- Account locking
- Retry mechanism for username generation
- Transaction management
