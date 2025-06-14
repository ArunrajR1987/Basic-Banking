package com.example.demo.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;

import org.springframework.stereotype.Component;

/**
 * Utility class for password encryption and decryption.
 * This class provides methods for client-side password encryption
 * and server-side password decryption.
 */
@Component
public class PasswordSecurityUtil {
    
    private final KeyPair keyPair;
    
    public PasswordSecurityUtil() {
        // Generate a key pair for RSA encryption/decryption
        // In a production environment, this should be properly managed with a key store
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption keys", e);
        }
    }
    
    /**
     * Gets the public key as a Base64 encoded string.
     * This key should be sent to the client for password encryption.
     * 
     * @return Base64 encoded public key
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    
    /**
     * Decrypts an encrypted password using the private key.
     * 
     * @param encryptedPasswordBase64 The Base64 encoded encrypted password
     * @return The decrypted password as a string
     */
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
    
    /**
     * Checks if a password appears to be encrypted.
     * This is a simple heuristic check based on the format of the encrypted string.
     * 
     * @param password The password to check
     * @return true if the password appears to be encrypted
     */
    public boolean isEncrypted(String password) {
        // Simple heuristic: encrypted passwords are Base64 encoded and typically longer
        return password != null && password.length() > 20 && isBase64(password);
    }
    
    /**
     * Checks if a string is Base64 encoded.
     * 
     * @param str The string to check
     * @return true if the string is Base64 encoded
     */
    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}