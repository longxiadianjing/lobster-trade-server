package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.*;
import com.lobster.trade.model.entity.*;
import com.lobster.trade.model.response.ProductDetailVO;
import com.lobster.trade.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final GameCategoryMapper gameCategoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数错误");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "商品不存在");
        }
        if (product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不能收藏自己的商品");
        }
        LambdaQueryWrapper<UserFavorite> existingQ = new LambdaQueryWrapper<>();
        existingQ.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)
                .eq(UserFavorite::getIsDeleted, 0);
        if (favoriteMapper.selectCount(existingQ) > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "已收藏过该商品");
        }
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        fav.setCreateTime(LocalDateTime.now());
        fav.setUpdateTime(LocalDateTime.now());
        fav.setIsDeleted(0);
        favoriteMapper.insert(fav);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) return;
        LambdaQueryWrapper<UserFavorite> q = new LambdaQueryWrapper<>();
        q.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)
                .eq(UserFavorite::getIsDeleted, 0);
        favoriteMapper.delete(q);
    }

    @Override
    public List<ProductDetailVO> getMyFavorites(Long userId) {
        LambdaQueryWrapper<UserFavorite> q = new LambdaQueryWrapper<>();
        q.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getIsDeleted, 0)
                .orderByDesc(UserFavorite::getCreateTime);
        List<UserFavorite> favs = favoriteMapper.selectList(q);
        List<ProductDetailVO> result = new ArrayList<>();
        for (UserFavorite fav : favs) {
            Product p = productMapper.selectById(fav.getProductId());
            if (p == null || p.getIsDeleted() == 1 || p.getStatus() != 1) continue;
            ProductDetailVO vo = new ProductDetailVO();
            vo.setId(p.getId());
            vo.setSellerId(p.getSellerId());
            vo.setGameId(p.getGameId());
            vo.setProductType(p.getProductType());
            vo.setTitle(p.getTitle());
            vo.setDescription(p.getDescription());
            vo.setImages(p.getImages());
            vo.setCoverImage(p.getCoverImage());
            vo.setPriceType(p.getPriceType());
            vo.setPrice(p.getPrice());
            vo.setUnit(p.getUnit());
            vo.setGameZone(p.getGameZone());
            vo.setServer(p.getServer());
            vo.setPlatform(p.getPlatform());
            vo.setStock(p.getStock());
            vo.setTotalOrders(p.getTotalOrders());
            vo.setViewCount(p.getViewCount());
            vo.setStatus(p.getStatus());
            GameCategory g = gameCategoryMapper.selectById(p.getGameId());
            if (g != null) vo.setGameName(g.getGameName());
            User u = userMapper.selectById(p.getSellerId());
            if (u != null) {
                vo.setSellerNickname(u.getNickname());
                vo.setSellerReputationScore(u.getReputationScore());
                vo.setSellerIsVerified(u.getIsVerified());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        if (userId == null || productId == null) return false;
        LambdaQueryWrapper<UserFavorite> q = new LambdaQueryWrapper<>();
        q.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)
                .eq(UserFavorite::getIsDeleted, 0);
        return favoriteMapper.selectCount(q) > 0;
    }
}