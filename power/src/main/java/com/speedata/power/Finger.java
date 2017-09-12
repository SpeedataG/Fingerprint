package com.speedata.power;

import android.support.annotation.Keep;

import java.util.List;

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
 * @author :Reginer in  2017/8/4 6:27.
 *         联系方式:QQ:282921012
 *         功能描述:
 */
@Keep
public class Finger {

    private FingerBean finger;



    public FingerBean getFinger() {
        return finger;
    }

    public void setFinger(FingerBean finger) {
        this.finger = finger;
    }
    @Keep
    public static class FingerBean {
        /**
         * serialPort :
         * braut : 115200
         * powerType : sys/class/misc/mtgpio/pin
         * gpio : [-1]
         */

        private String serialPort;
        private int braut;
        private String powerType;
        private String powerPath;
        private List<Integer> gpio;

        public String getSerialPort() {
            return serialPort;
        }

        public void setSerialPort(String serialPort) {
            this.serialPort = serialPort;
        }

        public int getBraut() {
            return braut;
        }

        public void setBraut(int braut) {
            this.braut = braut;
        }

        public String getPowerType() {
            return powerType;
        }

        public void setPowerType(String powerType) {
            this.powerType = powerType;
        }

        public String getPowerPath() {
            return powerPath;
        }

        public void setPowerPath(String powerPath) {
            this.powerPath = powerPath;
        }

        public List<Integer> getGpio() {
            return gpio;
        }

        public void setGpio(List<Integer> gpio) {
            this.gpio = gpio;
        }
    }

}
