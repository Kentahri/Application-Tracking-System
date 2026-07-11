# VNPay Flow Audit - Application-Tracking-System

> File phân tích luồng thanh toán VNPay đang chạy trong BE_ATS. Mục đích: chỉ ra **tại sao** API `POST /api/v1/vnpay/create` sau khi tạo xong vẫn trả về trạng thái "quá thời gian" (hết hạn thanh toán) dù lần đầu gọi thì OK. Tài liệu đi kèm code nguyên trạng của từng file để tra cứu nhanh.

---

## 1. Sơ đồ luồng hiện tại

```mermaid
flowchart TD
    A[Client POST /api/v1/vnpay/create] --> B[JwtFilter - permitAll]
    B --> C[VNPayController.create]
    C --> D[VNPayServiceImpl.createVnPayPayment]
    D --> E1[Lookup Candidate]
    D --> E2[Lookup UpgradePackage]
    D --> E3[Build params + ExpireDate = now + 30min]
    D --> E4[Hash SHA512 -> paymentUrl]
    D --> F[Save Payment PENDING]
    F --> G[Return paymentUrl]

    H[VNPay Sandbox] -->|redirect| I[VNPayController.return GET /api/v1/vnpay/return]
    H -->|IPN| J[VNPayController.ipn GET /api/v1/vnpay/ipn]

    I --> K[Redirect FE localhost:3000/payment-return]
    J --> L[VNPayServiceImpl.handleIPN]
    L --> M[Verify signature + amount]
    M --> N[Update Payment SUCCESS]

    O[FE polling GET /api/v1/vnpay/{txnRef}] --> P[VNPayServiceImpl.getByTransactionId]
    P --> Q[Find Payment in DB]
    Q -->|NOT FOUND -> 404| R[FE timeout -> 'het han']
    Q -->|FOUND PENDING| S[FE tiep tuc polling]
```

## 2. Các file liên quan (đường dẫn tuyệt đối)

- Controller: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\vnpay\controller\VNPayController.java`
- Service impl: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\vnpay\service\impl\VNPayServiceImpl.java`
- Service interface: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\vnpay\service\VNPayService.java`
- DTO request: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\dto\vnpay\CreateVnPayRequest.java`
- DTO response: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\dto\vnpay\VNPayResponse.java`
- DTO IPN: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\dto\vnpay\IPNResponse.java`
- VNPayUtil (vnpay): `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\util\VNPayUtil.java`
- VNPayUtil (cu - payment): `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\payment\VNPayUtil.java`
- VNPayConfig (cu - dang dung): `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\payment\VNPayConfig.java`
- VNPAYConfig (moi - KHONG dung): `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\config\VNPAYConfig.java`
- application.yaml: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\resources\application.yaml`
- .env: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\.env`
- SecurityConfig: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\config\SecurityConfig.java`
- JwtFilter: `E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\config\JwtFilter.java`
- Demo (khong loi): `E:\java_demo\vnpay-demo\src\main\java\com\example\vnpay\service\impl\VNPayServiceImpl.java`

## 3. Code nguyên trạng (snapshot)

### 3.1. Controller - VNPayController.java

```java
// E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\vnpay\controller\VNPayController.java
package ats.vnpay.controller;

