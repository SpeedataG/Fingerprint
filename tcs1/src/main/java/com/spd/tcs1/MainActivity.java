package com.spd.tcs1;


import android.app.Activity;
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
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.za.finger.ZA_finger;
import com.za.finger.ZAandroid;

import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;

public class MainActivity extends Activity {


    private Button btnopen;
    private Button btnfpmatch;
    private Button btngetchar;
    private Button btngetimg;
    private Button btneroll;
    private Button btnsearch;
    private Button btnequit;
    private Button btnupchar;
    private Button btndown;

    private boolean fpflag = false;
    private boolean fpcharflag = false;
    private boolean fpmatchflag = false;
    private boolean fperoll = false;
    private boolean fpsearch = false;
    private boolean isfpon = false;

    private int testcount = 0;

    private int fpcharbuf = 1;
    private TextView mtvMessage;
    long ssart = System.currentTimeMillis();
    long ssend = System.currentTimeMillis();
    private Handler objHandler_fp;
    private HandlerThread thread;
    private ImageView mFingerprintIv;

    byte[] pTempletbase = new byte[2304];

    private int IMG_SIZE = 0;//ͬ��������0:256x288 1:256x360��


    ZAandroid a6 = new ZAandroid();
    String TAG = "060";
    int DEV_ADDR = 0xffffffff;
    private Handler objHandler_3;
    String sdCardRoot = Environment.getExternalStorageDirectory()
            .getAbsolutePath();


    private int def_iCom = 3;
    private int def_iBaud = 6;
    private int usborcomtype;///0 noroot  1root
    private int defDeviceType;
    private int defiCom;
    private int defiBaud;

    private int iPageID = 0;
    Activity ahandle;
    //////////////////
    private int fpcharlen = 512;
    private int fpchcount = 2;


    private Spinner mySpinner;
    private ArrayAdapter<String> adapter;
    private Spinner myfpchar;
    private ArrayAdapter<String> adapterfpchar;

    private static final String[] m = {
            "060_usb_v3.0",
            "060_usb_root_v3.0",
            "060_com_v3.0",
            "060_usb_v4.0",
            "060_usb_root_v4.0",
            "060_com_v4.0",
            "050_usb_v3.0",
            "050_usb_root_v3.0",
            "050_com_v3.0",
            "050_usb_v4.0",
            "050_usb_root_v4.0",
            "050_com_v4.0",
            "test"
    };

    private static final String[] m2 = {
            "feature:512        2",
            "feature:1024       4",
            "feature:768        2",
            "feature:1536       4",
            "feature:2304       4"
    };


    public static final int opensuccess = 101;
    public static final int openfail = 102;
    public static final int usbfail = 103;

    private final Handler m_fEvent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String temp = null;
            switch (msg.what) {
                case opensuccess:
                    temp = getResources().getString(R.string.opensuccess_str);
                    mtvMessage.setText(temp);
                    btnopen.setText(getResources().getString(R.string.close_str));
                    break;
                case openfail:
                    temp = getResources().getString(R.string.openfail_str);
                    mtvMessage.setText(temp);
                    btnopen.setText(getResources().getString(R.string.open_str));
                    break;
                case usbfail:
                    temp = getResources().getString(R.string.usbfail_str);
                    mtvMessage.setText(temp);
                    btnopen.setText(getResources().getString(R.string.open_str));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thread = new HandlerThread("MyHandlerThread");
        thread.start();
        objHandler_fp = new Handler();//

        //String fptypes = a6.SetFptype("ZAZ-060","A","002","U");
        //Log.e("fptypes",fptypes);

        /////////////////////
        usborcomtype = 0;
        defDeviceType = 2;
        defiCom = 6;
        defiBaud = def_iBaud;
        ///////////////////////////
        ahandle = this;

        // myTextView = (TextView)findViewById(R.id.TextView_city);
        myfpchar = (Spinner) findViewById(R.id.Spinner_char);

        adapterfpchar = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m2);

        adapterfpchar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        myfpchar.setAdapter(adapterfpchar);

