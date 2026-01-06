package com.project.ecommerce.services.user;

import com.project.ecommerce.components.JwtTokenUtils;
import com.project.ecommerce.dtos.user.ChangePasswordRequestDTO;
import com.project.ecommerce.dtos.user.UpdateUserDTO;
import com.project.ecommerce.dtos.user.UserDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;

import com.project.ecommerce.exceptions.PermissionDenyException;
import com.project.ecommerce.models.*;
import com.project.ecommerce.repositories.RoleRepository;
import com.project.ecommerce.repositories.UserRepository;
import com.project.ecommerce.responses.UserResponse;
import com.project.ecommerce.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) {
        //register user
        String phoneNumber = userDTO.getPhoneNumber();
        // Kiểm tra xem số điện thoại đã tồn tại hay chưa
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role =roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException(MessageKeys.ROLE_DOES_NOT_EXISTS));
        if(role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You cannot register an admin account");
        }
        //convert from userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword()) // Plain text password
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .active(true)
                .build();

        newUser.setRole(role);

        // Kiểm tra nếu có accountId, không yêu cầu password
//        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
//            String password = userDTO.getPassword();
//            String encodedPassword = passwordEncoder.encode(password);
//            newUser.setPassword(encodedPassword);
//        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(
            String phoneNumber,
            String password
    ) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()) {
            throw new DataNotFoundException(MessageKeys.WRONG_PHONE_PASSWORD);
        }
        //return optionalUser.get();
        User existingUser = optionalUser.get();
        //check password

        if(!optionalUser.get().isActive()) {
            throw new DataNotFoundException(MessageKeys.USER_IS_LOCKED);
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                existingUser.getAuthorities()
        );

        //authenticate with Java Spring security
        try{
            authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException exception){
            throw new BadCredentialsException(MessageKeys.WRONG_PHONE_PASSWORD);
        }
        return jwtTokenUtil.generateToken(existingUser);
    }
    @Transactional
    @Override
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) {
        // Find the existing user by userId
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Check if the phone number is being changed and if it already exists for another user
        String newPhoneNumber = updatedUserDTO.getPhoneNumber();
        if (!existingUser.getPhoneNumber().equals(newPhoneNumber) &&
                userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        // Update user information based on the DTO
        if (updatedUserDTO.getFullName() != null) {
            existingUser.setFullName(updatedUserDTO.getFullName());
        }
        if (newPhoneNumber != null) {
            existingUser.setPhoneNumber(newPhoneNumber);
        }
        if (updatedUserDTO.getAddress() != null) {
            existingUser.setAddress(updatedUserDTO.getAddress());
        }
        if (updatedUserDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
        }

        // Update the password if it is provided in the DTO
//        if (updatedUserDTO.getPassword() != null
//                && !updatedUserDTO.getPassword().isEmpty()) {
//            if(!updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
//                throw new DataNotFoundException("Password and retype password not the same");
//            }
//            String newPassword = updatedUserDTO.getPassword();
//            String encodedPassword = passwordEncoder.encode(newPassword);
//            existingUser.setPassword(encodedPassword);
//        }

        // Save the updated user
        return userRepository.save(existingUser);
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)) {
            throw new Exception("Token is expired");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public Page<UserResponse> findAll(String keyword, Pageable pageable) {

        Page<User> pageUsers;
        pageUsers = userRepository.findAll(keyword, pageable);
        return pageUsers.map(UserResponse::fromUser);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO requestDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));

        if( !requestDTO.getCurrentPassword().equals(user.getPassword()) ){ // Plain text comparison
            throw new BadCredentialsException("Current password is incorrect");
        }

        if( !requestDTO.getNewPassword().equals(requestDTO.getConfirmPassword())){
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        user.setPassword(requestDTO.getNewPassword()); // Plain text password
        userRepository.save(user);

    }

}








