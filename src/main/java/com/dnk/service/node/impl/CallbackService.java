package com.dnk.service.node.impl;

import com.dnk.entity.AppUser;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.AppUserService;
import com.dnk.service.jpa.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class CallbackService {

    private final AppUserService appUserService;
    private final StudentService studentService;

    private InlineKeyboardMarkup createKeyboard(Supplier<List<?>> entitiesSupplier) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        try {
            List<?> entities = entitiesSupplier.get();
            for (Object entity : entities) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = getInlineKeyboardButton(entity);
                row.add(button);
                rows.add(row);
            }
            markup.setKeyboard(rows);
            return markup;
        } catch (ScheduleException exception) {
            return null;
        }
    }

    private static InlineKeyboardButton getInlineKeyboardButton(Object entity) {
        String buttonText = entity instanceof AppUser
                ? String.format("%s %s", ((AppUser) entity).getFirstName(), ((AppUser) entity).getLastName())
                : String.format("%s %s", ((Student) entity).getFirstName(), ((Student) entity).getLastName());
        String callbackData = entity instanceof AppUser
                ? ((AppUser) entity).getTelegramUserId().toString()
                : ((Student) entity).getId().toString();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(callbackData);
        return button;
    }

    public InlineKeyboardMarkup createKeyboardForAppUsers() {
        return createKeyboard(appUserService::findAppUsersWithoutStudents);
    }

    public InlineKeyboardMarkup createKeyboardForStudents() {
        return createKeyboard(studentService::findStudentsByWithoutAppUser);
    }

    public InlineKeyboardMarkup createKeyboardYesOrNo() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();

        yesButton.setText("Так");
        yesButton.setCallbackData("YES_BUTTON");

        InlineKeyboardButton noButton = new InlineKeyboardButton();

        noButton.setText("Ні");
        noButton.setCallbackData("NO_BUTTON");

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public boolean isAppUserExistFromCallback(String callbackData) {
        long userId = Long.parseLong(callbackData);
        return appUserService.existsByTelegramUserId(userId);
    }

    public boolean isStudentExistFromCallback(String callbackData) {
        long userId = Long.parseLong(callbackData);
        return studentService.existsById(userId);
    }


    public AppUser getAppUser(String callbackData) throws ScheduleException {
        long userId = Long.parseLong(callbackData);
        return appUserService.findByTelegramUserId(userId);
    }

    public Student getStudent(String callbackData) throws ScheduleException {
        long userId = Long.parseLong(callbackData);
        return studentService.findById(userId);
    }
}
