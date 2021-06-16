package com.example.demo.spi;


public class SPITestChineseServiceImpl implements SPITestService{

    @Override
    public void speak() {
        System.out.println("说中文");
    }
}
