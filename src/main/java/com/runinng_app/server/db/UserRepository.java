package com.runinng_app.server.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByLogin(String login);

    void deleteUserByLogin(String login);
}
