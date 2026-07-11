package ats.dto.vnpay;

import ats.constant.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayResponse {

    private String code;
    private String message;
    private String paymentUrl;
    private String transactionId;
    private PaymentStatus status;
}
