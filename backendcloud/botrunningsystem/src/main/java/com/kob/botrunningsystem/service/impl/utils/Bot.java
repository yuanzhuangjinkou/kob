package com.kob.botrunningsystem.service.impl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bot执行代码
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bot {

    Integer userId;

    /**
     *  bot代码
     */
    String botCode;

    /**
     * 地图信息
     */
    String input;

}
