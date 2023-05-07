package com.kob.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


/**
 * v1.0 添加
 * MiniMax tree search
 *
 * v2.0 优化
 * DEPTH(层数) 减少位置, 由max进入min, 修改为 min进入max时
 * (上右下左)
 *  v2.1更新评估函数
 *
 *
 * v3.0 叶子节点添加
 * Monte Carlo Rollout
 * 每个节点重复n次 rollout, 得到此节点获胜或者失败的概率
 */
public class MiniMaxBot_v2 implements java.util.function.Supplier<Integer> {

    //  位置
    static class Cell {
        public int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    // 玩家位置信息定义为全局变量 (使用双端队列)
    private static List<Cell> aCells = new LinkedList<>();
    private static List<Cell> bCells = new LinkedList<>();

    // minmax 层数
    private static int DEPTH = 10;

    private static final int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    private static int step; // 回合数

    private static int move = -1;

    // 失败分数
    private static final int LOSESCORE = 0;
    // 成功分数
    private static final int WINSCORE = 60000;

    // 检验当前回合，长度是否增加  true 增加, 增加时-头部移动,尾部不变, 不增加-头部移动,尾部删除
    private static boolean checkTailIncreasing(int step) {
        if (step <= 10) return true;    // 前10回合每回合长度+1
        return step % 3 == 1;    // 10回合之后没三回合长度+1
    }

