package com.company.shoppingbotv2.service;//package com.company.shoppingbotv2.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.MessageSource;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
//import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
//import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
//import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
//import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
//import org.telegram.telegrambots.meta.api.objects.InputFile;
//import org.telegram.telegrambots.meta.api.objects.Message;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
//import com.company.shoppingbotv2.config.TelegramBot;
//import com.company.shoppingbotv2.entity.Contract;
//import com.company.shoppingbotv2.entity.User;
//import com.company.shoppingbotv2.entity.enums.BotState;
//import com.company.shoppingbotv2.handler.callbackquery.CallbackQueryHandler;
//import com.company.shoppingbotv2.payload.ProductResponse;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//public class CallbackQueryHandlerImpl implements CallbackQueryHandler {
//    private final TelegramBot telegramBot;
//    private final UserService userService;
//    private final ApiClient apiClient;
//    private final MessageSource messageSource;
//    private final ContractService contractService;
//
//    @Override
//    public void handle(CallbackQuery callbackQuery, User user) {
//        if (!callbackQuery.getMessage().isUserMessage()) {
//            return;
//        }
//        if (user.getBotState() == BotState.VIEW_CONTRACTS && callbackQuery.getData().startsWith("contract_id:")) {
//            actionViewContract(callbackQuery, user);
//        } else if (user.getBotState() == BotState.GET_ACT_DOC) {
//            actionGetActDoc(callbackQuery, user);
//        } else if (user.getBotState() == BotState.CATEGORY_LIST) {
//            actionCategoryList(callbackQuery, user);
//        }
//    }
//
//    private void actionCategoryList(CallbackQuery callbackQuery, User user) {
//        String callbackQueryData = callbackQuery.getData();
//        int currentPage = 1; // Sahifani birinchi sahifadan boshlash
//        String[] part = callbackQueryData.split(":");
//        if (part.length == 2 && part[0].equals("category")) {
//            String categoryId = part[1];
//            List<ProductResponse> productList = apiClient.showProductList(user.getTelegramId(), categoryId).productResponseList();
//            sendProductListToUser(user, productList, currentPage);
//        } else if (callbackQueryData.equals("next_page")) {
//         currentPage++;
//
//        }
//    }
//
//
//
//    private void sendProductListToUser(User user, List<ProductResponse> productResponses, int currentPage) {
//        int pageSize = 6; // Number of products to display per page
//        int totalPages = (int) Math.ceil((double) productResponses.size() / pageSize); // Calculate total pages
//
//        SendMessage sendMessage = SendMessage.builder()
//                .text(messageSource.getMessage("showProductList", null, user.getLanguage().getLocale()))
//                .chatId(user.getTelegramId())
//                .build();
//
//        // InlineKeyboardMarkup creation directly inside the method
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
//
//        int buttonsPerRow = 2; // Change this value based on your preference
//        int buttonCount = 0;
//        List<InlineKeyboardButton> row = new ArrayList<>();
//
//        int startIdx = (currentPage - 1) * pageSize;
//        int endIdx = Math.min(currentPage * pageSize, productResponses.size());
//
//        for (int i = startIdx; i < endIdx; i++) {
//            ProductResponse product = productResponses.get(i);
//            String productId = product.id();
//            String productName = product.name();
//
//            InlineKeyboardButton button = InlineKeyboardButton.builder()
//                    .text(productName)
//                    .callbackData("product_id:" + productId)
//                    .build();
//            row.add(button);
//
//            buttonCount++;
//            if (buttonCount >= buttonsPerRow) {
//                keyboard.add(row);
//                row = new ArrayList<>();
//                buttonCount = 0;
//            }
//        }
//
//        if (!row.isEmpty()) {
//            keyboard.add(row);
//        }
//
//        // Adding pagination buttons
//        List<InlineKeyboardButton> paginationRow = new ArrayList<>();
//        if (currentPage > 1) {
//            paginationRow.add(InlineKeyboardButton.builder()
//                    .text(messageSource.getMessage("forward", null, user.getLanguage().getLocale()))
//                    .callbackData("previous_page")
//                    .build());
//        }
//        if (currentPage < totalPages) {
//            paginationRow.add(InlineKeyboardButton.builder()
//                    .text(messageSource.getMessage("next", null, user.getLanguage().getLocale()))
//                    .callbackData("next_page")
//                    .build());
//        }
//        keyboard.add(paginationRow);
//
//        // Adding back to main menu button
//        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
//                .text(messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale()))
//                .callbackData("back_to_main_menu")
//                .build();
//        List<InlineKeyboardButton> backRow = new ArrayList<>();
//        backRow.add(backButton);
//        keyboard.add(backRow);
//
//        markup.setKeyboard(keyboard);
//
//        sendMessage.setReplyMarkup(markup);
//
//
//        telegramBot.sendAnswerMessage(sendMessage);
//
//    }
//
//
//
//
//
//    private void actionGetActDoc(CallbackQuery callbackQuery, User user) {
//        String data = callbackQuery.getData();
//        if ("fromDateRange".equals(data)) {
//            deleteCallBackQueryMessage(callbackQuery, user);
//            sendEnterFromDateRangeMessage(user);
//        } else if ("toDateRange".equals(data)) {
//            deleteCallBackQueryMessage(callbackQuery, user);
//            sendEnterToDateRangeMessage(user);
//        } else if ("getActDoc".equals(data)) {
//            sendCurrentDocAct(callbackQuery, user);
//        }
//    }
//
//    private void deleteCallBackQueryMessage(CallbackQuery callbackQuery, User user) {
//        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), ((Message) callbackQuery.getMessage()).getMessageId()));
//    }
//
//    private void sendCurrentDocAct(CallbackQuery callbackQuery, User user) {
//        Message callbackQueryMessage = (Message) callbackQuery.getMessage();
//        telegramBot.sendEditMessage(EditMessageText.builder()
//                .chatId(user.getTelegramId())
//                .text(messageSource.getMessage("waiting", null, user.getLanguage().getLocale()))
//                .messageId(callbackQueryMessage.getMessageId())
//                .build()
//        );
//        Contract currentContract = user.getCurrentContract();
//        String currentContractRemoteId = null;
//        if (!Objects.isNull(currentContract)) {
//            currentContractRemoteId = currentContract.getRemoteId();
//        }
//        String filePath = apiClient.downloadActDoc(
//                user.getTelegramId(),
//                currentContractRemoteId,
//                user.getFromDateRange(),
//                user.getToDateRange()
//        );
//        telegramBot.sendChatAction(SendChatAction.builder().chatId(user.getTelegramId()).action("upload_document").build());
//        File file = new File(filePath);
//        SendDocument sendDocument = new SendDocument(String.valueOf(user.getTelegramId()), new InputFile(file));
//        telegramBot.sendDocument(sendDocument);
//        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), callbackQueryMessage.getMessageId()));
//        file.delete();
//    }
//
//    private void actionViewContract(CallbackQuery callbackQuery, User user) {
//        user.setBotState(BotState.VIEW_CONTRACT);
//        String contractId = callbackQuery.getData().split(":")[1];
//        String messageText;
//        if ("0".equals(contractId)) {
//            List<Contract> contracts = user.getContracts();
//            StringBuilder messageTextBuilder = new StringBuilder();
//            double totalDebtSum = 0;
//            for (Contract contract : contracts) {
//                messageTextBuilder.append("\n\n");
//                messageTextBuilder.append(messageSource.getMessage(
//                        "contractView",
//                        new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
//                        user.getLanguage().getLocale()
//                ));
//                totalDebtSum += contract.getSum();
//            }
//            messageTextBuilder.insert(0, new StringBuilder(messageSource.getMessage(
//                    "yourCurrentDebt",
//                    new Object[]{totalDebtSum, contracts.get(0).getEkvivalentCurrency()},
//                    user.getLanguage().getLocale()
//            )));
//            messageText = messageTextBuilder.toString();
//            user.setCurrentContract(null);
//        } else {
//            Contract contract = contractService.getById(Integer.parseInt(contractId));
//            messageText = messageSource.getMessage(
//                    "contractView",
//                    new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
//                    user.getLanguage().getLocale()
//            );
//            user.setCurrentContract(contract);
//        }
//        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
//        sendMessage.setReplyMarkup(getContractViewKeyboard(user.getLanguage().getLocale()));
//        telegramBot.sendAnswerMessage(sendMessage);
//        deleteCallBackQueryMessage(callbackQuery, user);
//        userService.updateUser(user);
//    }
//
//    private void sendEnterFromDateRangeMessage(User user) {
//        SendMessage sendMessage = new SendMessage(
//                String.valueOf(user.getTelegramId()),
//                messageSource.getMessage("enterFromDateRange", null, user.getLanguage().getLocale())
//        );
//        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.ENTER_FROM_DATE);
//        userService.updateUser(user);
//    }
//
//    private void sendEnterToDateRangeMessage(User user) {
//        SendMessage sendMessage = new SendMessage(
//                String.valueOf(user.getTelegramId()),
//                messageSource.getMessage("enterToDateRange", null, user.getLanguage().getLocale())
//        );
//        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
//        telegramBot.sendAnswerMessage(sendMessage);
//        user.setBotState(BotState.ENTER_TO_DATE);
//        userService.updateUser(user);
//    }
//
//    protected ReplyKeyboard getContractViewKeyboard(Locale locale) {
//        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(
//                new KeyboardRow(List.of(new KeyboardButton(messageSource.getMessage("getContractDocButton", null, locale)))),
//                new KeyboardRow(List.of(
//                        new KeyboardButton(messageSource.getMessage("backToButton", null, locale)),
//                        new KeyboardButton(messageSource.getMessage("backToMainMenu", null, locale))
//                ))
//        ));
//        keyboard.setResizeKeyboard(true);
//        return keyboard;
//    }
//
//    private void actionSetWaitingAnswer(CallbackQuery callbackQuery, User user) {
//        AnswerCallbackQuery chatAction = AnswerCallbackQuery.builder()
//                .callbackQueryId(callbackQuery.getId())
//                .text(messageSource.getMessage("waiting", null, user.getLanguage().getLocale()))
//                .cacheTime(60)
//                .build();
//        telegramBot.sendAnswerCallbackQuery(chatAction);
//    }
//}
