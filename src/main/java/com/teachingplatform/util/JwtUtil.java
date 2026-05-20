package com.teachingplatform.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class JwtUtil {
    private static final String SECRET = "teaching-platform-secret-2026";
    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L;

    public static String generate(String userId, int permission) {
        long now = System.currentTimeMillis();
        long exp = now + EXPIRE_MS;
        String header = base64Encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Encode("{\"userId\":\"" + userId + "\",\"permission\":" + permission + ",\"exp\":" + exp + "}");
        String signature = hmacSha256(header + "." + payload, SECRET);
        return header + "." + payload + "." + signature;
    }

    public static String[] parse(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String expectedSig = hmacSha256(parts[0] + "." + parts[1], SECRET);
            if (!expectedSig.equals(parts[2])) return null;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), "UTF-8");
            long exp = Long.parseLong(payload.replaceAll(".*\"exp\":(\\d+).*", "$1"));
            if (System.currentTimeMillis() > exp) return null;

            String userId = payload.replaceAll(".*\"userId\":\"([^\"]+)\".*", "$1");
            String permission = payload.replaceAll(".*\"permission\":(\\d+).*", "$1");
            return new String[]{userId, permission};
        } catch (Exception e) {
            return null;
        }
    }

    private static String base64Encode(String str) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(str.getBytes("UTF-8"));
        } catch (Exception e) { return ""; }
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) { return ""; }
    }
}