import ats.dto.vnpay.CreateVnPayRequest;
import ats.dto.vnpay.IPNResponse;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    public ApiResponse<VNPayResponse> create(
            @Valid @RequestBody CreateVnPayRequest req,
            HttpServletRequest httpReq) {
        log.info("POST /api/v1/vnpay/create - candidateId={}, upgradePackageId={}, bankCode={}",
                req.getCandidateId(), req.getUpgradePackageId(), req.getBankCode());
        VNPayResponse response = vnPayService.createVnPayPayment(req, httpReq);
        return ApiResponse.<VNPayResponse>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(response)
                .build();
    }

    @GetMapping("/return")
    public void returnUrl(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String txnRef = req.getParameter("vnp_TxnRef");
        String responseCode = req.getParameter("vnp_ResponseCode");
        log.info("VNPay return: txnRef={}, responseCode={}", txnRef, responseCode);

        String feUrl = "http://localhost:3000/payment-return";
        String redirectUrl = feUrl + "?txnRef="
                + URLEncoder.encode(txnRef != null ? txnRef : "", StandardCharsets.UTF_8)
                + "&status=" + URLEncoder.encode(responseCode != null ? responseCode : "", StandardCharsets.UTF_8);

        resp.sendRedirect(redirectUrl);
    }

    @GetMapping(value = "/ipn", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> ipn(HttpServletRequest req) {
        log.info("VNPay IPN: received callback");
        IPNResponse ipnResponse = vnPayService.handleIPN(req);
        String body = "RspCode=" + ipnResponse.getResponseCode()
                + "&Message=" + URLEncoder.encode(ipnResponse.getMessage(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{transactionId}")
    public ApiResponse<VNPayResponse> polling(@PathVariable String transactionId) {
        log.info("GET /api/v1/vnpay/{} - polling status", transactionId);
        VNPayResponse response = vnPayService.getByTransactionId(transactionId);
        return ApiResponse.<VNPayResponse>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(response)
                .build();
    }
}
```

### 3.2. Service - VNPayServiceImpl.java (luong moi ats.vnpay)

```java
// E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\java\ats\vnpay\service\impl\VNPayServiceImpl.java
package ats.vnpay.service.impl;

// ... import ...

import ats.config.VNPayConfig;        // <-- (1) DANG INJECT BEAN CU, KHONG PHAI VNPAYConfig MOI
import ats.util.VNPayUtil;              // <-- dung util moi nhung goi buildPaymentURL khong consistent

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final PaymentRepository paymentRepository;
    private final CandidateRepository candidateRepository;
    private final UpgradePackageRepository upgradePackageRepository;
    private final VNPayConfig vnPayConfig;  // ats.config.VNPayConfig (cu)

    @Override
    @Transactional
    public VNPayResponse createVnPayPayment(CreateVnPayRequest req, HttpServletRequest httpReq) {
        // ...
        String txnRef = VNPayUtil.getRandomNumber(8);                 // (2) random 8 chu so - xung dot cao
        String orderId = "ATS" + System.currentTimeMillis();
        String orderInfo = "Thanh toan goi " + upgradePackage.getPackageName();

        long amountMinor = upgradePackage.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        Map<String, String> params = buildParams(txnRef, orderInfo, amountMinor, req.getBankCode(), httpReq);

        String hashData = VNPayUtil.buildHashData(params);
        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        String queryUrl = VNPayUtil.buildPaymentURL(params, true);
        String paymentUrl = vnPayConfig.getVnpPayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
        // ...
    }

    private Map<String, String> buildParams(String txnRef, String orderInfo, long amountMinor,
                                            String bankCode, HttpServletRequest httpReq) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnPayConfig.getVnpVersion());
        // ...
        params.put("vnp_ExpireDate", buildExpireDate());   // (3) +30 phut - QUA NGAN
        params.put("vnp_CreateDate", buildCreateDate());
        params.put("vnp_Amount", String.valueOf(amountMinor));
        params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpReq));  // (4) X-FORWARDED-FOR khong split
        // ...
    }

    private String buildExpireDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fmt.setTimeZone(TimeZone.getTimeZone("Etc/GMT+7"));
        cal.add(Calendar.MINUTE, 30);                       // (3)
        return fmt.format(cal.getTime());
    }
}
```

### 3.3. application.yaml (doan payment)

```yaml
# E:\Project_web\ATS_WEB\BE_ATS\Application-Tracking-System\src\main\resources\application.yaml
payment:
  vnPay:
    url: ${PAY_URL}
    tmnCode: ${TMN_CODE}
    secretKey: ${SECRET_KEY}
    returnUrl: ${VNPAY_RETURN_URL}    # (5) bien moi, KHONG phai RETURN_URL
    version: ${VERSION}
    command: ${COMMAND}
    orderType: ${ORDER_TYPE}
  vn-pay:                              # (6) block moi nhung KHONG co bean nao doc
    pay-url: ${VNPAY_PAY_URL}
    tmn-code: ${VNPAY_TMN_CODE}
    secret-key: ${VNPAY_SECRET_KEY}
    return-url: ${VNPAY_RETURN_URL}
    ipn-url: ${VNPAY_IPN_URL}
    fe-return-url: ${FE_RETURN_URL}
    version: ${VNPAY_VERSION:2.1.0}
    command: ${VNPAY_COMMAND:pay}
    order-type: ${VNPAY_ORDER_TYPE:other}
