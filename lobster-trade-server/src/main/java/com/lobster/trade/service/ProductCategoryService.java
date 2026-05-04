package com.lobster.trade.service;

import com.lobster.trade.model.entity.ProductCategory;
import java.util.List;

public interface ProductCategoryService {
    List<ProductCategory> getByGameId(Long gameId);
}