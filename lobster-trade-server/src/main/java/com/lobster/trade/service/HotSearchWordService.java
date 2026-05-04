package com.lobster.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.model.entity.HotSearchWord;
import java.util.List;

public interface HotSearchWordService {

    IPage<HotSearchWord> listWords(String keyword, int page, int size);

    void create(HotSearchWord word);

    void update(Long id, HotSearchWord word);

    void delete(Long id);

    void toggleStatus(Long id);

    List<HotSearchWord> getEnabledWords();
}
