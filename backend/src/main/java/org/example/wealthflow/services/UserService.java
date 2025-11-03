package org.example.wealthflow.services;

import lombok.extern.slf4j.Slf4j;
import org.example.wealthflow.dtos.user.UserRequestDto;
import org.example.wealthflow.dtos.user.UserResponseDto;
import org.example.wealthflow.dtos.user.UserUpdateDto;
import org.example.wealthflow.exceptions.AlreadyExistsException;
import org.example.wealthflow.exceptions.BadRequestException;
import org.example.wealthflow.exceptions.NotFoundException;
import org.example.wealthflow.mappers.UserMapper;
import org.example.wealthflow.models.User;
import org.example.wealthflow.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordService passwordService;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordService passwordService) {
        this.passwordService = passwordService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    /* CREATE */

    @Transactional
    public UserResponseDto create(UserRequestDto dto) {
        if (dto == null) throw new BadRequestException("Registration data is required");

        if (userRepository.existsByLogin(dto.getLogin())) {
            throw new AlreadyExistsException("Login already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        User user = userMapper.toEntity(dto);

        String salt = passwordService.generateSalt();
        String hash = passwordService.hashPassword(dto.getPassword(), salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);
        log.info("Registered {}: id={}, login={}", savedUser.getRole().toString(), savedUser.getId(), savedUser.getLogin());
        return userMapper.toResponse(savedUser);
    }

    /* READ */

    @Transactional(readOnly = true)
    public User loadById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getByIdDto(Long id) {
        User user = loadById(id);
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> listAll(boolean includeSoftDeleted) {
        List<User> users;
        if(includeSoftDeleted) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findAllActive();
        }
        return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    /* UPDATE */

    @Transactional
    public UserResponseDto updateProfile(Long userId, UserUpdateDto dto) {
        User user = loadById(userId);

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new AlreadyExistsException("Email already registered");
            }
        }

        userMapper.updateFromDto(dto, user);
        User savedUser = userRepository.save(user);
        log.info("Updated profile for user id={}", userId);
        return userMapper.toResponse(savedUser);
    }

    /* DELETE */

    @Transactional
    public boolean softDelete(Long id) {
        boolean softDeleted = userRepository.softDeleteById(id);
        if (softDeleted) log.info("Soft deleted user id={}", id);
        return softDeleted;
    }


    @Transactional
    public boolean hardDelete(Long id) {
        boolean hardDeleted = userRepository.deleteById(id);
        if (hardDeleted) log.info("Hard deleted user id={}", id);
        return hardDeleted;
    }

    @Transactional
    public boolean restore(Long id) {
        boolean restored = userRepository.restoreById(id);
        if (restored) log.info("Restored user id={}", id);
        return restored;
    }
}
