package ats.controller;

import ats.dto.upgradepackage.UpgradePackageRequest;
import ats.dto.upgradepackage.UpgradePackageResponse;
import ats.dto.upgradepackage.UpgradePackageUpdateRequest;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.service.UpgradePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/upgrade-packages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Upgrade Packages", description = "APIs for admin VIP upgrade package management")
public class UpgradePackageController {

    private final UpgradePackageService upgradePackageService;

    @GetMapping
    @Operation(summary = "Get all upgrade packages", description = "Get paginated VIP upgrade packages")
    @ApiResponse(responseCode = "200", description = "Upgrade packages retrieved successfully")
    public PageResponse<UpgradePackageResponse> getAll(@Parameter(description = "Page index, starting from 1")
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @Parameter(description = "Number of records per page")
                                                       @RequestParam(defaultValue = "10") int size) {
        log.debug("REST request to get upgrade packages page: {}, size: {}", page, size);
        return upgradePackageService.getAllUpgradePackages(new PagingRequest(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get upgrade package by id", description = "Get VIP upgrade package detail by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upgrade package retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Upgrade package not found")
    })
    public UpgradePackageResponse getById(@Parameter(description = "Upgrade package id") @PathVariable Long id) {
        log.debug("REST request to get upgrade package by id: {}", id);
        return upgradePackageService.getUpgradePackageById(id);
    }

    @PostMapping
    @Operation(summary = "Create upgrade package", description = "Create a new VIP upgrade package")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upgrade package created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public UpgradePackageResponse create(@Valid @RequestBody UpgradePackageRequest request) {
        log.debug("REST request to create upgrade package: {}", request);
        return upgradePackageService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update upgrade package", description = "Update an existing VIP upgrade package by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upgrade package updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Upgrade package not found")
    })
    public UpgradePackageResponse update(@Parameter(description = "Upgrade package id") @PathVariable Long id,
                                         @Valid @RequestBody UpgradePackageUpdateRequest request) {
        log.debug("REST request to update upgrade package id: {}", id);
        return upgradePackageService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete upgrade package", description = "Soft delete a VIP upgrade package by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upgrade package deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Upgrade package not found")
    })
    public void delete(@Parameter(description = "Upgrade package id") @PathVariable Long id) {
        log.debug("REST request to delete upgrade package: {}", id);
        upgradePackageService.delete(id);
    }
}
