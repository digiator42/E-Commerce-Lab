package com.ecommerce.lab.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.repository.base.UserRepository;

@Service
public class MigrationService {

    private final UserRepository userRepoPostgres;
    private final UserRepository userRepoMySQL;

    public MigrationService(
        @Qualifier("userRepositoryPostgres") UserRepository userRepoPostgres,
        @Qualifier("userRepositoryMySQL") UserRepository userRepoMySQL
    ) {
        this.userRepoPostgres = userRepoPostgres;
        this.userRepoMySQL = userRepoMySQL;
    }

    public void compareUsers(Long id) {
        var pgUser = userRepoPostgres.findById(id);
        var myUser = userRepoMySQL.findById(id);

        System.out.println("Postgres: " + pgUser);
        System.out.println("MySQL: " + myUser);
    }
}
