package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.*;
import com.lobster.trade.model.entity.*;
import com.lobster.trade.model.request.CsMessageRequest;
import com.lobster.trade.model.request.CsStartRequest;
import com.lobster.trade.model.response.CsSessionVO;
import com.lobster.trade.service.CsService;
import com.lobster.trade.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsServiceImpl implements CsService {

    private final CsSessionMapper csSessionMapper;
    private final CsMessageMapper csMessageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CsSessionVO startSession(Long userId, CsStartRequest req) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        CsSession session = new CsSession();
        session.setSessionNo("CS" + SnowflakeIdUtil.generateOrderNo());
        session.setCustomerId(userId);
        if (req.getOrderId() != null) session.setOrderId(req.getOrderId());
        if (req.getSubject() != null) session.setSubject(req.getSubject());
        session.setPriority(req.getPriority() != null ? req.getPriority() : 0);
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        csSessionMapper.insert(session);
        return buildSessionVO(session);
    }

    @Override
    public CsSessionVO getSession(Long userId, Long sessionId) {
        CsSession session = csSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        if (!session.getCustomerId().equals(userId) && !userId.equals(session.getOperatorId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无权限查看此会话");
        }
        return buildSessionVO(session);
    }

    @Override
    @Transactional
    public CsSessionVO sendMessage(Long userId, CsMessageRequest req) {
        CsSession session = csSessionMapper.selectById(req.getSessionId());
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        CsMessage message = new CsMessage();
        message.setSessionId(req.getSessionId());
        message.setSenderId(userId);
        message.setSenderType(userId.equals(1L) ? 1 : 0);
        message.setContent(req.getContent());
        message.setMessageType(req.getMessageType() != null ? req.getMessageType() : "text");
        message.setAttachmentUrl(req.getAttachmentUrl());
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        csMessageMapper.insert(message);

        if (session.getStatus() == 0) {
            session.setStatus(1);
            session.setUpdateTime(LocalDateTime.now());
            csSessionMapper.updateById(session);
        }
        return buildSessionVO(session);
    }

    @Override
    @Transactional
    public void closeSession(Long userId, Long sessionId) {
        CsSession session = csSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        if (!session.getCustomerId().equals(userId) && !userId.equals(session.getOperatorId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无权限关闭此会话");
        }
        session.setStatus(2);
        session.setCloseTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        csSessionMapper.updateById(session);
    }

    @Override
    public List<CsSessionVO> getMySessions(Long userId) {
        LambdaQueryWrapper<CsSession> query = new LambdaQueryWrapper<>();
        query.eq(CsSession::getCustomerId, userId).eq(CsSession::getIsDeleted, 0).orderByDesc(CsSession::getCreateTime);
        List<CsSession> sessions = csSessionMapper.selectList(query);
        if (sessions == null || sessions.isEmpty()) return new ArrayList<>();
        return sessions.stream().map(this::buildSessionVO).collect(Collectors.toList());
    }

    // ===================== Admin methods =====================

    @Override
    public CsSessionVO getSessionAdmin(Long sessionId) {
        CsSession session = csSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        return buildSessionVO(session);
    }

    @Override
    @Transactional
    public CsSessionVO sendMessageAdmin(Long sessionId, String content, String messageType, String attachmentUrl) {
        CsSession session = csSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        CsMessage message = new CsMessage();
        message.setSessionId(sessionId);
        message.setSenderId(1L); // admin userId = 1
        message.setSenderType(1); // 1 = 客服
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setAttachmentUrl(attachmentUrl);
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        csMessageMapper.insert(message);

        if (session.getStatus() == 0) {
            session.setStatus(1);
            session.setUpdateTime(LocalDateTime.now());
            csSessionMapper.updateById(session);
        }
        return buildSessionVO(session);
    }

    @Override
    @Transactional
    public void assignOperator(Long sessionId, Long operatorId, String operatorName) {
        CsSession session = csSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        session.setOperatorId(operatorId);
        session.setHandlerName(operatorName);
        session.setStatus(1);
        session.setUpdateTime(LocalDateTime.now());
        csSessionMapper.updateById(session);
    }

    @Override
    @Transactional
    public void closeSessionAdmin(Long sessionId) {
        CsSession session = csSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        session.setStatus(2);
        session.setCloseTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        csSessionMapper.updateById(session);
    }

    @Override
    public IPage<CsSessionVO> listForAdmin(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<CsSession> query = new LambdaQueryWrapper<>();
        query.eq(status != null, CsSession::getStatus, status)
              .eq(CsSession::getIsDeleted, 0)
              .orderByDesc(CsSession::getCreateTime);
        Page<CsSession> pageResult = csSessionMapper.selectPage(new Page<>(page, size), query);
        Page<CsSessionVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<CsSessionVO> records = pageResult.getRecords().stream().map(this::buildSessionVO).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public Map<String, Object> getCsStats() {
        long total = csSessionMapper.selectCount(new LambdaQueryWrapper<CsSession>().eq(CsSession::getIsDeleted, 0));
        long waiting = csSessionMapper.selectCount(new LambdaQueryWrapper<CsSession>().eq(CsSession::getIsDeleted, 0).eq(CsSession::getStatus, 0));
        long active = csSessionMapper.selectCount(new LambdaQueryWrapper<CsSession>().eq(CsSession::getIsDeleted, 0).eq(CsSession::getStatus, 1));
        long closed = csSessionMapper.selectCount(new LambdaQueryWrapper<CsSession>().eq(CsSession::getIsDeleted, 0).eq(CsSession::getStatus, 2));
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("waiting", waiting);
        stats.put("active", active);
        stats.put("closed", closed);
        return stats;
    }

    // ===================== Private helpers =====================

    private CsSessionVO buildSessionVO(CsSession session) {
        CsSessionVO vo = new CsSessionVO();
        vo.setId(session.getId());
        vo.setSessionNo(session.getSessionNo());
        vo.setCustomerId(session.getCustomerId());
        vo.setOperatorId(session.getOperatorId());
        vo.setOrderId(session.getOrderId());
        vo.setSubject(session.getSubject());
        vo.setStatus(session.getStatus());
        vo.setPriority(session.getPriority());
        vo.setCreateTime(session.getCreateTime());
        vo.setCloseTime(session.getCloseTime());
        vo.setHandlerName(session.getHandlerName());

        if (session.getCustomerId() != null) {
            User customer = userMapper.selectById(session.getCustomerId());
            if (customer != null) vo.setCustomerNickname(customer.getNickname());
        }
        if (session.getOperatorId() != null) {
            User operator = userMapper.selectById(session.getOperatorId());
            if (operator != null) vo.setOperatorNickname(operator.getNickname());
        }

        LambdaQueryWrapper<CsMessage> msgQuery = new LambdaQueryWrapper<>();
        msgQuery.eq(CsMessage::getSessionId, session.getId()).eq(CsMessage::getIsDeleted, 0).orderByAsc(CsMessage::getCreateTime);
        List<CsMessage> messages = csMessageMapper.selectList(msgQuery);
        vo.setMessages(messages != null ? messages.stream().map(this::buildMessageVO).collect(Collectors.toList()) : new ArrayList<>());

        return vo;
    }

    private CsSessionVO.CsMessageVO buildMessageVO(CsMessage msg) {
        CsSessionVO.CsMessageVO mv = new CsSessionVO.CsMessageVO();
        mv.setId(msg.getId());
        mv.setSenderId(msg.getSenderId());
        mv.setSenderType(msg.getSenderType());
        mv.setContent(msg.getContent());
        mv.setMessageType(msg.getMessageType());
        mv.setAttachmentUrl(msg.getAttachmentUrl());
        mv.setCreateTime(msg.getCreateTime());
        mv.setIsRead(msg.getIsRead());
        return mv;
    }
}
