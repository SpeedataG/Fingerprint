package com.spd.mx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import org.zz.jni.zzFingerAlg;
import org.zz.mxusbfingerdriver.MXFingerDriver;

import java.util.Calendar;

/**
 * @author :Reginer in  2018/4/2 11:03.
 * 联系方式:QQ:282921012
 * 功能描述:中正指纹demo
 */
public class MainActivity extends AppCompatActivity {

    private static final int PROMTP_MSG = 0;      // 提示信息
    private static final int SUCCESS_MSG = 1;      // 成功
    private static final int FAILED_MSG = 2;       // 失败
    private static final int IMG_SUCCESS_MSG = 3; // 获取图像成功

    // 图像
    private static final int TIME_OUT = 15 * 1000; // 等待按手指的超时时间，单位：毫
    private static final int IMAGE_X = 152;
    private static final int IMAGE_Y = 200;
    private static final int IMAGE_SIZE = IMAGE_X * IMAGE_Y + 54;
    private byte[] m_bImgBuf = new byte[IMAGE_SIZE];

    //特征、模板
    private static final int TZ_SIZE = 344;
    private byte[] m_bFingerTz = new byte[TZ_SIZE];
    private byte[] m_bFingerMb = new byte[TZ_SIZE];

    // 线程
    private GetImageThread m_GetImageThread = null;
    private FingerEnrollThread m_FingerEnrollThread = null;
    private FingerMatchThread m_FingerMatchThread = null;
    private GetDevVersionThread m_GetDevVersionThread = null;

    // 中正指纹仪驱动
    MXFingerDriver fingerDriver;

    //中正指纹JNI算法库
    static {
        System.loadLibrary("FingerAlg");
    }

    zzFingerAlg fingeralg;

    // 定义一个负责更新的进度的Handler
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
        setContentView(R.layout.activity_main);

        fingerDriver = new MXFingerDriver(this, LinkDetectedHandler);

