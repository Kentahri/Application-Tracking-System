package ats.service.impl;

import ats.constant.PaymentStatus;
import ats.dto.vnpay.CallbackResponse;
import ats.dto.vnpay.CreateVnPayRequest;
import ats.dto.vnpay.VNPayResponse;
import ats.entity.Application;
import ats.entity.Candidate;
import ats.entity.Payment;
import ats.entity.UpgradePackage;
import ats.exception.NotFoundException;
import ats.helper.MessageHelper;
import ats.config.VNPayConfig;
import ats.repository.ApplicationRepository;
import ats.repository.CandidateRepository;
import ats.repository.PaymentRepository;
import ats.repository.UpgradePackageRepository;
import ats.util.VNPayUtil;
import ats.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final long PAYMENT_TIMEOUT_MINUTES = 15;

    private final PaymentRepository paymentRepository;
    private final CandidateRepository candidateRepository;
    private final UpgradePackageRepository upgradePackageRepository;
    private final ApplicationRepository applicationRepository;
    private final VNPayConfig vnPayConfig;
    private final VNPayUtil vnPayUtil;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    @Override
    @Transactional
    public VNPayResponse createVnPayPayment(CreateVnPayRequest req, HttpServletRequest httpReq) {
        log.info("VNPay: creating payment for candidateId={}, upgradePackageId={}",
                req.getCandidateId(), req.getUpgradePackageId());

        Candidate candidate = candidateRepository.findById(req.getCandidateId())
                .orElseThrow(() -> new NotFoundException(message("candidate.notFound", req.getCandidateId())));

        UpgradePackage upgradePackage = upgradePackageRepository.findById(req.getUpgradePackageId())
                .orElseThrow(
                        () -> new NotFoundException(message("upgradePackage.notFound", req.getUpgradePackageId())));

        String txnRef = vnPayUtil.getRandomNumber(8);
        String orderId = "ATS" + System.currentTimeMillis();
        String orderInfo = "Thanh toan goi " + upgradePackage.getPackageName();

        long amountMinor = upgradePackage.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        Map<String, String> params = buildParams(txnRef, orderInfo, amountMinor, req.getBankCode(), httpReq);

        String hashData = vnPayUtil.buildHashData(params);

        String secureHash = vnPayUtil.hmacSHA512(
                vnPayConfig.getSecretKey(),
                hashData
        );

        String queryUrl = vnPayUtil.buildPaymentURL(params, true);

        String paymentUrl = vnPayConfig.getVnpPayUrl()
                + "?"
                + queryUrl
                + "&vnp_SecureHash="
                + secureHash;

        Payment payment = Payment.builder()
                .transactionId(txnRef)
                .orderId(orderId)
                .amount(upgradePackage.getPrice())
                .bankCode(req.getBankCode())
                .paymentMethod("VNPAY")
                .status(PaymentStatus.PENDING)
                .candidate(candidate)
                .upgradePackage(upgradePackage)
                .build();
        paymentRepository.save(payment);

        log.info("VNPay: payment created txnRef={}, orderId={}, amountMinor={}", txnRef, orderId, amountMinor);

        return VNPayResponse.builder()
                .code("00")
                .message("Success")
                .paymentUrl(paymentUrl)
                .transactionId(txnRef)
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Override
    @Transactional
    public CallbackResponse handleCallback(HttpServletRequest httpReq) {
        Map<String, String> params = extractAll(httpReq);

        String txnRef = params.get("vnp_TxnRef");

        if (!VNPayUtil.verifySignature(params, vnPayConfig.getSecretKey())) {
            log.warn("VNPay callback: invalid signature for txnRef={}", txnRef);
            return new CallbackResponse("97", "Invalid Signature");
        }

        Payment payment = paymentRepository.findByTransactionIdAndIsDeletedFalse(txnRef).orElse(null);
        if (payment == null) {
            log.warn("VNPay callback: payment not found with txnRef={}", txnRef);
            return new CallbackResponse("01", "Order not found");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("VNPay callback: payment already SUCCESS for txnRef={}", txnRef);
            return new CallbackResponse("00", "Confirm Success");
        }

        long expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
        long incomingAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        if (expectedAmount != incomingAmount) {
            log.warn("VNPay callback: amount mismatch txnRef={}, expected={}, incoming={}",
                    txnRef, expectedAmount, incomingAmount);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setVnpResponseCode("04");
            paymentRepository.save(payment);
            return new CallbackResponse("04", "Invalid Amount");
        }

        String responseCode = params.get("vnp_ResponseCode");
        payment.setVnpResponseCode(responseCode);
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpSecureHash(params.get("vnp_SecureHash"));
        payment.setBankCode(params.get("vnp_BankCode"));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now(VIETNAM_ZONE));

            Candidate candidate = payment.getCandidate();
            UpgradePackage pkg = payment.getUpgradePackage();
            if (candidate != null && pkg != null) {
                int currentQuota = candidate.getNumberOfQueryQuota() != null ? candidate.getNumberOfQueryQuota() : 0;
                candidate.setNumberOfQueryQuota(currentQuota + pkg.getNumberOfQueryQuota());
                candidate.setUpgradePackageId(pkg);
                candidateRepository.save(candidate);

                List<Application> applications = applicationRepository.findAllByCandidateId(candidate);
                for (Application app : applications) {
                    app.setPriority(pkg.getPriority());
                }
                applicationRepository.saveAll(applications);
                log.info("VNPay callback: candidate {} quota updated +{}, priority updated to {} for {} applications",
                        candidate.getId(), pkg.getNumberOfQueryQuota(), pkg.getPriority(), applications.size());
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        log.info("VNPay callback: processed txnRef={}, status={}, responseCode={}",
                txnRef, payment.getStatus(), responseCode);

        return new CallbackResponse("00", "Confirm Success");
    }

    @Override
    @Transactional(readOnly = true)
    public VNPayResponse getByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionIdAndIsDeletedFalse(transactionId)
                .orElseThrow(() -> new NotFoundException(
                        message("payment.notFound", transactionId)));
        return VNPayResponse.builder()
                .code("00")
                .message("Success")
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .build();
    }

    private Map<String, String> buildParams(
            String txnRef,
            String orderInfo,
            long amountMinor,
            String bankCode,
            HttpServletRequest httpReq
    ) {
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);

        String createDate = now.format(VNPAY_DATE_FORMAT);
        String expireDate = now.plusMinutes(PAYMENT_TIMEOUT_MINUTES)
                .format(VNPAY_DATE_FORMAT);

        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", vnPayConfig.getVnpVersion());
        params.put("vnp_Command", vnPayConfig.getVnpCommand());
        params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", vnPayConfig.getOrderType());
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getVnpCallbackUrl());

        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        params.put("vnp_Amount", String.valueOf(amountMinor));
        params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpReq));

        if (bankCode != null && !bankCode.isBlank()) {
            params.put("vnp_BankCode", bankCode.trim());
        }

        log.info(
                "VNPay time: txnRef={}, createDate={}, expireDate={}, zone={}",
                txnRef,
                createDate,
                expireDate,
                VIETNAM_ZONE
        );

        return params;
    }

    private Map<String, String> extractAll(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(name -> map.put(name, request.getParameter(name)));
        return map;
    }
}
