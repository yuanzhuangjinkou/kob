package com.kob.backend.controller.record;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.record.GetRecordListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 对局记录
 */
@Slf4j
@RestController
public class GetRecordListController {
    @Autowired
    private GetRecordListService getRecordListService;

    @GetMapping("/record/getlist/")
    JSONObject getList(@RequestParam Map<String, String> data) {
        log.info("对局记录");
        Integer page = Integer.parseInt(data.get("page"));
        System.out.println(page);
        // 传页码
        return getRecordListService.getList(page);
    }
}
