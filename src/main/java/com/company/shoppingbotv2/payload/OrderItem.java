package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderItem(
        @JsonProperty("itemID") String itemID,
        @JsonProperty("Quantity") String Quantity,
        @JsonProperty("Price") String Price
) {
}
