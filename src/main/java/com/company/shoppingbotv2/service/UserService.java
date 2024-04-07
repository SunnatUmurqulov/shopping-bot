package com.company.shoppingbotv2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.entity.enums.UserStatus;
import com.company.shoppingbotv2.repository.UserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getOrCreate(org.telegram.telegrambots.meta.api.objects.User createUser) {
        User user = userRepository.findByTelegramId(createUser.getId())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .telegramId(createUser.getId())
                                .firstName(createUser.getFirstName())
                                .lastName(createUser.getLastName())
                                .username(createUser.getUserName())
                                .status(UserStatus.IN_ACTIVE)
                                .botState(BotState.START)
                                .build()
                ));
        user.setFirstName(createUser.getFirstName());
        user.setLastName(createUser.getLastName());
        user.setUsername(createUser.getUserName());
        return userRepository.save(user);
    }

    public void updateUser(User user) {
        if (Objects.isNull(user.getId())) {
            return;
        }
        userRepository.save(user);
    }
}
