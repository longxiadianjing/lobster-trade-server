package com.lobster.trade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.model.entity.ProductRecommendSlot;
import com.lobster.trade.model.entity.Product;
import java.util.List;

public interface ProductRecommendSlotService {

    /** 后台分页列表 */
    Page<ProductRecommendSlot> listSlots(String slotKey, Integer page, Integer size);

    /** 创建推荐位 */
    void create(ProductRecommendSlot slot);

    /** 更新推荐位 */
    void update(Long id, ProductRecommendSlot slot);

    /** 删除推荐位 */
    void delete(Long id);

    /** 切换启用状态 */
    void toggleStatus(Long id);

    /** 获取当前生效的推荐位商品（带商品详情） */
    List<Product> getActiveSlots(String slotKey);

    /** 获取某商品所在的所有推荐位 */
    List<ProductRecommendSlot> getSlotsByProductId(Long productId);
}
