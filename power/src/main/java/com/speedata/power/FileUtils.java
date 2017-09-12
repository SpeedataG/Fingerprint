package com.speedata.power;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ----------Dragon be here!----------/
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃神兽保佑
 * 　　　　┃　　　┃代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━
 *
 * @author :Reginer in  2017/8/4 6:12.
 *         联系方式:QQ:282921012
 *         功能描述:文件操作
 */
public class FileUtils {
    private static final String FILE_PATH = "/system/usr/config.cfg";

    /**
     * 读取文件内容.
     *
     * @return 文件内容
     */
    public static String getTextFromFile() {
        StringBuilder content = new StringBuilder();
        File file = new File(FILE_PATH);
        try {
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            //分行读取
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    /**
     * .
     *
     * @return 文件是否存在
     */
    public static boolean fileExists() {
        File file = new File(FILE_PATH);
        return file.exists();
    }
}
