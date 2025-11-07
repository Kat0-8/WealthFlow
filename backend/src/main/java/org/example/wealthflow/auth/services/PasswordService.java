package org.example.wealthflow.auth.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Component
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private static final int ENCRYPTION_STRENGTH = 12;
    private static final int SALT_LENGTH = 16;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder(ENCRYPTION_STRENGTH);
        this.secureRandom = new SecureRandom();
    }

    public String generateSalt() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        secureRandom.nextBytes(saltBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
    }

    public String hashPassword(String password, String salt) {
        Objects.requireNonNull(password, "Password must not be null");
        Objects.requireNonNull(salt, "Salt must not be null");
        return passwordEncoder.encode(password+salt);
    }

    public boolean verifyPassword(String password, String salt, String storedHash) {
        if(password == null || storedHash == null || salt == null) {
            return false;
        } else {
            return passwordEncoder.matches(password+salt, storedHash);
        }
    }
}
