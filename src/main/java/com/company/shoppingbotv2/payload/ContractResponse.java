package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContractResponse(
        @JsonProperty("contract_id") String id,
        @JsonProperty("contract") String name,
        @JsonProperty("contractsumm") Double sum,
        @JsonProperty("contractcurrency") String currency,
        @JsonProperty("contractekvivalent") String ekvivalent
) {
}
