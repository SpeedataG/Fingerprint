package org.zz.jni; 
public class zzFingerAlg{
	
	/**
	 * @author   chen.gs
	 * @category ��ȡ�㷨�汾��
	 * @param    version �C �㷨�汾��100�ֽ�
	 * @return    0 - �ɹ�
	 *           ����  - ʧ��  	
	 * */
	public native int mxGetVersion(byte[] version);
	
	/**
	 * @author   chen.gs
	 * @category ��ָ��ͼ���г�ȡ����
	 * @param    ucImageBuf - ָ��ָ��ͼ�󻺳��ָ�룬ͼ�󻺳�Ϊ30454�ֽ�
	 *           tzBuf      - ָ��ָ���ֳ�¼���ָ������������=344�ֽ�
	 * @return   1 - �ɹ�
	 *           0 - ʧ��	
	 * @see      ͼ���ʽ��ISO��ʽͼ��+8�ֽ�MACֵ
	 * */
	public native int mxGetTzBase64FromISO(byte[] ucImageBuf,byte[] tzBuf);
	
	/**
	 * @author   chen.gs
	 * @category ������ָ�������е�¼ָ��ģ��
	 * @param    tzBuf1  - ָ��ָ������1��ָ�룬����=344�ֽ�(base64)
	 *           tzBuf2  - ָ��ָ������2��ָ�룬����=344�ֽ�(base64)
	 *           tzBuf3  - ָ��ָ������3��ָ�룬����=344�ֽ�(base64) 
	 *           mbBuf   - ָ��ָ��ģ�建������ָ�룬����=344�ֽ�(base64)
	 * @return    > 0    - �ɹ�����ֵ��ʾģ��������Խ������Խ�ߣ�1~100��
	 * 			   0     - ʧ��
	 * */
	public native int mxGetMBBase64(byte[] tzBuf1,byte[] tzBuf2,byte[] tzBuf3,byte[] mbBuf); 
	
	/**
	 * @author   chen.gs
	 * @category �����������ָ������ֵ���бȶ�
	 * @param   mbBuf  - ָ��ָ��ģ���ָ�룬����=344�ֽ�(base64)
	 *          tzBuf  - ָ��ָ��������ָ�룬����=344�ֽ�(base64)
	 *          level  -  ƥ��ȼ�
	 * @return   0 - �ɹ�
	 *          ���� - ʧ��  	
	 * */
	public native int mxFingerMatchBase64(byte[] mbBuf,byte[] tzBuf,int level);  
	

	
	
}
