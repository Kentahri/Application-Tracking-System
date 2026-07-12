package ats.controller;

import ats.dto.vnpay.CallbackResponse;
import ats.dto.vnpay.CreateVnPayRequest;
import ats.dto.vnpay.VNPayResponse;
import ats.http.ApiResponse;
import ats.service.VNPayService;
import ats.service.impl.CandidateUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {

    private final VNPayService vnPayService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/create")
    public ApiResponse<VNPayResponse> create(
            @Valid @RequestBody CreateVnPayRequest req,
            Authentication authentication,
            HttpServletRequest httpReq) {
        CandidateUserDetails principal = (CandidateUserDetails) authentication.getPrincipal();
        Long candidateId = principal.getCandidate().getId();

        log.info(
                "POST /api/v1/vnpay/create - candidateId={}, upgradePackageId={}, bankCode={}",
                candidateId,
                req.getUpgradePackageId(),
                req.getBankCode()
        );

        VNPayResponse response = vnPayService.createVnPayPayment(candidateId, req, httpReq);

        return ApiResponse.<VNPayResponse>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(response)
                .build();
    }

    @GetMapping("/callback")
    public void callback(HttpServletRequest req, HttpServletResponse httpResponse) throws IOException {
        String transactionId = req.getParameter("vnp_TxnRef");
        CallbackResponse callbackResponse = vnPayService.handleCallback(req);

        log.info("VNPay callback processed: transactionId={}, RspCode={}, Message={}",
                transactionId, callbackResponse.getResponseCode(), callbackResponse.getMessage());

        httpResponse.sendRedirect(frontendUrl + "/ats/");
    }

    @GetMapping("/{transactionId}")
    public ApiResponse<VNPayResponse> polling(
            @PathVariable String transactionId,
            Authentication authentication) {
        CandidateUserDetails principal = (CandidateUserDetails) authentication.getPrincipal();

        VNPayResponse response = vnPayService.getByTransactionId(
                principal.getCandidate().getId(), transactionId);

        return ApiResponse.<VNPayResponse>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(response)
                .build();
    }
}
