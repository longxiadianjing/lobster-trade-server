package com.lobster.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.model.entity.ServiceTicket;
import java.util.Map;

public interface ServiceTicketService {

    // User
    IPage<ServiceTicket> getMyTickets(Long userId, String status, int page, int size);
    ServiceTicket createTicket(Long userId, ServiceTicket ticket);
    void reply(Long userId, Long ticketId, String reply);
    void close(Long userId, Long ticketId, Integer satisfaction, String feedback);

    // Admin
    IPage<ServiceTicket> listForAdmin(String keyword, String status, String category, int page, int size);
    void assignHandler(Long ticketId, Long handlerId, String handlerName);
    void handle(Long ticketId, String reply, Integer status);
    void closeTicket(Long ticketId);
    Map<String, Object> getStats();
}