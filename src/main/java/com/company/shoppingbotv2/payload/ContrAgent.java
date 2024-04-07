package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContrAgent(
        @JsonProperty("name") String name,
        @JsonProperty("инн") String userTIN
) {
}
