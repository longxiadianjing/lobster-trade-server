package com.lobster.trade.controller.admin;

import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.ServiceProviderCertification;
import com.lobster.trade.service.CertificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/certification")
@RequiredArgsConstructor
public class AdminCertificationController {

    private final CertificationService certificationService;

    @GetMapping("/pending")
    public Result<List<ServiceProviderCertification>> pendingList() {
        return Result.success(certificationService.getPendingCertifications());
    }

    @PostMapping("/review")
    public Result<String> review(@RequestBody CertificationReviewRequest req) {
        certificationService.reviewCertification(req.getCertId(), req.getStatus(), req.getRejectReason(), req.getProviderLevel());
        return Result.success("审核完成");
    }

    @Data
    public static class CertificationReviewRequest {
        private Long certId;
        private Integer status;
        private String rejectReason;
        private Integer providerLevel;
    }
}
