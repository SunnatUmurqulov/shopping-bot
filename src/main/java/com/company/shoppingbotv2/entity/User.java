package com.company.shoppingbotv2.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.entity.enums.Language;
import com.company.shoppingbotv2.entity.enums.UserStatus;

import java.util.List;

/**
 * The User entity class for work with users table
 */
@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    Long telegramId;

    @Column(nullable = false, length = 64)
    String firstName;

    @Column(length = 64)
    String lastName;

    @Column(length = 32, unique = true)
    String username;

    @Column(length = 32, unique = true)
    String phoneNumber;

    @Enumerated(EnumType.STRING)
    Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserStatus status = UserStatus.IN_ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BotState botState = BotState.START;

    String userTIN = null;

    boolean firstInit;

    @OneToOne(targetEntity = Contract.class)
    Contract currentContract;

    @OneToMany(targetEntity = Contract.class, mappedBy = "user", cascade = CascadeType.ALL)
    List<Contract> contracts;

    String fromDateRange;
    String toDateRange;
    String currentCategoryId;
    Integer currentPage;
    String searchKey;
    Integer currentOrderProductForCounter;
}
