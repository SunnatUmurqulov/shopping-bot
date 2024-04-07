package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchResponse(
        @JsonProperty("numenclatury") String numenclaturyName,
        @JsonProperty("numenclatury_id") int productId
) {
}
