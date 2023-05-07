package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.StartGameService;
import org.springframework.stereotype.Service;

/**
 * 在MatchingPool 中调用, 当匹配成功后用http请求调用创建地图
 */
@Service
public class StartGameServiceImpl implements StartGameService {
    @Override
    public String startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotid) {
        System.out.println("start game-(StartGameServiceImpl): " + aId + " " + bId);
        WebSocketServer.startGame(aId, aBotId, bId, bBotid);
        return "start game success";
    }
}
