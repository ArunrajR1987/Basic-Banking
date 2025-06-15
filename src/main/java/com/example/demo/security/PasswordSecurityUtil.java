package com.example.demo.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for password encryption and decryption.
 * This class provides methods for client-side password encryption
 * and server-side password decryption.
 */
@Component
public class PasswordSecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordSecurityUtil.class);
    private static final String RSA_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    
    private final KeyPair keyPair;
    
    public PasswordSecurityUtil() {
        // Generate a key pair for RSA encryption/decryption
        // In a production environment, this should be properly managed with a key store
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            this.keyPair = keyPairGenerator.generateKeyPair();
            logger.info("RSA key pair generated successfully with {} bit length", KEY_SIZE);
        } catch (Exception e) {
            logger.error("Failed to initialize encryption keys", e);
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
     * @throws RuntimeException if decryption fails
     */
    public String decryptPassword(String encryptedPasswordBase64) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPasswordBase64);
            
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes);
        } catch (Exception e) {
            logger.error("Failed to decrypt password", e);
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