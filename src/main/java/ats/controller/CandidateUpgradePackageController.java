package ats.controller;

import ats.dto.upgradepackage.UpgradePackageResponse;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.service.UpgradePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidate/upgrade-packages")
@RequiredArgsConstructor
public class CandidateUpgradePackageController {

    private final UpgradePackageService upgradePackageService;

    @GetMapping
    public PageResponse<UpgradePackageResponse> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return upgradePackageService.getAllUpgradePackages(new PagingRequest(page, size));
    }
}
