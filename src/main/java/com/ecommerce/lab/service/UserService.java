package com.ecommerce.lab.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.lab.repository.UserRepository;
import com.ecommerce.lab.dto.LoginRequestDTO;
import com.ecommerce.lab.dto.RegisterRequestDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.dto.UserUpdateDTO;
import com.ecommerce.lab.exception.AuthenticationException;
import com.ecommerce.lab.exception.UserAlreadyExistsException;
import com.ecommerce.lab.exception.UserNotFoundException;
import com.ecommerce.lab.model.User;
import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        if (existingUser != null) {
            existingUser.setEmail(user.getEmail());
            existingUser.setUserName(user.getUserName());
            existingUser.setName(user.getName());
            return userRepository.save(existingUser);
        }
        return null;
    }

    public UserResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new AuthenticationException("Invalid  email or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO registerUser(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        if (userRepository.existsByUserName(dto.username())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        User user = new User();
        user.setName(dto.name());
        user.setUserName(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        user = userRepository.save(user);

        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public void updateProfile(String email, UserUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.displayName() != null)
            user.setName(dto.displayName());
        if (dto.defaultAddress() != null)
            user.setAddress(dto.defaultAddress());

        if (dto.newPassword() != null && !dto.newPassword().isBlank()) {

            if (dto.currentPassword() == null || dto.currentPassword().isBlank()) {
                throw new RuntimeException("Current password is required to set a new password");
            }

            if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
                throw new RuntimeException("The current password you entered is incorrect");
            }

            if (dto.newPassword().length() < 8) {
                throw new RuntimeException("New password must be at least 8 characters long");
            }

            user.setPassword(passwordEncoder.encode(dto.newPassword()));
        }

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
