package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.*;
import com.lobster.trade.model.entity.*;
import com.lobster.trade.model.request.ImSendMessageRequest;
import com.lobster.trade.model.response.ImSessionVO;
import com.lobster.trade.service.ImService;
import com.lobster.trade.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImServiceImpl implements ImService {

    private final ImSessionMapper imSessionMapper;
    private final ImMessageMapper imMessageMapper;
    private final UserMapper userMapper;
    private final TradeOrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public List<ImSessionVO> getMySessions(Long userId) {
        LambdaQueryWrapper<ImSession> query = new LambdaQueryWrapper<>();
        query.and(w -> w.eq(ImSession::getBuyerId, userId).or().eq(ImSession::getSellerId, userId))
              .eq(ImSession::getIsDeleted, 0)
              .orderByDesc(ImSession::getLastMessageAt);
        List<ImSession> sessions = imSessionMapper.selectList(query);
        return sessions.stream().map(s -> buildSessionVO(s, userId)).collect(Collectors.toList());
    }

    @Override
    public ImSessionVO getSession(Long userId, Long sessionId) {
        ImSession session = imSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无权限查看此会话");
        }
        return buildSessionVO(session, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImSessionVO getOrCreateSession(Long userId, Long orderId) {
        // 验证订单存在且属于当前用户
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无权限访问此订单会话");
        }

        // 查询是否已存在
        ImSession session = findSessionByOrderId(orderId);
        if (session != null) {
            return buildSessionVO(session, userId);
        }

        // 创建新会话
        session = new ImSession();
        session.setSessionNo("IM" + SnowflakeIdUtil.generateOrderNo());
        session.setOrderId(orderId);
        session.setBuyerId(order.getBuyerId());
        session.setSellerId(order.getSellerId());
        session.setUnreadBuyer(0);
        session.setUnreadSeller(0);
        session.setStatus(1);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        imSessionMapper.insert(session);

        // 发送系统消息
        addSystemMessage(session.getId(), "会话已建立，有问题可以在这里沟通");

        return buildSessionVO(session, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImSessionVO getOrCreateSessionByProduct(Long userId, Long productId) {
        // 1. 获取商品信息
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }

        Long sellerId = product.getSellerId();

        // 不能和自己聊天
        if (userId.equals(sellerId)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不能和自己聊天");
        }

        // 2. 查询是否已存在该买卖双方的会话（同一个卖家ID，不依赖订单）
        ImSession existing = findSessionByUsers(userId, sellerId, null);
        if (existing != null) {
            return buildSessionVO(existing, userId);
        }

        // 3. 创建新会话
        ImSession session = new ImSession();
        session.setSessionNo("IM" + SnowflakeIdUtil.generateOrderNo());
        session.setOrderId(null);  // 无订单
        session.setBuyerId(userId);
        session.setSellerId(sellerId);
        session.setUnreadBuyer(0);
        session.setUnreadSeller(0);
        session.setStatus(1);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        imSessionMapper.insert(session);

        // 系统消息
        addSystemMessage(session.getId(), "您已与卖家建立沟通，有任何问题可在此询问~");

        return buildSessionVO(session, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImSessionVO sendMessage(Long userId, ImSendMessageRequest req) {
        ImSession session = imSessionMapper.selectById(req.getSessionId());
        if (session == null || session.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话不存在");
        }
        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无权限发送消息");
        }
        if (session.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "会话已禁用");
        }

        // 判断发送者角色
        String senderRole = userId.equals(session.getBuyerId()) ? "buyer" : "seller";

        // 保存消息
        ImMessage message = new ImMessage();
        message.setSessionId(req.getSessionId());
        message.setSenderId(userId);
        message.setSenderRole(senderRole);
        message.setMessageType(req.getMessageType() != null ? req.getMessageType() : "text");
        message.setContent(req.getContent());
        message.setAttachmentUrl(req.getAttachmentUrl());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        imMessageMapper.insert(message);

        // 更新会话最后消息
        session.setLastMessage(req.getContent().length() > 100
            ? req.getContent().substring(0, 100) : req.getContent());
        session.setLastMessageAt(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        // 增加对方未读数
        if ("buyer".equals(senderRole)) {
            session.setUnreadSeller(session.getUnreadSeller() + 1);
        } else {
            session.setUnreadBuyer(session.getUnreadBuyer() + 1);
        }
        imSessionMapper.updateById(session);

        return buildSessionVO(session, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long sessionId) {
        ImSession session = imSessionMapper.selectById(sessionId);
        if (session == null || session.getIsDeleted() == 1) return;
        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) return;

        boolean isBuyer = session.getBuyerId().equals(userId);

        // 将对方发的未读消息标记为已读
        LambdaQueryWrapper<ImMessage> query = new LambdaQueryWrapper<>();
        query.eq(ImMessage::getSessionId, sessionId)
              .eq(ImMessage::getIsRead, 0)
              .ne(isBuyer ? ImMessage::getSenderId : ImMessage::getSenderId, userId);
        List<ImMessage> unread = imMessageMapper.selectList(query);
        if (unread != null && !unread.isEmpty()) {
            for (ImMessage msg : unread) {
                msg.setIsRead(1);
                imMessageMapper.updateById(msg);
            }
        }

        // 清零当前用户的未读数
        if (isBuyer) {
            session.setUnreadBuyer(0);
        } else {
            session.setUnreadSeller(0);
        }
        session.setUpdateTime(LocalDateTime.now());
        imSessionMapper.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSessionForOrder(Long orderId, Long buyerId, Long sellerId) {
        // 检查是否已存在
        if (findSessionByOrderId(orderId) != null) return;

        ImSession session = new ImSession();
        session.setSessionNo("IM" + SnowflakeIdUtil.generateOrderNo());
        session.setOrderId(orderId);
        session.setBuyerId(buyerId);
        session.setSellerId(sellerId);
        session.setUnreadBuyer(0);
        session.setUnreadSeller(0);
        session.setStatus(1);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        imSessionMapper.insert(session);

        addSystemMessage(session.getId(), "订单已创建，您可以在这里与卖家沟通交付细节");
    }

    private ImSession findSessionByOrderId(Long orderId) {
        return imSessionMapper.selectOne(
            new LambdaQueryWrapper<ImSession>()
                .eq(ImSession::getOrderId, orderId)
                .eq(ImSession::getIsDeleted, 0)
        );
    }

    private ImSession findSessionByUsers(Long userId, Long sellerId, Long orderId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .and(cond -> cond.eq(ImSession::getBuyerId, userId).eq(ImSession::getSellerId, sellerId))
            .or()
            .and(cond -> cond.eq(ImSession::getBuyerId, sellerId).eq(ImSession::getSellerId, userId))
        );
        if (orderId != null) {
            wrapper.eq(ImSession::getOrderId, orderId);
        } else {
            wrapper.isNull(ImSession::getOrderId);
        }
        wrapper.eq(ImSession::getIsDeleted, 0);
        wrapper.orderByDesc(ImSession::getCreateTime).last("LIMIT 1");
        return imSessionMapper.selectOne(wrapper);
    }

    private void addSystemMessage(Long sessionId, String content) {
        ImMessage msg = new ImMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(0L);
        msg.setSenderRole("system");
        msg.setMessageType("system");
        msg.setContent(content);
        msg.setIsRead(1);
        msg.setCreateTime(LocalDateTime.now());
        imMessageMapper.insert(msg);
    }

    private ImSessionVO buildSessionVO(ImSession session, Long currentUserId) {
        ImSessionVO vo = new ImSessionVO();
        vo.setId(session.getId());
        vo.setSessionNo(session.getSessionNo());
        vo.setOrderId(session.getOrderId());
        vo.setBuyerId(session.getBuyerId());
        vo.setSellerId(session.getSellerId());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageAt(session.getLastMessageAt());
        vo.setUnreadBuyer(session.getUnreadBuyer());
        vo.setUnreadSeller(session.getUnreadSeller());
        vo.setStatus(session.getStatus());
        vo.setCreateTime(session.getCreateTime());

        // 当前用户未读数
        vo.setMyUnread(session.getBuyerId().equals(currentUserId)
            ? session.getUnreadBuyer() : session.getUnreadSeller());

        // 填充昵称
        User buyer = userMapper.selectById(session.getBuyerId());
        if (buyer != null) vo.setBuyerNickname(buyer.getNickname());
        User seller = userMapper.selectById(session.getSellerId());
        if (seller != null) vo.setSellerNickname(seller.getNickname());

        // 填充订单信息
        if (session.getOrderId() != null) {
            TradeOrder order = orderMapper.selectById(session.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setProductTitle(order.getProductTitle());
                vo.setOrderStatus(order.getStatus());
            }
        }

        // 填充最近消息
        LambdaQueryWrapper<ImMessage> msgQuery = new LambdaQueryWrapper<>();
        msgQuery.eq(ImMessage::getSessionId, session.getId())
                .eq(ImMessage::getIsDeleted, 0)
                .orderByDesc(ImMessage::getCreateTime)
                .last("LIMIT 20");
        List<ImMessage> messages = imMessageMapper.selectList(msgQuery);
        if (messages != null) {
            // 倒序转正序
            List<ImSessionVO.ImMessageVO> msgVOList = new ArrayList<>();
            for (int i = messages.size() - 1; i >= 0; i--) {
                ImMessage msg = messages.get(i);
                ImSessionVO.ImMessageVO mv = new ImSessionVO.ImMessageVO();
                mv.setId(msg.getId());
                mv.setSenderId(msg.getSenderId());
                mv.setSenderRole(msg.getSenderRole());
                mv.setMessageType(msg.getMessageType());
                mv.setContent(msg.getContent());
                mv.setAttachmentUrl(msg.getAttachmentUrl());
                mv.setIsRead(msg.getIsRead());
                mv.setCreateTime(msg.getCreateTime());
                // 填充发送者昵称
                if (!"system".equals(msg.getSenderRole())) {
                    User sender = userMapper.selectById(msg.getSenderId());
                    if (sender != null) mv.setSenderNickname(sender.getNickname());
                } else {
                    mv.setSenderNickname("系统");
                }
                msgVOList.add(mv);
            }
            vo.setRecentMessages(msgVOList);
        } else {
            vo.setRecentMessages(new ArrayList<>());
        }

        return vo;
    }
}