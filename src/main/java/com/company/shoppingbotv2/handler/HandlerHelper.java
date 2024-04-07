package com.company.shoppingbotv2.handler;

import com.company.shoppingbotv2.payload.ContrAgent;
import com.company.shoppingbotv2.payload.ContrAgentList;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.Contract;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.entity.enums.Language;
import com.company.shoppingbotv2.payload.DebtCheckResponse;
import com.company.shoppingbotv2.payload.ProductResponse;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.ContractService;
import com.company.shoppingbotv2.service.UserService;
import com.company.shoppingbotv2.utils.AppConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HandlerHelper {
    private final MessageSource messageSource;
    private final UserService userService;
    private final TelegramBot telegramBot;
    private final ApiClient apiClient;
    private final ContractService contractService;

    public void startCommand(User user) {
        sendChooseLanguage(user);
        user.setFirstInit(true);
        userService.updateUser(user);
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(Locale lang) {
        return ReplyKeyboardMarkup.builder()
                .clearKeyboard()
                .keyboard(List.of(
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("categoryListButton", null, lang))
                        )),
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("basketButton", null, lang))
                        )),
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("myOrdersButton", null, lang))
                        )),
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("contactUsButton", null, lang))
                        )),
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("changeLanguageButton", null, lang))
                        ))
                ))
                .resizeKeyboard(true)
                .build();
    }

    public void sendChooseLanguage(User user) {
        SendMessage message = new SendMessage(String.valueOf(user.getTelegramId()), AppConstants.TELEGRAM_BOT_START_MESSAGE);
        List<KeyboardRow> rows = new ArrayList<>();
        for (String buttonText : Language.getTexts()) {
            KeyboardRow row = new KeyboardRow();
            row.add(buttonText);
            rows.add(row);
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        message.setReplyMarkup(keyboard);
        telegramBot.sendAnswerMessage(message);
        user.setBotState(BotState.CHOOSE_LANGUAGE);
        userService.updateUser(user);
    }

    public void checkDebt(User user, boolean fromLocalDb) {
        user.setFromDateRange(null);
        user.setToDateRange(null);
        // Load contracts data from api and save contracts
        Message waitingMessage = setWaitingMessage(user);
        List<Contract> contracts;
        Double totalSum = 0D;
        if (fromLocalDb) {
            contracts = user.getContracts();
            for (Contract contract : contracts) {
                totalSum += contract.getSum();
            }
        } else {
            DebtCheckResponse response = apiClient.getDebtContracts(user.getTelegramId());
            contracts = contractService.saveUserContracts(user, response);
            totalSum = response.totalSum();
        }

        SendMessage sendMessage;
        BotState botState;
        if (Objects.isNull(totalSum) || totalSum.equals(0D)) {
            sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("youHaveNotDebts", null, user.getLanguage().getLocale()))
                    .replyMarkup(getMainMenuKeyboard(user.getLanguage().getLocale()))
                    .build();
            user.setBotState(null);
            user.setCurrentContract(null);
            sendGetActDocMessage(user);
            return;
        } else if (contracts.size() == 1) {
            Contract contract = contracts.get(0);
            user.setCurrentContract(contract);
            String messageText = String.format(
                    "%s\n\n%s",
                    messageSource.getMessage(
                            "yourCurrentDebt",
                            new Object[]{totalSum, contract.getEkvivalentCurrency()},
                            user.getLanguage().getLocale()
                    ),
                    messageSource.getMessage(
                            "contractView",
                            new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
                            user.getLanguage().getLocale()
                    )
            );
            sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageText)
                    .replyMarkup(getContractDocKeyboard(user.getLanguage().getLocale()))
                    .build();
            botState = BotState.VIEW_CONTRACT;
        } else {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            String messageText = String.format("%s\n\n%s",
                    messageSource.getMessage(
                            "yourCurrentDebt",
                            new Object[]{totalSum, contracts.get(0).getEkvivalentCurrency()},
                            user.getLanguage().getLocale()
                    ),
                    messageSource.getMessage("contracts", null, user.getLanguage().getLocale())
            );
            for (Contract contract : contracts) {
                buttons.add(List.of(
                        InlineKeyboardButton.builder()
                                .text(contract.getName())
                                .callbackData("contract_id:" + contract.getId())
                                .build()
                ));
            }
            buttons.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(messageSource.getMessage("allContracts", null, user.getLanguage().getLocale()))
                            .callbackData("contract_id:0")
                            .build()
            ));
            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(buttons)
                    .build();
            sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageText)
                    .replyMarkup(keyboard)
                    .build();
            botState = BotState.VIEW_CONTRACTS;
        }
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(botState);
        userService.updateUser(user);
    }

    public Message setWaitingMessage(User user) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("waiting", null, user.getLanguage().getLocale()))
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
        return telegramBot.sendAnswerMessage(sendMessage);
    }

    public ReplyKeyboard getContractDocKeyboard(Locale lang) {
        return ReplyKeyboardMarkup.builder()
                .clearKeyboard()
                .keyboard(List.of(
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("getContractDocButton", null, lang))
                        )),
                        new KeyboardRow(List.of(
                                new KeyboardButton(messageSource.getMessage("backToMainMenu", null, lang))
                        ))
                ))
                .resizeKeyboard(true)
                .build();
    }

    public void resetMainMenu(User user) {
        user.setCurrentCategoryId(null);
        user.setSearchKey(null);
        user.setCurrentPage(null);
        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageSource.getMessage("mainMenu", null, user.getLanguage().getLocale()));
        sendMessage.setReplyMarkup(getMainMenuKeyboard(user.getLanguage().getLocale()));
        user.setBotState(BotState.MAIN_MENU);
        telegramBot.sendAnswerMessage(sendMessage);
        userService.updateUser(user);
    }

    public List<String> getButtonTexts(String code) {
        List<String> result = new ArrayList<>();
        for (Language language : Language.values()) {
            result.add(messageSource.getMessage(code, null, language.getLocale()));
        }
        return result;
    }

    public String validateDateRange(String fromDateStr, String toDateStr) {
        if (fromDateStr == null && toDateStr == null) {
            return "No date range provided";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            if (fromDateStr != null) {
                Date fromDate = dateFormat.parse(fromDateStr);
                // Check if fromDate is not greater than toDate
                if (toDateStr != null) {
                    Date toDate = dateFormat.parse(toDateStr);
                    if (fromDate.after(toDate)) {
                        return "fromDateMustBeBeforeToDate";
                    }
                }

                // Check if fromDate is not greater than the current date
                Date currentDate = new Date();
                if (fromDate.after(currentDate)) {
                    return "fromDateCannotBeInTheFuture";
                }
            }

            // Check if toDate is not greater than the current date
            if (toDateStr != null) {
                Date toDate = dateFormat.parse(toDateStr);
                Date currentDate = new Date();
                if (toDate.after(currentDate)) {
                    return "toDateCannotBeInTheFuture";
                }
            }

            return null;
        } catch (ParseException e) {
            return "incorrectFormat";
        }
    }

    public void sendGetActDocMessage(User user) {
        Contract currentContract = user.getCurrentContract();
        String currentContractName;
        if (Objects.isNull(currentContract)) {
            currentContractName = messageSource.getMessage("allContracts", null, user.getLanguage().getLocale());
        } else {
            currentContractName = currentContract.getName();
        }
        String fromDateRange = user.getFromDateRange();
        if (Objects.isNull(fromDateRange)) {
            fromDateRange = "-";
        }
        String toDateRange = user.getToDateRange();
        if (Objects.isNull(toDateRange)) {
            toDateRange = "-";
        }
        SendMessage mockMessage = new SendMessage(String.valueOf(user.getTelegramId()), ".");
        ReplyKeyboardMarkup mockMessageKeyboard = new ReplyKeyboardMarkup(
                List.of(new KeyboardRow(List.of(new KeyboardButton(
                                messageSource.getMessage("backToButton", null, user.getLanguage().getLocale())
                        ))),
                        new KeyboardRow(List.of(new KeyboardButton(
                                messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale())
                        )))
                ));
        mockMessageKeyboard.setResizeKeyboard(true);
        mockMessage.setReplyMarkup(mockMessageKeyboard);
        SendMessage sendMessage = new SendMessage(
                String.valueOf(user.getTelegramId()),
                messageSource.getMessage(
                        "getActDoc",
                        new Object[]{currentContractName, fromDateRange, toDateRange},
                        user.getLanguage().getLocale()
                ));
        sendMessage.setReplyMarkup(
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("fromDateRangeButton", null, user.getLanguage().getLocale()))
                                .callbackData("fromDateRange")
                                .build()
                        ),
                        List.of(InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("toDateRangeButton", null, user.getLanguage().getLocale()))
                                .callbackData("toDateRange")
                                .build()
                        ),
                        List.of(InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("getActDocButton", null, user.getLanguage().getLocale()))
                                .callbackData("getActDoc")
                                .build()
                        )
                )));
        telegramBot.sendAnswerMessage(mockMessage);
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.GET_ACT_DOC);
        userService.updateUser(user);
    }

    public void deleteCallBackQueryMessage(CallbackQuery callbackQuery, User user) {
        telegramBot.sendDeleteMessage(
                DeleteMessage.builder()
                        .chatId(user.getTelegramId())
                        .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                        .build()
        );
    }

    public InlineKeyboardMarkup getUserCartKeyboard(Locale locale, List<OrderProduct> orderProduct) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (OrderProduct product : orderProduct) {
            rows.add(List.of(InlineKeyboardButton.builder().text(product.getProductName()).callbackData(".").build()));
            rows.add(List.of(
                    InlineKeyboardButton.builder().text("➖").callbackData("decrement:" + product.getProductId()).build(),
                    InlineKeyboardButton.builder().text(String.valueOf(product.getCount())).callbackData("counter:" + product.getId()).build(),
                    InlineKeyboardButton.builder().text("➕").callbackData("increment:" + product.getProductId()).build()
            ));
            rows.add(List.of(
                    InlineKeyboardButton.builder().text(messageSource.getMessage("orderCancellation", null, locale))
                            .callbackData("cancel:" + product.getProductId())
                            .build())
            );
        }
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text(messageSource.getMessage("placeAnOrder", null, locale))
                        .callbackData("place_an_order")
                        .build(),
                InlineKeyboardButton.builder()
                        .text(messageSource.getMessage("backToMainMenu", null, locale))
                        .callbackData("back_to_main_menu")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    public void sendProductListToUser(User user, List<ProductResponse> productList, int currentPage, int totalPage, String categoryId) {
        user.setBotState(BotState.SHOW_PRODUCT_LIST);
        SendMessage sendMessage = SendMessage.builder()
                .text(messageSource.getMessage("showProductList", null, user.getLanguage().getLocale()))
                .chatId(user.getTelegramId())
                .build();
        sendMessage.setReplyMarkup(getProductsKeyboard(categoryId, productList, currentPage, totalPage));

        telegramBot.sendAnswerMessage(sendMessage);
        userService.updateUser(user);
    }

    public InlineKeyboardMarkup getProductsKeyboard(String categoryId, List<ProductResponse> productList, int currentPage, int totalPage) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (ProductResponse productResponse : productList) {
            keyboard.add(List.of(InlineKeyboardButton.builder().text(productResponse.name()).callbackData("product:" + productResponse.id()).build()));
        }
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text("❌")
                .callbackData("backCategory")
                .build();
        InlineKeyboardButton nextButton;
        if (totalPage > 1) {
            InlineKeyboardButton backButton;
            if (currentPage == 1) {
                backButton = InlineKeyboardButton.builder()
                        .text("⏹")
                        .callbackData("stop")
                        .build();
            } else {
                backButton = InlineKeyboardButton.builder()
                        .text("⬅️")
                        .callbackData(String.format("page:%s;category:%s", (currentPage - 1), categoryId))
                        .build();
            }
            if (currentPage == totalPage) {
                nextButton = InlineKeyboardButton.builder()
                        .text("⏹")
                        .callbackData("stop")
                        .build();
            } else {
                nextButton = InlineKeyboardButton.builder()
                        .text("➡️")
                        .callbackData(String.format("page:%s;category:%s", (currentPage + 1), categoryId))
                        .build();
            }
            keyboard.add(List.of(backButton, cancelButton, nextButton));
        } else {
            keyboard.add(List.of(cancelButton));
        }

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }


    public InlineKeyboardMarkup getProductViewKeyboard(String productId, double count, User user) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text("➖")
                                .callbackData("decrement:" + productId + ":" + count) // Decrement button
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(String.valueOf(count)) // Initial value of the counter
                                .callbackData("counter:" + productId + ":" + count) // Counter value
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("➕")
                                .callbackData("increment:" + productId + ":" + count) // Increment button
                                .build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("addToCart", null, user.getLanguage().getLocale()))
                                .callbackData("add_to_cart:" + productId + ":" + count)
                                .build(),

                        InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("Cancel", null, user.getLanguage().getLocale()))
                                .callbackData("cancel:" + productId)
                                .build()
                ))
                .build();
    }


    public InlineKeyboardMarkup getCounterKeyboard(OrderProduct orderProduct, Locale locale) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(InlineKeyboardButton.builder().text("7️⃣").callbackData("button:7:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("8️⃣").callbackData("button:8:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("9️⃣").callbackData("button:9:" + orderProduct.getId()).build()),
                        List.of(InlineKeyboardButton.builder().text("4️⃣").callbackData("button:4:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("5️⃣").callbackData("button:5:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("6️⃣").callbackData("button:6:" + orderProduct.getId()).build()),
                        List.of(InlineKeyboardButton.builder().text("1️⃣").callbackData("button:1:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("2️⃣").callbackData("button:2:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("3️⃣").callbackData("button:3:" + orderProduct.getId()).build()),
                        List.of(InlineKeyboardButton.builder().text("0️⃣").callbackData("button:0:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("\uD83C\uDD91").callbackData("button:clear:" + orderProduct.getId()).build(), InlineKeyboardButton.builder().text("⬅️").callbackData("button:remove:" + orderProduct.getId()).build()),
                        List.of(InlineKeyboardButton.builder().text(messageSource.getMessage("confirm", null, locale)).callbackData("button:confirm:" + orderProduct.getId()).build())
                ))
                .build();
    }

    public String getCounterMessageText(OrderProduct orderProduct, Locale locale) {
        return messageSource.getMessage("counterView", new Object[]{orderProduct.getProductName(), orderProduct.getProductPrice(), orderProduct.getAmount(), orderProduct.getCount()}, locale);
    }

    public String buildAllContractMessage(User user, List<Contract> contracts) {
        StringBuilder messageTextBuilder = new StringBuilder();
        double totalDebtSum = 0;
        for (Contract contract : contracts) {
            messageTextBuilder.append("\n\n");
            messageTextBuilder.append(messageSource.getMessage(
                    "contractView",
                    new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
                    user.getLanguage().getLocale()
            ));
            totalDebtSum += Double.parseDouble(contract.getEkvivalent());
        }
        messageTextBuilder.insert(0, new StringBuilder(messageSource.getMessage(
                "yourCurrentDebt",
                new Object[]{totalDebtSum, contracts.get(0).getEkvivalentCurrency()},
                user.getLanguage().getLocale()
        )));
        return messageTextBuilder.toString();
    }


    public InlineKeyboardMarkup getContrAgentsKeyboard(List<ContrAgent> contrAgentList, int currentPage, int totalPage, @Nullable String contrAgentName) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (ContrAgent contrAgent : contrAgentList) {
            keyboard.add(List.of(InlineKeyboardButton.builder().text(contrAgent.name()).callbackData("contr_agent:" + contrAgent.userTIN()).build()));
        }
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text(String.format("%s/%s", currentPage, totalPage))
                .callbackData("nothing")
                .build();
        InlineKeyboardButton nextButton;
        if (totalPage > 1) {
            InlineKeyboardButton backButton;
            if (currentPage == 1) {
                backButton = InlineKeyboardButton.builder()
                        .text("⏹")
                        .callbackData("stop")
                        .build();
            } else {
                backButton = InlineKeyboardButton.builder()
                        .text("⬅️")
                        .callbackData(String.format("page:%s;name:%s", (currentPage - 1), contrAgentName))
                        .build();
            }
            if (currentPage == totalPage) {
                nextButton = InlineKeyboardButton.builder()
                        .text("⏹")
                        .callbackData("stop")
                        .build();
            } else {
                nextButton = InlineKeyboardButton.builder()
                        .text("➡️")
                        .callbackData(String.format("page:%s;name:%s", (currentPage + 1), contrAgentName))
                        .build();
            }
            keyboard.add(List.of(backButton, cancelButton, nextButton));
        }

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public void getContrAgentList(User user, @Nullable Integer currentPage, @Nullable String searchContrAgentName) {
        Message waitingMessage = setWaitingMessage(user);
        if (Objects.isNull(currentPage)) currentPage = 1;
        ContrAgentList contrAgentList = apiClient.getContrAgentList(user, currentPage, searchContrAgentName);
        int totalPage = contrAgentList.totalCount() % AppConstants.PAGE_SIZE == 0
                ? contrAgentList.totalCount() / AppConstants.PAGE_SIZE
                : contrAgentList.totalCount() / AppConstants.PAGE_SIZE + 1;

        InlineKeyboardMarkup keyboard = getContrAgentsKeyboard(contrAgentList.contrAgents(), currentPage, totalPage, searchContrAgentName);

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(
                        new KeyboardButton(messageSource.getMessage("searchContrAgentName", null, user.getLanguage().getLocale())),
                        new KeyboardButton(messageSource.getMessage("searchContrAgentTIN", null, user.getLanguage().getLocale()))
                )),
                new KeyboardRow(List.of(
                        new KeyboardButton(messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale()))
                ))));

        replyKeyboard.setResizeKeyboard(true);
        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("SelectOrSearch", null, user.getLanguage().getLocale()))
                .replyMarkup(replyKeyboard)
                .build());

        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("contrAgentList", null, user.getLanguage().getLocale()))
                .replyMarkup(keyboard)
                .build()
        );
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
        userService.updateUser(user);
    }
}
