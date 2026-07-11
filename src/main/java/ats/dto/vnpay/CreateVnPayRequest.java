package ats.dto.vnpay;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVnPayRequest {

    @NotNull(message = "{validation.vnpay.candidateId.null}")
    private Long candidateId;

    @NotNull(message = "{validation.vnpay.upgradePackageId.null}")
    private Long upgradePackageId;

    @Size(max = 50, message = "{validation.vnpay.bankCode.size}")
    private String bankCode;
}
