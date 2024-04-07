package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductResponse(
        @JsonProperty("numenclatury_id") String id,
        @JsonProperty("numenclatury") String name
) {
}
