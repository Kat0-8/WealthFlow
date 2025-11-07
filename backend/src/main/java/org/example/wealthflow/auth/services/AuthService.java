package org.example.wealthflow.auth.services;

import lombok.extern.slf4j.Slf4j;
import org.example.wealthflow.auth.dtos.AuthRegisterResponseDto;
import org.example.wealthflow.auth.dtos.AuthResponseDto;
import org.example.wealthflow.auth.dtos.LoginRequestDto;
import org.example.wealthflow.user.dtos.user.UserRequestDto;
import org.example.wealthflow.user.dtos.user.UserResponseDto;
import org.example.wealthflow.user.dtos.user.UserRoleDto;
import org.example.wealthflow.user.events.UserRegisteredEvent;
import org.example.wealthflow.common.exceptions.AlreadyExistsException;
import org.example.wealthflow.common.exceptions.BadRequestException;
import org.example.wealthflow.common.exceptions.NotFoundException;
import org.example.wealthflow.common.exceptions.UnauthorizedException;
import org.example.wealthflow.user.repositories.UserRepository;
import org.example.wealthflow.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String TOKEN_TYPE = "Bearer";

    @Autowired
    public AuthService(UserService userService,
                       UserRepository userRepository,
                       PasswordService passwordService,
                       JwtTokenService jwtTokenService,
                       ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.eventPublisher = eventPublisher;
    }
    @Transactional
    public UserResponseDto register(UserRequestDto dto) {
        validateRegistrationDto(dto);

        dto.setLogin(dto.getLogin().trim());
        dto.setEmail(dto.getEmail().trim().toLowerCase());
        dto.setRole(UserRoleDto.USER);

        try {
            UserResponseDto created = userService.create(dto);

            eventPublisher.publishEvent(new UserRegisteredEvent(created.getId(), created.getEmail()));

            log.info("User registered (public) id={}, login={}", created.getId(), created.getLogin());
            return created;
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyExistsException("Login or email already exists");
        }
    }

    @Transactional
    public AuthRegisterResponseDto registerAndLogin(UserRequestDto dto) {
        UserResponseDto created = register(dto);

        AuthResponseDto auth = createAuthResponse(created.getId(), created.getRole().toString());

        return AuthRegisterResponseDto.builder()
                .user(created)
                .token(auth)
                .build();
    }

    @Transactional
    public UserResponseDto registerAdmin(UserRequestDto dto) {
        Objects.requireNonNull(dto, "payload required");
        try {
            UserResponseDto created = userService.create(dto);
            log.info("Admin created user id={}, role={}", created.getId(), created.getRole());
            return created;
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyExistsException("Login or email already exists");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto dto) {
        if (dto == null) throw new BadRequestException("Login payload required");
        String login = dto.getLogin() == null ? "" : dto.getLogin().trim();
        String password = dto.getPassword();

        var userOpt = userRepository.findByLogin(login);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid credentials");
        }
        var user = userOpt.get();

        if (user.isDeleted()) {
            throw new NotFoundException("User not found");
        }

        boolean ok = passwordService.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!ok) {
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("User logged in: id={}, login={}", user.getId(), user.getLogin());
        return createAuthResponse(user.getId(),user.getRole().toString());
    }

    private void validateRegistrationDto(UserRequestDto dto) {
        if (dto == null) throw new BadRequestException("Payload required");
        if (dto.getLogin() == null || dto.getLogin().isBlank()) throw new BadRequestException("Login is required");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new BadRequestException("Email is required");
        if (dto.getPassword() == null || dto.getPassword().length() < MIN_PASSWORD_LENGTH)
            throw new BadRequestException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
    }

    private AuthResponseDto createAuthResponse(Long userId, String userRole) {
        String token = jwtTokenService.generateToken(userId, userRole);
        long expiresIn = jwtTokenService.getExpirationSeconds();
        return AuthResponseDto.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .expiresIn(expiresIn)
                .build();
    }
}
