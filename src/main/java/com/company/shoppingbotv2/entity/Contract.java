package com.company.shoppingbotv2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * The Contract entity class for work with users table
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Contract extends BaseEntity {
    @ManyToOne(targetEntity = User.class)
    User user;
    @Column(nullable = false)
    String remoteId;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    Double sum;
    @Column(nullable = false)
    String currency;
    @Column(nullable = false)
    String ekvivalent;
    @Column(nullable = false)
    String ekvivalentCurrency;
}
