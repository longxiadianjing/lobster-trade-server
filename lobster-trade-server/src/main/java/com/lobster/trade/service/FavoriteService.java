package com.lobster.trade.service;

import com.lobster.trade.model.response.ProductDetailVO;
import java.util.List;

public interface FavoriteService {

    void addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);

    List<ProductDetailVO> getMyFavorites(Long userId);

    boolean isFavorited(Long userId, Long productId);
}