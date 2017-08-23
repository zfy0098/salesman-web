package com.rhjf.salesman.web.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
* @author zzg
*
* @version 1.0
*
* 创建时间：2017年6月27日 下午6:42:55
*
* @ClassName 类名称
*
* @Description 类描述
*/
@Transactional
@Component
public class TestMessageHandler {

    private Logger logger = LoggerFactory.getLogger(TestMessageHandler.class);

    public void handleMessage(String message) {
        try {
            long begin = System.currentTimeMillis();
            logger.info("deal handler..." + message);
            long end = System.currentTimeMillis();
            logger.info("deal cost " + String.valueOf((end - begin) / 1000) + " seconds");
        } catch (NumberFormatException e) {


        } catch (Exception e) {


        }
    }


}
