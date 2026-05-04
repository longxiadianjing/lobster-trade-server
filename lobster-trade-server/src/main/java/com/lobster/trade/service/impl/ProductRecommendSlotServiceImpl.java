package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.ProductMapper;
import com.lobster.trade.mapper.ProductRecommendSlotMapper;
import com.lobster.trade.model.entity.Product;
import com.lobster.trade.model.entity.ProductRecommendSlot;
import com.lobster.trade.service.ProductRecommendSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRecommendSlotServiceImpl implements ProductRecommendSlotService {

    private final ProductRecommendSlotMapper slotMapper;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductRecommendSlot> listSlots(String slotKey, Integer page, Integer size) {
        Page<ProductRecommendSlot> p = new Page<>(page, size);
        LambdaQueryWrapper<ProductRecommendSlot> wrapper = new LambdaQueryWrapper<>();
        if (slotKey != null && !slotKey.isEmpty()) {
            wrapper.eq(ProductRecommendSlot::getSlotKey, slotKey);
        }
        wrapper.orderByAsc(ProductRecommendSlot::getSortOrder);
        Page<ProductRecommendSlot> result = slotMapper.selectPage(p, wrapper);
        // 填充商品名称
        for (ProductRecommendSlot slot : result.getRecords()) {
            if (slot.getProductId() != null) {
                Product product = productMapper.selectById(slot.getProductId());
                if (product != null) {
                    slot.setRemark(product.getTitle()); // 借用remark字段存商品标题，前端显示用
                }
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void create(ProductRecommendSlot slot) {
        if (slot.getSlotKey() == null || slot.getProductId() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "推荐位标识和商品ID不能为空");
        }
        // 校验商品存在
        Product product = productMapper.selectById(slot.getProductId());
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        slot.setStatus(slot.getStatus() != null ? slot.getStatus() : 1);
        slot.setSortOrder(slot.getSortOrder() != null ? slot.getSortOrder() : 0);
        slotMapper.insert(slot);
    }

    @Override
    @Transactional
    public void update(Long id, ProductRecommendSlot slot) {
        ProductRecommendSlot existing = slotMapper.selectById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "推荐位不存在");
        }
        if (slot.getProductId() != null) {
            Product product = productMapper.selectById(slot.getProductId());
            if (product == null || product.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
            }
        }
        if (slot.getSortOrder() != null) existing.setSortOrder(slot.getSortOrder());
        if (slot.getStartTime() != null) existing.setStartTime(slot.getStartTime());
        if (slot.getEndTime() != null) existing.setEndTime(slot.getEndTime());
        if (slot.getStatus() != null) existing.setStatus(slot.getStatus());
        if (slot.getRemark() != null) existing.setRemark(slot.getRemark());
        existing.setUpdateTime(LocalDateTime.now());
        slotMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        slotMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        ProductRecommendSlot slot = slotMapper.selectById(id);
        if (slot == null || slot.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "推荐位不存在");
        }
        slot.setStatus(slot.getStatus() == 1 ? 0 : 1);
        slot.setUpdateTime(LocalDateTime.now());
        slotMapper.updateById(slot);
    }

    @Override
    public List<Product> getActiveSlots(String slotKey) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ProductRecommendSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRecommendSlot::getSlotKey, slotKey)
               .eq(ProductRecommendSlot::getStatus, 1)
               .eq(ProductRecommendSlot::getIsDeleted, 0)
               .and(w -> w.le(ProductRecommendSlot::getStartTime, now).or().isNull(ProductRecommendSlot::getStartTime))
               .and(w -> w.ge(ProductRecommendSlot::getEndTime, now).or().isNull(ProductRecommendSlot::getEndTime))
               .orderByAsc(ProductRecommendSlot::getSortOrder);

        List<ProductRecommendSlot> slots = slotMapper.selectList(wrapper);
        if (slots.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> productIds = slots.stream()
                .map(ProductRecommendSlot::getProductId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getId, productIds)
                      .eq(Product::getStatus, 1)
                      .eq(Product::getIsDeleted, 0);
        List<Product> products = productMapper.selectList(productWrapper);

        // 按 slots 顺序返回，并计算 coverImage
        List<Product> sortedProducts = products.stream()
                .sorted((a, b) -> {
                    int idxA = productIds.indexOf(a.getId());
                    int idxB = productIds.indexOf(b.getId());
                    return Integer.compare(idxA, idxB);
                })
                .collect(Collectors.toList());

        // 计算每件商品的封面图
        for (Product p : sortedProducts) {
            p.computeCoverImage();
        }

        return sortedProducts;
    }

    @Override
    public List<ProductRecommendSlot> getSlotsByProductId(Long productId) {
        LambdaQueryWrapper<ProductRecommendSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRecommendSlot::getProductId, productId)
               .eq(ProductRecommendSlot::getIsDeleted, 0)
               .orderByAsc(ProductRecommendSlot::getSortOrder);
        return slotMapper.selectList(wrapper);
    }
}
