package com.spd.tcs1;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.za.finger.ZA_finger;
import com.za.finger.ZAandroid;

import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


	private Button btnopen ;
	private Button btnclose;
	private Button btngetchar;
	private Button btngetimg;
	private Button btneroll;
	private Button btnsearch;
	private Button btnequit;
	private Button btnupchar;
	private Button btndown;
	private ImageView mFingerprintIv ;
	Bitmap bmpDefaultPic;

	private boolean fpflag=false;
	private boolean fpcharflag = false;
	private boolean fpmatchflag = false;
	private boolean fperoll = false;
	private boolean fpsearch = false;
	private boolean isfpon  = false;




	private TextView mtvMessage;
	long ssart = System.currentTimeMillis();
	long ssend = System.currentTimeMillis();
	private Handler objHandler_fp;
	//private HandlerThread thread;



	private int testcount = 0;
	private ZAandroid a6 = new ZAandroid();
	private int fpcharbuf = 1;
	private byte[] pTempletbase = new byte[2304];
	private int IMG_SIZE = 0;//同参数：（0:256x288 1:256x360）

	private String TAG = "zazdemo";
	private int DEV_ADDR = 0xffffffff;
	private byte[] pPassword = new byte[4];
	private Handler objHandler_3 ;
	private int rootqx = 1;///0 noroot  1root
	private int defDeviceType = 2;
	private int defiCom = 3;
	private int defiBaud = 6;
	private boolean isshowbmp = true;

	private int iPageID = 0;
	Context ahandle;
	//////////////////
	private int fpcharlen = 512;
	private int  fpchcount = 2;



	public static final int opensuccess = 101;
	public static final int openfail = 102;
	public static final int usbfail = 103;

	private final Handler m_fEvent = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String temp  = null;
			switch (msg.what) {
				case opensuccess:
					temp =getResources().getString(R.string.opensuccess_str);
					mtvMessage.setText(temp);
					//btnopen.setText(getResources().getString(R.string.close_str));
					break;
				case openfail:
					temp =getResources().getString(R.string.openfail_str);
					mtvMessage.setText(temp);
					//btnopen.setText(getResources().getString(R.string.open_str));
					break;
				case usbfail:
					temp =getResources().getString(R.string.usbfail_str);
					mtvMessage.setText(temp);
					//btnopen.setText(getResources().getString(R.string.open_str));
					break;
			}
		}
	};

	private void Sleep(int times)
	{
		try {
			Thread.sleep(times);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void skipshow(String Str)
	{
		Toast.makeText(ahandle,Str,Toast.LENGTH_SHORT).show();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//按键及页面属性初始化
		mtvMessage = (TextView) findViewById(R.id.textView2);
		mFingerprintIv = (ImageView)findViewById(R.id.imageView1);
		btnopen = (Button) findViewById(R.id.btnopen);
		btnclose = (Button) findViewById(R.id.btnclose);
		btngetchar = (Button) findViewById(R.id.btngetchar);
		btngetimg = (Button) findViewById(R.id.btngetimg);
		btneroll = (Button) findViewById(R.id.btneroll);
		btnsearch = (Button) findViewById(R.id.btnsearch);
		btnequit = (Button) findViewById(R.id.btnequit);
		btnupchar =  (Button) findViewById(R.id.btnupchar);
		btndown =  (Button) findViewById(R.id.btndown);
		btnOnClick();


		objHandler_fp = new Handler();//

		//初始化基本参数
		ahandle = this;		//页面句柄
		rootqx = 1;			//系统权限(0:not root  1:root)
		defDeviceType=2;	//设备通讯类型(2:usb  1:串口)
		defiCom= 6;			//设备波特率(1:9600 2:19200 3:38400 4:57600 6:115200  usb无效)

	}



	private void btnOnClick()
	{
		//打开
		btnopen.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("unused")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] pPassword = new byte[4];
				skipshow("open");
				Runnable r = new Runnable() {
					@Override
					public void run() {
						isusbfinshed = 3;
						ZA_finger fppower = new ZA_finger();
						fppower.finger_power_off();
						Sleep(700);
						OpenDev();
					}
				};
				Thread s = new Thread(r);
				s.start();
			}

		});
		//关闭
		btnclose.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("unused")
			@Override
			public void onClick(View v) {
				CloseDev();
			}
		});
		//获取图像
		btngetimg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setflag(true);
				fpflag = false;
				objHandler_fp.removeCallbacks(fpcharTasks);
				objHandler_fp.removeCallbacks(fperollTasks);
				objHandler_fp.removeCallbacks(fpsearchTasks);
				objHandler_fp.removeCallbacks(fpTasks);
				readsfpimg();
			}
		});
		//获取特征
		btngetchar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setflag(true);
				Sleep(500);
				fpcharflag = false;
				objHandler_fp.removeCallbacks(fpcharTasks);
				objHandler_fp.removeCallbacks(fperollTasks);
				objHandler_fp.removeCallbacks(fpsearchTasks);
				objHandler_fp.removeCallbacks(fpTasks);
				readsfpchar();
			}
		});
		//清空指纹
		btnequit.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setflag(true);
				int Rnet  =  a6.ZAZEmpty(DEV_ADDR);
				String temp =getResources().getString(R.string.equitsuccess_str)+"\r\n";
				iPageID = 0;
				mtvMessage.setText(temp);
			}
		});
		//注册指纹
		btneroll.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setflag(true);
				Sleep(500);
				fperoll = false;
				objHandler_fp.removeCallbacks(fpcharTasks);
				objHandler_fp.removeCallbacks(fperollTasks);
				objHandler_fp.removeCallbacks(fpsearchTasks);
				objHandler_fp.removeCallbacks(fpTasks);

				erollfp();
			}
		});
		//搜索指纹
		btnsearch.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setflag(true);
				Sleep(500);
				fpsearch = false;
				objHandler_fp.removeCallbacks(fpcharTasks);
				objHandler_fp.removeCallbacks(fperollTasks);
				objHandler_fp.removeCallbacks(fpsearchTasks);
				objHandler_fp.removeCallbacks(fpTasks);

				searchfp();
			}
		});



		btnupchar.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] pTemplet = new byte[512];
				int[] iTempletLength = new int[1];
				ssart = System.currentTimeMillis();
				int nRet=a6.ZAZUpChar(DEV_ADDR,a6.CHAR_BUFFER_A, pTemplet, iTempletLength);
				ssend = System.currentTimeMillis();
				long timecount=0;
				timecount = (ssend - ssart);
				if(nRet ==a6.PS_OK)
				{

					String temp=getResources().getString(R.string.upcharsuccess_str)+ timecount;
					// temp +=charToHexString(pTemplet);
					mtvMessage.setText(temp);
				}
			}
		});

		btndown.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] pTemplet = new byte[512];
				int[] iTempletLength = new int[1];
				iTempletLength[0] = 512;
				ssart = System.currentTimeMillis();
				int nRet=a6.ZAZDownChar(DEV_ADDR,a6.CHAR_BUFFER_A, pTemplet, iTempletLength[0]);
				ssend = System.currentTimeMillis();
				long timecount=0;
				timecount = (ssend - ssart);
				if(nRet ==a6.PS_OK)
				{
					String temp=getResources().getString(R.string.downcharsuccess_str) +  timecount;
					// temp +=charToHexString(pTemplet);
					mtvMessage.setText(temp);
				}
				else
				{
					String temp= "下载失败  nRet = " +nRet +"   " +  timecount;
					// temp +=charToHexString(pTemplet);
					mtvMessage.setText(temp);

				}
			}
		});


	}


	//打开设备
	private void OpenDev() {
		// TODO Auto-generated method stub
		Log.i(TAG,"start Opendev");
		int status = -1;
		rootqx = 0;
		if( 1 == rootqx){
			//	skipshow("tryusbroot");
			Log.i(TAG,"use by root ");
			LongDunD8800_CheckEuq();
			status = a6.ZAZOpenDevice(-1, defDeviceType, defiCom, defiBaud, 0, 0);
			Log.i(TAG,"status =  "+status + "  (1:success other：error)");
			if(status == 0 ){
				status = a6.ZAZVfyPwd(DEV_ADDR, pPassword) ;
				a6.ZAZSetImageSize(IMG_SIZE);
			}
			else{
				rootqx = 0;
			}
		}

		//if(false)
		if( 0 == rootqx)
		{
			Log.i(TAG,"use by not root ");
			device = null;
			isusbfinshed  = 0;
			int fd = 0;
			isusbfinshed = getrwusbdevices();
			//skipshow("watting a time");
			Log.i(TAG,"waiting user put root ");
			if(WaitForInterfaces() == false)  {
				m_fEvent.sendMessage(m_fEvent.obtainMessage(usbfail, R.id.btnopen, 0));
				return;
			}
			fd = OpenDeviceInterfaces();
			if(fd == -1)
			{
				m_fEvent.sendMessage(m_fEvent.obtainMessage(usbfail, R.id.btnopen, 0));
				return;
			}
			Log.e(TAG, "open fd: " + fd);
			status = a6.ZAZOpenDevice(fd, defDeviceType, defiCom, defiBaud, 0, 0);
			Log.e("ZAZOpenDeviceEx",""+defDeviceType +"  "+defiCom+"   "+defiBaud +"  status "+status);
			if(status == 0 ){
				status = a6.ZAZVfyPwd(DEV_ADDR, pPassword) ;
				a6.ZAZSetImageSize(IMG_SIZE);
			}
		}
		Log.e(TAG, " open status: " + status);
		if(status == 0){
			m_fEvent.sendMessage(m_fEvent.obtainMessage(opensuccess, R.id.btnopen, 0));
		}
		else{
			m_fEvent.sendMessage(m_fEvent.obtainMessage(openfail, R.id.btnopen, 0));
		}
	}


	//关闭设备
	private void CloseDev()
	{
		//a6.ZAZBT_rev(tmp, tmp.length);
		skipshow("close");
		int status = a6.ZAZCloseDeviceEx();
		ZA_finger fppower = new ZA_finger();
		fppower.finger_power_off();
		Log.e(TAG, " close status: " + status);
		//offLine(false);
		mtvMessage.setText("关闭设备成功");
	}


	//获取图像
	public void readsfpimg()
	{
		ssart = System.currentTimeMillis();
		ssend = System.currentTimeMillis();
		testcount = 0;
		objHandler_fp.postDelayed(fpTasks, 0);
	}
	private Runnable fpTasks = new Runnable() {
		public void run()// 运行该服务执行此函数
		{
			String temp="";
			long st = System.currentTimeMillis();
			long sd = System.currentTimeMillis();
			long timecount=0;
			ssend = System.currentTimeMillis();
			timecount = (ssend - ssart);
			if (timecount >10000)
			{
				temp =getResources().getString(R.string.readfptimeout_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			if(fpflag){
				temp =getResources().getString(R.string.stopgetimage_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			int nRet = 0;
			st = System.currentTimeMillis();
			nRet = a6.ZAZGetImage(DEV_ADDR);
			sd = System.currentTimeMillis();
			timecount = (sd - st);
			temp = getResources().getString(R.string.getimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
			st = System.currentTimeMillis();
			if(nRet  == 0)
			{
				testcount = 0;
				int[] len = { 0, 0 };
				byte[] Image = new byte[256 * 360];
				a6.ZAZUpImage(DEV_ADDR, Image, len);
				sd = System.currentTimeMillis();
				timecount = (sd - st);
				temp += getResources().getString(R.string.upimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
				mtvMessage.setText(temp);

				String str = "/mnt/sdcard/test.bmp";
				a6.ZAZImgData2BMP(Image, str);
				bmpDefaultPic = BitmapFactory.decodeFile(str,null);
				mFingerprintIv.setImageBitmap(bmpDefaultPic);
			}
			else if(nRet==a6.PS_NO_FINGER){
				temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
				mtvMessage.setText(temp);
				objHandler_fp.postDelayed(fpTasks, 100);
			}
			else if(nRet==a6.PS_GET_IMG_ERR){
				temp =getResources().getString(R.string.getimageing_str);
				Log.d(TAG, temp+"2: "+nRet);
				objHandler_fp.postDelayed(fpTasks, 100);
				mtvMessage.setText(temp);
				return;
			}else if(nRet == -2)
			{
				testcount ++;
				if(testcount <3){
					temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"s";
					isfpon = false;
					mtvMessage.setText(temp);
					objHandler_fp.postDelayed(fpTasks, 10);
				}
				else{
					temp =getResources().getString(R.string.Communicationerr_str);
					Log.d(TAG, temp+": "+nRet);
					mtvMessage.setText(temp);
					return;
				}
			}
			else
			{
				temp =getResources().getString(R.string.Communicationerr_str);
				Log.d(TAG, temp+"2: "+nRet);
				mtvMessage.setText(temp);
				return;
			}

		}
	};


	//获取特征
	public void readsfpchar()
	{
		ssart = System.currentTimeMillis();
		ssend = System.currentTimeMillis();
		testcount = 0;
		objHandler_fp.postDelayed(fpcharTasks, 0);
	}
	private Runnable fpcharTasks = new Runnable() {
		public void run()// 运行该服务执行此函数
		{
			String temp="";
			long st = System.currentTimeMillis();
			long sd = System.currentTimeMillis();
			long timecount=0;
			ssend = System.currentTimeMillis();
			timecount = (ssend - ssart);
			if (timecount >10000)
			{
				temp =getResources().getString(R.string.readfptimeout_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			if(fpcharflag){
				temp =getResources().getString(R.string.stopgetchar_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			int nRet = 0;
			st = System.currentTimeMillis();
			nRet = a6.ZAZGetImage(DEV_ADDR);
			sd = System.currentTimeMillis();
			timecount = (sd - st);
			temp = getResources().getString(R.string.getimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
			st = System.currentTimeMillis();

			if(nRet  == 0)
			{
				if(isshowbmp)
				{
					int[] len = { 0, 0 };
					byte[] Image = new byte[256 * 360];
					a6.ZAZUpImage(DEV_ADDR, Image, len);
					sd = System.currentTimeMillis();
					timecount = (sd - st);
					temp += getResources().getString(R.string.upimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
					st = System.currentTimeMillis();
					mtvMessage.setText(temp);

					String str = "/mnt/sdcard/test.bmp";
					a6.ZAZImgData2BMP(Image, str);
					bmpDefaultPic = BitmapFactory.decodeFile(str,null);
					mFingerprintIv.setImageBitmap(bmpDefaultPic);
				}
				nRet= a6.ZAZGenChar(DEV_ADDR, a6.CHAR_BUFFER_A);// != PS_OK) {
				if(nRet ==a6.PS_OK)
				{
					sd = System.currentTimeMillis();
					timecount = (sd - st);
					temp = getResources().getString(R.string.getcharsuccess_str) + "耗时:"+timecount+"ms\r\n";
					st = System.currentTimeMillis();
					int[] iTempletLength = { 0, 0 };
					byte[] pTemplet = new byte[512];
					a6.ZAZSetCharLen(512);
					nRet=a6.ZAZUpChar(DEV_ADDR,a6.CHAR_BUFFER_A, pTemplet, iTempletLength);
					if(nRet ==a6.PS_OK)
					{
						sd = System.currentTimeMillis();
						timecount = (sd - st);
						temp += getResources().getString(R.string.upcharsuccess_str) + "耗时:"+timecount+"ms\r\n";
						st = System.currentTimeMillis();
						temp +=charToHexString(pTemplet,20);
						temp += ".....\r\n";
						st = System.currentTimeMillis();
						mtvMessage.setText(temp);
						Log.e("ssss ","特征: "+charToHexString(pTemplet,512));
					}
					nRet = a6.ZAZDownChar(DEV_ADDR, a6.CHAR_BUFFER_A, pTemplet, iTempletLength[0]);
					if(nRet ==a6.PS_OK)
					{
						sd = System.currentTimeMillis();
						timecount = (sd - st);
						temp += getResources().getString(R.string.downsuccess_str) + "耗时:"+timecount+"ms\r\n";
						st = System.currentTimeMillis();
						mtvMessage.setText(temp);
					}
				}
				else
				{	temp =getResources().getString(R.string.getfailchar_str);
					mtvMessage.setText(temp);
					ssart = System.currentTimeMillis();
					objHandler_fp.postDelayed(fpcharTasks, 1000);

				}
			}
			else if(nRet==a6.PS_NO_FINGER){
				temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
				mtvMessage.setText(temp);
				objHandler_fp.postDelayed(fpcharTasks, 10);
			}else if(nRet==a6.PS_GET_IMG_ERR){
				temp =getResources().getString(R.string.getimageing_str);
				Log.d(TAG, temp+"1: "+nRet);
				objHandler_fp.postDelayed(fpcharTasks, 10);
				mtvMessage.setText(temp);
				return;
			}else if(nRet == -2)
			{
				testcount ++;
				if(testcount <3){
					temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
					isfpon = false;
					mtvMessage.setText(temp);
					objHandler_fp.postDelayed(fpcharTasks, 10);
				}
				else{
					temp =getResources().getString(R.string.Communicationerr_str);
					Log.d(TAG, temp+": "+nRet);
					mtvMessage.setText(temp);
					return;
				}
			}
			else
			{
				temp =getResources().getString(R.string.Communicationerr_str);
				Log.d(TAG, temp+"1: "+nRet);
				mtvMessage.setText(temp);
				return;
			}

		}
	};


	public void erollfp()
	{
		ssart = System.currentTimeMillis();
		ssend = System.currentTimeMillis();
		fpcharbuf= 1;
		isfpon = false;
		testcount = 0;
		objHandler_fp.postDelayed(fperollTasks, 0);
	}

	private Runnable fperollTasks = new Runnable() {
		public void run()// 运行该服务执行此函数
		{
			String temp="";
			long timecount=0;
			ssend = System.currentTimeMillis();
			timecount = (ssend - ssart);
			if (timecount >10000)
			{
				temp =getResources().getString(R.string.readfptimeout_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			if(fperoll){
				temp =getResources().getString(R.string.stoperoll_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			int nRet = 0;
			nRet = a6.ZAZGetImage(DEV_ADDR);
			if(nRet  == 0)
			{
				if(isfpon){
					temp =getResources().getString(R.string.pickupfinger_str);
					mtvMessage.setText(temp);
					ssart = System.currentTimeMillis();
					objHandler_fp.postDelayed(fperollTasks, 100);
					return;
				}
				if(isshowbmp)
				{
					int[] len = { 0, 0 };
					byte[] Image = new byte[256 * 360];
					a6.ZAZUpImage(DEV_ADDR, Image, len);
					String str = "/mnt/sdcard/test.bmp";
					a6.ZAZImgData2BMP(Image, str);
					temp ="获取图像成功";
					mtvMessage.setText(temp);
					Bitmap bmpDefaultPic;
					bmpDefaultPic = BitmapFactory.decodeFile(str,null);
					mFingerprintIv.setImageBitmap(bmpDefaultPic);
				}
				nRet= a6.ZAZGenChar(DEV_ADDR, fpcharbuf);// != PS_OK) {
				if(nRet ==a6.PS_OK  )
				{
					fpcharbuf++;
					isfpon = true;
					if(fpcharbuf > 2){
						nRet = a6.ZAZRegModule(DEV_ADDR);
						if(nRet != a6.PS_OK)
						{
							temp =getResources().getString(R.string.RegModulefail_str);
							mtvMessage.setText(temp);
						}
						else{
							nRet = a6.ZAZStoreChar(DEV_ADDR, 1, iPageID);
							if(nRet == a6.PS_OK){
								temp =getResources().getString(R.string.erollsuccess_str)+iPageID;
								int[] iTempletLength = new int[1];
								nRet=a6.ZAZUpChar(DEV_ADDR,1, pTempletbase, iTempletLength);
								//System.arraycopy(pTemplet, 0, pTempletbase, 0, 2304);
								mtvMessage.setText(temp);
								iPageID++;
							}
							else
							{
								temp =getResources().getString(R.string.erollfail_str);
								mtvMessage.setText(temp);
							}
						}
					}
					else
					{
						temp =getResources().getString(R.string.getfpsuccess_str);
						mtvMessage.setText(temp);
						ssart = System.currentTimeMillis();
						objHandler_fp.postDelayed(fperollTasks, 500);
					}
				}
				else
				{	temp =getResources().getString(R.string.getfailchar_str);
					mtvMessage.setText(temp);
					ssart = System.currentTimeMillis();
					objHandler_fp.postDelayed(fperollTasks, 1000);
				}
			}
			else if(nRet==a6.PS_NO_FINGER){
				temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
				isfpon = false;
				mtvMessage.setText(temp);
				objHandler_fp.postDelayed(fperollTasks, 10);
			}else if(nRet==a6.PS_GET_IMG_ERR){
				temp =getResources().getString(R.string.getimageing_str);
				Log.d(TAG, temp+": "+nRet);
				objHandler_fp.postDelayed(fperollTasks, 10);
				mtvMessage.setText(temp);
				return;
			}else if(nRet == -2)
			{
				testcount ++;
				if(testcount <3){
					temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
					isfpon = false;
					mtvMessage.setText(temp);
					objHandler_fp.postDelayed(fperollTasks, 10);
				}
				else{
					temp =getResources().getString(R.string.Communicationerr_str);
					Log.d(TAG, temp+": "+nRet);
					mtvMessage.setText(temp);

					return;
				}
			}
			else
			{
				temp =getResources().getString(R.string.Communicationerr_str);
				Log.d(TAG, temp+": "+nRet);
				mtvMessage.setText(temp);

				return;
			}

		}
	};




	public void searchfp()
	{
		ssart = System.currentTimeMillis();
		ssend = System.currentTimeMillis();
		fpcharbuf= 1;
		testcount = 0;
		objHandler_fp.postDelayed(fpsearchTasks, 0);
	}

	private Runnable fpsearchTasks = new Runnable() {
		public void run()// 运行该服务执行此函数
		{
			String temp="";
			long st = System.currentTimeMillis();
			long sd = System.currentTimeMillis();
			long timecount=0;
			int[] id_iscore = new int[1];
			ssend = System.currentTimeMillis();
			timecount = (ssend - ssart);

			if (timecount >10000)
			{
				temp =getResources().getString(R.string.readfptimeout_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			if(fpsearch){
				temp =getResources().getString(R.string.stopsearch_str)+"\r\n";
				mtvMessage.setText(temp);
				return;
			}
			int nRet = 0;
			nRet = a6.ZAZGetImage(DEV_ADDR);
			sd = System.currentTimeMillis();
			timecount = (sd - st);
			temp += getResources().getString(R.string.getimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
			st = System.currentTimeMillis();
			if(nRet  == 0)
			{
				if(isshowbmp)
				{
					int[] len = { 0, 0 };
					byte[] Image = new byte[256 * 360];
					a6.ZAZUpImage(DEV_ADDR, Image, len);
					sd = System.currentTimeMillis();
					timecount = (sd - st);
					temp += getResources().getString(R.string.upimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
					st = System.currentTimeMillis();
					String str = "/mnt/sdcard/test.bmp";
					a6.ZAZImgData2BMP(Image, str);
					mtvMessage.setText(temp);
					Bitmap bmpDefaultPic;
					bmpDefaultPic = BitmapFactory.decodeFile(str,null);
					mFingerprintIv.setImageBitmap(bmpDefaultPic);
				}
				nRet= a6.ZAZGenChar(DEV_ADDR, fpcharbuf );// != PS_OK) {
				if(nRet ==a6.PS_OK  )
				{
					sd = System.currentTimeMillis();
					timecount = (sd - st);
					temp += getResources().getString(R.string.getcharsuccess_str) + "耗时:"+timecount+"ms\r\n";
					st = System.currentTimeMillis();
					st = System.currentTimeMillis();
					nRet = a6.ZAZHighSpeedSearch(DEV_ADDR, 1, 0, 1000, id_iscore);
					if(nRet == a6.PS_OK){
						sd = System.currentTimeMillis();
						timecount = (sd - st);
						temp += getResources().getString(R.string.searchsuccess_str) + "耗时:"+timecount+"ms  ID ="+id_iscore[0]+"\r\n";
						st = System.currentTimeMillis();
						mtvMessage.setText(temp);
					}
					else
					{
						temp =getResources().getString(R.string.searchfail_str);
						mtvMessage.setText(temp);
					}

				}
				else
				{	temp =getResources().getString(R.string.getfailchar_str);
					mtvMessage.setText(temp);
					ssart = System.currentTimeMillis();
					objHandler_fp.postDelayed(fpsearchTasks, 1000);

				}

			}
			else if(nRet==a6.PS_NO_FINGER){
				temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
				mtvMessage.setText(temp);
				objHandler_fp.postDelayed(fpsearchTasks, 10);
			}else if(nRet==a6.PS_GET_IMG_ERR){
				temp =getResources().getString(R.string.getimageing_str);
				Log.d(TAG, temp+": "+nRet);
				objHandler_fp.postDelayed(fpsearchTasks, 10);
				mtvMessage.setText(temp);
				return;
			}else if(nRet == -2)
			{
				testcount ++;
				if(testcount <3){
					temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
					isfpon = false;
					mtvMessage.setText(temp);
					objHandler_fp.postDelayed(fpsearchTasks, 10);
				}
				else{
					temp =getResources().getString(R.string.Communicationerr_str);
					Log.d(TAG, temp+": "+nRet);
					mtvMessage.setText(temp);

					return;
				}
			}
			else
			{
				temp =getResources().getString(R.string.Communicationerr_str);
				Log.d(TAG, temp+": "+nRet);
				mtvMessage.setText(temp);

				return;
			}

		}
	};




	private void setflag(boolean value)
	{
		fpflag = value;
		fpcharflag = value;
		fpmatchflag= value;
		fperoll = value;
		fpsearch = value;


	}













	/*****************************************
	 * 线程   end
	 * ***************************************/


	private static String charToHexString(byte[] val,int len) {
		String temp="";
		for(int i=0;i<len;i++)
		{
			String hex = Integer.toHexString(0xff & val[i]);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			temp += hex.toUpperCase();
		}
		return temp;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}





	public int LongDunD8800_CheckEuq()
	{
		Process process = null;
		DataOutputStream os = null;

		// for (int i = 0; i < 10; i++)
		// {
		String path = "/dev/bus/usb/00*/*";
		String path1 = "/dev/bus/usb/00*/*";
		File fpath = new File(path);
		Log.d("*** LongDun D8800 ***", " check path:" + path);
		// if (fpath.exists())
		// {
		String command = "chmod 777 " + path;
		String command1 = "chmod 777 " + path1;
		Log.d("*** LongDun D8800 ***", " exec command:" + command);
		try
		{
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command+"\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			return 1;
		}
		catch (Exception e)
		{
			Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
		}
		//  }
		//  }
		return 0;
	}



	private UsbManager mDevManager = null;
	private PendingIntent permissionIntent = null;
	private UsbInterface intf = null;
	private UsbDeviceConnection connection = null;
	private UsbDevice device = null;
	public int isusbfinshed = 0;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	public int getrwusbdevices() {

		mDevManager = ((UsbManager) this.getSystemService(Context.USB_SERVICE));
		permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		this.registerReceiver(mUsbReceiver, filter);
		//this.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
		HashMap<String, UsbDevice> deviceList = mDevManager.getDeviceList();
		if (true) Log.e(TAG, "news:" + "mDevManager");


		for (UsbDevice tdevice : deviceList.values()) {
			Log.i(TAG,	tdevice.getDeviceName() + " "+ Integer.toHexString(tdevice.getVendorId()) + " "
					+ Integer.toHexString(tdevice.getProductId()));
			if (tdevice.getVendorId() == 0x2109 && (tdevice.getProductId() == 0x7638))
			{
				Log.e(TAG, " 指纹设备准备好了 ");
				mDevManager.requestPermission(tdevice, permissionIntent);
				return 1;
			}
		}
		Log.e(TAG, "news:" + "mDevManager  end");
		return 2;
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(mUsbReceiver);
			isusbfinshed = 0;
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (context) {
					device = (UsbDevice) intent	.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					Log.e("BroadcastReceiver","3333");
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							if (true) Log.e(TAG, "Authorize permission " + device);
							isusbfinshed = 1;
						}
					}
					else {
						if (true) Log.e(TAG, "permission denied for device " + device);
						device=null;
						isusbfinshed = 2;

					}
				}
			}
		}
	};

	public boolean WaitForInterfaces() {

		while (device==null || isusbfinshed == 0) {
			if(isusbfinshed == 2)break;
			if(isusbfinshed == 3)break;
		}
		if(isusbfinshed == 2)
			return false;
		if(isusbfinshed == 3)
			return false;
		return true;
	}

	public int OpenDeviceInterfaces() {
		UsbDevice mDevice = device;
		Log.d(TAG, "setDevice " + mDevice);
		int fd = -1;
		if (mDevice == null) return -1;
		connection = mDevManager.openDevice(mDevice);
		if (!connection.claimInterface(mDevice.getInterface(0), true)) return -1;

		if (mDevice.getInterfaceCount() < 1) return -1;
		intf = mDevice.getInterface(0);

		if (intf.getEndpointCount() == 0) 	return -1;

		if ((connection != null)) {
			if (true) Log.e(TAG, "open connection success!");
			fd = connection.getFileDescriptor();
			return fd;
		}
		else {
			if (true) Log.e(TAG, "finger device open connection FAIL");
			return -1;
		}
	}

}
