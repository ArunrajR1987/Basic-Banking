# Secure Password Handling Implementation

This document explains the implementation of secure password handling in the Pita Bank application to prevent password exposure during transmission.

## Overview

To protect passwords from being exposed in transit, even if intercepted, this implementation uses RSA encryption:

1. The server generates an RSA key pair (public and private keys)
2. The client fetches the public key from the server
3. The client encrypts the password using the public key before sending it
4. The server decrypts the password using its private key
5. The server continues to hash the password with BCrypt before storing it in the database

## Server-Side Implementation

### 1. PasswordSecurityUtil

A utility class that handles encryption/decryption operations:

```java
@Component
public class PasswordSecurityUtil {
    
    private final KeyPair keyPair;
    
    public PasswordSecurityUtil() {
        // Generate a key pair for RSA encryption/decryption
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption keys", e);
        }
    }
    
    // Get the public key to send to clients
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    
    // Decrypt an encrypted password
    public String decryptPassword(String encryptedPasswordBase64) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPasswordBase64);
            
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt password", e);
        }
    }
    
    // Check if a password appears to be encrypted
    public boolean isEncrypted(String password) {
        return password != null && password.length() > 20 && isBase64(password);
    }
    
    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 2. SecurityController

Exposes an endpoint to provide the public key to clients:

```java
@RestController
@RequestMapping("/api/security")
@Tag(name = "Security", description = "Security-related API endpoints")
public class SecurityController {

    @Autowired
    private PasswordSecurityUtil passwordSecurityUtil;
    
    @Operation(summary = "Get public key", 
              description = "Returns the public key for password encryption on the client side")
    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", passwordSecurityUtil.getPublicKeyBase64());
        return response;
    }
}
```

### 3. Updated DTOs

Modified to protect passwords from being exposed in logs or serialization:

```java
public class RegisterRequest {
    // Other fields...
    
    @Schema(description = "User password (will be encrypted before transmission)", 
           example = "encrypted_password_string")
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    
    // Getters and setters...
    
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
```

### 4. Updated AuthController

Modified to handle encrypted passwords:

```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
    // Check if email is already in use
    if(customerRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
    }

    Customer newCustomer = new Customer();
    newCustomer.setFirstName(registerRequest.getFirstName());
    newCustomer.setLastName(registerRequest.getLastName());
    newCustomer.setEmail(registerRequest.getEmail());
    newCustomer.generateUsername();
    
    // Handle the password - decrypt it if it was encrypted on the client side
    String password = registerRequest.getPassword();
    if (passwordSecurityUtil.isEncrypted(password)) {
        try {
            password = passwordSecurityUtil.decryptPassword(password);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid password format");
        }
    }
    
    // Encode the password before storing
    newCustomer.setPassword(passwordEncoder.encode(password));
    // Rest of the method...
}
```

## Client-Side Implementation

### 1. Password Encryption Utility

```typescript
// src/utils/passwordEncryption.ts
export const fetchPublicKey = async (): Promise<string> => {
  const response = await fetch('/api/security/public-key');
  if (!response.ok) {
    throw new Error('Failed to fetch public key');
  }
  const data = await response.json();
  return data.publicKey;
};

export const encryptPassword = async (password: string, publicKeyBase64: string): Promise<string> => {
  // Import the public key
  const publicKey = await importPublicKey(publicKeyBase64);
  
  // Encrypt the password
  const encodedPassword = new TextEncoder().encode(password);
  const encryptedBuffer = await window.crypto.subtle.encrypt(
    { name: "RSA-OAEP" },
    publicKey,
    encodedPassword
  );
  
  // Convert to Base64
  return arrayBufferToBase64(encryptedBuffer);
};
```

### 2. React Hook for Password Encryption

```typescript
// src/hooks/usePasswordEncryption.ts
const usePasswordEncryption = () => {
  const [publicKey, setPublicKey] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);

  // Fetch the public key when the hook is first used
  useEffect(() => {
    const getPublicKey = async () => {
      try {
        setLoading(true);
        const key = await fetchPublicKey();
        setPublicKey(key);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Failed to fetch public key'));
      } finally {
        setLoading(false);
      }
    };

    getPublicKey();
  }, []);

  // Encrypt a password using the fetched public key
  const encryptPasswordWithKey = useCallback(
    async (password: string): Promise<string | null> => {
      if (!publicKey) {
        setError(new Error('Public key not available'));
        return null;
      }

      try {
        return await encryptPassword(password, publicKey);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Failed to encrypt password'));
        return null;
      }
    },
    [publicKey]
  );

  return {
    encryptPassword: encryptPasswordWithKey,
    loading,
    error,
    publicKeyAvailable: !!publicKey,
  };
};
```

### 3. Secure Password Field Component

```tsx
// src/components/SecureForm/SecurePasswordField.tsx
const SecurePasswordField: React.FC<SecurePasswordFieldProps> = ({
  value,
  onChange,
  onEncryptedValueChange,
  // Other props...
}) => {
  const [plainPassword, setPlainPassword] = useState(value);
  const { encryptPassword, loading, error, publicKeyAvailable } = usePasswordEncryption();

  // Handle password change
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setPlainPassword(newValue);
    onChange(newValue);
  };

  // Handle blur event - encrypt password if encryptOnBlur is true
  const handleBlur = async () => {
    if (encryptOnBlur && plainPassword && publicKeyAvailable) {
      const encrypted = await encryptPassword(plainPassword);
      if (encrypted && onEncryptedValueChange) {
        onEncryptedValueChange(encrypted);
      }
    }
  };

  return (
    <div className={`secure-password-field ${className}`}>
      {label && (
        <label htmlFor="password">
          {label} {required && <span className="required">*</span>}
        </label>
      )}
      <input
        type="password"
        id="password"
        value={plainPassword}
        onChange={handleChange}
        onBlur={handleBlur}
        placeholder={placeholder}
        required={required}
        autoComplete={autoComplete}
        disabled={disabled || loading}
      />
      {error && <div className="error-message">{error.message}</div>}
      {!publicKeyAvailable && !loading && (
        <div className="warning-message">
          Secure encryption not available. Your password may be sent in a less secure format.
        </div>
      )}
    </div>
  );
};
```

## Security Considerations

1. **Key Management**: In a production environment, consider using a more robust key management system rather than generating keys on application startup.

2. **HTTPS**: Always use HTTPS in production to provide an additional layer of security for all data in transit.

3. **Key Rotation**: Implement a key rotation strategy to periodically change the encryption keys.

4. **Fallback Mechanism**: The implementation includes a fallback mechanism that checks if a password is encrypted and handles it appropriately, ensuring backward compatibility.

5. **Password Storage**: Passwords are still hashed using BCrypt before storage in the database, following security best practices.

## Usage Example

### Client-Side

```tsx
// In a login form component
import { SecureLoginForm } from '../components/SecureForm';

const LoginPage = () => {
  const handleLogin = async (data) => {
    try {
      // data.password is already encrypted
      await loginUser(data);
      // Handle successful login
    } catch (err) {
      // Handle error
    }
  };

  return (
    <div>
      <h1>Login</h1>
      <SecureLoginForm onSubmit={handleLogin} />
    </div>
  );
};
```

### Server-Side

The server automatically handles the encrypted passwords in the AuthController, so no changes are needed to the API endpoints.