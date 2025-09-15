package com.budget.ai.logging;

import com.budget.ai.user.dto.request.PasswordUpdateRequest;
import com.budget.ai.user.dto.request.RegisterRequest;

import java.util.Arrays;

public class MaskingUtil {

    public static Object[] makeMaskingData(Object[] args) {
        return Arrays.stream(args)
                .map(MaskingUtil::makeMaskingObject)
                .toArray();
    }

    public static Object makeMaskingObject(Object arg) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof RegisterRequest request) {
            return new RegisterRequest(
                    MaskingUtil.makeMaskingEmail(request.email()),
                    "********",
                    request.name()
            );
        } else if (arg instanceof PasswordUpdateRequest request) {
            return new PasswordUpdateRequest(
                    "********",
                    "********"
            );
        }

        return arg;
    }

    public static String makeMaskingEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        int idx = email.indexOf("@");
        String prefix = email.substring(0, Math.min(3, idx));

        return prefix + "****" + email.substring(idx);
    }

    public static String makeMaskingToken(String token) {
        if (token == null || !token.contains(".")) {
            return token;
        }

        return token.substring(0, 10) + "..." + token.substring(token.length() - 4);
    }

    public static String makeMaskingCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}