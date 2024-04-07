package com.company.shoppingbotv2.handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.callbackquery.*;
import com.company.shoppingbotv2.handler.message.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HandlerFactory {
    StartCommandHandler startCommandHandler;
    ChooseLanguageHandler chooseLanguageHandler;
    SetContactHandler setContactHandler;
    MainMenuHandler mainMenuHandler;
    ViewContractHandler viewContractHandler;
    GetActDocHandler getActDocHandler;
    EnterFromDateHandler enterFromDateHandler;
    EnterToDateHandler enterToDateHandler;
    ViewContractsHandler viewContractsHandler;
    CategoryAndProductKeyboardHandler categoryAndProductKeyboardHandler;
    GetActDocCallbackQueryHandler getActDocCallbackQueryHandler;
    ShowProductListHandler showProductListHandler;
    UserCartHandler userCartHandler;
    CategoryListHandler categoryListHandler;
    SearchProductHandler searchProductHandler;
    ProductViewHandler productViewHandler;
    ProductCounterHandler productCounterHandler;
    ProductCountByUserCartMessageHandler productCountByUserCartMessageHandler;
    ProductCountMessageHandler productCountMessageHandler;
    ProductCounterByUserCartHandler productCounterByUserCartHandler;
    MyOrdersHandler myOrdersHandler;
    SearchContrAgentName searchContrAgentName;
    SearchContrAgentTIN searchContrAgentTIN;
    ShowContrAgentListHandler showContrAgentListHandler;
    ViewContrAgentList viewContrAgentList;

    public HandlerFactory(@Lazy StartCommandHandler startCommandHandler,
                          @Lazy ChooseLanguageHandler chooseLanguageHandler,
                          @Lazy SetContactHandler setContactHandler,
                          @Lazy MainMenuHandler mainMenuHandler,
                          @Lazy ViewContractHandler viewContractHandler,
                          @Lazy GetActDocHandler getActDocHandler,
                          @Lazy EnterFromDateHandler enterFromDateHandler,
                          @Lazy EnterToDateHandler enterToDateHandler,
                          @Lazy ViewContractsHandler viewContractsHandler,
                          @Lazy CategoryAndProductKeyboardHandler categoryAndProductKeyboardHandler,
                          @Lazy ShowProductListHandler showProductListHandler,
                          @Lazy GetActDocCallbackQueryHandler getActDocCallbackQueryHandler,
                          @Lazy UserCartHandler userCartHandler,
                          @Lazy CategoryListHandler categoryListHandler,
                          @Lazy SearchProductHandler searchProductHandler,
                          @Lazy ProductViewHandler productViewHandler,
                          @Lazy ProductCounterHandler productCounterHandler,
                          @Lazy ProductCountByUserCartMessageHandler productCountByUserCartMessageHandler,
                          @Lazy ProductCounterByUserCartHandler productCounterByUserCartHandler,
                          @Lazy ProductCountMessageHandler productCountMessageHandler,
                          @Lazy MyOrdersHandler myOrdersHandler,
                          @Lazy SearchContrAgentName searchContrAgentName,
                          @Lazy SearchContrAgentTIN searchContrAgentTIN,
                          @Lazy ShowContrAgentListHandler showContrAgentListHandler,
                          @Lazy ViewContrAgentList viewContrAgentList) {
        this.startCommandHandler = startCommandHandler;
        this.chooseLanguageHandler = chooseLanguageHandler;
        this.setContactHandler = setContactHandler;
        this.mainMenuHandler = mainMenuHandler;
        this.viewContractHandler = viewContractHandler;
        this.getActDocHandler = getActDocHandler;
        this.enterFromDateHandler = enterFromDateHandler;
        this.enterToDateHandler = enterToDateHandler;
        this.viewContractsHandler = viewContractsHandler;
        this.categoryAndProductKeyboardHandler = categoryAndProductKeyboardHandler;
        this.showProductListHandler = showProductListHandler;
        this.getActDocCallbackQueryHandler = getActDocCallbackQueryHandler;
        this.userCartHandler = userCartHandler;
        this.categoryListHandler = categoryListHandler;
        this.searchProductHandler = searchProductHandler;
        this.productViewHandler = productViewHandler;
        this.productCounterHandler = productCounterHandler;
        this.productCounterByUserCartHandler = productCounterByUserCartHandler;
        this.myOrdersHandler = myOrdersHandler;
        this.productCountByUserCartMessageHandler = productCountByUserCartMessageHandler;
        this.productCountMessageHandler = productCountMessageHandler;
        this.searchContrAgentName = searchContrAgentName;
        this.searchContrAgentTIN = searchContrAgentTIN;
        this.showContrAgentListHandler = showContrAgentListHandler;
        this.viewContrAgentList = viewContrAgentList;
    }

    public MessageHandler createMessageHandler(BotState botState) {
        return switch (botState) {
            case START -> startCommandHandler;
            case CHOOSE_LANGUAGE -> chooseLanguageHandler;
            case SEND_PHONE_NUMBER -> setContactHandler;
            case MAIN_MENU -> mainMenuHandler;
            case VIEW_CONTRACT -> viewContractHandler;
            case GET_ACT_DOC -> getActDocHandler;
            case ENTER_FROM_DATE -> enterFromDateHandler;
            case ENTER_TO_DATE -> enterToDateHandler;
            case CATEGORY_LIST, SHOW_PRODUCT_LIST, PRODUCT_VIEW -> categoryAndProductKeyboardHandler;
            case SEARCH_PRODUCT -> searchProductHandler;
            case PRODUCT_COUNTER -> productCountMessageHandler;
            case PRODUCT_COUNTER_USER_CART -> productCountByUserCartMessageHandler;
            case ENTER_CONTR_AGENT_NAME_FOR_SEARCH -> searchContrAgentName;
            case ENTER_TIN -> searchContrAgentTIN;
            case CONTR_AGENT_LIST -> viewContrAgentList;
            default -> throw new IllegalArgumentException("Unsupported bot state: " + botState);
        };
    }

    public CallbackQueryHandler createCallbackQueryHandler(BotState botState) {
        return switch (botState) {
            case VIEW_CONTRACTS -> viewContractsHandler;
            case GET_ACT_DOC -> getActDocCallbackQueryHandler;
            case CATEGORY_LIST -> categoryListHandler;
            case SHOW_PRODUCT_LIST -> showProductListHandler;
            case USER_CART -> userCartHandler;
            case PRODUCT_VIEW -> productViewHandler;
            case PRODUCT_COUNTER -> productCounterHandler;
            case PRODUCT_COUNTER_USER_CART -> productCounterByUserCartHandler;
            case SHOW_ORDERS -> myOrdersHandler;
            case CONTR_AGENT_LIST -> showContrAgentListHandler;
            default -> throw new IllegalArgumentException("Unsupported bot state: " + botState);
        };
    }
}
