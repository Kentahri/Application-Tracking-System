package ats.service;

import ats.dto.upgradepackage.UpgradePackageRequest;
import ats.dto.upgradepackage.UpgradePackageResponse;
import ats.dto.upgradepackage.UpgradePackageUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;

public interface UpgradePackageService {

    PageResponse<UpgradePackageResponse> getAllUpgradePackages(PagingRequest pagingRequest);

    UpgradePackageResponse getUpgradePackageById(Long id);

    UpgradePackageResponse create(UpgradePackageRequest request);

    UpgradePackageResponse update(Long id, UpgradePackageUpdateRequest request);

    void delete(Long id);
}
