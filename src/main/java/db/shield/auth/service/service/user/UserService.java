package db.shield.auth.service.service.user;


import db.shield.auth.service.dto.user.UserCreateRequest;
import db.shield.auth.service.dto.user.UserResponse;
import db.shield.auth.service.dto.user.UserUpdateRequest;


public interface UserService {

    void createUser(UserCreateRequest userDto);

    UserResponse updateUser(UserUpdateRequest userDto);

    UserResponse findUserById(Long userId);

    UserResponse findUserByUsername(String username);
}
