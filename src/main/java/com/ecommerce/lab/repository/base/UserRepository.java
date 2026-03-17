package com.ecommerce.lab.repository.base;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.User;

import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"addressObjects"})
    boolean existsByEmail(String email);
    
    @EntityGraph(attributePaths = {"addressObjects"})
    boolean existsByUserName(String username);
    
    @EntityGraph(attributePaths = {"addressObjects"})
    Optional<User> findByUserName(String userName);

    @EntityGraph(attributePaths = {"addressObjects"})
    Optional<User> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"addressObjects"})
    List<User> findAllByName(String userName);

    @EntityGraph(attributePaths = {"addressObjects"})
    List<User> findAllByNameOrEmail(String name, String email);
    
    @EntityGraph(attributePaths = {"addressObjects"})
    Optional<User> findByResetToken(String token);

}
