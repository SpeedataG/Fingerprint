# Fingerprint

可选上电配置：

1.文件上电：首先根据机器型号修改config.cfg文件中的finger部分。

之后将config.cfg文件push到手机目录下 /system/usr/下

可能使用到的命令：

adb remount 

adb push  config.cfg  /system/usr/

之后编译出apk可正常使用

2.指定gpio上电：

可以参照power工程


