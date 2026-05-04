package com.lobster.trade.controller;

import com.lobster.trade.model.entity.ServiceProviderCertification;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.CertificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/certification")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * 用户申请服务商认证
     * POST /api/certification/apply
     */
    @PostMapping("/apply")
    public ApiResponse<Void> apply(HttpServletRequest request,
                                    @RequestBody CertificationApplyRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.fail(401, "请先登录");
        }
        certificationService.applyCertification(
            userId,
            req.getCertType(),
            req.getGameId(),
            req.getDescription(),
            req.getRegions(),
            req.getHourlyRate(),
            req.getProviderLevel(),
            req.getCredentials()
        );
        return ApiResponse.success("认证申请已提交，请等待审核");
    }

    /**
     * 获取当前用户的认证状态
     * GET /api/certification/my
     */
    @GetMapping("/my")
    public ApiResponse<ServiceProviderCertification> myCertification(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(certificationService.getUserCertification(userId));
    }

    /**
     * 获取认证服务商列表（前端展示）
     * GET /api/certification/list?certType=boost&gameId=1
     */
    @GetMapping("/list")
    public ApiResponse<List<ServiceProviderCertification>> list(
            @RequestParam(required = false) String certType,
            @RequestParam(required = false) Long gameId) {
        return ApiResponse.success(
            certificationService.getCertifiedProviders(certType, gameId)
        );
    }

    /**
     * 获取所有待审核的认证申请（管理员）
     * GET /api/certification/admin/pending
     */
    @GetMapping("/admin/pending")
    public ApiResponse<List<ServiceProviderCertification>> pendingList() {
        return ApiResponse.success(certificationService.getPendingCertifications());
    }

    /**
     * 审核认证申请（管理员）
     * POST /api/certification/admin/review
     */
    @PostMapping("/admin/review")
    public ApiResponse<Void> review(@RequestBody CertificationReviewRequest req) {
        certificationService.reviewCertification(req.getCertId(), req.getStatus(), req.getRejectReason(), req.getProviderLevel());
        return ApiResponse.success("审核完成");
    }

    @Data
    public static class CertificationApplyRequest {
        private String certType;      // boost / accompany / studio
        private Long gameId;
        private String description;
        private String regions;
        private BigDecimal hourlyRate;
        private Integer providerLevel; // 1普通 2铜牌 3银牌 4金牌
        private String credentials;   // JSON array of image URLs
    }

    @Data
    public static class CertificationReviewRequest {
        private Long certId;
        private Integer status;        // 1通过 2拒绝
        private String rejectReason;
        private Integer providerLevel; // 1普通 2铜牌 3银牌 4金牌
    }
}
