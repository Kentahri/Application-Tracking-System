package ats.service.impl;

import ats.dto.upgradepackage.UpgradePackageRequest;
import ats.dto.upgradepackage.UpgradePackageResponse;
import ats.dto.upgradepackage.UpgradePackageUpdateRequest;
import ats.entity.UpgradePackage;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.helper.MessageHelper;
import ats.http.PageResponse;
import ats.http.PagingRequest;
import ats.mapper.UpgradePackageMapper;
import ats.repository.UpgradePackageRepository;
import ats.service.UpgradePackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UpgradePackageServiceImpl implements UpgradePackageService {

    private final UpgradePackageRepository upgradePackageRepository;
    private final UpgradePackageMapper upgradePackageMapper;

    private String message(String code, Object... args) {
        return MessageHelper.getMessage(code, args);
    }

    private UpgradePackage getUpgradePackageOrThrow(Long id) {
        return upgradePackageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Upgrade package not found with id: {}", id);
                    return new NotFoundException(message("error.upgradePackage.notFound", id));
                });
    }

    @Override
    public PageResponse<UpgradePackageResponse> getAllUpgradePackages(PagingRequest pagingRequest) {
        log.debug("getting upgrade packages page: {}, size: {}", pagingRequest.getPage(), pagingRequest.getSize());

        Page<UpgradePackage> upgradePackages = upgradePackageRepository.findAll(
                pagingRequest.toPageable(Sort.by(Sort.Direction.ASC, "priority").and(Sort.by(Sort.Direction.ASC, "price")))
        );
        return PageResponse.of(upgradePackages.map(upgradePackageMapper::toDto));
    }

    @Override
    public UpgradePackageResponse getUpgradePackageById(Long id) {
        log.debug("getting upgrade package by id: {}", id);
        return upgradePackageMapper.toDto(getUpgradePackageOrThrow(id));
    }

    @Override
    @Transactional
    public UpgradePackageResponse create(UpgradePackageRequest request) {
        validateCreateRequest(request);

        UpgradePackage upgradePackage = upgradePackageMapper.toEntity(request);
        UpgradePackage saved = upgradePackageRepository.save(upgradePackage);

        log.info("created upgrade package with id: {}", saved.getId());
        return upgradePackageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UpgradePackageResponse update(Long id, UpgradePackageUpdateRequest request) {
        UpgradePackage upgradePackage = getUpgradePackageOrThrow(id);
        validateUpdateRequest(id, request);

        upgradePackageMapper.updateEntity(request, upgradePackage);

        log.info("updated upgrade package with id: {}", id);
        return upgradePackageMapper.toDto(upgradePackage);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UpgradePackage upgradePackage = getUpgradePackageOrThrow(id);
        upgradePackageRepository.delete(upgradePackage);
        log.info("deleted upgrade package with id: {}", id);
    }

    private void validateCreateRequest(UpgradePackageRequest request) {
        validatePackageNameForCreate(request.getPackageName());
        validatePrice(request.getPrice());
        validateQuota(request.getNumberOfQueryQuota());
        validatePriority(request.getPriority());
    }

    private void validateUpdateRequest(Long id, UpgradePackageUpdateRequest request) {
        if (request.getPackageName() != null) {
            validatePackageNameForUpdate(id, request.getPackageName());
        }
        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
        }
        if (request.getNumberOfQueryQuota() != null) {
            validateQuota(request.getNumberOfQueryQuota());
        }
        if (request.getPriority() != null) {
            validatePriority(request.getPriority());
        }
    }

    private void validatePackageNameForCreate(String packageName) {
        validatePackageNameNotBlank(packageName);
        if (upgradePackageRepository.existsByPackageName(packageName)) {
            log.warn("Upgrade package name already exists: {}", packageName);
            throw new BadRequestException(message("error.upgradePackage.packageName.exists"));
        }
    }

    private void validatePackageNameForUpdate(Long id, String packageName) {
        validatePackageNameNotBlank(packageName);
        if (upgradePackageRepository.existsByPackageNameAndIdNot(packageName, id)) {
            log.warn("Upgrade package name already exists: {}", packageName);
            throw new BadRequestException(message("error.upgradePackage.packageName.exists"));
        }
    }

    private void validatePackageNameNotBlank(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            throw new BadRequestException(message("error.upgradePackage.packageName.blank"));
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new BadRequestException(message("error.upgradePackage.price.required"));
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(message("error.upgradePackage.price.positive"));
        }
    }

    private void validateQuota(Integer numberOfQueryQuota) {
        if (numberOfQueryQuota == null) {
            throw new BadRequestException(message("error.upgradePackage.numberOfQueryQuota.required"));
        }
        if (numberOfQueryQuota <= 0) {
            throw new BadRequestException(message("error.upgradePackage.numberOfQueryQuota.positive"));
        }
    }

    private void validatePriority(Integer priority) {
        if (priority == null) {
            throw new BadRequestException(message("error.upgradePackage.priority.required"));
        }
        if (priority < 0) {
            throw new BadRequestException(message("error.upgradePackage.priority.negative"));
        }
    }
}
