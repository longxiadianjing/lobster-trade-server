package com.lobster.trade.service;

import com.lobster.trade.model.entity.GameCategory;
import java.util.List;

public interface GameService {
    List<GameCategory> getOnlineGames();
}
