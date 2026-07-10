package ats.mapper;

import ats.dto.upgradepackage.UpgradePackageRequest;
import ats.dto.upgradepackage.UpgradePackageResponse;
import ats.dto.upgradepackage.UpgradePackageUpdateRequest;
import ats.entity.UpgradePackage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UpgradePackageMapper {

    UpgradePackageResponse toDto(UpgradePackage upgradePackage);

    List<UpgradePackageResponse> toDto(List<UpgradePackage> upgradePackages);

    UpgradePackage toEntity(UpgradePackageRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpgradePackageUpdateRequest request, @MappingTarget UpgradePackage upgradePackage);
}