```

### 3.4. .env (cac key lien quan VNPay)

```
PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
TMN_CODE=IY83LZDY
SECRET_KEY=CWZ5054QH6QSEX9JOD6NEML5L8BCGD6T
RETURN_URL=http://localhost:8080/api/v1/payment/vn-pay-callback    # KHONG dung trong vnpay
VERSION=2.1.0
COMMAND=pay
ORDER_TYPE=other

VNPAY_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_TMN_CODE=IY83LZDY
VNPAY_SECRET_KEY=CWZ5054QH6QSEX9JOD6NEML5L8BCGD6T
VNPAY_RETURN_URL=http://localhost:8080/api/v1/vnpay/return
VNPAY_IPN_URL=http://localhost:8080/api/v1/vnpay/ipn
FE_RETURN_URL=http://localhost:3000/payment-return
VNPAY_VERSION=2.1.0
VNPAY_COMMAND=pay
VNPAY_ORDER_TYPE=other
```

### 3.5. VNPayUtil.java (ats/util/VNPayUtil.java)

```java
public static String buildPaymentURL(Map<String, String> paramsMap, boolean encodeValue) {
    return paramsMap.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                return URLEncoder.encode(key, StandardCharsets.US_ASCII) + "="   // (7) US_ASCII
                        + (encodeValue
                        ? URLEncoder.encode(value, StandardCharsets.US_ASCII)
                        : value);
            })
            .collect(Collectors.joining("&"));
}

public static String getIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-FORWARDED-FOR");
    if (ipAddress == null || ipAddress.isBlank()) {
        ipAddress = request.getRemoteAddr();
    }
    return ipAddress;                                                  // (4) tra ca chuoi "client, proxy1, proxy2"
}
```

### 3.6. SecurityConfig - cho phep cac endpoint VNPay

```java
.requestMatchers(HttpMethod.GET, "/api/v1/vnpay/**").permitAll()
.requestMatchers(HttpMethod.POST, "/api/v1/vnpay/create").permitAll()
```

### 3.7. JwtFilter

```java
private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
        "/api/auth", "/swagger-ui", "/v3/api-docs"
);
// NOTE: "/api/v1/vnpay" KHONG co o day, nhung SecurityConfig da permitAll nen khong bi chan.
```

## 4. Root cause: vi sao "qua thoi gian"?

**Tong ket:** Loi "Giao dich het han" (responseCode `11` / status PENDING vinh vien) xay ra do **6 nguyen nhan dong thoi**, khong phai loi cau hinh don le.

### Bug #1 - `vnp_ExpireDate` chi +30 phut (nguyen nhan chinh)

File `VNPayServiceImpl.buildExpireDate()`:

```java
cal.add(Calendar.MINUTE, 30);
```

Trong khi sandbox VNPay **mac dinh 15 phut**. Khi `ExpireDate > CreateDate + 15p`:
- VNPay sandbox chap nhan nhung **doi khi tra ve `vnp_ResponseCode = 11` (het han)** neu phien sandbox cua ban dang trong cuoc kiem tra.
- Quan trong hon: khi FE mo paymentUrl sau >15 phut (debug, sua code,...) → sandbox tra `Expired`.
- **So sanh voi demo `vnpay-demo/VNPayServiceImpl` cung +30 phut, nhung demo chay test ngay lap tuc nen khong thay loi**. Trong ATS, ban polling nhieu lan (IPN cham, FE doi redirect) → tran qua 30p.

### Bug #2 - Bean `vnPayConfig` inject NHAM file cu

```java
// VNPayServiceImpl dong 12:
```

Trong khi bean moi nhat o `ats.config.VNPAYConfig` (file `VNPAYConfig.java`) lai doc cac key `payment.vn-pay.*`. Hai bean cung ton tai, service dang dung bean cu (doc `payment.vnPay.*`) → `payment.vn-pay.*` bi bo qua hoan toan.

### Bug #3 - `payment.vnPay.returnUrl` tro nham bien moi

Trong `application.yaml`:

```yaml
returnUrl: ${VNPAY_RETURN_URL}    # bien cua luong moi
```

Trong khi `RETURN_URL` moi (cu, gia tri `http://localhost:8080/api/v1/payment/vn-pay-callback`) **khong duoc dung** trong luong `ats.vnpay`. Nen neu ban sua `.env` bang cach them `RETURN_URL=http://localhost:8080/api/v1/vnpay/return` → moi thu se dung, nguoc lai `payment.vnPay.returnUrl` hien tai nhan gia tri **tu bien moi** nen thuc te van chay, nhung de vo cung rooi.

