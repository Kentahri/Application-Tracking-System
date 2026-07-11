package ats.controller;

import ats.dto.vnpay.CallbackResponse;
import ats.dto.vnpay.CreateVnPayRequest;
import ats.dto.vnpay.VNPayResponse;
import ats.http.ApiResponse;
import ats.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    public ApiResponse<VNPayResponse> create(@Valid @RequestBody CreateVnPayRequest req, HttpServletRequest httpReq) {
        log.info(
                "POST /api/v1/vnpay/create - candidateId={}, upgradePackageId={}, bankCode={}",
                req.getCandidateId(),
                req.getUpgradePackageId(),
                req.getBankCode()
        );

        VNPayResponse response = vnPayService.createVnPayPayment(req, httpReq);

        return ApiResponse.<VNPayResponse>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(response)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<CallbackResponse> callback(HttpServletRequest req) {
        log.info("VNPay callback received: query={}", req.getQueryString());
        CallbackResponse response = vnPayService.handleCallback(req);

        log.info("VNPay callback response: RspCode={}, Message={}", response.getResponseCode(), response.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ApiResponse<VNPayResponse> polling(@PathVariable String transactionId) {

        VNPayResponse response = vnPayService.getByTransactionId(transactionId);

        return ApiResponse.<VNPayResponse>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(response)
                .build();
    }
}
