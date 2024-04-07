package com.company.shoppingbotv2.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.company.shoppingbotv2.entity.enums.OrderProductStatus;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderProduct extends BaseEntity {
    @ManyToOne
    User user;
    @Column(nullable = false)
    String productId;
    @Column(nullable = false)
    String productName;
    @Column(nullable = false)
    Double productPrice;
    double remainder;
    double count;
    String contrAgentTIN;
    Double amount;
    @Enumerated(value = EnumType.STRING)
    OrderProductStatus status;
}
