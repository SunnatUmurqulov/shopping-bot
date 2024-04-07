package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderDocument(
        @JsonProperty("document_id")String documentId,
        @JsonProperty("document_date")String documentDate,
        @JsonProperty("document_status")String documentStatus
) {
}
