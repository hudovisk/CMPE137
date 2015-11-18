package com.assignment.sjsu.hudoassenco.cmpe137;

/**
 * Created by hudoassenco on 11/17/15.
 */
public class Utils {

    public static class ValidationResult {
        public boolean valid;
        public int messageRes;

        public ValidationResult(boolean valid, int messageRes) {
            this.valid = valid;
            this.messageRes = messageRes;
        }
    }

    public static ValidationResult isPasswordValid(String password) {
        //TODO: Proper password validation logic.
        if(password.isEmpty()) {
            return new ValidationResult(false, R.string.error_invalid_password);
        } else {
            return new ValidationResult(true, 0);
        }
    }

    public static ValidationResult isEmailValid(String email) {
        //TODO: Proper email validation logic.
        if(email.isEmpty()) {
            return new ValidationResult(false, R.string.error_invalid_email);
        } else {
            return new ValidationResult(true, 0);
        }
    }

    public static ValidationResult isNameValid(String name) {
        //TODO: Proper name validation logic.
        if(name.isEmpty()) {
            return new ValidationResult(false, R.string.error_invalid_password);
        } else {
            return new ValidationResult(true, 0);
        }
    }

}
