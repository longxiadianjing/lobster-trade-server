package com.lobster.trade.service;

public interface JwtAuthService {
    Long getUserIdFromToken(String token);
}
