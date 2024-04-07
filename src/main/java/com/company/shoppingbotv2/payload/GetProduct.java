package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetProduct(
        @JsonProperty("product_id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("price") Double price,
        @JsonProperty("remainder") Double remainder
) {
}
