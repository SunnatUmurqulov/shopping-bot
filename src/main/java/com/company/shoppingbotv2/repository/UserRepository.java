package com.company.shoppingbotv2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.company.shoppingbotv2.entity.User;

import java.util.Optional;

/**
 * The UserRepository dao for work with User entity
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * @param telegramId the telegram id of the desired user
     * @return Optional User
     */
    Optional<User> findByTelegramId(long telegramId);

    /**
     * @param phoneNumber the telegram id of the desired user
     * @return Optional User
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}
