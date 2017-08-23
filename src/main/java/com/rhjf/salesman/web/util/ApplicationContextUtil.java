package com.rhjf.salesman.web.util;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by hadoop on 2017/8/18.
 */


final public class ApplicationContextUtil {
    /**
     * 由于applicationContext.xml 的唯一性,在这里可以把它写成单例模式
     */
    private static AbstractApplicationContext applicationContext = null;

    private ApplicationContextUtil() {

    } //空的构造方法

    static {
        /**
         * static 静态代码快,只在该类被加载时,执行一次
         */
        applicationContext = new ClassPathXmlApplicationContext("classpath*:applicationContext.xml");
    }

    public static AbstractApplicationContext getApplicationContext() {

        return applicationContext;
    }

}

