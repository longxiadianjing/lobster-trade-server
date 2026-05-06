package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.ServiceTicketMapper;
import com.lobster.trade.model.entity.ServiceTicket;
import com.lobster.trade.service.ServiceTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ServiceTicketServiceImpl implements ServiceTicketService {

    private final ServiceTicketMapper ticketMapper;
    private static final AtomicLong ticketSeq = new AtomicLong(1);

    @Override
    public IPage<ServiceTicket> getMyTickets(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<ServiceTicket> q = new LambdaQueryWrapper<>();
        q.eq(ServiceTicket::getUserId, userId);
        if (StringUtils.hasText(status)) q.eq(ServiceTicket::getStatus, Integer.parseInt(status));
        q.orderByDesc(ServiceTicket::getCreateTime);
        return ticketMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceTicket createTicket(Long userId, ServiceTicket ticket) {
        String ticketNo = "TK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%05d", ticketSeq.getAndIncrement());
        ticket.setTicketNo(ticketNo);
        ticket.setUserId(userId);
        ticket.setStatus(1); // 待处理
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticket.setLastReplyTime(LocalDateTime.now());
        ticketMapper.insert(ticket);
        return ticket;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long userId, Long ticketId, String reply) {
        ServiceTicket t = ticketMapper.selectById(ticketId);
        if (t == null || t.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工单不存在");
        }
        if (!t.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无权操作");
        }
        if (t.getStatus() == 4 || t.getStatus() == 5 || t.getStatus() == 6) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工单已结束");
        }
        // 用户补充回复，仅更新最后回复时间
        t.setLastReplyTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long userId, Long ticketId, Integer satisfaction, String feedback) {
        ServiceTicket t = ticketMapper.selectById(ticketId);
        if (t == null || t.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工单不存在");
        }
        if (!t.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无权操作");
        }
        t.setStatus(4); // 已解决
        t.setCloseTime(LocalDateTime.now());
        t.setSatisfaction(satisfaction);
        t.setUserFeedback(feedback);
        t.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(t);
    }

    @Override
    public IPage<ServiceTicket> listForAdmin(String keyword, String status, String category, int page, int size) {
        LambdaQueryWrapper<ServiceTicket> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) q.eq(ServiceTicket::getStatus, Integer.parseInt(status));
        if (StringUtils.hasText(category)) q.eq(ServiceTicket::getCategory, Integer.parseInt(category));
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(ServiceTicket::getTicketNo, keyword)
                    .or().like(ServiceTicket::getSubject, keyword)
                    .or().like(ServiceTicket::getDescription, keyword));
        }
        q.orderByDesc(ServiceTicket::getLastReplyTime);
        return ticketMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignHandler(Long ticketId, Long handlerId, String handlerName) {
        ServiceTicket t = ticketMapper.selectById(ticketId);
        if (t == null || t.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工单不存在");
        }
        t.setHandlerId(handlerId);
        t.setHandlerName(handlerName);
        if (t.getStatus() == 1) t.setStatus(2); // 从待处理改为处理中
        t.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long ticketId, String reply, Integer status) {
        ServiceTicket t = ticketMapper.selectById(ticketId);
        if (t == null || t.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工单不存在");
        }
        t.setHandlerReply(reply);
        t.setHandleTime(LocalDateTime.now());
        t.setStatus(status != null ? status : 3); // 默认待用户确认
        t.setLastReplyTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeTicket(Long ticketId) {
        ServiceTicket t = ticketMapper.selectById(ticketId);
        if (t == null || t.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "工单不存在");
        }
        t.setStatus(5); // 已关闭
        t.setCloseTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(t);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", ticketMapper.selectCount(null));
        stats.put("pending", ticketMapper.selectCount(new LambdaQueryWrapper<ServiceTicket>().eq(ServiceTicket::getStatus, 1)));
        stats.put("processing", ticketMapper.selectCount(new LambdaQueryWrapper<ServiceTicket>().eq(ServiceTicket::getStatus, 2)));
        stats.put("resolved", ticketMapper.selectCount(new LambdaQueryWrapper<ServiceTicket>().eq(ServiceTicket::getStatus, 4)));
        stats.put("closed", ticketMapper.selectCount(new LambdaQueryWrapper<ServiceTicket>().eq(ServiceTicket::getStatus, 5)));
        return stats;
    }
}