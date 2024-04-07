package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ContrAgentList(
        @JsonProperty("contragents") List<ContrAgent> contrAgents,
        @JsonProperty("total_county") int totalCount
) {
}
