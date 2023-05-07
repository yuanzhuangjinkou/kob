package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;

    private Integer botId;  // -1表示亲自出马，否则表示用AI打

    private String botCode;
    /**
     * 起始坐标
     */
    private Integer sx;

    private Integer sy;
    /**
     * 玩家在这局中的操作方向(0123)  通过 起点坐标和操作方向实现 回放 功能
     */
    private List<Integer> steps;

    /**
     * 前十回合,每回合 + 1,  后续每3回合+1
     * @param step
     * @return
     */
    private boolean check_tail_increasing(int step) {  // 检验当前回合，蛇的长度是否增加 蛇尾
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    /**
     * 存储蛇的身体
     * @return
     */
    public List<Cell> getCells() {
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0; // 回合数
        res.add(new Cell(x, y));
        for (int d: steps) {
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing( ++ step)) { // 当前回合蛇尾不应该增加
                res.remove(0); // 删除多增加的蛇尾
            }
        }
        return res;
    }

    /**
     * 将蛇的操作(0123)转为字符串
     * @return
     */
    public String getStepsString() {
        StringBuilder res = new StringBuilder();
        for (int d: steps) {
            res.append(d);
        }
        return res.toString();
    }
}
