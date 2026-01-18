package com.hotel.management.system.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public final class Validator {

    private Validator() {}

    public static void required(TextField field, String fieldName) {
        if (field.getText() == null || field.getText().isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static void numeric(TextField field, String fieldName) {
        try {
            Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be numeric.");
        }
    }

    public static void email(TextField field) {
        String value = field.getText().trim();
        if (!value.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new ValidationException("Invalid email format.");
        }
    }

    public static <T> void required(ComboBox<T> comboBox, String fieldName) {
        if (comboBox.getValue() == null) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static int integer(TextField field, String fieldName) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid number.");
        }
    }

    public static double decimal(TextField field, String fieldName) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid decimal value.");
        }
    }

    public static void required(DatePicker picker, String fieldName) {
        if (picker.getValue() == null) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static void dateNotInPast(DatePicker picker, String fieldName) {
        LocalDate date = picker.getValue();
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException(fieldName + " must be today or later.");
        }
    }

    public static void dateAfter(
            DatePicker start,
            DatePicker end,
            String startName,
            String endName
    ) {
        if (!end.getValue().isAfter(start.getValue())) {
            throw new ValidationException(endName + " must be after " + startName + ".");
        }
    }

    public static void passwordMatches(
            String actualPassword,
            String inputPassword
    ) {
        if (!actualPassword.equals(inputPassword)) {
            throw new ValidationException("Current password is incorrect.");
        }
    }

    public static void passwordsMatch(
            String password,
            String confirmPassword
    ) {
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }
    }

    public static void passwordDifferent(
            String oldPassword,
            String newPassword
    ) {
        if (oldPassword.equals(newPassword)) {
            throw new ValidationException("New password must be different from the current password.");
        }
    }




    public static String value(TextField field) {
        return field.getText().trim();
    }
}
