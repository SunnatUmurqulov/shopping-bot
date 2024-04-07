package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateOrderRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("order_item_list") List<OrderItem> orderItemList
) {
}
