package com.lobster.trade.controller;

import com.lobster.trade.model.entity.GameCategory;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ApiResponse<List<GameCategory>> list() {
        return ApiResponse.success(gameService.getOnlineGames());
    }
}
