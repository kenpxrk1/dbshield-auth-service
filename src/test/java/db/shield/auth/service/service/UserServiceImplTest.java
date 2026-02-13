package db.shield.auth.service.service;

import db.shield.auth.service.Initializer;
import db.shield.auth.service.dto.user.UserResponse;
import db.shield.auth.service.mapper.UserMapper;
import db.shield.auth.service.mapper.UserMapperImpl;
import db.shield.auth.service.model.UserEntity;
import db.shield.auth.service.repository.UserRepository;
import db.shield.auth.service.service.user.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest extends Initializer {

    @Spy
    private final UserMapper userMapper = new UserMapperImpl();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldSaveUser() {
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(userEntity);

        when(passwordEncoder.encode(any(CharSequence.class)))
                .thenReturn("hashedPassword");


        userService.createUser(userCreateRequest);

        verify(userRepository).save(any(UserEntity.class));
        verify(passwordEncoder).encode(userCreateRequest.password());

    }

    @Test
    void updateUser_shouldUpdateAndReturnResponse() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(userEntity));

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(userEntity);

        UserResponse response = userService.updateUser(userUpdateRequest);

        verify(userRepository).save(userEntity);
        assertThat(response.username()).isEqualTo(USERNAME);
    }

    @Test
    void updateUser_shouldThrowIfNotFound() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userUpdateRequest));
    }

    @Test
    void findUserById_shouldReturnUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(userEntity));

        UserResponse response = userService.findUserById(USER_ID);

        verify(userRepository).findById(USER_ID);
        assertThat(response.username()).isEqualTo(USERNAME);
    }

    @Test
    void findUserById_shouldThrowIfNotFound() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.findUserById(USER_ID));
    }

    @Test
    void findUserByUsername_shouldReturnUser() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(userEntity));

        UserResponse response = userService.findUserByUsername(USERNAME);

        verify(userRepository).findByUsername(USERNAME);
        assertThat(response.username()).isEqualTo(USERNAME);
    }

    @Test
    void findUserByUsername_shouldThrowIfNotFound() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.findUserByUsername(USERNAME));
    }
}

