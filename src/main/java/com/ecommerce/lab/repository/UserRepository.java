package com.ecommerce.lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByUserName(String username);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);
    
    List<User> findAllByName(String userName);
    
    List<User> findAllByNameOrEmail(String name, String email);
    
    Optional<User> findByResetToken(String token);

}
