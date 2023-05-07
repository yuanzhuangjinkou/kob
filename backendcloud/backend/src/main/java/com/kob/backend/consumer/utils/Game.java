package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 一个对局 中的等待用户输入 创建一个线程开启一个对局 创建一个Game对象  在WebSocketServer 中匹配成功后创建
 *
 * 游戏中包括地图信息, 两名玩家信息
 *
 * 玩家执行操作时, 由于会存在多次对局, 创建多线程
 *
 */
public class Game extends Thread {
    // 地图行列数
    private final Integer rows;
    private final Integer cols;
    // 地图中障碍物的数量
    private final Integer inner_walls_count;
    // 地图数组
    private final int[][] g;

    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    // 维护两名玩家信息     //A在左下角,B右上角
    private final Player playerA;
    private final Player playerB;

    // 两个玩家的下一步操作
    private Integer nextStepA = null;
    private Integer nextStepB = null;

    private ReentrantLock lock = new ReentrantLock();
    // 游戏的状态
    private String status = "playing";  // playing -> finished

    // 蛇的输入情况,  all: 平局  A: a输  B: b输
    private String loser = "";  // all: 平局，A: A输，B: B输
    // com.kob.botrunningsystem.controller.BotRunningController
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";

    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Bot botA, Integer idB, Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";
        if (botA != null) { // 表示a 是代码执行
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if (botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }
        // 初始化两名玩家                              // 起始坐标
        playerA = new Player(idA, botIdA, botCodeA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, botIdB, botCodeB, 1, cols - 2, new ArrayList<>());

        System.out.println(botCodeB);

    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }


    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回地图数组
     *
     * @return
     */
    public int[][] getG() {
        return g;
    }

    /**
     * 判断地图是否连通
     *
     * @param sx 两点的坐标
     * @param sy
     * @param tx
     * @param ty
     * @return
     */
    private boolean check_connectivity(int sx, int sy, int tx, int ty) {

        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1; // 标记走过

        for (int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0; // 还原现场
                    return true;
                }
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    /**
     * 画地图
     * 两名玩家,两个客户端 生成的是一个Game类
     * 1: 障碍物
     *
     * @return
     */
    private boolean draw() {  // 画地图
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                g[i][j] = 0;
            }
        }

        // 周围边框
        for (int r = 0; r < this.rows; r++) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for (int c = 0; c < this.cols; c++) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        // 地图内部 随机生成障碍物 生成地图
//        Random random = new Random();
//        // 轴对称, 取一半循环创建
//        for (int i = 0; i < this.inner_walls_count / 2; i++) {
//            // 每个格子随机1000次
//            for (int j = 0; j < 1000; j++) {
//                // 随机生成障碍物坐标
//                int r = random.nextInt(this.rows);
//                int c = random.nextInt(this.cols);
//
//                // 如果已经是障碍物
//                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1)
//                    continue;
//                // 蛇头的位置
//                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2)
//                    continue;
//
//                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
//                break;
//            }
//        }

