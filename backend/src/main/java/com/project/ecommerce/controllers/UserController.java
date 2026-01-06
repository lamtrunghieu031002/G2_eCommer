package com.project.ecommerce.controllers;

import com.project.ecommerce.components.SecurityUtils;
import com.project.ecommerce.dtos.user.ChangePasswordRequestDTO;
import com.project.ecommerce.dtos.user.UpdateUserDTO;
import com.project.ecommerce.dtos.user.UserDTO;
import com.project.ecommerce.dtos.user.UserLoginDTO;
import com.project.ecommerce.models.User;
import com.project.ecommerce.responses.*;
import com.project.ecommerce.services.user.IUserService;
import com.project.ecommerce.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final SecurityUtils securityUtils;

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> createUser(
            @Valid @RequestBody UserDTO userDTO
    ) {
        RegisterResponse registerResponse = new RegisterResponse();

        User user = userService.createUser(userDTO);
        registerResponse.setMessage(MessageKeys.REGISTER_SUCCESSFULLY);
        registerResponse.setUser(user);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.CREATED)
                .data(UserResponse.fromUser(user))
                .message("Account registration successful")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
    ) throws Exception {
        // Kiểm tra thông tin đăng nhập và sinh token
        String token = userService.login(
                userLoginDTO.getPhoneNumber(),
                userLoginDTO.getPassword()
        );
        LoginResponse loginResponse = LoginResponse.builder()
                .message(MessageKeys.LOGIN_SUCCESSFULLY)
                .token(token)
                .build();
        // Trả về token trong response
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(loginResponse)
                .build());
    }
    @PostMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/details/{userId}")
    public ResponseEntity<UserResponse> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailsFromToken(extractedToken);
            // Ensure that the user making the request matches the user being updated
            if (!Objects.equals(user.getId(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updatedUser = userService.updateUser(userId, updatedUserDTO);
            return ResponseEntity.ok(UserResponse.fromUser(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) throws Exception {

            // Tạo Pageable từ thông tin trang và giới hạn
            Pageable pageRequest = PageRequest.of(
                    page, limit,
                    //Sort.by("createdAt").descending()
                    Sort.by("id").ascending()
            );
            Page<UserResponse> userPage = userService.findAll(keyword, pageRequest);

            // Lấy tổng số trang
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();
            UserListResponse userListResponse = UserListResponse
                    .builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build();

            return ResponseEntity.ok().body(ResponseObject.builder()
                    .data(userListResponse)
                    .message("Get users successfully")
                    .status(HttpStatus.OK)
                    .build());

    }

    @PutMapping("/change-password/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> changePassword(
            @PathVariable("id") Long userId,
            @RequestBody ChangePasswordRequestDTO requestDTO) {

        try {
            userService.changePassword(userId, requestDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Change password successfully!")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build());
        }

    }
}
