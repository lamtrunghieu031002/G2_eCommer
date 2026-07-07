package com.project.userservice.services.user;

import com.project.userservice.dtos.user.ChangePasswordRequestDTO;
import com.project.userservice.dtos.user.UpdateUserDTO;
import com.project.userservice.dtos.user.UserDTO;
import com.project.userservice.models.User;
import com.project.userservice.responses.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    User createUser(UserDTO userDTO) ;
    String login(String phoneNumber, String password) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;
    User updateUser(Long userId, UpdateUserDTO updatedUserDTO);
    Page<UserResponse> findAll(String keyword, Pageable pageable);
    void changePassword(Long userId, ChangePasswordRequestDTO requestDTO);
}
