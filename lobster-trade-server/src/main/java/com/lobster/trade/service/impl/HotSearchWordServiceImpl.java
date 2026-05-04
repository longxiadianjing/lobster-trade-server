package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lobster.trade.exception.BusinessException;
import com.lobster.trade.exception.ErrorCode;
import com.lobster.trade.mapper.HotSearchWordMapper;
import com.lobster.trade.model.entity.HotSearchWord;
import com.lobster.trade.service.HotSearchWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotSearchWordServiceImpl implements HotSearchWordService {

    private final HotSearchWordMapper hotSearchWordMapper;

    @Override
    public IPage<HotSearchWord> listWords(String keyword, int page, int size) {
        LambdaQueryWrapper<HotSearchWord> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            q.like(HotSearchWord::getWord, keyword);
        }
        q.orderByAsc(HotSearchWord::getSortOrder);
        Page<HotSearchWord> p = new Page<>(page, size);
        return hotSearchWordMapper.selectPage(p, q);
    }

    @Override
    @Transactional
    public void create(HotSearchWord word) {
        if (word.getSortOrder() == null) word.setSortOrder(100);
        if (word.getStatus() == null) word.setStatus(1);
        if (word.getSearchCount() == null) word.setSearchCount(0);
        word.setCreateTime(LocalDateTime.now());
        word.setUpdateTime(LocalDateTime.now());
        hotSearchWordMapper.insert(word);
    }

    @Override
    @Transactional
    public void update(Long id, HotSearchWord word) {
        HotSearchWord existing = hotSearchWordMapper.selectById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "热搜词不存在");
        }
        if (word.getWord() != null) existing.setWord(word.getWord());
        if (word.getSortOrder() != null) existing.setSortOrder(word.getSortOrder());
        if (word.getStatus() != null) existing.setStatus(word.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        hotSearchWordMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        hotSearchWordMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        HotSearchWord word = hotSearchWordMapper.selectById(id);
        if (word == null || word.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "热搜词不存在");
        }
        word.setStatus(word.getStatus() == 1 ? 0 : 1);
        word.setUpdateTime(LocalDateTime.now());
        hotSearchWordMapper.updateById(word);
    }

    @Override
    public List<HotSearchWord> getEnabledWords() {
        LambdaQueryWrapper<HotSearchWord> q = new LambdaQueryWrapper<>();
        q.eq(HotSearchWord::getStatus, 1);
        q.orderByAsc(HotSearchWord::getSortOrder);
        return hotSearchWordMapper.selectList(q);
    }
}
