package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.GameCategoryMapper;
import com.lobster.trade.mapper.ProductMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.GameCategory;
import com.lobster.trade.model.entity.Product;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.request.ProductPublishRequest;
import com.lobster.trade.model.response.ProductDetailVO;
import com.lobster.trade.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final GameCategoryMapper gameCategoryMapper;
    private final UserMapper userMapper;

    @Override
    public Long publish(ProductPublishRequest req, Long sellerId) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setGameId(req.getGameId());
        product.setCategoryId(req.getCategoryId());
        product.setProductType(req.getProductType());
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setImages(req.getImages());
        product.setPriceType(req.getPriceType());
        product.setPrice(req.getPrice());
        product.setUnit(req.getUnit());
        product.setGameZone(req.getGameZone());
        product.setServer(req.getServer());
        product.setPlatform(req.getPlatform());
        product.setMinDeposit(req.getMinDeposit());
        product.setEstimatedHours(req.getEstimatedHours());
        product.setStock(req.getStock() != null ? req.getStock() : 1);
        product.setStatus(1);
        product.setTotalOrders(0);
        product.setCompletedOrders(0);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    public Page<ProductDetailVO> list(Long gameId, String productType, Long sellerId, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, 1)
            .eq(Product::getIsDeleted, 0);
        if (gameId != null) wrapper.eq(Product::getGameId, gameId);
        if (productType != null && !productType.isEmpty()) wrapper.eq(Product::getProductType, productType);
        if (sellerId != null) wrapper.eq(Product::getSellerId, sellerId);
        wrapper.orderByDesc(Product::getCreateTime);

        // Count total first
        long total = productMapper.selectCount(wrapper);

        // Manual pagination via offset/size using selectList
        long offset = (long) (page - 1) * size;
        wrapper.last("LIMIT " + size + " OFFSET " + offset);
        java.util.List<Product> productList = productMapper.selectList(wrapper);

        java.util.List<ProductDetailVO> records = new java.util.ArrayList<>();
        for (Product p : productList) {
            ProductDetailVO vo = toVO(p);
            GameCategory g = gameCategoryMapper.selectById(p.getGameId());
            if (g != null) vo.setGameName(g.getGameName());
            User u = userMapper.selectById(p.getSellerId());
            if (u != null) {
                vo.setSellerNickname(u.getNickname());
                vo.setSellerReputationScore(u.getReputationScore());
                vo.setSellerIsVerified(u.getIsVerified());
            }
            records.add(vo);
        }
        Page<ProductDetailVO> voPage = new Page<>(page, size, total);
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public Page<ProductDetailVO> list(String keyword, Long gameId, String productType, int page, int size) {
        // Use apply() with flat OR structure - same logic as search()
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, 1)
            .eq(Product::getIsDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            String likePattern = "%" + keyword + "%";
            wrapper.apply("title LIKE {0} OR description LIKE {0}", likePattern);
        }
        if (gameId != null) wrapper.eq(Product::getGameId, gameId);
        if (productType != null && !productType.isEmpty()) wrapper.eq(Product::getProductType, productType);
        wrapper.orderByDesc(Product::getCreateTime);

        // Count: fetch all matching products
        java.util.List<Product> allProducts = productMapper.selectList(wrapper);
        long total = allProducts.size();

        // Apply pagination in Java
        long offset = (long) (page - 1) * size;
        java.util.List<Product> pageProducts = allProducts.stream()
            .skip(offset).limit(size).collect(java.util.stream.Collectors.toList());

        java.util.List<ProductDetailVO> records = new java.util.ArrayList<>();
        for (Product p : pageProducts) {
            ProductDetailVO vo = toVO(p);
            GameCategory g = gameCategoryMapper.selectById(p.getGameId());
            if (g != null) vo.setGameName(g.getGameName());
            User u = userMapper.selectById(p.getSellerId());
            if (u != null) {
                vo.setSellerNickname(u.getNickname());
                vo.setSellerReputationScore(u.getReputationScore());
                vo.setSellerIsVerified(u.getIsVerified());
            }
            records.add(vo);
        }
        Page<ProductDetailVO> voPage = new Page<>(page, size, total);
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public ProductDetailVO getDetail(Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null || p.getIsDeleted() == 1) throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        ProductDetailVO vo = toVO(p);
        GameCategory g = gameCategoryMapper.selectById(p.getGameId());
        if (g != null) vo.setGameName(g.getGameName());
        User u = userMapper.selectById(p.getSellerId());
        if (u != null) {
            vo.setSellerNickname(u.getNickname());
            vo.setSellerReputationScore(u.getReputationScore());
            vo.setSellerIsVerified(u.getIsVerified());
        }
        return vo;
    }

    @Override
    public void increaseView(Long productId) {
        Product p = productMapper.selectById(productId);
        if (p != null) {
            p.setViewCount(p.getViewCount() == null ? 1 : p.getViewCount() + 1);
            productMapper.updateById(p);
        }
    }

    @Override
    public List<Product> getMyProducts(Long sellerId) {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
            .eq(Product::getSellerId, sellerId)
            .eq(Product::getIsDeleted, 0)
            .orderByDesc(Product::getCreateTime));
    }

    @Override
    public Page<ProductDetailVO> search(String keyword, Long gameId, String productType, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, 1)
            .eq(Product::getIsDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            String likePattern = "%" + keyword + "%";
            wrapper.apply("title LIKE {0} OR description LIKE {0}", likePattern);
        }
        if (gameId != null) wrapper.eq(Product::getGameId, gameId);
        if (productType != null && !productType.isEmpty()) wrapper.eq(Product::getProductType, productType);
        wrapper.orderByDesc(Product::getViewCount);

        java.util.List<Product> allProducts = productMapper.selectList(wrapper);
        long total = allProducts.size();

        long offset = (long) (page - 1) * size;
        java.util.List<Product> pageProducts = allProducts.stream()
            .skip(offset).limit(size).collect(java.util.stream.Collectors.toList());

        java.util.List<ProductDetailVO> records = new java.util.ArrayList<>();
        for (Product p : pageProducts) {
            ProductDetailVO vo = toVO(p);
            GameCategory g = gameCategoryMapper.selectById(p.getGameId());
            if (g != null) vo.setGameName(g.getGameName());
            User u = userMapper.selectById(p.getSellerId());
            if (u != null) {
                vo.setSellerNickname(u.getNickname());
                vo.setSellerReputationScore(u.getReputationScore());
                vo.setSellerIsVerified(u.getIsVerified());
            }
            records.add(vo);
        }
        Page<ProductDetailVO> voPage = new Page<>(page, size, total);
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional
    public void updateStatus(Long productId, Integer status, String reason) {
        Product p = productMapper.selectById(productId);
        if (p == null || p.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        p.setStatus(status);
        p.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(p);
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> productIds, Integer status, String reason) {
        for (Long id : productIds) {
            updateStatus(id, status, reason);
        }
    }

    ProductDetailVO toVO(Product p) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(p.getId());
        vo.setSellerId(p.getSellerId());
        vo.setGameId(p.getGameId());
        vo.setProductType(p.getProductType());
        vo.setTitle(p.getTitle());
        vo.setDescription(p.getDescription());
        vo.setImages(p.getImages());
        try {
            if (p.getImages() != null && !p.getImages().isBlank()) {
                com.alibaba.fastjson2.JSONArray arr = com.alibaba.fastjson2.JSONArray.parseArray(p.getImages());
                if (arr != null && !arr.isEmpty()) {
                    vo.setCoverImage(arr.getString(0));
                }
            }
        } catch (Exception ignored) {}
        vo.setPriceType(p.getPriceType());
        vo.setPrice(p.getPrice());
        vo.setUnit(p.getUnit());
        vo.setGameZone(p.getGameZone());
        vo.setServer(p.getServer());
        vo.setPlatform(p.getPlatform());
        vo.setMinDeposit(p.getMinDeposit());
        vo.setEstimatedHours(p.getEstimatedHours());
        vo.setStock(p.getStock());
        vo.setTotalOrders(p.getTotalOrders());
        vo.setCompletedOrders(p.getCompletedOrders());
        vo.setViewCount(p.getViewCount());
        vo.setFavoriteCount(p.getFavoriteCount());
        vo.setStatus(p.getStatus());
        vo.setCreateTime(p.getCreateTime());
        return vo;
    }
}