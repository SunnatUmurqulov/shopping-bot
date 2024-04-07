package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductList(
        @JsonProperty("numenclaturies") List<ProductResponse> productResponseList,
        @JsonProperty("total_count") int totalCount
) {
}