    // 通过操作字符串 返回玩家位置list	   起始坐标			玩家操作信息字符串
    public static List<Cell> getCells(int sx, int sy, String steps) {
        List<Cell> res = new LinkedList<>();
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i++) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!checkTailIncreasing(++step)) { // 长度不增加,
                res.remove(0);
            }
        }
        return res;
    }

    // 地图#自己起始横坐标#自己起始纵坐标#(自己操作)#对手起始横坐标#对手起始纵坐标#(对手操作)
    public static Integer nextMove(String input) {
        String[] strs = input.split("#");    // (#拼接)	棋盘(0/1)#a玩家起始x坐标#a玩家起始y坐标	// 对于棋盘来说,只有可走不可走(0/1)
        int[][] g = new int[13][14];    // 棋盘中 0:可走位置 1:不可走位置
        // 棋盘 13 * 14
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (strs[0].charAt(k) == '1') {    // 棋盘中的墙
                    g[i][j] = 1;
                }
            }
        }

        // 起始坐标
        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        // 把操作 转换为🐍
        aCells = getCells(aSx, aSy, strs[3].substring(1, strs[3].length() - 1)); // (1010101)
        bCells = getCells(bSx, bSy, strs[6].substring(1, strs[6].length() - 1));

        // 回合数 玩家移动次数
        step = strs[3].length() - 2;

        // 将初始🐍转换为地图信息
        for (Cell c : aCells) g[c.x][c.y] = 1;    // a玩家游戏位置
        for (Cell c : bCells) g[c.x][c.y] = 1;    // b玩家游戏位置

        // 特殊情况处理 -----------------------------------
        // 玩家可走当前可走方向数量只有4种 0, 1, 2, 3
        int moveNumber = moveNumber(g, aCells);
        if (moveNumber == 0) { // 0种 表示已经输, 特殊处理, 无需minmax, 随便返回一个方向即可
            return 0;
        }
        if (moveNumber == 1)  // 1种 只能这样走, 特殊处理, 无需minmax, 返回此时能走的方向
            for (int i = 0; i < 4; i++) {
                int x = aCells.get(aCells.size() - 1).x + dx[i];
                int y = aCells.get(aCells.size() - 1).y + dy[i];
                if (isMove(g, x, y))
                    return i;
            }

        int depth = DEPTH; // 深度
        // ?这种是处理什么情况?  忘记了
        while (move == -1 && depth >= 0) {
            max(g, depth--, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        }

        // 玩家必死的情况, minmax没有值, 随机选择一个
        // v2.1更新评估函数, 不需要了
        if (move == -1) {
            for (int i = 0; i < 4; i++) {
                int x = aCells.get(aCells.size() - 1).x + dx[i];
                int y = aCells.get(aCells.size() - 1).y + dy[i];
                if (isMove(g, x, y)) {
                    move = i;
                    break;
                }
            }
        }
        // ------------------------------------------------

        return move; // 返回操作

    }

    // 棋盘中 0:可走位置 1:a玩家位置(本人) 2:b玩家位置(对手) 3:障碍物
    // minmax算法实现	棋盘	 深度: depth回合		α剪枝	β剪枝             全局分数
    public static int max(int[][] g, int depth, int alpha, int beta, int moveScore) {
        step++; // 回合数 ++;
        Cell cell = null;
        int i = 0;
        int score = checkScore(g, aCells, bCells); // 计算分数
        if (score >= WINSCORE) return score;    // a玩家确定胜局 或 预测回合数结束
        if (score <= LOSESCORE) return score;    // a玩家确定胜局 或 预测回合数结束
        if (depth == 0) return score;   // 走到最底层, 返回全局分数

        // move
        for (i = 0; i < 4; i++) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            // 判断位置是否合法(是否能走)
            if (!isMove(g, x, y)) continue;

            // 应该在b计算完分数之后再添加信息, 要不然,b计算的将是a已经领先一步的分数
            // 操作
            g[x][y] = 1;
            aCells.add(new Cell(x, y)); // 更新玩家位置信息, 玩家位置信息为全局变量

            if (!checkTailIncreasing(step)) { // 长度不增加
                aCells.remove(0);
                cell = new Cell(aCells.get(0).x, aCells.get(0).y);
                g[cell.x][cell.y] = 0;
            }

            // 计算对方操作   // 对方的操作会影响我的分数, 计算的依然是我的分数,
            // 回合制,双方同时出手,a和b计算此回合的分数应该是一样的,直接传就行,
            // (假如b要计算的话,此回合a不应该先移动, 应该在b计算完之后移动)
            int value = min(g, depth, alpha, beta, moveScore, score);
            // 还原现场 (可以不用)
            g[x][y] = 0;
            aCells.remove(aCells.size() - 1);
            if (cell != null) {
                aCells.add(0, cell);
                g[cell.x][cell.y] = 1;
            }

            // α剪枝 , 再分数判断中进行方向判断
            if (value > alpha) {
                alpha = value;
                if (depth == DEPTH)
                    move = i;
            }
            if (alpha >= beta) {
                return beta;
            }
        }
        return alpha;
    }

    public static int min(int[][] g, int depth, int alpha, int beta, int moveScore, int score) {

//        int score = checkScore(g, aCells, bCells); // 计算分数
        if (score <= LOSESCORE) return score;    // a玩家输 或 预测回合数结束

        Cell cell = null;
        // b落子
        for (int i = 0; i < 4; i++) {
            int x = bCells.get(bCells.size() - 1).x + dx[i];
            int y = bCells.get(bCells.size() - 1).y + dy[i];

            // 判断位置是否合法(是否能走), 属于分数的范畴,直接失败的操作,单独提取出来
            if (!isMove(g, x, y)) continue;

            // 操作
            g[x][y] = 1;
            bCells.add(new Cell(x, y));

            if (!checkTailIncreasing(step)) { // 长度不增加
                bCells.remove(0);
                cell = new Cell(bCells.get(0).x, bCells.get(0).y);
                g[cell.x][cell.y] = 0;
            }

            int value = max(g, depth - 1, alpha, beta, moveScore);
            // 还原现场
            g[x][y] = 0;
            bCells.remove(bCells.size() - 1);
            if (cell != null) {
                bCells.add(0, cell);
                g[cell.x][cell.y] = 1;
            }

            // β剪枝
            if (value < beta) {
                beta = value;
            }
            if (alpha >= beta) {
                return alpha;
            }
        }
        return beta;
    }

    // 下个位置是可移动
    public static boolean isMove(int[][] g, int x, int y) {
        // 越界
        if (x < 0 || x >= 13 || y < 0 || y >= 14) return false;
        // 碰撞 0:可走位置 1:不可走 玩家位置,障碍物
        if (g[x][y] == 1) return false;

        return true;
    }

    // 此位置下一步可走方向数量
    public static int moveNumber(int[][] g, List<Cell> playerCells) {
        int res = 0;
        for (int i = 0; i < 4; i++) {
            int x = playerCells.get(playerCells.size() - 1).x + dx[i];
            int y = playerCells.get(playerCells.size() - 1).y + dy[i];
            if (isMove(g, x, y))
                res++;
        }
        return res;
    }

    //
    public int checkScoreNot(int[][] g, List<Cell> playerCells, List<Cell> foe) {
        // 失败 自己四个方法无法移动
        if (moveNumber(g, playerCells) == 0) return LOSESCORE;

        // 胜利 对手四个方向无法移动
        if (moveNumber(g, foe) == 0) return WINSCORE;
        return 1;
    }

    // 计算分数 评估函数				自己的信息				对手的信息
    public static int checkScore(int[][] g, List<Cell> playerCells, List<Cell> foe) {
        int[][] gg = g.clone();
        // 失败  玩家四个方法无法移动
        if (moveNumber(gg, playerCells) == 0) return 0;

        // 胜利 对手四个方向无法移动
        if (moveNumber(gg, foe) == 0) return 60000;

        // Monte Carlo Rollout
        // 四个方向随意走,直到成功或者失败,返回移动步数
        // 四个方向随意走,移动10步,返回可走步数

        // 返回当前位置可走步数 (小分数)
        return moveNumber(g, playerCells);

    }

    @Override
    public Integer get() {
        // (#拼接)
        File file = new File("input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
