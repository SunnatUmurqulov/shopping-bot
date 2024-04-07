package com.company.shoppingbotv2.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CategoryList(
       @JsonProperty("categories") List<CategoryResponse> categoryResponses
) {
}
