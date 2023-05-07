package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 一个链接就是一个 WebSocketServer 实例
 *  创建一个类 维护链接
 *
 *  一局游戏会创建一个 Game线程
 */
@Component
@ServerEndpoint("/websocket/{token}")   // 链接 接收前端返回token
public class WebSocketServer {

    // 存储所有链接中  当匹配成功后,可以利用链接 给前端发送  会有多个线程操作
    //UserId
    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    // 用户信息
    private User user;
    // WebSocket 的一个包
    private Session session = null;
    // 地图
    public Game game = null;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerurl = "http://127.0.0.1:3001/player/remove/";


    public static UserMapper userMapper;

    public static RecordMapper recordMapper;

    private static BotMapper botMapper;

    public static RestTemplate restTemplate;

    // WebSocket 是多对象的,每个链接都会创建一个对象, 使用Autowired直接注入的Mapper是单例的
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    /**
     * 建立链接  链接建立的时候自动调用
     *
     * @param session
     * @param token
     * @throws IOException
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 保存session
        this.session = session;
        System.out.println("connected!-(WebSocketServer89)" + "  --session: " + this.session);
        // gwt  websocket中没有gwt, 将token直接放在url中传回后端                        (断线重连)
        Integer userId = JwtAuthentication.getUserId(token);
        // 获取用户信息
        this.user = userMapper.selectById(userId);

        if (this.user != null) {
            // 存储用户
            users.put(userId, this);

        } else {
            // 断开链接
            this.session.close();
        }
        System.out.println("users-(WebsocketServer102):" + users);
    }

    /**
     * 关闭链接
     * 链接关闭的时候自动调用
     */
    @OnClose
    public void onClose() {
        System.out.println("disconnected!-(WebSocketServer)");
        if (this.user != null) {
            users.remove(this.user.getId());
        }
    }

    /**
     * 匹配成功 创建地图
     *
     * @param aId
     * @param aBotId
     * @param bId
     * @param bBotId
     */
    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {

        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);

        // 创建地图  一个对局创建一个地图
        Game game = new Game(13, 14, 20, a.getId(), botA, b.getId(), botB);
        game.createMap();

        if (users.get(a.getId()) != null)
            users.get(a.getId()).game = game;
        if (users.get(b.getId()) != null)
            users.get(b.getId()).game = game;

        // 一个对局开启一个线程
        game.start();

        // 地图信息封装json, AB返回同样的地图信息
        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        // 给A前端返回信息
        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if (users.get(a.getId()) != null)
            //                   向A的前端返回
            users.get(a.getId()).sendMessage(respA.toJSONString());

        // 给B前端返回信息
        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if (users.get(b.getId()) != null)
            //                   向B的前端返回
            users.get(b.getId()).sendMessage(respB.toJSONString());
    }

    /**
     * 开始匹配
     *
     * @param botId
     */
    private void startMatching(Integer botId) {
        System.out.println("start matching!-(WebSocketServer)");
        // 向MatchingSystem 发送请求, 传一个玩家
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    /**
     * 取消匹配
     */
    private void stopMatching() {
        System.out.println("stop matching-(WebSocketServer)");
        // 向MatchingSystem 发送请求, 取消此玩家的匹配
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerurl, data, String.class);
    }

    /**
     *
     * @param direction
     */
    private void move(int direction) {
        System.out.println("move-(WebSocketServer) --direction:" + direction);
        if (game.getPlayerA().getId().equals(user.getId())) { // 如果前端传回来 我是A
            if (game.getPlayerA().getBotId().equals(-1))  // 亲自出马
                game.setNextStepA(direction); // 设置A移动
        } else if (game.getPlayerB().getId().equals(user.getId())) { // B
            if (game.getPlayerB().getBotId().equals(-1))  // 亲自出马
                game.setNextStepB(direction);
        }
    }

    /**
     * 接收 Client 发送的信息
     * 前端返回信息时调用
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("receive message!-(WebSocketServer)");

        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");

        // 接收前端返回的信息后
        if ("start-matching".equals(event)) { // 开始匹配
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) { // 取消匹配
            stopMatching();
        } else if ("move".equals(event)) { // 移动
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 向前端发送信息
     *
     * @param message
     */
    public void sendMessage(String message) {
        synchronized (this.session) {
            try {   // 后端向这个session(链接)发送
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}