# power
指纹上电操作

确认名为config.cfg的配置文件放到/system/usr/下

上电:
```
 try {
            PowerUtils.powerFingerOn();
            Log.d(TAG, " 上电成功");
        } catch (Exception e) {
            Log.e(TAG, " 上电失败:" + Log.getStackTraceString(e));
        }
```
下电:
```
  try {
             PowerUtils.powerFingerOff();
             Log.d(TAG, "下电成功");
         } catch (IOException e) {
             Log.e(TAG, " 下电失败:" + Log.getStackTraceString(e));
         }
```

其中使用到了fastJson1.2.31,如果不想依赖于它，可以

```
  implementation('power:***') {
      exclude group: 'com.alibaba'
  }
```

如果不想使用配置文件的话，可以这样：
```
//指定上电路径为sys/class/misc/mtgpio/pin
FingerGpio power = new FingerGpio("sys/class/misc/mtgpio/pin");
//拉高gpio93和64
power.powerOnDevice(93,64);
//拉低gpio64和93
power.powerOffDevice(64,93);
```



