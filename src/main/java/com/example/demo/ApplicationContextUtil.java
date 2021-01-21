package com.example.demo;

import org.springframework.context.ApplicationContext;

public class ApplicationContextUtil {

    private static ApplicationContext applicationContext;

    // 启动时设置
    public static void setApplicationContext(ApplicationContext applicationContext){
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
}
