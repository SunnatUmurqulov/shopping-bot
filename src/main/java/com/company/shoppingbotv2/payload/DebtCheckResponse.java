package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DebtCheckResponse(
        @JsonProperty("allsumm") Double totalSum,
        @JsonProperty("currency") String currency,
        @JsonProperty("contracts") List<ContractResponse> contractResponses
) {
}
