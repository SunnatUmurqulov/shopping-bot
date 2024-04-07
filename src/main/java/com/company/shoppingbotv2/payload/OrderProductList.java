package com.company.shoppingbotv2.payload;

import java.util.List;

public record OrderProductList(
   List<OrderItem> orderItems
) {
}
