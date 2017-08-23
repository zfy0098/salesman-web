package com.rhjf.salesman.web.test;


import com.rhjf.salesman.core.util.MD5;
import com.rhjf.salesman.core.util.UtilsConstant;

public class Test {

    public static void main(String[] args){
//        for (int i = 0 ; i < 10 ; i ++){
//            System.out.println(UtilsConstant.getUUID());
//        }
        // 为商户分配秘钥
        String termTmkKey = MD5.md5(UtilsConstant.getUUID(), "UTF-8").toUpperCase();
        String tmkKey = MD5.md5(UtilsConstant.getUUID(), "UTF-8").toUpperCase();

        System.out.println("uuid:" + UtilsConstant.getUUID());
        System.out.println("termTmkKey:" + termTmkKey);
        System.out.println("tmkKey：" +  tmkKey);

    }
}