### Bug #4 - `ddl-auto: create` xoa bang Payment khi restart

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto:  create
```

Khi restart server (debug sua code), bang `payment` bi **drop + recreate** → cac Payment PENDING truoc do bien mat. Polling `GET /api/v1/vnpay/{txnRef}` → 404 → FE timeout → hien thi "qua thoi gian".

### Bug #5 - `X-FORWARDED-FOR` khong split

`getIpAddress` tra ve nguyen chuoi `client, proxy1, proxy2` → VNPay sandbox validate IP fail → tra `vnp_ResponseCode = 11` ngay lan redirect dau.

### Bug #6 - `buildPaymentURL` encode US_ASCII

```java
URLEncoder.encode(value, StandardCharsets.US_ASCII)
```

Khi `vnp_OrderInfo` co dau tieng Viet (vi du `"Thanh toán gói"`), ky tu se bi bien thanh `?`. Sandbox reject. Hien tai ban dang de `"Thanh toan goi "` (khong dau) → khong phai nguyen nhan chinh nhung la **noi bom**.

## 5. So sanh voi vnpay-demo (tai sao demo OK)

Demo `E:\java_demo\vnpay-demo\src\main\java\com\example\vnpay\service\impl\VNPayServiceImpl.java` **giong het** (cung `buildExpireDate` +30p, cung `buildPaymentURL` US_ASCII, cung inject bean `VNPAYConfig` cu). Demo OK vi:

1. Demo tao request → mo trinh duyet → thanh toan ngay → redirect FE ngay.
2. Demo khong polling, khong co IPN thuc su (sandbox goi IPN nhung demo khong co DB → return 404).
3. Demo khong restart server giua chung.
4. Demo test 1 user, khong xung dot `txnRef` 8 chu so.

Trong ATS, ban co DB that, IPN that, FE polling that → cac bug #1, #4, #5 bieu hien manh.

## 6. De xuat sua (tom tat)

1. **Giam `vnp_ExpireDate` ve +10 phut** (sandbox chap nhan toi da 15p, giu khoang dem).
2. **Chuyen `import ats.config.VNPayConfig` → `ats.config.VNPAYConfig`** trong `VNPayServiceImpl` (dong 12, 40).
3. **Bo bean `ats.config.VNPayConfig` va `ats.util.VNPayUtil`** neu khong dung (hoac doi ten, doi package de tranh nham).
4. **Sua `application.yaml`**: `payment.vnPay.returnUrl: ${RETURN_URL}` (them bien moi trong `.env`).
5. **Sua `ddl-auto: create` → `update` hoac `validate`** (giam sai DB).
6. **Sua `getIpAddress`**: tach chuoi theo `,`, lay phan tu dau, trim.
7. **Sua `buildPaymentURL`**: dung `URLEncoder.encode(value, StandardCharsets.UTF_8)` (VNPay sandbox dung UTF-8).
8. **Tang `txnRef` random tu 8 len 12 chu so** de tranh xung dot.

## 7. Verify nhanh (checklist thu cong)

- [ ] `curl -X POST http://localhost:8080/api/v1/vnpay/create -H "Content-Type: application/json" -d '{"candidateId":1,"upgradePackageId":1}'` → tra `paymentUrl`
- [ ] Copy `paymentUrl`, paste vao trinh duyet → sandbox VNPay mo len, KHONG bao loi "het han"
- [ ] Click thanh toan (dung the test 4200000000000000 / 12/30 / 123)
- [ ] Quay lai `http://localhost:3000/payment-return?txnRef=...` → FE goi polling → Payment status = SUCCESS
- [ ] Kiem tra DB `SELECT * FROM payment WHERE transaction_id = ?` → status = 'SUCCESS', paid_at khac NULL