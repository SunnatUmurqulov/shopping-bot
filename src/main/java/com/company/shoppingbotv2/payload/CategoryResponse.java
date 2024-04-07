package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;


public record CategoryResponse(
        @JsonProperty("category_id") String id,
        @JsonProperty("category") String name
){}