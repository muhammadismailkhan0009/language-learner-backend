package com.myriadcode.languagelearner.user_management.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ExtensionTokenUtils {

    private ExtensionTokenUtils() {
    }

    private static final String SECRET_KEY = "your-very-secret-key";

    public static String generateHmac(String message) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        sha256Hmac.init(keySpec);
        byte[] hash = sha256Hmac.doFinal(message.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public static boolean isValidToken(String incomingToken, String installId) {
        try {
            String expectedToken = generateHmac(installId);
            return expectedToken.equalsIgnoreCase(incomingToken);
        } catch (Exception e) {
            return false;
        }
    }

}
