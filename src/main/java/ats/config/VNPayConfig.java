package ats.config;

import ats.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class VNPayConfig {

    @Getter
    @Value("${payment.vn-pay.pay-url}")
    private String vnpPayUrl;

    @Getter
    @Value("${payment.vn-pay.vnp-callback-url}")
    private String vnpCallbackUrl;

    @Getter
    @Value("${payment.vn-pay.tmn-code}")
    private String vnpTmnCode;

    @Getter
    @Value("${payment.vn-pay.secret-key}")
    private String secretKey;

    @Getter
    @Value("${payment.vn-pay.version}")
    private String vnpVersion;

    @Getter
    @Value("${payment.vn-pay.command}")
    private String vnpCommand;

    @Getter
    @Value("${payment.vn-pay.order-type}")
    private String orderType;

    public Map<String, String> getVNPayConfig(HttpServletRequest request) {
        return getVNPayConfigImpl(request);
    }

    public Map<String, String> getVNPayConfig() {
        return getVNPayConfigImpl(null);
    }

    private Map<String, String> getVNPayConfigImpl(HttpServletRequest request) {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", this.vnpVersion);
        vnpParamsMap.put("vnp_Command", this.vnpCommand);
        vnpParamsMap.put("vnp_TmnCode", this.vnpTmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef", VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderType", this.orderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", this.vnpCallbackUrl);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);

        calendar.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_ExpireDate", vnpExpireDate);

        if (request != null) {
            String clientIp = VNPayUtil.getIpAddress(request);
            if (clientIp != null) {
                vnpParamsMap.put("vnp_IpAddr", clientIp);
            }
        }

        return vnpParamsMap;
    }
}