        fingeralg = new zzFingerAlg();
    }

    /* Toast控件显示提示信息 */
    public void DisplayToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void EnableButton(Boolean bFlag) {
        Button btn_getImage = (Button) findViewById(R.id.btn_getImage);
        btn_getImage.setEnabled(bFlag);
        Button btn_fingerEnroll = (Button) findViewById(R.id.btn_fingerEnroll);
        btn_fingerEnroll.setEnabled(bFlag);
        Button btn_fingerMatch = (Button) findViewById(R.id.btn_fingerMatch);
        btn_fingerMatch.setEnabled(bFlag);
        Button btn_getDriverVersion = (Button) findViewById(R.id.btn_getDriverVersion);
        btn_getDriverVersion.setEnabled(bFlag);
        Button btn_getDevVersion = (Button) findViewById(R.id.btn_getDevVersion);
        btn_getDevVersion.setEnabled(bFlag);
        Button btn_getAlgVersion = (Button) findViewById(R.id.btn_getAlgVersion);
        btn_getAlgVersion.setEnabled(bFlag);
    }

    /**
     * 提示信息
     */
    private void ShowMessage(String strMsg, Boolean bAdd) {
        EditText edit_show_msg = (EditText) findViewById(R.id.edit_show_msg);
        if (bAdd) {
            String strShowMsg = edit_show_msg.getText().toString();
            strMsg = strShowMsg + strMsg;
        }
        edit_show_msg.setText(strMsg + "\r\n");
        ScrollView scrollView_show_msg = (ScrollView) findViewById(R.id.scrollView_show_msg);
        scrollToBottom(scrollView_show_msg, edit_show_msg);
    }

    public static void scrollToBottom(final View scroll, final View inner) {
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });
    }

    private void SendMsg(int what, String obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        message.arg1 = 0;
        LinkDetectedHandler.sendMessage(message);
    }

    private Handler LinkDetectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROMTP_MSG:
                    ShowMessage("" + msg.obj, true);
                    EnableButton(false);
                    break;
                case SUCCESS_MSG:
                case FAILED_MSG:
                    ShowMessage("" + msg.obj, true);
                    EnableButton(true);
                    break;
                case IMG_SUCCESS_MSG:
                    Bitmap bm1 = fingerDriver.Iso2Bimap(m_bImgBuf);
                    if (bm1 != null) {
                        ImageView image_open = (ImageView) findViewById(R.id.image_open);
                        image_open.setImageBitmap(bm1);
                    }
                    //DisplayToast((String) msg.obj);
                    ShowMessage("" + msg.obj, true);
                    EnableButton(true);
                    break;
                default:
                    ShowMessage("" + msg.obj, true);
                    break;
            }
        }
    };

    /**
     * 功能：获取图像
     */
    public void OnClickGetImage(View view) {
        if (m_GetImageThread != null) {
            m_GetImageThread.interrupt();
            m_GetImageThread = null;
        }
        m_GetImageThread = new GetImageThread();
        m_GetImageThread.start();
    }

    private class GetImageThread extends Thread {
        @Override
        public void run() {
            try {
                GetImage();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void GetImage() {
        int iRV = 0;
        SendMsg(PROMTP_MSG, getString(R.string.btn_getImage).toString()
                + ","
                + getString(R.string.str_promptPressFinger).toString() + "...");
        Calendar time1 = Calendar.getInstance();
        iRV = fingerDriver.mxGetIsoImage(m_bImgBuf, IMAGE_SIZE, TIME_OUT, 0);
        Calendar time2 = Calendar.getInstance();
        long bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
        if (iRV != 0) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString() + ",iRV=" + iRV);
            return;
        }
        SendMsg(IMG_SUCCESS_MSG, getString(R.string.str_success).toString() + "," +
                getString(R.string.str_time).toString() + "：" + bt_time + "ms");
    }

    /**
     * 功能：指纹注册
     */
    public void OnClickFingerEnroll(View view) {
        if (m_FingerEnrollThread != null) {
            m_FingerEnrollThread.interrupt();
            m_FingerEnrollThread = null;
        }
        m_FingerEnrollThread = new FingerEnrollThread();
        m_FingerEnrollThread.start();
    }

    private class FingerEnrollThread extends Thread {
        @Override
        public void run() {
            try {
                FingerEnroll();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void FingerEnroll() {
        int iRV = 0;
        byte[] bFingerTz1 = new byte[TZ_SIZE];
        byte[] bFingerTz2 = new byte[TZ_SIZE];
        byte[] bFingerTz3 = new byte[TZ_SIZE];
        // 1
        SendMsg(PROMTP_MSG, getString(R.string.btn_fingerEnroll).toString()
                + ","
                + getString(R.string.str_promptPressThreeFinger).toString() + "...");
        iRV = fingerDriver.mxGetIsoImage(m_bImgBuf, IMAGE_SIZE, TIME_OUT, 0);
        if (iRV != 0) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString() + ",iRV=" + iRV);
            return;
        }
        iRV = fingeralg.mxGetTzBase64FromISO(m_bImgBuf, bFingerTz1);
        if (iRV != 1) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString());
            return;
        }
        // 2
        SendMsg(PROMTP_MSG, getString(R.string.btn_fingerEnroll).toString()
                + "," + getString(R.string.str_promptPress2ndFinger).toString() + "...");
        iRV = fingerDriver.mxGetIsoImage(m_bImgBuf, IMAGE_SIZE, TIME_OUT, 0);
        if (iRV != 0) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString() + ",iRV=" + iRV);
            return;
        }
        iRV = fingeralg.mxGetTzBase64FromISO(m_bImgBuf, bFingerTz2);
        if (iRV != 1) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString());
            return;
        }
        // 3
        SendMsg(PROMTP_MSG, getString(R.string.btn_fingerEnroll).toString()
                + "," + getString(R.string.str_promptPress3rdFinger).toString() + "...");
        iRV = fingerDriver.mxGetIsoImage(m_bImgBuf, IMAGE_SIZE, TIME_OUT, 0);
        if (iRV != 0) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString() + ",iRV=" + iRV);
            return;
        }
        iRV = fingeralg.mxGetTzBase64FromISO(m_bImgBuf, bFingerTz3);
        if (iRV != 1) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString());
            return;
        }
        // 合并
        iRV = fingeralg.mxGetMBBase64(bFingerTz1, bFingerTz2, bFingerTz3,
                m_bFingerMb);
        if (iRV <= 0) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString());
            return;
        }
        SendMsg(SUCCESS_MSG, getString(R.string.str_success).toString());
    }

    /**
     * 功能：指纹比对
     */
    public void OnClickFingerMatch(View view) {
        if (m_FingerMatchThread != null) {
            m_FingerMatchThread.interrupt();
            m_FingerMatchThread = null;
        }
        m_FingerMatchThread = new FingerMatchThread();
        m_FingerMatchThread.start();
    }

    private class FingerMatchThread extends Thread {
        @Override
        public void run() {
            try {
                FingerMatch();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void FingerMatch() {
        int iRV = 0;
        SendMsg(PROMTP_MSG, getString(R.string.btn_fingerMatch).toString()
                + ","
                + getString(R.string.str_promptPressFinger).toString() + "...");
        iRV = fingerDriver.mxGetIsoImage(m_bImgBuf, IMAGE_SIZE, TIME_OUT, 0);
        if (iRV != 0) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString() + ",iRV=" + iRV);
            return;
        }
        iRV = fingeralg.mxGetTzBase64FromISO(m_bImgBuf, m_bFingerTz);
        if (iRV != 1) {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString());
            return;
        }
        iRV = fingeralg.mxFingerMatchBase64(m_bFingerMb, m_bFingerTz, 3);
        if (iRV == 0) {
            SendMsg(SUCCESS_MSG, getString(R.string.str_success).toString());
        } else {
            SendMsg(FAILED_MSG, getString(R.string.str_failed).toString());
        }
    }

    /**
     * 功能：获取设备版本
     */
    public void OnClickGetDevVersion(View view) {
        if (m_GetDevVersionThread != null) {
            m_GetDevVersionThread.interrupt();
            m_GetDevVersionThread = null;
        }
        m_GetDevVersionThread = new GetDevVersionThread();
        m_GetDevVersionThread.start();
    }

    private class GetDevVersionThread extends Thread {
        @Override
        public void run() {
            try {
                GetDevVersion();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void GetDevVersion() {
        int iRV = -1;
        byte[] bVersion = new byte[120];
        iRV = fingerDriver.mxGetDevVersion(bVersion);
        if (iRV != 0) {
            SendMsg(FAILED_MSG, getString(R.string.btn_getDevVersion).toString() +
                    getString(R.string.str_failed).toString());
            return;
        }
        SendMsg(SUCCESS_MSG, getString(R.string.btn_getDevVersion).toString() + ":" +
                new String(bVersion));
    }

    /**
     * 功能：获取驱动版本
     */
    public void OnClickGetDriverVersion(View view) {
        SendMsg(SUCCESS_MSG, getString(R.string.btn_getDriverVersion).toString() + ":" +
                fingerDriver.mxGetDriverVersion());
    }

    /**
     * 功能：获取算法版本
     */
    public void OnClickGetAlgVersion(View view) {
        byte[] bVersion = new byte[120];
        fingeralg.mxGetVersion(bVersion);
        SendMsg(SUCCESS_MSG, getString(R.string.btn_getAlgVersion).toString() + ":" +
                new String(bVersion));
    }
}
