package com.kob.matchingsystem.controller;

import com.kob.matchingsystem.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 和backend交互
 * com.kob.backend.consumer.WebSocketServer
 */
@RestController
public class MatchingController {
    @Autowired
    private MatchingService matchingService;

    /**
     * WebSocketServer 中调用
     * 开始匹配后, 调用 将玩家 添加到匹配池中
     * @param data
     * @return
     */
    @PostMapping("/player/add/")
    public String addPlayer(@RequestParam MultiValueMap<String, String> data) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("user_id")));
        Integer rating = Integer.parseInt(Objects.requireNonNull(data.getFirst("rating")));
        Integer botId = Integer.parseInt(Objects.requireNonNull(data.getFirst("bot_id")));
        return matchingService.addPlayer(userId, rating, botId);
    }

    /**
     * 取消匹配, 将玩家从匹配池中删除
     * @param data
     * @return
     */
    @PostMapping("/player/remove/")
    public String removePlayer(@RequestParam MultiValueMap<String, String> data) {
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("user_id")));
        return matchingService.removePlayer(userId);
    }
}
