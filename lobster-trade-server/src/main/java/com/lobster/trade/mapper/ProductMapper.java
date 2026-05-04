package com.lobster.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lobster.trade.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存（乐观锁）
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响的行数，0表示库存不足或商品不存在
     */
    @Update("UPDATE product SET stock = stock - #{quantity}, update_time = NOW() " +
            "WHERE id = #{productId} AND stock >= #{quantity} AND is_deleted = 0")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 原子还原库存
     * @param productId 商品ID
     * @param quantity 还原数量
     * @return 影响的行数
     */
    @Update("UPDATE product SET stock = stock + #{quantity}, update_time = NOW() " +
            "WHERE id = #{productId} AND is_deleted = 0")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
