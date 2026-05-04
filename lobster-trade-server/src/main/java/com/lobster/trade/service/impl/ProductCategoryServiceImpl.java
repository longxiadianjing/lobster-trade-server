package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.ProductCategoryMapper;
import com.lobster.trade.model.entity.ProductCategory;
import com.lobster.trade.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;

    @Override
    public List<ProductCategory> getByGameId(Long gameId) {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getGameId, gameId)
                .eq(ProductCategory::getStatus, 1)
                .eq(ProductCategory::getIsDeleted, 0)
                .orderByAsc(ProductCategory::getSortOrder)
        );
    }
}