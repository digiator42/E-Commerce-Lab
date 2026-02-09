package com.ecommerce.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
