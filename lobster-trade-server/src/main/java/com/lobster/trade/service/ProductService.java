package com.lobster.trade.service;

import com.lobster.trade.model.entity.Product;
import com.lobster.trade.model.request.ProductPublishRequest;
import com.lobster.trade.model.response.ProductDetailVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

public interface ProductService {
    Long publish(ProductPublishRequest req, Long sellerId);
    Page<ProductDetailVO> list(Long gameId, String productType, Long sellerId, int page, int size);

    Page<ProductDetailVO> list(String keyword, Long gameId, String productType, int page, int size);

    Page<ProductDetailVO> search(String keyword, Long gameId, String productType, Long sellerId, int page, int size);
    ProductDetailVO getDetail(Long productId);
    void increaseView(Long productId);
    List<Product> getMyProducts(Long sellerId);

    // Admin
    void updateStatus(Long productId, Integer status, String reason, Long userId);
    void batchUpdateStatus(List<Long> productIds, Integer status, String reason, Long userId);
}