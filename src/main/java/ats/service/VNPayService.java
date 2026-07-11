package ats.service;

import ats.dto.vnpay.CallbackResponse;
import ats.dto.vnpay.CreateVnPayRequest;
import ats.dto.vnpay.VNPayResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {

    VNPayResponse createVnPayPayment(CreateVnPayRequest req, HttpServletRequest httpReq);

    CallbackResponse handleCallback(HttpServletRequest httpReq);

    VNPayResponse getByTransactionId(String transactionId);
}
