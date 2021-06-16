package com.example.demo.spi;


public class SPITestEnglishServiceImpl implements SPITestService{

    @Override
    public void speak() {
        System.out.println("说英文");
    }
}
