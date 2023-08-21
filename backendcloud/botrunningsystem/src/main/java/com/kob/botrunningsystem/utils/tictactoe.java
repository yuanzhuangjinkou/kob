package com.kob.botrunningsystem.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class tictactoe {

    public static void main(String[] args) {

        int[][] g = new int[3][3];

        // 棋盘初始化
        for(int i = 0; i < 3; i ++)
            Arrays.fill(g[i], 0);

        // 对局开始 1, -1
        while (true) {

            // 对局是否结束   0 未结束
            if(checkWin(g) == 0) {
                System.out.println("回合开始");
                // 定义初始数据
                int score = Integer.MIN_VALUE;  // 先手取最大
                int x = 0, y = 0;

                // 模拟落子
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++) {
                        if (g[i][j] == 0) {
                            // 落子
                            g[i][j] = 1;

                            int depth = 0;
                            int tempScore = min(g, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
                            // 还原现场
                            g[i][j] = 0;

                            if (tempScore > score) {
                                score = tempScore;
                                x = i;
                                y = j;
                            }
                        }
                        System.out.println("先手:score:" + score + " x:" + i + " y:" + j);
                    }
                //  模拟落子结束, 落子
                g[x][y] = 1;
                System.out.println("落子-x:" + x + " y:" + y);
                // 先手结束
                print(g);
                // 先手操作后对局结束
//                if(checkWin(g) == 3 || moveList(g).size() == 0)    break;
                // -------------------------------------

                // 后手模拟落子
                score = Integer.MAX_VALUE;
                x = 0; y = 0;
                // 模拟落子
                for(int i = 0; i < 3; i ++)
                    for(int j = 0; j < 3; j ++) {
                        if (g[i][j] == 0) {
                            g[i][j] = -1;
                            int tempScore = max(g, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                            System.out.println("后手:score:" + tempScore + " x:" + i + " y:" + j);
                            // 还原现场
                            g[i][j] = 0;
                            if (tempScore < score) {
                                score = tempScore;
                                x = i;
                                y = j;
                            }
                        }
                        System.out.println("后手:score:" + score + " x:" + i + " y:" + j);
                    }
                g[x][y] = -1;
                System.out.println("落子-x:" + x + " y:" + y);
                print(g);

            } else {
                System.out.println("对局结束 ===============================================");
                break;
            }
        }
    }

    public static int max(int[][] g, int depth, int alpha, int beta) {

        int score = checkWin(g);
        if(score == 3 || score == -3) return score;
        // 返回棋盘可走位置列表
        ArrayList<Pair> pairs = moveList(g);
        // 棋盘已满 return 0;
        if(pairs.size() == 0)   return 0;

        // 棋盘可走位置
        for (Pair pair : pairs) {
            int x = pair.x;
            int y = pair.y;
            g[x][y] = 1;
            int temp = min(g, depth ++, alpha, beta);
            g[x][y] = 0;


            alpha = Math.max(alpha, temp);
            if(alpha >= beta)
                return beta;

        }
        return alpha;
    }

    public static int min(int[][] g, int depth, int alpha, int beta) {

        int score = checkWin(g);
        if(score == 3 || score == -3) return score;
        // 返回棋盘可走位置列表
        ArrayList<Pair> pairs = moveList(g);
        // 棋盘已满 return 0;
        if(pairs.size() == 0)
            return 0;

        // 棋盘可走位置
        for (Pair pair : pairs) {
            int x = pair.x;
            int y = pair.y;
            g[x][y] = -1;
            int temp = max(g, depth ++, alpha, beta);
            g[x][y] = 0;


            beta = Math.min(beta, temp);
            if(alpha >= beta)
                return alpha;


        }
        return beta;
    }

    // 返回棋盘可走位置List
    public static ArrayList<Pair> moveList(int[][] g) {
        ArrayList<Pair> pairs = new ArrayList<>();

        for(int i = 0; i < 3; i ++)
            for(int j = 0; j < 3; j ++)
                if(g[i][j] == 0)
                    pairs.add(new Pair(i, j));
        return pairs;
    }

    // 优化评估函数, 加入概率走法概率
    // 对局是否结束, 玩家获胜  false
    public static int checkWin(int[][] g) {

        //横向检查
        for (int i = 0; i < 3; i++) {
            int sum = g[i][0] + g[i][1] + g[i][2];
            if (sum == 3 || sum == -3) return sum;
        }
        // 检查纵向
        for (int i = 0; i < 3; i++) {
            int sum = g[0][i] + g[1][i] + g[2][i];
            if (sum == 3 || sum == -3) return sum;
        }
        // 检查对角线
        int sum = g[0][0] + g[1][1] + g[2][2];
        if (sum == 3 || sum == -3) return sum;
        sum = g[0][2] + g[1][1] + g[2][0];
        if (sum == 3 || sum == -3) return sum;

        if(moveList(g).size() == 0) return 100;

        // 没有人胜利
        return 0;
    }

    public static void print(int[][] g) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (g[i][j] == 0)
                    System.out.print("| ");
                else if (g[i][j] == 1)
                    System.out.print("|O");
                else if (g[i][j] == -1)
                    System.out.print("|X");
            }
            System.out.print("|");
            System.out.println();
        }
        System.out.println("----------------------------------");
    }

    static class Pair {
        int x;
        int y;
        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }

}
