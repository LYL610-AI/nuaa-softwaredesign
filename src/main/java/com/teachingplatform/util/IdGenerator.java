package com.teachingplatform.util;

import java.security.SecureRandom;

public class IdGenerator {
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final int LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