        myfpchar.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                switch (arg2) {
                    case 0:
                        fpcharlen = 512;
                        fpchcount = 2;
                        break;
                    case 1:
                        fpcharlen = 512;
                        fpchcount = 2;
                        break;
                    case 2:
                        fpcharlen = 768;
                        fpchcount = 2;
                        break;
                    case 3:
                        fpcharlen = 1536;
                        fpchcount = 4;
                        break;
                    case 4:
                        fpcharlen = 2304;
                        fpchcount = 4;
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub    
                //  myTextView.setText("NONE");
                arg0.setVisibility(View.VISIBLE);
            }
        });

        // myTextView = (TextView)findViewById(R.id.TextView_city);
        mySpinner = (Spinner) findViewById(R.id.Spinner_city);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mySpinner.setAdapter(adapter);

        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub    
                switch (arg2) {
                    case 0:
                        usborcomtype = 0;
                        defDeviceType = 2;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 1:
                        usborcomtype = 1;
                        defDeviceType = 2;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 2:
                        usborcomtype = 0;
                        defDeviceType = 1;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 3:
                        usborcomtype = 0;
                        defDeviceType = 5;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 4:
                        usborcomtype = 1;
                        defDeviceType = 5;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 5:
                        usborcomtype = 1;
                        defDeviceType = 4;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 6:
                        usborcomtype = 0;
                        defDeviceType = 12;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 7:
                        usborcomtype = 1;
                        defDeviceType = 12;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 8:
                        usborcomtype = 0;
                        defDeviceType = 11;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 9:
                        usborcomtype = 0;
                        defDeviceType = 15;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 10:
                        usborcomtype = 1;
                        defDeviceType = 15;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 11:
                        usborcomtype = 1;
                        defDeviceType = 14;
                        defiCom = def_iCom;
                        defiBaud = def_iBaud;
                        break;
                    case 12:
                        usborcomtype = 0;
                        defDeviceType = 1;
                        defiCom = 21;
                        defiBaud = 12;
                        break;
                    default:
                        ;


                }
                /* ��mySpinner ��ʾ*/
                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub    
                //  myTextView.setText("NONE");
                arg0.setVisibility(View.VISIBLE);
            }

        });

        mtvMessage = (TextView) findViewById(R.id.textView2);
        mFingerprintIv = (ImageView) findViewById(R.id.imageView1);

        btnopen = (Button) findViewById(R.id.btnopen);
        btnfpmatch = (Button) findViewById(R.id.button4);
        btngetchar = (Button) findViewById(R.id.btngetchar);
        btngetimg = (Button) findViewById(R.id.btngetimg);
        btneroll = (Button) findViewById(R.id.btneroll);
        btnsearch = (Button) findViewById(R.id.btnsearch);
        btnequit = (Button) findViewById(R.id.btnequit);
        btnupchar = (Button) findViewById(R.id.btnupchar);
        btndown = (Button) findViewById(R.id.btndown);
        btnOnClick();


    }

    private void OpenDev() {
        // TODO Auto-generated method stub
        byte[] pPassword = new byte[4];
        int status = 0;
        if (1 == usborcomtype) {
            Log.e("test", "  11----> " + System.currentTimeMillis());
            LongDunD8800_CheckEuq();
            Log.e("test", "  22----> " + System.currentTimeMillis());
            status = a6.ZAZOpenDeviceEx(-1, defDeviceType, defiCom, defiBaud, 0, 0);
            if (status == 1 && a6.ZAZVfyPwd(DEV_ADDR, pPassword) == 0) {
                status = 1;
            } else {
                status = 0;
            }
            a6.ZAZSetImageSize(IMG_SIZE);
        } else {
            device = null;
            isusbfinshed = 0;
            int fd = 0;
            isusbfinshed = getrwusbdevices();
            if (WaitForInterfaces() == false) {
                m_fEvent.sendMessage(m_fEvent.obtainMessage(usbfail, R.id.btnopen, 0));
                return;
            }
            fd = OpenDeviceInterfaces();
            if (fd == -1) {
                m_fEvent.sendMessage(m_fEvent.obtainMessage(usbfail, R.id.btnopen, 0));
                return;
            }
            Log.e(TAG, "zhw === open fd: " + fd);
            status = a6.ZAZOpenDeviceEx(fd, 5, 3, 6, 0, 0);
            Log.e("ZAZOpenDeviceEx", "" + defDeviceType + "  " + defiCom + "   " + defiBaud + "  status " + status);
            if (status == 1 && a6.ZAZVfyPwd(DEV_ADDR, pPassword) == 0) {
                status = 1;
            } else {
                status = 0;
            }
            a6.ZAZSetImageSize(IMG_SIZE);
        }
        Log.e(TAG, " open status: " + status);
        if (status == 1) {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(opensuccess, R.id.btnopen, 0));
            //	Toast.makeText(MainActivity.this, getResources().getString(R.string.opensuccess_str),
            //		Toast.LENGTH_SHORT).show();
////			btnopen.setText(getResources().getString(R.string.close_str));
////			String temp =getResources().getString(R.string.opensuccess_str);
////			mtvMessage.setText(temp);
        } else {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(openfail, R.id.btnopen, 0));
            ZA_finger fppower = new ZA_finger();
            fppower.finger_power_off();
