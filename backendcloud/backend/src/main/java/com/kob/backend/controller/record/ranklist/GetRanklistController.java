package com.kob.backend.controller.record.ranklist;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.ranklist.GetRanklistService;
import com.mysql.cj.log.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * 排行榜
 */
@Slf4j
@RestController
public class GetRanklistController {
    @Autowired
    private GetRanklistService getRanklistService;

    @GetMapping("/ranklist/getlist/")
    public JSONObject getList(@RequestParam Map<String, String> data) {
        log.error("排行榜");
        Integer page = Integer.parseInt(data.get("page"));
        System.out.println("page-(GetRanklistController): " + page);
        // 传递 页码
        return getRanklistService.getList(page);
    }
}
