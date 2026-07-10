package ats.dto.upgradepackage;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpgradePackageRequest {

    @NotBlank(message = "{validation.upgradePackage.packageName.required}")
    @Size(max = 255, message = "{validation.upgradePackage.packageName.size}")
    private String packageName;

    @Size(max = 1000, message = "{validation.upgradePackage.description.size}")
    private String description;

    @NotNull(message = "{validation.upgradePackage.price.required}")
    @DecimalMin(value = "0.01", message = "{validation.upgradePackage.price.min}")
    @Digits(integer = 13, fraction = 2, message = "{validation.upgradePackage.price.digits}")
    private BigDecimal price;

    @NotNull(message = "{validation.upgradePackage.numberOfQueryQuota.required}")
    @Min(value = 1, message = "{validation.upgradePackage.numberOfQueryQuota.min}")
    private Integer numberOfQueryQuota;

    @NotNull(message = "{validation.upgradePackage.priority.required}")
    @Min(value = 0, message = "{validation.upgradePackage.priority.min}")
    private Integer priority;

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