//			Toast.makeText(MainActivity.this, getResources().getString(R.string.openfail_str),
//					Toast.LENGTH_SHORT).show();
////			String temp =getResources().getString(R.string.openfail_str);
////			mtvMessage.setText(temp);
        }

    }

    private void btnOnClick() {
        btnopen.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte[] pPassword = new byte[4];

                if (btnopen.getText().equals(getResources().getString(R.string.open_str))) {

                    Runnable r = new Runnable() {
                        public void run() {
                            isusbfinshed = 3;
//							ZA_finger fppower = new ZA_finger();
//							fppower.finger_power_on();
                            try {
                                thread.sleep(700);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            OpenDev();
                        }
                    };
                    Thread s = new Thread(r);
                    s.start();
                } else {
                    byte[] tmp = {5, 6, 7};
                    //a6.ZAZBT_rev(tmp, tmp.length);
                    int status = a6.ZAZCloseDeviceEx();
                    ZA_finger fppower = new ZA_finger();
                    fppower.finger_power_off();
                    Log.e(TAG, " close status: " + status);
                    //offLine(false);
                    mtvMessage.setText("");
                    btnopen.setText(getResources().getString(R.string.open_str));
                    //ZA_finger.fppower(0);
                    //ZA_finger.cardpower(0);

                }
            }

        });


        btnfpmatch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setflag(true);
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fpmatchflag = false;
                objHandler_fp.removeCallbacks(fpmatchTasks);
                objHandler_fp.removeCallbacks(fpcharTasks);
                objHandler_fp.removeCallbacks(fperollTasks);
                objHandler_fp.removeCallbacks(fpsearchTasks);
                objHandler_fp.removeCallbacks(fpTasks);

                readsfpmatch();


            }
        });

        btngetchar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setflag(true);
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fpcharflag = false;
                objHandler_fp.removeCallbacks(fpmatchTasks);
                objHandler_fp.removeCallbacks(fpcharTasks);
                objHandler_fp.removeCallbacks(fperollTasks);
                objHandler_fp.removeCallbacks(fpsearchTasks);
                objHandler_fp.removeCallbacks(fpTasks);

                readsfpchar();

            }
        });

        btngetimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setflag(true);
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fpflag = false;
                objHandler_fp.removeCallbacks(fpmatchTasks);
                objHandler_fp.removeCallbacks(fpcharTasks);
                objHandler_fp.removeCallbacks(fperollTasks);
                objHandler_fp.removeCallbacks(fpsearchTasks);
                objHandler_fp.removeCallbacks(fpTasks);

                readsfpimg();
            }
        });

        btneroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setflag(true);
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fperoll = false;
                objHandler_fp.removeCallbacks(fpmatchTasks);
                objHandler_fp.removeCallbacks(fpcharTasks);
                objHandler_fp.removeCallbacks(fperollTasks);
                objHandler_fp.removeCallbacks(fpsearchTasks);
                objHandler_fp.removeCallbacks(fpTasks);

                erollfp();
            }
        });

        btnsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setflag(true);
                try {
                    thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fpsearch = false;
                objHandler_fp.removeCallbacks(fpmatchTasks);
                objHandler_fp.removeCallbacks(fpcharTasks);
                objHandler_fp.removeCallbacks(fperollTasks);
                objHandler_fp.removeCallbacks(fpsearchTasks);
                objHandler_fp.removeCallbacks(fpTasks);

                searchfp();
            }
        });

        btnequit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setflag(true);
                int Rnet = a6.ZAZEmpty(DEV_ADDR);
                String temp = getResources().getString(R.string.equitsuccess_str) + "\r\n";
                iPageID = 0;
                mtvMessage.setText(temp);

            }
        });

        btnupchar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte[] pTemplet = new byte[512];
                int[] iTempletLength = new int[1];
                ssart = System.currentTimeMillis();
                int nRet = a6.ZAZUpChar(DEV_ADDR, a6.CHAR_BUFFER_A, pTemplet, iTempletLength);
                ssend = System.currentTimeMillis();
                long timecount = 0;
                timecount = (ssend - ssart);
                if (nRet == a6.PS_OK) {

                    String temp = getResources().getString(R.string.upcharsuccess_str) + timecount;
                    // temp +=charToHexString(pTemplet);
                    mtvMessage.setText(temp);
                }
            }
        });

        btndown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte[] pTemplet = new byte[512];
                int[] iTempletLength = new int[1];
                iTempletLength[0] = 512;
                ssart = System.currentTimeMillis();
                int nRet = a6.ZAZDownChar(DEV_ADDR, a6.CHAR_BUFFER_A, pTemplet, iTempletLength[0]);
                ssend = System.currentTimeMillis();
                long timecount = 0;
                timecount = (ssend - ssart);
                if (nRet == a6.PS_OK) {
                    String temp = getResources().getString(R.string.downcharsuccess_str) + timecount;
                    // temp +=charToHexString(pTemplet);
                    mtvMessage.setText(temp);
                } else {
                    String temp = "����ʧ��  nRet = " + nRet + "   " + timecount;
                    // temp +=charToHexString(pTemplet);
                    mtvMessage.setText(temp);

                }
            }
        });


    }

    private void setflag(boolean value) {
        fpflag = value;
        fpcharflag = value;
        fpmatchflag = value;
        fperoll = value;
        fpsearch = value;


    }


    /*****************************************
     * �߳�   start
     * ***************************************/

    public void readsfpmatch() {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        fpcharbuf = 1;
        isfpon = false;
        testcount = 0;
        objHandler_fp.postDelayed(fpmatchTasks, 0);
    }

    private Runnable fpmatchTasks = new Runnable() {
        public void run()// ���и÷���ִ�д˺���
        {
            String temp = "";
            long timecount = 0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);

            if (timecount > 10000) {
                temp = getResources().getString(R.string.readfptimeout_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            if (fpmatchflag) {
                temp = getResources().getString(R.string.stopmatch_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testcount = 0;
                try {
                    thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }
            if (nRet == 0) {
                if (isfpon) {
                    temp = getResources().getString(R.string.pickupfinger_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpmatchTasks, 100);
                    return;
                }

                //nRet = a6.ZAZLoadChar( DEV_ADDR,2,1);
                //a6.ZAZSetCharLen(2304);
                //nRet = a6.ZAZDownChar(DEV_ADDR, 2, pTempletbase, 2304);
                nRet = a6.ZAZGenChar(DEV_ADDR, fpcharbuf);// != PS_OK) {
                if (nRet == a6.PS_OK) {
                    if (fpcharbuf != 1) {
                        int[] iScore = {0, 0};
                        nRet = a6.ZAZMatch(DEV_ADDR, iScore);
                        if (nRet == a6.PS_OK) {
                            temp = getResources().getString(R.string.matchsuccess_str) + iScore[0];
                            mtvMessage.setText(temp);
                        } else {
                            temp = getResources().getString(R.string.matchfail_str) + iScore[0];
                            mtvMessage.setText(temp);
                        }
                        return;
                    }

                    fpcharbuf = 2;
                    isfpon = true;
                    temp = getResources().getString(R.string.putyourfinger_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpmatchTasks, 100);
                } else {
                    temp = getResources().getString(R.string.getfailchar_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpmatchTasks, 1000);

                }

            } else if (nRet == a6.PS_NO_FINGER) {
                temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                isfpon = false;
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpmatchTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp + ": " + nRet);
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpmatchTasks, 10);
                //mtvMessage.setText(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                    isfpon = false;
                    mtvMessage.setText(temp);
                    objHandler_fp.postDelayed(fpmatchTasks, 10);
                } else {
                    temp = getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp + ": " + nRet);
                    mtvMessage.setText(temp);

                    return;
                }
            } else {
                temp = getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp + ": " + nRet);
                mtvMessage.setText(temp);

                return;
            }
        }
    };


    public void readsfpchar() {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        testcount = 0;
        objHandler_fp.postDelayed(fpcharTasks, 0);
    }

    private Runnable fpcharTasks = new Runnable() {
        public void run()// ���и÷���ִ�д˺���
        {
            String temp = "";
            long timecount = 0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);

            if (timecount > 10000) {
                temp = getResources().getString(R.string.readfptimeout_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }

            if (fpcharflag) {
                temp = getResources().getString(R.string.stopgetchar_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testcount = 0;
                try {
                    thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }
            if (nRet == 0) {

//	 		{
//		 		int[] len = { 0, 0 };
//				byte[] Image = new byte[256 * 360];
//				a6.ZAZUpImage(DEV_ADDR, Image, len); 
//				String str = "/mnt/sdcard/test.bmp";
//				a6.ZAZImgData2BMP(Image, str);
//				temp ="��ȡͼ��ɹ�";
//		 		mtvMessage.setText(temp);
//				
//				Bitmap bmpDefaultPic;
//				bmpDefaultPic = BitmapFactory.decodeFile(str,null);
//				mFingerprintIv.setImageBitmap(bmpDefaultPic);			
//	 		}
                nRet = a6.ZAZGenChar(DEV_ADDR, a6.CHAR_BUFFER_A);// != PS_OK) {
                if (nRet == a6.PS_OK) {
                    int[] iTempletLength = {0, 0};
                    byte[] pTemplet = new byte[2304];
                    //a6.ZAZSetCharLen(512);
                    nRet = a6.ZAZUpChar(DEV_ADDR, a6.CHAR_BUFFER_A, pTemplet, iTempletLength);
                    if (nRet == a6.PS_OK) {

                        //temp="ָ������:"+iTempletLength[0] +" \r\n";
                        temp = charToHexString(pTemplet);
                        mtvMessage.setText(temp);
                    }
                    nRet = a6.ZAZDownChar(DEV_ADDR, a6.CHAR_BUFFER_A, pTemplet, iTempletLength[0]);
                    if (nRet == a6.PS_OK) {
                        temp += getResources().getString(R.string.downsuccess_str);
                        mtvMessage.setText(temp);
                    }
//	 			else
//		 		{	
//	 				temp ="�ϴ�����ʧ�ܣ�������¼��";
//		 			mtvMessage.setText(temp);
//		 			ssart = System.currentTimeMillis();
//				 	objHandler_fp.postDelayed(fpcharTasks, 1000);
//		 			
//		 		}

                } else {
                    temp = getResources().getString(R.string.getfailchar_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpcharTasks, 1000);

                }
//				Log.d(TAG, "Gen Char fail!");
//			}
            } else if (nRet == a6.PS_NO_FINGER) {
                temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpcharTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp + "1: " + nRet);
                objHandler_fp.postDelayed(fpcharTasks, 10);
                mtvMessage.setText(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                    isfpon = false;
                    mtvMessage.setText(temp);
                    objHandler_fp.postDelayed(fpmatchTasks, 10);
                } else {
                    temp = getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp + ": " + nRet);
                    mtvMessage.setText(temp);

                    return;
                }
            } else {
                temp = getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp + "1: " + nRet);
                mtvMessage.setText(temp);
                return;
            }

        }
    };


    public void readsfpimg() {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        testcount = 0;
        objHandler_fp.postDelayed(fpTasks, 0);
    }

    private Runnable fpTasks = new Runnable() {
        public void run()// ���и÷���ִ�д˺���
        {
            String temp = "";
            long timecount = 0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);

            if (timecount > 10000) {
                temp = getResources().getString(R.string.readfptimeout_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            if (fpflag) {
                temp = getResources().getString(R.string.stopgetimage_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testcount = 0;
                int[] len = {0, 0};
                byte[] Image = new byte[256 * 360];
                a6.ZAZUpImage(DEV_ADDR, Image, len);
                String str = "/mnt/sdcard/test.bmp";
                a6.ZAZImgData2BMP(Image, str);
                temp = getResources().getString(R.string.getimagesuccess_str);
                mtvMessage.setText(temp);

                Bitmap bmpDefaultPic;
                bmpDefaultPic = BitmapFactory.decodeFile(str, null);
                mFingerprintIv.setImageBitmap(bmpDefaultPic);
            } else if (nRet == a6.PS_NO_FINGER) {
                temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpTasks, 100);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp + "2: " + nRet);
                objHandler_fp.postDelayed(fpTasks, 100);
                mtvMessage.setText(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                    isfpon = false;
                    mtvMessage.setText(temp);
                    objHandler_fp.postDelayed(fpmatchTasks, 10);
                } else {
                    temp = getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp + ": " + nRet);
                    mtvMessage.setText(temp);

                    return;
                }
            } else {
                temp = getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp + "2: " + nRet);
                mtvMessage.setText(temp);
                return;
            }

        }
    };

    public void erollfp() {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        fpcharbuf = 1;
        isfpon = false;
        testcount = 0;
        objHandler_fp.postDelayed(fperollTasks, 0);
    }

    private Runnable fperollTasks = new Runnable() {
        public void run()// ���и÷���ִ�д˺���
        {
            String temp = "";
            long timecount = 0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);

            if (timecount > 10000) {
                temp = getResources().getString(R.string.readfptimeout_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            if (fperoll) {
                temp = getResources().getString(R.string.stoperoll_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testcount = 0;
                try {
                    thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }

            if (nRet == 0) {
                if (isfpon) {
                    temp = getResources().getString(R.string.pickupfinger_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fperollTasks, 100);
                    return;
                }
//		 		{
//			 		int[] len = { 0, 0 };
//					byte[] Image = new byte[256 * 360];
//					a6.ZAZUpImage(DEV_ADDR, Image, len); 
//					String str = "/mnt/sdcard/test.bmp";
//					a6.ZAZImgData2BMP(Image, str);
//					temp ="��ȡͼ��ɹ�";
//			 		mtvMessage.setText(temp);
//					
//					Bitmap bmpDefaultPic;
//					bmpDefaultPic = BitmapFactory.decodeFile(str,null);
//					mFingerprintIv.setImageBitmap(bmpDefaultPic);			
//		 		}
                nRet = a6.ZAZGenChar(DEV_ADDR, fpcharbuf);// != PS_OK) {
                if (nRet == a6.PS_OK) {
                    fpcharbuf++;
                    isfpon = true;
                    if (fpcharbuf > 2) {
                        nRet = a6.ZAZRegModule(DEV_ADDR);
                        if (nRet != a6.PS_OK) {
                            temp = getResources().getString(R.string.RegModulefail_str);
                            mtvMessage.setText(temp);

                        } else {
                            nRet = a6.ZAZStoreChar(DEV_ADDR, 1, iPageID);
                            if (nRet == a6.PS_OK) {
                                temp = getResources().getString(R.string.erollsuccess_str) + iPageID;
                                int[] iTempletLength = new int[1];
                                nRet = a6.ZAZUpChar(DEV_ADDR, 1, pTempletbase, iTempletLength);
                                //System.arraycopy(pTemplet, 0, pTempletbase, 0, 2304);
                                mtvMessage.setText(temp);
                                iPageID++;
                            } else {
                                temp = getResources().getString(R.string.erollfail_str);
                                mtvMessage.setText(temp);
                            }
                        }
                    } else {
                        temp = getResources().getString(R.string.getfpsuccess_str);
                        mtvMessage.setText(temp);
                        ssart = System.currentTimeMillis();
                        objHandler_fp.postDelayed(fperollTasks, 500);

                    }
                } else {
                    temp = getResources().getString(R.string.getfailchar_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fperollTasks, 1000);

                }

            } else if (nRet == a6.PS_NO_FINGER) {
                temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                isfpon = false;
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fperollTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp + ": " + nRet);
                objHandler_fp.postDelayed(fperollTasks, 10);
                mtvMessage.setText(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                    isfpon = false;
                    mtvMessage.setText(temp);
                    objHandler_fp.postDelayed(fpmatchTasks, 10);
                } else {
                    temp = getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp + ": " + nRet);
                    mtvMessage.setText(temp);

                    return;
                }
            } else {
                temp = getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp + ": " + nRet);
                mtvMessage.setText(temp);

                return;
            }

        }
    };


    public void searchfp() {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        fpcharbuf = 1;
        testcount = 0;
        objHandler_fp.postDelayed(fpsearchTasks, 0);
    }

    private Runnable fpsearchTasks = new Runnable() {
        public void run()// ���и÷���ִ�д˺���
        {
            String temp = "";
            long timecount = 0;
            int[] id_iscore = new int[1];
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);

            if (timecount > 10000) {
                temp = getResources().getString(R.string.readfptimeout_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            if (fpsearch) {
                temp = getResources().getString(R.string.stopsearch_str) + "\r\n";
                mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                testcount = 0;
                try {
                    thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                nRet = a6.ZAZGetImage(DEV_ADDR);
            }
            if (nRet == 0) {

                nRet = a6.ZAZGenChar(DEV_ADDR, fpcharbuf);// != PS_OK) {
                if (nRet == a6.PS_OK) {
                    nRet = a6.ZAZHighSpeedSearch(DEV_ADDR, 1, 0, 1000, id_iscore);
                    if (nRet == a6.PS_OK) {
                        temp = getResources().getString(R.string.searchsuccess_str) + id_iscore[0];
                        mtvMessage.setText(temp);
                    } else {
                        temp = getResources().getString(R.string.searchfail_str);
                        mtvMessage.setText(temp);
                    }

                } else {
                    temp = getResources().getString(R.string.getfailchar_str);
                    mtvMessage.setText(temp);
                    ssart = System.currentTimeMillis();
                    objHandler_fp.postDelayed(fpsearchTasks, 1000);

                }

            } else if (nRet == a6.PS_NO_FINGER) {
                temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpsearchTasks, 10);
            } else if (nRet == a6.PS_GET_IMG_ERR) {
                temp = getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp + ": " + nRet);
                objHandler_fp.postDelayed(fpsearchTasks, 10);
                mtvMessage.setText(temp);
                return;
            } else if (nRet == -2) {
                testcount++;
                if (testcount < 3) {
                    temp = getResources().getString(R.string.readingfp_str) + ((10000 - (ssend - ssart))) / 1000 + "s";
                    isfpon = false;
                    mtvMessage.setText(temp);
                    objHandler_fp.postDelayed(fpmatchTasks, 10);
                } else {
                    temp = getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp + ": " + nRet);
                    mtvMessage.setText(temp);

                    return;
                }
            } else {
                temp = getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp + ": " + nRet);
                mtvMessage.setText(temp);

                return;
            }

        }
    };


    /*****************************************
     * �߳�   end
     * ***************************************/


    private static String charToHexString(byte[] val) {
        String temp = "";
        for (int i = 0; i < val.length; i++) {
            String hex = Integer.toHexString(0xff & val[i]);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            temp += hex.toUpperCase();
        }
        return temp;
    }


    public int LongDunD8800_CheckEuq() {
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
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return 1;
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: " + e.getMessage());
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
            Log.i(TAG, tdevice.getDeviceName() + " " + Integer.toHexString(tdevice.getVendorId()) + " "
                    + Integer.toHexString(tdevice.getProductId()));
            if (tdevice.getVendorId() == 0x2109 && (tdevice.getProductId() == 0x7638)) {
                Log.e(TAG, " ָ���豸׼������ ");
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
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.e("BroadcastReceiver", "3333");
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            if (true) Log.e(TAG, "Authorize permission " + device);
                            isusbfinshed = 1;
                        }
                    } else {
                        if (true) Log.e(TAG, "permission denied for device " + device);
                        device = null;
                        isusbfinshed = 2;

                    }
                }
            }
        }
    };

    public boolean WaitForInterfaces() {

        while (device == null || isusbfinshed == 0) {
            if (isusbfinshed == 2) break;
            if (isusbfinshed == 3) break;
        }
        if (isusbfinshed == 2)
            return false;
        if (isusbfinshed == 3)
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

        if (intf.getEndpointCount() == 0) return -1;

        if ((connection != null)) {
            if (true) Log.e(TAG, "open connection success!");
            fd = connection.getFileDescriptor();
            return fd;
        } else {
            if (true) Log.e(TAG, "finger device open connection FAIL");
            return -1;
        }
    }

}
