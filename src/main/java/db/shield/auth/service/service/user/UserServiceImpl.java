package db.shield.auth.service.service.user;

import db.shield.auth.service.dto.user.UserCreateRequest;
import db.shield.auth.service.dto.user.UserResponse;
import db.shield.auth.service.dto.user.UserUpdateRequest;
import db.shield.auth.service.mapper.UserMapper;
import db.shield.auth.service.model.UserEntity;
import db.shield.auth.service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public void createUser(UserCreateRequest userDto) {
        log.info("Creating user with username: {}", userDto.username());
        UserEntity user = userMapper.toEntity(userDto);
        user = userRepository.save(user);
        log.info("User with id {} successfully created at {}", user.getId(), user.getCreatedAt());
    }

    @Transactional
    @Override
    public UserResponse updateUser(UserUpdateRequest userDto) {
        log.info("Updating user {}", userDto.username());
        UserEntity userToUpdate = userRepository.findByUsername(userDto.username()).orElseThrow(
                () -> new EntityNotFoundException("User with username " + userDto.username() + "not found.")
        );
        userMapper.updateEntity(userDto, userToUpdate);
        UserEntity user = userRepository.save(userToUpdate);
        log.info("User {} successfully updated", user.getUsername());
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse findUserById(Long userId) {
        log.debug("Getting user with id: {}", userId);
        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + userId + "not found.")
        );
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse findUserByUsername(String username) {
        log.debug("Getting user with username: {}", username);
        UserEntity user = userRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException("User with username " + username + "not found.")
        );
        return userMapper.toResponse(user);
    }
}
