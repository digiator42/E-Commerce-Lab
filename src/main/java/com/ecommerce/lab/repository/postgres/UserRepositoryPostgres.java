package com.ecommerce.lab.repository.postgres;

import org.springframework.stereotype.Repository;

import com.ecommerce.lab.repository.base.UserRepository;

import org.springframework.context.annotation.Primary;
@Primary
public interface UserRepositoryPostgres extends UserRepository {}