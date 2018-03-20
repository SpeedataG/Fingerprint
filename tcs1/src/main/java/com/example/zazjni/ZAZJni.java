package com.example.zazjni;


public class ZAZJni {

    /**
     *
     * @param a 指纹特征1
     * @param b 指纹特征2
     * @return 比对分数
     */
    public native int matchTwoChar(byte[] a, byte[] b);

    static {
        System.loadLibrary("zazjni");
    }
}
