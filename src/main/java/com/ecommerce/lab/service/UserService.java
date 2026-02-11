package com.ecommerce.lab.service;

import org.springframework.stereotype.Service;
import com.ecommerce.lab.repository.UserRepository;
import com.ecommerce.lab.dto.RegisterRequestDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.exception.UserAlreadyExistsException;
import com.ecommerce.lab.exception.UserNotFoundException;
import com.ecommerce.lab.model.User;
import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;
    private UserResponseDTO userResponseDTO;

    public UserService(UserRepository userRepository, UserResponseDTO userResponseDTO) {
        this.userRepository = userRepository;
        this.userResponseDTO = userResponseDTO;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
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

    public UserResponseDTO registerUser(RegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(dto.password());

        userRepository.save(user);

        return userResponseDTO.fromEntity(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
