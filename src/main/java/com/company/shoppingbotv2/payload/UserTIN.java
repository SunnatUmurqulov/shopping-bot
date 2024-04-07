package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserTIN(
        @JsonProperty("name") String contrAgentName
) {
}
