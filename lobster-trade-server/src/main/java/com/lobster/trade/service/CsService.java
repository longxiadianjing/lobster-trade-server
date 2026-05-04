package com.lobster.trade.service;

import com.lobster.trade.model.request.*;
import com.lobster.trade.model.response.CsSessionVO;

import java.util.List;
import java.util.Map;

public interface CsService {

    CsSessionVO startSession(Long userId, CsStartRequest req);

    CsSessionVO getSession(Long userId, Long sessionId);

    CsSessionVO sendMessage(Long userId, CsMessageRequest req);

    void closeSession(Long userId, Long sessionId);

    List<CsSessionVO> getMySessions(Long userId);

    // ===================== Admin methods =====================

    CsSessionVO getSessionAdmin(Long sessionId);

    CsSessionVO sendMessageAdmin(Long sessionId, String content, String messageType, String attachmentUrl);

    void assignOperator(Long sessionId, Long operatorId, String operatorName);

    void closeSessionAdmin(Long sessionId);

    com.baomidou.mybatisplus.core.metadata.IPage<CsSessionVO> listForAdmin(String keyword, Integer status, int page, int size);

    Map<String, Object> getCsStats();
}
