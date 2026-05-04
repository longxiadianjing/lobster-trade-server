package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.GameCategoryMapper;
import com.lobster.trade.model.entity.GameCategory;
import com.lobster.trade.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameCategoryMapper gameCategoryMapper;

    @Override
    public List<GameCategory> getOnlineGames() {
        return gameCategoryMapper.selectList(
            new LambdaQueryWrapper<GameCategory>()
                .eq(GameCategory::getStatus, 1)
                .eq(GameCategory::getIsDeleted, 0)
                .orderByAsc(GameCategory::getSortOrder)
        );
    }
}
