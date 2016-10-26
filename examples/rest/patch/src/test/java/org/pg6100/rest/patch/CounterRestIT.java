package org.pg6100.rest.patch;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pg6100.utils.web.HttpUtil;
import org.pg6100.utils.web.JBossUtil;

import static org.junit.Assert.*;

public class CounterRestIT {

    @BeforeClass
    public static void initClass(){
        JBossUtil.waitForJBoss(10);
    }


    @Test
    public void testCreate() throws Exception{

        String message = "POST /patch/api/counters HTTP/1.1\n";
        message += "Host:localhost\n";

        message += "\n";

        String response = HttpUtil.executeHttpCommand("localhost",8080, message);

    }
}