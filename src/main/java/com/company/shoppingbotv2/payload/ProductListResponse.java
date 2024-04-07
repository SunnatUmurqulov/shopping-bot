package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductListResponse(
        @JsonProperty("products")
        List<GetProduct> products,
        @JsonProperty("total_county")
        int totalCount
) {
}