        String map = "11111111111111100000000000011000100101000110101010000001100010000000111000000010000110000100100001100001000000011100000001000110000001010101100010100100011000000000000111111111111111";
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (map.charAt(k) == '1') {    // 棋盘中的墙
                    g[i][j] = 1;
                }
            }
        }

        // 判断是否连通       参数 两点坐标
        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    /**
     * 创建地图
     */
    public void createMap() {
        // 随机1000次, 选择满足条件的地图
        for (int i = 0; i < 1000; i++) {
            if (draw())
                break;
        }
    }

    /**
     * 将当前的局面信息 编码成一个字符串
     * 地图#自己起始横坐标#自己起始纵坐标#(自己操作)#对手起始横坐标#对手起始纵坐标#(对手操作)
     *
     * @param player
     * @return
     */
    private String getInput(Player player) {  // 将当前的局面信息，编码成字符串
        Player me, you;
        if (playerA.getId().equals(player.getId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }

        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }

    /**
     * 判断是否需要执行代码
     *
     * @param player
     */
    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1)) return;  // 亲自出马，不需要执行代码
        // 执行代码
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }

    /**
     * 等待两个玩家的下一步操作
     *
     * @return
     */
    private boolean nextStep() {  // 等待两名玩家的下一步操作

        // 前端设置🐍的速度是一秒5格,一格200ms, 在两个操作已经输入,蛇正在移动中两名玩家输入的话,不符合规则,所以在蛇的移动时不能接收下一步操作
        // 所有需要先睡200ms
        try {
            Thread.sleep(200);// 蛇在移动过程中不能接收操作
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("(Game253)" + playerA.toString());
//        System.out.println("(Game253)" + playerB.toString());
        sendBotCode(playerA);
        sendBotCode(playerB);

        // 等待时间分为5秒, 5秒分为50次,,为了给玩家更好的体验, 当玩家有输入后能立刻将信息传递
        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) { // 两名玩家都有操作
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 判断 参数A 的最后一位是否合法
     *
     * @param cellsA
     * @param cellsB
     * @return
     */
    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);// 获取蛇移动后的最新位置
        // 最新位置是否为墙壁
        if (g[cell.x][cell.y] == 1) return false;

        // 最新位置是否和自己身体碰撞
        for (int i = 0; i < n - 1; i++) {
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y)
                return false;
        }
        // 最新位置是否和对手身体碰撞
        for (int i = 0; i < n - 1; i++) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y)
                return false;
        }

        return true;
    }

    /**
     * 判断两名玩家下一步操作是否合法
     */
    private void judge() {  // 判断两名玩家下一步操作是否合法
        // ab 蛇的身体
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";

            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    /**
     * 向每个玩家广播信息
     * users 中存储了玩家信息 包括wb链接
     */
    private void sendAllMessage(String message) {
        if (WebSocketServer.users.get(playerA.getId()) != null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        if (WebSocketServer.users.get(playerB.getId()) != null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }

    /**
     * 向两个client传递移动信息
     */
    private void sendMove() {  // 向两个Client传递移动信息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将地图转化为字符串便于存储
     *
     * @return
     */
    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    /**
     * 向两个client公布结果
     */
    private void updateUserRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    /**
     * 存储到数据库中
     */
    private void saveToDatabase() {
        // 进行分数存储
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        // A输
        if ("A".equals(loser)) {
            ratingA -= 2;
            ratingB += 5;
        } else if ("B".equals(loser)) { // B输
            ratingA += 5;
            ratingB -= 2;
        }
        // 修改数据库
        updateUserRating(playerA, ratingA);
        updateUserRating(playerB, ratingB);

        // 存储对局记录
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),   // A蛇的操作
                playerB.getStepsString(),   // B蛇的操作
                getMapString(),             // 地图信息
                loser,                      // 对局结果
                new Date()                  // 对局时间
        );

        WebSocketServer.recordMapper.insert(record); // 数据库
    }

    private void sendResult() {  // 向两个Client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        // 对局记录存储数据库
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    @Override
    public void run() {
        // 总过13*14(182)格子, 最多有182个操作,循环1000次
        for (int i = 0; i < 1000; i++) {
            if (nextStep()) {  // 是否获取了两条蛇的下一步操作
                judge(); // 判断两个操作是否合法
                if (status.equals("playing")) { // 对战正在进行
                    // 后端接收到两名玩家的请求, 向两个客户端 同时返回两名玩家的操作
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";    // 标记 对局结束
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all"; // 平局
                    } else if (nextStepA == null) { // A没有输入
                        loser = "A";    // A输
                    } else { // B没有输入
                        loser = "B"; // B输
                    }
                } finally {
                    lock.unlock();
                }
                sendResult(); // 返回结果
                break;
            }
        }
    }
}
