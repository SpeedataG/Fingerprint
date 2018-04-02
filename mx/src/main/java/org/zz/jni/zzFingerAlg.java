package org.zz.jni;
public class zzFingerAlg{

	/**
	 * @author   chen.gs
	 * @category 获取算法版本号
	 * @param    version – 算法版本，100字节
	 * @return    0 - 成功
	 *           其他  - 失败
	 * */
	public native int mxGetVersion(byte[] version);

	/**
	 * @author   chen.gs
	 * @category 从指纹图象中抽取特征
	 * @param    ucImageBuf - 指向指纹图象缓冲的指针，图象缓冲为30454字节
	 *           tzBuf      - 指针指向现场录入的指纹特征，长度=344字节
	 * @return   1 - 成功
	 *           0 - 失败
	 * @see      图像格式：ISO格式图像+8字节MAC值
	 * */
	public native int mxGetTzBase64FromISO(byte[] ucImageBuf,byte[] tzBuf);

	/**
	 * @author   chen.gs
	 * @category 从三个指纹特征中登录指纹模板
	 * @param    tzBuf1  - 指向指纹特征1的指针，长度=344字节(base64)
	 *           tzBuf2  - 指向指纹特征2的指针，长度=344字节(base64)
	 *           tzBuf3  - 指向指纹特征3的指针，长度=344字节(base64)
	 *           mbBuf   - 指向指纹模板缓冲区的指针，长度=344字节(base64)
	 * @return    > 0    - 成功，数值表示模板质量，越大质量越高（1~100）
	 * 			   0     - 失败
	 * */
	public native int mxGetMBBase64(byte[] tzBuf1,byte[] tzBuf2,byte[] tzBuf3,byte[] mbBuf);

	/**
	 * @author   chen.gs
	 * @category 对输入的两个指纹特征值进行比对
	 * @param   mbBuf  - 指向指纹模板的指针，长度=344字节(base64)
	 *          tzBuf  - 指向指纹特征的指针，长度=344字节(base64)
	 *          level  -  匹配等级
	 * @return   0 - 成功
	 *          其他 - 失败
	 * */
	public native int mxFingerMatchBase64(byte[] mbBuf,byte[] tzBuf,int level);




}
