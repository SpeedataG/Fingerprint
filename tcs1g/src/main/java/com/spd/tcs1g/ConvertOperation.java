package com.spd.tcs1g;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfj.ImporterImpl;

/**
 * @author :Reginer in  2018/7/10 16:11.
 * 联系方式:QQ:282921012
 * 功能描述:指纹转换方法
 */
public class ConvertOperation {
    /**
     * 指纹特征转换为String
     *
     * @param fmd 指纹特征
     * @return 16进制字符串
     */
    public static String convertToString(Fmd fmd) {
        return bytes2hexStr(fmd.getData());
    }

    /**
     * 16进制字符串转换成指纹特征
     *
     * @param fmdData 16进制fmd特征
     * @return 指纹特征fmd
     */
    public static Fmd convertToFmd(String fmdData) {
        ImporterImpl importer = new ImporterImpl();
        try {
            return importer.ImportFmd(hexStr2Bytes(fmdData), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
        } catch (UareUException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     */
    private static String bytes2hexStr(byte[] b) {
        String tmp;
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            tmp = Integer.toHexString(aB & 0xFF);
            sb.append((tmp.length() == 1) ? "0" + tmp : tmp);
        }
        return sb.toString().toUpperCase().trim();
    }


    /**
     * 十六进制字符串转bytes
     *
     * @param src 16进制字符串
     * @return 字节数组
     */
    private static byte[] hexStr2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

}