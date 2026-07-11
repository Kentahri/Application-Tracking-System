package ats.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VNPayUtil {

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new IllegalArgumentException("Key and data must not be null");
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate HMAC SHA512", ex);
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }
            return ipAddress;
        } catch (Exception e) {
            return "Invalid IP:" + e.getMessage();
        }
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String buildPaymentURL(
            Map<String, String> paramsMap,
            boolean encodeValue
    ) {
        return paramsMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue() != null
                                && !entry.getValue().isBlank()
                )
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String key = encode(entry.getKey());
                    String value = encodeValue
                            ? encode(entry.getValue())
                            : entry.getValue();

                    return key + "=" + value;
                })
                .collect(Collectors.joining("&"));
    }

    public static String buildHashData(Map<String, String> paramsMap) {
        return paramsMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue() != null
                                && !entry.getValue().isBlank()
                )
                .sorted(Map.Entry.comparingByKey())
                .map(entry ->
                        entry.getKey()
                                + "="
                                + encode(entry.getValue())
                )
                .collect(Collectors.joining("&"));
    }

    public static String getPaymentURL(Map<String, String> paramsMap, boolean encodeValue) {
        return buildPaymentURL(paramsMap, encodeValue);
    }

    public static boolean verifySignature(
            Map<String, String> params,
            String secretKey
    ) {
        if (params == null || secretKey == null || secretKey.isBlank()) {
            return false;
        }

        String receivedHash = params.get("vnp_SecureHash");

        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> signParams = new TreeMap<>();

        params.forEach((key, value) -> {
            if (key != null
                    && !key.isBlank()
                    && value != null
                    && !value.isBlank()
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)) {

                signParams.put(key, value);
            }
        });

        String hashData = buildHashData(signParams);
        String expectedHash = hmacSHA512(secretKey, hashData);

        return expectedHash.equalsIgnoreCase(receivedHash);
    }
}
