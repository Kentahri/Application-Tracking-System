package ats.dto.upgradepackage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpgradePackageResponse {
    private Long id;
    private String packageName;
    private String description;
    private BigDecimal price;
    private Integer numberOfQueryQuota;
    private Integer priority;
}
