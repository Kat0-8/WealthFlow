package org.example.wealthflow.services;

import lombok.extern.slf4j.Slf4j;
import org.example.wealthflow.dtos.auth.AuthRegisterResponseDto;
import org.example.wealthflow.dtos.auth.AuthResponseDto;
import org.example.wealthflow.dtos.auth.LoginRequestDto;
import org.example.wealthflow.dtos.user.UserRequestDto;
import org.example.wealthflow.dtos.user.UserResponseDto;
import org.example.wealthflow.events.UserRegisteredEvent;
import org.example.wealthflow.exceptions.AlreadyExistsException;
import org.example.wealthflow.exceptions.BadRequestException;
import org.example.wealthflow.exceptions.NotFoundException;
import org.example.wealthflow.exceptions.UnauthorizedException;
import org.example.wealthflow.models.User;
import org.example.wealthflow.repositories.UserRepository;
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

        // normalize
        dto.setLogin(dto.getLogin().trim());
        dto.setEmail(dto.getEmail().trim().toLowerCase());
        dto.setRole(User.Role.USER);

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

        String token = jwtTokenService.generateToken(created.getId(), created.getRole().name());
        long expiresIn = jwtTokenService.getExpirationSeconds();

        AuthResponseDto auth = AuthResponseDto.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .expiresIn(expiresIn)
                .build();

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

        String token = jwtTokenService.generateToken(user.getId(), user.getRole().name());
        long expiresIn = jwtTokenService.getExpirationSeconds();

        log.info("User logged in: id={}, login={}", user.getId(), user.getLogin());
        return AuthResponseDto.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .expiresIn(expiresIn)
                .build();
    }

    private void validateRegistrationDto(UserRequestDto dto) {
        if (dto == null) throw new BadRequestException("Payload required");
        if (dto.getLogin() == null || dto.getLogin().isBlank()) throw new BadRequestException("Login is required");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new BadRequestException("Email is required");
        if (dto.getPassword() == null || dto.getPassword().length() < MIN_PASSWORD_LENGTH)
            throw new BadRequestException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
    }
}
