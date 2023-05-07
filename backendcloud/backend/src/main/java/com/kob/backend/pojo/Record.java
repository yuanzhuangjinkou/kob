package com.kob.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 对局记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    @TableId(type = IdType.AUTO)

    private Integer id;

    /**
     * a  id 和 起始坐标
     */
    private Integer aId;

    private Integer aSx;

    private Integer aSy;

    /**
     * b  id 和 起始坐标
     */
    private Integer bId;

    private Integer bSx;

    private Integer bSy;

    /**
     * a的所有操作   01234
     */
    private String aSteps;

    /**
     * bd所有操作   -1234
     */
    private String bSteps;

    /**
     * 地图
     */
    private String map;

    /**
     * 输赢
     * all:平局  A: A输  B: B输
     */
    private String loser;

    /**
     * 对局时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createtime;
}
