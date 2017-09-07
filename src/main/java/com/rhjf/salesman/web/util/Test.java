package com.rhjf.salesman.web.util;

import java.util.UUID;

/**
 * Created by hadoop on 2017/8/29.
 */
public class Test {

    public static void main(String[] args){
        for (int i =0 ; i < 5 ; i++){
            System.out.println(UUID.randomUUID().toString().toUpperCase());
        }
    }
}
