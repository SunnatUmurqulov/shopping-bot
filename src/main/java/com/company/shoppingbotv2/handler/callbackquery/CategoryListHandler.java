package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.payload.ProductList;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.utils.AppConstants;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class CategoryListHandler implements CallbackQueryHandler {
    ApiClient apiClient;
    HandlerHelper helper;

    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        String callbackQueryData = callbackQuery.getData();
        String[] part = callbackQueryData.split(":");

        if (!(part.length == 2 && part[0].equals("category"))) {
            return;
        }
        String categoryId = part[1];
        user.setCurrentCategoryId(categoryId);
        user.setCurrentPage(1);
        ProductList productList = apiClient.showProductList(user.getTelegramId(), categoryId, 1);
        int pageSize = productList.totalCount() % AppConstants.PAGE_SIZE == 0
                ? productList.totalCount() / AppConstants.PAGE_SIZE
                : productList.totalCount() / AppConstants.PAGE_SIZE + 1;
        helper.sendProductListToUser(user, productList.productResponseList(), 1, pageSize, categoryId);
        helper.deleteCallBackQueryMessage(callbackQuery, user);
    }
}
