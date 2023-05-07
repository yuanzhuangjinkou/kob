package com.kob.backend;

import org.junit.jupiter.api.Test;

import java.util.*;

public class MainTest {

    @Test
    public void te01() {

        Random random = new Random();
        int move = 0;
        int j = 0;
        while (true) {
            if (f(move = random.nextInt(4), j = move)) {
                break;
            }
            System.out.println(move + "  " + j);
        }

    }

    public boolean f(int i, int j) {
        if (i == 3) return true;
        return false;
    }


    @Test
    public void te03() {
        Map<String, String> map = new HashMap<>();
        map.put("12", "true");
        map.put("34", "true");
        map.put("56", "false");

        if (map.containsKey("12") && map.get("12").equals("true")) {
            System.out.println("true");
        }
    }


}
