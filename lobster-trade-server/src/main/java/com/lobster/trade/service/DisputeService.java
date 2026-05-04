package com.lobster.trade.service;

import com.lobster.trade.model.request.DisputeRequest;

import java.util.Map;

public interface DisputeService {

    void createDispute(Long userId, DisputeRequest req);

    void createDispute(Long userId, String orderIdStr, String reason, String description, String images);

    void resolveDispute(Long adminId, Long orderId, String result);

    void cancelDispute(Long userId, Long orderId);

    long countPending();

    long countTotal();

    Map<String, Object> getDisputeDetail(Long orderId);
}
