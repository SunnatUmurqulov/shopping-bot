package com.company.shoppingbotv2.service;//package com.company.shoppingbotv2.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.MessageSource;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
//import org.telegram.telegrambots.meta.api.objects.Message;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
//import com.company.shoppingbotv2.config.DataLoader;
//import com.company.shoppingbotv2.config.TelegramBot;
//import com.company.shoppingbotv2.entity.Contract;
//import com.company.shoppingbotv2.entity.User;
//import com.company.shoppingbotv2.entity.enums.BotState;
//import com.company.shoppingbotv2.entity.enums.Language;
//import com.company.shoppingbotv2.entity.enums.UserStatus;
//import com.company.shoppingbotv2.handler.message.MessageHandler;
//import com.company.shoppingbotv2.payload.CategoryList;
//import com.company.shoppingbotv2.payload.DebtCheckResponse;
//import com.company.shoppingbotv2.utils.AppConstants;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
////@Service
//@RequiredArgsConstructor
//public class MessageHandlerImpl implements MessageHandler {
////    private final TelegramBot telegramBot;
////    private final UserService userService;
////    private final MessageSource messageSource;
////    private final ApiClient apiClient;
////    private final ContractService contractService;
////    private final CallbackQueryHandlerImpl callbackQueryHandlerImpl;
//
//    @Override
//    public void handle(Message message, User user) {
//        if (message.hasText() && (user.getBotState() == BotState.START || "/start".equals(message.getText()))) {
//            if ("/start".equals(message.getText())) actionStartCommand(user);
//        } else if (user.getBotState() == BotState.CHOOSE_LANGUAGE) {
//            actionChooseLanguage(message, user);
//        } else if (user.getBotState() == BotState.SEND_PHONE_NUMBER) {
//            actionSetContact(message, user);
//        } else if (user.getBotState() == BotState.MAIN_MENU) {
//            actionMainMenu(message, user);
//        } else if (user.getBotState() == BotState.VIEW_CONTRACT) {
//            actionViewContract(message, user);
//        } else if (user.getBotState() == BotState.GET_ACT_DOC) {
//            actionGetActDocMessage(message, user);
//        } else if (user.getBotState() == BotState.ENTER_FROM_DATE) {
//            actionEnterFromDate(message, user);
//        } else if (user.getBotState() == BotState.ENTER_TO_DATE) {
//            actionEnterToDate(message, user);
//        } else if (user.getBotState() == BotState.CATEGORY_LIST) {
//            actionCategoryList(message, user);
//        } else if (user.getBotState() == BotState.SEARCH) {
//            actionSearchCategory(message, user);
//        }
//    }
//
//    private void actionSearchCategory(Message message, User user) {
//        if (!message.hasText()){
//            actionSearchSetCategory(user);
//        }
//    }
//
//    private void actionCategoryList(Message message, User user) {
//        if (!message.hasText()) {
//            actionGetCategoryList(user);
//        }
//        String messageText = message.getText();
//        if (getButtonTexts("searchButton").contains(messageText)) {
//            actionSearchSetCategory(user);
//        } else if (getButtonTexts("backToMainMenu").contains(messageText)) {
//            actionResetMainMenu(user);
//        } else {
//            actionGetCategoryList(user);
//        }
//    }
//
//    private void actionSearchSetCategory(User user) {
//        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()),
//                messageSource.getMessage(
//                        "search",
//                        null,
//                        user.getLanguage().getLocale()));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.SEARCH);
//        userService.updateUser(user);
//
//    }
//
//    private void actionEnterToDate(Message message, User user) {
//        if (!message.hasText()) {
//            reenterToDate("incorrectFormat", user);
//        }
//        String fromDate = user.getFromDateRange();
//        String errorCode = validateDateRange(fromDate, message.getText());
//        if (!Objects.isNull(errorCode)) {
//            reenterToDate(errorCode, user);
//            return;
//        }
//        user.setToDateRange(message.getText());
//        sendGetActDocMessage(user);
//    }
//
//    private void actionEnterFromDate(Message message, User user) {
//        if (!message.hasText()) {
//            reenterFromDate("incorrectFormat", user);
//        }
//        String toDate = user.getToDateRange();
//        String fromDate = message.getText();
//        String errorCode = validateDateRange(fromDate, toDate);
//        if (!Objects.isNull(errorCode)) {
//            reenterFromDate(errorCode, user);
//            return;
//        }
//        user.setFromDateRange(fromDate);
//        sendGetActDocMessage(user);
//    }
//
//    private void reenterFromDate(String errorCode, User user) {
//        String messageText = messageSource.getMessage(errorCode, null, user.getLanguage().getLocale()) + "\n" +
//                messageSource.getMessage("enterFromDateRange", null, user.getLanguage().getLocale());
//        SendMessage sendMessage = new SendMessage(
//                String.valueOf(user.getTelegramId()),
//                messageText
//        );
//        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.ENTER_FROM_DATE);
//        userService.updateUser(user);
//    }
//
//    private void reenterToDate(String errorCode, User user) {
//        String messageText = messageSource.getMessage(errorCode, null, user.getLanguage().getLocale()) + "\n" +
//                messageSource.getMessage("enterToDateRange", null, user.getLanguage().getLocale());
//        SendMessage sendMessage = new SendMessage(
//                String.valueOf(user.getTelegramId()),
//                messageText
//        );
//        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.ENTER_TO_DATE);
//        userService.updateUser(user);
//    }
//
//    private void actionGetActDocMessage(Message message, User user) {
//        if (!message.hasText()) return;
//
//        String messageText = message.getText();
//        if (getButtonTexts("backToButton").contains(messageText)) {
//            actionCheckDebt(user, true);
//        } else if (getButtonTexts("backToMainMenu").contains(messageText)) {
//            actionResetMainMenu(user);
//        }
//    }
//
//    private void actionViewContract(Message message, User user) {
//        if (!message.hasText()) {
//            sendFailedMessage(user);
//            return;
//        }
//
//        String messageText = message.getText();
//        if (getButtonTexts("getContractDocButton").contains(messageText)) {
//            sendGetActDocMessage(user);
//        } else if (getButtonTexts("backToButton").contains(messageText)) {
//            actionCheckDebt(user, true);
//        } else if (getButtonTexts("backToMainMenu").contains(messageText)) {
//            actionResetMainMenu(user);
//        } else {
//            sendFailedMessage(user);
//        }
//    }
//
//    private void sendFailedMessage(User user) {
//        String messageText = messageSource.getMessage("plsPressKeyboard", null, user.getLanguage().getLocale());
//        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
//        sendMessage.setReplyMarkup(callbackQueryHandlerImpl.getContractViewKeyboard(user.getLanguage().getLocale()));
//        telegramBot.sendAnswerMessage(sendMessage);
//    }
//
//    private void sendGetActDocMessage(User user) {
//        Contract currentContract = user.getCurrentContract();
//        String currentContractName;
//        if (Objects.isNull(currentContract)) {
//            currentContractName = messageSource.getMessage("allContracts", null, user.getLanguage().getLocale());
//        } else {
//            currentContractName = currentContract.getName();
//        }
//        String fromDateRange = user.getFromDateRange();
//        if (Objects.isNull(fromDateRange)) {
//            fromDateRange = "-";
//        }
//        String toDateRange = user.getToDateRange();
//        if (Objects.isNull(toDateRange)) {
//            toDateRange = "-";
//        }
//        SendMessage mockMessage = new SendMessage(String.valueOf(user.getTelegramId()), ".");
//        ReplyKeyboardMarkup mockMessageKeyboard = new ReplyKeyboardMarkup(
//                List.of(new KeyboardRow(List.of(new KeyboardButton(
//                                messageSource.getMessage("backToButton", null, user.getLanguage().getLocale())
//                        ))),
//                        new KeyboardRow(List.of(new KeyboardButton(
//                                messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale())
//                        )))
//                ));
//        mockMessageKeyboard.setResizeKeyboard(true);
//        mockMessage.setReplyMarkup(mockMessageKeyboard);
//        SendMessage sendMessage = new SendMessage(
//                String.valueOf(user.getTelegramId()),
//                messageSource.getMessage(
//                        "getActDoc",
//                        new Object[]{currentContractName, fromDateRange, toDateRange},
//                        user.getLanguage().getLocale()
//                ));
//        sendMessage.setReplyMarkup(
//                new InlineKeyboardMarkup(List.of(
//                        List.of(InlineKeyboardButton.builder()
//                                .text(messageSource.getMessage("fromDateRangeButton", null, user.getLanguage().getLocale()))
//                                .callbackData("fromDateRange")
//                                .build()
//                        ),
//                        List.of(InlineKeyboardButton.builder()
//                                .text(messageSource.getMessage("toDateRangeButton", null, user.getLanguage().getLocale()))
//                                .callbackData("toDateRange")
//                                .build()
//                        ),
//                        List.of(InlineKeyboardButton.builder()
//                                .text(messageSource.getMessage("getActDocButton", null, user.getLanguage().getLocale()))
//                                .callbackData("getActDoc")
//                                .build()
//                        )
//                )));
//        telegramBot.sendAnswerMessage(mockMessage);
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.GET_ACT_DOC);
//        userService.updateUser(user);
//    }
//
//    private void actionMainMenu(Message message, User user) {
//        if (!message.hasText()) {
//            actionResetMainMenu(user);
//            return;
//        }
//        String messageText = message.getText();
//        if (getButtonTexts("checkDebtButton").contains(messageText)) {
//            actionCheckDebt(user, false);
//        } else if (getButtonTexts("contactUsButton").contains(messageText)) {
//            actionContactUs(user);
//        } else if (getButtonTexts("changeLanguageButton").contains(messageText)) {
//            actionSendChooseLanguage(user);
//        } else if (getButtonTexts("categoryListButton").contains(messageText)) {
//            actionGetCategoryList(user);
//        } else if (getButtonTexts("basketButton").contains(messageText)) {
//            actionGetBasket(user);
//        } else if (getButtonTexts("myOrdersButton").contains(messageText)) {
//            actionGetMyOrders(user);
//        } else {
//            actionResetMainMenu(user);
//        }
//    }
//
//    private void actionGetMyOrders(User user) {
//
//    }
//
//    private void actionGetBasket(User user) {
//
//    }
//
//    private void actionGetCategoryList(User user) {
//        user.setBotState(BotState.CATEGORY_LIST);
//        CategoryList categories = apiClient.getCategories(user.getTelegramId());
//
//        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
//        for (int i = 0; i < categories.categoryResponses().size(); i += 2) {
//            if (categories.categoryResponses().size() == i + 1) {
//                rows.add(List.of(
//                        InlineKeyboardButton.builder().text(categories.categoryResponses().get(i).id()).callbackData("category:" + categories.categoryResponses().get(i).id()).build()
//                ));
//            } else {
//                rows.add(List.of(
//                        InlineKeyboardButton.builder().text(categories.categoryResponses().get(i).name()).callbackData("category:" + categories.categoryResponses().get(i).id()).build(),
//                        InlineKeyboardButton.builder().text(categories.categoryResponses().get(i + 1).name()).callbackData("category:" + categories.categoryResponses().get(i + 1).id()).build()
//                ));
//            }
//        }
//
//        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(
//                new KeyboardButton(messageSource.getMessage("searchButton", null, user.getLanguage().getLocale())),
//                new KeyboardButton(messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale()))
//        ))));
//        replyKeyboard.setResizeKeyboard(true);
//        telegramBot.sendAnswerMessage(SendMessage.builder().chatId(user.getTelegramId()).text(".").replyMarkup(replyKeyboard).build());
//        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().clearKeyboard().keyboard(rows).build();
//        telegramBot.sendAnswerMessage(SendMessage.builder()
//                .chatId(user.getTelegramId())
//                .text(messageSource.getMessage("categoryList", null, user.getLanguage().getLocale()))
//                .replyMarkup(keyboard)
//                .build()
//        );
//        userService.updateUser(user);
//    }
//
////    private void actionShowProductList(User user, String category_id){
////        List<ProductResponse> productList = apiClient.showProductList(user.getTelegramId(), category_id);
////
////
////    }
//
//    private void actionContactUs(User user) {
//        SendMessage sendMessage = new SendMessage(String.valueOf(
//                user.getTelegramId()), messageSource.getMessage(
//                "contactUs",
//                new Object[]{DataLoader.phoneNumber},
//                user.getLanguage().getLocale()
//        ));
//        telegramBot.sendAnswerMessage(sendMessage);
//        actionResetMainMenu(user);
//    }
//
//    private void actionCheckDebt(User user, boolean fromLocalDb) {
//        user.setFromDateRange(null);
//        user.setToDateRange(null);
//        // Load contracts data from api and save contracts
//        Message waitingMessage = actionSetWaitingMessage(user);
//        List<Contract> contracts;
//        Double totalSum = 0D;
//        if (fromLocalDb) {
//            contracts = user.getContracts();
//            for (Contract contract : contracts) {
//                totalSum += contract.getSum();
//            }
//        } else {
//            DebtCheckResponse response = apiClient.getDebtContracts(user.getTelegramId());
//            contracts = contractService.saveUserContracts(user, response);
//            totalSum = response.totalSum();
//        }
//
//        SendMessage sendMessage;
//        BotState botState;
//        if (Objects.isNull(totalSum) || totalSum.equals(0D)) {
//            sendMessage = SendMessage.builder()
//                    .chatId(user.getTelegramId())
//                    .text(messageSource.getMessage("youHaveNotDebts", null, user.getLanguage().getLocale()))
//                    .replyMarkup(getMainMenuKeyboard(user.getLanguage().getLocale()))
//                    .build();
//            botState = BotState.MAIN_MENU;
//        } else if (contracts.size() == 1) {
//            Contract contract = contracts.get(0);
//            user.setCurrentContract(contract);
//            String messageText = String.format(
//                    "%s\n\n%s",
//                    messageSource.getMessage(
//                            "yourCurrentDebt",
//                            new Object[]{totalSum, contract.getEkvivalentCurrency()},
//                            user.getLanguage().getLocale()
//                    ),
//                    messageSource.getMessage(
//                            "contractView",
//                            new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
//                            user.getLanguage().getLocale()
//                    )
//            );
//            sendMessage = SendMessage.builder()
//                    .chatId(user.getTelegramId())
//                    .text(messageText)
//                    .replyMarkup(getContractDocKeyboard(user.getLanguage().getLocale()))
//                    .build();
//            botState = BotState.VIEW_CONTRACT;
//        } else {
//            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
//            String messageText = String.format("%s\n\n%s",
//                    messageSource.getMessage(
//                            "yourCurrentDebt",
//                            new Object[]{totalSum, contracts.get(0).getEkvivalentCurrency()},
//                            user.getLanguage().getLocale()
//                    ),
//                    messageSource.getMessage("contracts", null, user.getLanguage().getLocale())
//            );
//            for (Contract contract : contracts) {
//                buttons.add(List.of(
//                        InlineKeyboardButton.builder()
//                                .text(contract.getName())
//                                .callbackData("contract_id:" + contract.getId())
//                                .build()
//                ));
//            }
//            buttons.add(List.of(
//                    InlineKeyboardButton.builder()
//                            .text(messageSource.getMessage("allContracts", null, user.getLanguage().getLocale()))
//                            .callbackData("contract_id:0")
//                            .build()
//            ));
//            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
//                    .keyboard(buttons)
//                    .build();
//            sendMessage = SendMessage.builder()
//                    .chatId(user.getTelegramId())
//                    .text(messageText)
//                    .replyMarkup(keyboard)
//                    .build();
//            botState = BotState.VIEW_CONTRACTS;
//        }
//        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(botState);
//        userService.updateUser(user);
//    }
//
//    private void actionResetMainMenu(User user) {
//        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()),
//                messageSource.getMessage(
//                        "mainMenu",
//                        null,
//                        user.getLanguage().getLocale()));
//        sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().getLocale()));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.MAIN_MENU);
//        userService.updateUser(user);
//    }
//
//    private void actionSetContact(Message message, User user) {
//        if (!(message.hasContact()
//                && message.isReply()
//                && user.getTelegramId().equals(message.getContact().getUserId()))) {
//            actionRestartContact(user);
//            return;
//        }
//        Message waitingMessage = actionSetWaitingMessage(user);
//        String phoneNumber = message.getContact().getPhoneNumber();
//        user.setPhoneNumber(phoneNumber);
//        Map<String, String> response = apiClient.getCustomerName(user.getLanguage(), phoneNumber, user.getTelegramId());
//        if (!response.containsKey("customer_name")) {
//            String messageText = messageSource.getMessage("customerNotFound", null, user.getLanguage().getLocale());
//            if (response.containsKey("error_text")) {
//                messageText = !response.get("error_text").isBlank() ? response.get("error_text") : messageText;
//            }
//            SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
//            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
//            telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
//            telegramBot.sendAnswerMessage(sendMessage);
//            return;
//        }
//        String messageText = messageSource.getMessage("successfullyRegistered", new Object[]{response.get("customer_name")}, user.getLanguage().getLocale());
//        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
//        sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().getLocale()));
//        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setFirstInit(false);
//        user.setBotState(BotState.MAIN_MENU);
//        user.setStatus(UserStatus.ACTIVE);
//        userService.updateUser(user);
//    }
//
//    private Message actionSetWaitingMessage(User user) {
//        SendMessage sendMessage = SendMessage.builder()
//                .chatId(user.getTelegramId())
//                .text(messageSource.getMessage("waiting", null, user.getLanguage().getLocale()))
//                .replyMarkup(new ReplyKeyboardRemove(true))
//                .build();
//        return telegramBot.sendAnswerMessage(sendMessage);
//    }
//
//    private void actionRestartContact(User user) {
//        KeyboardButton button = new KeyboardButton(messageSource.getMessage("sendPhoneNumber", null, user.getLanguage().getLocale()));
//        button.setRequestContact(true);
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(button))));
//        keyboardMarkup.setOneTimeKeyboard(true);
//        keyboardMarkup.setResizeKeyboard(true);
//
//        SendMessage sendMessage = SendMessage.builder()
//                .chatId(user.getTelegramId())
//                .text(messageSource.getMessage("shareContactToContinue", null, user.getLanguage().getLocale()))
//                .replyMarkup(keyboardMarkup)
//                .build();
//        telegramBot.sendAnswerMessage(sendMessage);
//    }
//
//    private void actionChooseLanguage(Message message, User user) {
//        if (!message.hasText()) {
//            actionStartCommand(user);
//            return;
//        }
//        Language language = Language.getLanguageByText(message.getText());
//        if (Objects.isNull(language)) {
//            actionStartCommand(user);
//            return;
//        }
//        user.setLanguage(language);
//        SendMessage sendMessage;
//        if (user.isFirstInit()) {
//            String comment = DataLoader.starterComment.get(user.getLanguage());
//            String bundleMessage = messageSource.getMessage("sharePhoneNumber", null, language.getLocale());
//            String messageText = String.format("%s\n%s", comment, bundleMessage);
//            sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
//            KeyboardButton button = new KeyboardButton(messageSource.getMessage("sendPhoneNumber", null, language.getLocale()));
//            button.setRequestContact(true);
//            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(button))));
//            keyboardMarkup.setOneTimeKeyboard(true);
//            keyboardMarkup.setResizeKeyboard(true);
//            sendMessage.setReplyMarkup(keyboardMarkup);
//            user.setBotState(BotState.SEND_PHONE_NUMBER);
//        } else {
//            user.setBotState(BotState.MAIN_MENU);
//            sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageSource.getMessage("languageUpdatedSuccessfully", null, user.getLanguage().getLocale()));
//            sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().getLocale()));
//        }
//        telegramBot.sendAnswerMessage(sendMessage);
//        userService.updateUser(user);
//    }
//
//    private void actionSendChooseLanguage(User user) {
//        SendMessage message = new SendMessage(String.valueOf(user.getTelegramId()), AppConstants.TELEGRAM_BOT_START_MESSAGE);
//        List<KeyboardRow> rows = new ArrayList<>();
//        for (String buttonText : Language.getTexts()) {
//            KeyboardRow row = new KeyboardRow();
//            row.add(buttonText);
//            rows.add(row);
//        }
//        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
//        keyboard.setResizeKeyboard(true);
//        keyboard.setOneTimeKeyboard(true);
//        message.setReplyMarkup(keyboard);
//        telegramBot.sendAnswerMessage(message);
//        user.setBotState(BotState.CHOOSE_LANGUAGE);
//        userService.updateUser(user);
//    }
//
//    private void actionStartCommand(User user) {
//        actionSendChooseLanguage(user);
//        user.setFirstInit(true);
//        userService.updateUser(user);
//    }
//
//    private ReplyKeyboardMarkup getMainMenuKeyboard(Locale lang) {
//
//        return ReplyKeyboardMarkup.builder()
//                .clearKeyboard()
//                .keyboard(List.of(
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("categoryListButton", null, lang))
//                        )), new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("myOrdersButton", null, lang))
//                        )),
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("basketButton", null, lang))
//                        )),
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("checkDebtButton", null, lang))
//                        )),
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("contactUsButton", null, lang))
//                        )),
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("changeLanguageButton", null, lang))
//                        ))
//                ))
//                .resizeKeyboard(true)
//                .build();
//    }
//
//    private ReplyKeyboard getContractDocKeyboard(Locale lang) {
//        return ReplyKeyboardMarkup.builder()
//                .clearKeyboard()
//                .keyboard(List.of(
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("getContractDocButton", null, lang))
//                        )),
//                        new KeyboardRow(List.of(
//                                new KeyboardButton(messageSource.getMessage("backToMainMenu", null, lang))
//                        ))
//                ))
//                .resizeKeyboard(true)
//                .build();
//    }
//
//    private List<String> getButtonTexts(String code) {
//        List<String> result = new ArrayList<>();
//        for (Language language : Language.values()) {
//            result.add(messageSource.getMessage(code, null, language.getLocale()));
//        }
//        return result;
//    }
//
//    public String validateDateRange(String fromDateStr, String toDateStr) {
//        if (fromDateStr == null && toDateStr == null) {
//            return "No date range provided";
//        }
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        dateFormat.setLenient(false);
//
//        try {
//            if (fromDateStr != null) {
//                Date fromDate = dateFormat.parse(fromDateStr);
//                // Check if fromDate is not greater than toDate
//                if (toDateStr != null) {
//                    Date toDate = dateFormat.parse(toDateStr);
//                    if (fromDate.after(toDate)) {
//                        return "fromDateMustBeBeforeToDate";
//                    }
//                }
//
//                // Check if fromDate is not greater than the current date
//                Date currentDate = new Date();
//                if (fromDate.after(currentDate)) {
//                    return "fromDateCannotBeInTheFuture";
//                }
//            }
//
//            // Check if toDate is not greater than the current date
//            if (toDateStr != null) {
//                Date toDate = dateFormat.parse(toDateStr);
//                Date currentDate = new Date();
//                if (toDate.after(currentDate)) {
//                    return "toDateCannotBeInTheFuture";
//                }
//            }
//
//            return null;
//        } catch (ParseException e) {
//            return "incorrectFormat";
//        }
//    }
//}