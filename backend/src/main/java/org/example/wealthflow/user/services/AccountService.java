package org.example.wealthflow.user.services;

import lombok.extern.slf4j.Slf4j;
import org.example.wealthflow.auth.services.PasswordService;
import org.example.wealthflow.user.dtos.account.AccountResponseDto;
import org.example.wealthflow.user.dtos.account.ChangeLoginRequestDto;
import org.example.wealthflow.user.dtos.account.ChangePasswordRequestDto;
import org.example.wealthflow.common.exceptions.AlreadyExistsException;
import org.example.wealthflow.common.exceptions.BadRequestException;
import org.example.wealthflow.common.exceptions.NotFoundException;
import org.example.wealthflow.user.models.User;
import org.example.wealthflow.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@Slf4j
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Autowired
    public AccountService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public AccountResponseDto changeLogin(Long userId, ChangeLoginRequestDto dto) {
        if (dto == null) throw new BadRequestException("Payload required");
        if (dto.getNewLogin() == null || dto.getNewLogin().isBlank()) throw new BadRequestException("New login required");
        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) throw new BadRequestException("Current password required");

        String newLogin = dto.getNewLogin().trim();

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        if (Objects.equals(user.getLogin(), newLogin)) {
            throw new BadRequestException("New login must be different from current login");
        }

        boolean ok = passwordService.verifyPassword(dto.getCurrentPassword(), user.getSalt(), user.getPasswordHash());
        if (!ok) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        try {
            boolean updated = userRepository.updateLoginIfAvailable(userId, newLogin);
            if (!updated) {
                throw new AlreadyExistsException("Login already exists");
            }
            log.info("User id={} changed login to {}", userId, newLogin);
            return AccountResponseDto.builder()
                    .message("Login changed successfully")
                    .timestamp(Instant.now())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            log.warn("DataIntegrityViolation while changing login for user {}: {}", userId, ex.getMessage());
            throw new AlreadyExistsException("Login already exists");
        }
    }

    @Transactional
    public AccountResponseDto changePassword(Long userId, ChangePasswordRequestDto dto) {
        if (dto == null) throw new BadRequestException("Payload required");
        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) throw new BadRequestException("Current password required");
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 8)
            throw new BadRequestException("New password must be at least " + 8 + " characters");
        if (dto.getNewPassword().equals(dto.getCurrentPassword()))
            throw new BadRequestException("New password must be different from current password");

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        boolean ok = passwordService.verifyPassword(dto.getCurrentPassword(), user.getSalt(), user.getPasswordHash());
        if (!ok) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        String newSalt = passwordService.generateSalt();
        String newHash = passwordService.hashPassword(dto.getNewPassword(), newSalt);
        user.setSalt(newSalt);
        user.setPasswordHash(newHash);

        userRepository.save(user);
        log.info("User id={} changed password", userId);

        return AccountResponseDto.builder()
                .message("Password changed successfully")
                .timestamp(Instant.now())
                .build();
    }
}
