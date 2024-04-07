package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.Contract;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.ContractService;
import com.company.shoppingbotv2.service.UserService;

import java.util.List;
import java.util.Locale;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ViewContractsHandler implements CallbackQueryHandler {
    TelegramBot telegramBot;
    MessageSource messageSource;
    ContractService contractService;
    HandlerHelper helper;
    UserService userService;

    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        user.setBotState(BotState.VIEW_CONTRACT);
        String contractId = callbackQuery.getData().split(":")[1];
        String messageText;
        if ("0".equals(contractId)) {
            List<Contract> contracts = user.getContracts();
            StringBuilder messageTextBuilder = new StringBuilder();
            double totalDebtSum = 0;
            for (Contract contract : contracts) {
                messageTextBuilder.append("\n\n");
                messageTextBuilder.append(messageSource.getMessage(
                        "contractView",
                        new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
                        user.getLanguage().getLocale()
                ));
                totalDebtSum += contract.getSum();
            }
            messageTextBuilder.insert(0, new StringBuilder(messageSource.getMessage(
                    "yourCurrentDebt",
                    new Object[]{totalDebtSum, contracts.get(0).getEkvivalentCurrency()},
                    user.getLanguage().getLocale()
            )));
            messageText = messageTextBuilder.toString();
            user.setCurrentContract(null);
        } else {
            Contract contract = contractService.getById(Integer.parseInt(contractId));
            messageText = messageSource.getMessage(
                    "contractView",
                    new Object[]{contract.getName(), contract.getSum(), contract.getCurrency(), contract.getEkvivalent(), contract.getEkvivalentCurrency()},
                    user.getLanguage().getLocale()
            );
            user.setCurrentContract(contract);
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
        sendMessage.setReplyMarkup(getContractViewKeyboard(user.getLanguage().getLocale()));
        telegramBot.sendAnswerMessage(sendMessage);
        helper.deleteCallBackQueryMessage(callbackQuery, user);
        userService.updateUser(user);
    }

    public ReplyKeyboard getContractViewKeyboard(Locale locale) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(
                new KeyboardRow(List.of(new KeyboardButton(messageSource.getMessage("getContractDocButton", null, locale)))),
                new KeyboardRow(List.of(
                        new KeyboardButton(messageSource.getMessage("backToButton", null, locale)),
                        new KeyboardButton(messageSource.getMessage("backToMainMenu", null, locale))
                ))
        ));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }
}
