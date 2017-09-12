package com.spd.s21;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.suprema.BioMiniAndroid;
import com.suprema.IBioMiniCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends Activity {

    public final static String TAG = "BM-SDK Sample";
    // BioMini SDK variable
    private static BioMiniAndroid mBioMiniHandle = null;

    // Callback
    private final IBioMiniCallback mBioMiniCallbackHandler = new IBioMiniCallback() {
        private int mWidth = 0;
        private int mHeight = 0;

        @Override
        public void onCaptureCallback(final byte[] capturedimage, int width, int height, int resolution, boolean bfingeron) {
            mWidth = width;
            mHeight = height;
            e(String.valueOf("onCaptureCallback called!" + " width:" + width + " height:" + height + " fingerOn:" + bfingeron));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int width = mWidth;
                    int height = mHeight;
                    byte[] Bits = new byte[width * height * 4];
                    for (int i = 0; i < width * height; i++) {
                        Bits[i * 4] =
                                Bits[i * 4 + 1] =
                                        Bits[i * 4 + 2] = capturedimage[i];
                        Bits[i * 4 + 3] = -1;
                    }
                    Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bm.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
                    ImageView vv = findViewById(R.id.imageView1);
                    vv.setImageBitmap(bm);

                    vv.invalidate();
                }
            });
        }

        @Override
        public void onErrorOccurred(String msg) {

        }
    };
    ECurrentTemplateType g_curTemplateType = ECurrentTemplateType.SUPREMA;
    private BioMiniAndroid.ECODE ufa_res;
    private String errmsg = "Hi";
    private byte[] pImage = new byte[320 * 480];
    // Enroll Template Array
    private byte[][] ptemplate1 = new byte[50][1024];
    // Enroll Template Size
    private int[][] ntemplateSize1 = new int[50][4];
    // Enroll User
    private String[] pEnrolledUser = new String[50];
    // Input Template Buffer
    private byte[] ptemplate2 = new byte[1024];
    // Input Template Size
    private int[] ntemplateSize2 = new int[4];
    // Quality of template
    private int[] nquality = new int[4];
    private int nenrolled = 0;
    private boolean isname = false;
    private SeekBar sensitivity;
    private SeekBar timeout;
    private SeekBar securitylevel;
    private CheckBox fastmode;
    private int nsensitivity;
    private int ntimeout;
    private int nsecuritylevel;
    private int bfastmode;


    private MainActivity mainContext;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBioMiniHandle.UFA_Uninit();
            if (mListScannerAdapter != null) mListScannerAdapter.clear();
            Toast.makeText(mainContext, "USB Device Detached", Toast.LENGTH_SHORT).show();
        }
    };
    private String m_strUserName = "";

    private static void l(Object msg) {
        Log.d(TAG, ">==< " + msg.toString() + " >==<");
    }

    private static void e(Object msg) {
        Log.e(TAG, ">==< " + msg.toString() + " >==<");
    }

    private final boolean mUseUsbManager = true;
    private static UsbManager mUsbManager = null;
    private static Context mApplicationContext = null;
    private static final int MAX_DEVICES = 4;
    private UsbDevice[] mDeviceList = new UsbDevice[MAX_DEVICES];
    private ListView mListScanners;
    private ArrayAdapter<String> mListScannerAdapter = null;

    private void enumerate(IPermissionListener listener) {
        l("enumerating");
        if (mUsbManager == null) {
            l("mUsbManager null!!");
        }

        mListScannerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextSize(16);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };
        mListScanners.setAdapter(mListScannerAdapter);

        int mNumDevices = 0;
        if (mUsbManager != null) {
            HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
            for (UsbDevice d : devlist.values()) {
                l("Found device: "
                        + String.format("%04X:%04X", d.getVendorId(),
                        d.getProductId()));

                if (BioMiniAndroid.isSupported(d.getVendorId(), d.getProductId())) {
                    l("Device under: " + d.getDeviceName());
                    if (!mUsbManager.hasPermission(d)) {
                        l("onPermissionDenied");
                        listener.onPermissionDenied(d);
                    } else {
                        if (mNumDevices > MAX_DEVICES) {
                            l("Too many devices attached (max:4)");
                            break;
                        }
                        mDeviceList[mNumDevices] = d;
                        mListScannerAdapter.add("Device #" + mNumDevices);
                        mNumDevices++;
                        //l("UFA_SetDevice");
                        //mBioMiniHandle.UFA_SetDevice(d);
                        //return;
                    }
                    //break;
                }
            }
        }

        if (mNumDevices != 0) {
            l("UFA_SetDevice : " + mDeviceList[0]);
            mBioMiniHandle.UFA_SetDevice(mDeviceList[0]);
        }
    }


    private interface IPermissionListener {
        void onPermissionDenied(UsbDevice d);
    }

    protected static final String ACTION_USB_PERMISSION = "com.android.biomini.USB_PERMISSION";

    private class PermissionReceiver extends BroadcastReceiver {
        private final IPermissionListener mPermissionListener;

        public PermissionReceiver(IPermissionListener permissionListener) {
            mPermissionListener = permissionListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            l("onReceive");
            if (mApplicationContext != null) {
                mApplicationContext.unregisterReceiver(this);
                if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mPermissionListener.onPermissionDenied((UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE));
                    } else {
                        l("Permission granted");
                        UsbDevice dev = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (dev != null) {
                            if (BioMiniAndroid.isSupported(dev.getVendorId(), dev.getProductId())) {
                                l("startHandler_onReceive");
                                mBioMiniHandle.UFA_SetDevice(dev);
                            }
                        } else {
                            l("device not present!");
                        }
                    }
                }
            }
        }
    }

    private PermissionReceiver mPermissionReceiver = new PermissionReceiver(
            new IPermissionListener() {
                @Override
                public void onPermissionDenied(UsbDevice d) {
                    l("Permission denied on " + d.getDeviceId());
                }
            });

    @Override
    protected void onPause() {
        super.onPause();

        mBioMiniHandle.UFA_AbortCapturing();
        mBioMiniHandle.UFA_Uninit();


        TextView tv = findViewById(R.id.txmessage);
        tv.setText("OnPause, UFA_UnInit()");
        e("onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        if (mListScannerAdapter != null) {
            mListScannerAdapter.clear();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mainContext = this;
        registerReceiver(mUsbReceiver, new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED"));
        sensitivity = findViewById(R.id.seekBar1);
        timeout = findViewById(R.id.SeekBar01);
        securitylevel = findViewById(R.id.SeekBar02);
        fastmode = findViewById(R.id.chkfastmode);

        // allocate SDK instance
        if (mBioMiniHandle == null) {
            mApplicationContext = getApplicationContext();
            mUsbManager = (UsbManager) mApplicationContext.getSystemService(Context.USB_SERVICE);
            mBioMiniHandle = new BioMiniAndroid(mUsbManager);
        }

        sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nsensitivity = progress;

                // set sensitivity parameter : 0~7 [7:default]
                ufa_res = mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.SENSITIVITY, nsensitivity);

                if (ufa_res == BioMiniAndroid.ECODE.OK) {
                    int[] nValue = new int[4];
                    ufa_res = mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.SENSITIVITY, nValue);

                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("sensitivity changed : " + nValue[0]);
                } else {
                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("UFA_SetParameter res: " + errmsg);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        securitylevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nsecuritylevel = progress;

                if (nsecuritylevel == 0)
                    nsecuritylevel = 1;

                // set security level parameter : 1~7 [4:default]
                ufa_res = mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.SECURITY_LEVEL, nsecuritylevel);

                if (ufa_res == BioMiniAndroid.ECODE.OK) {
                    int[] nValue = new int[4];
                    ufa_res = mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.SECURITY_LEVEL, nValue);

                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("security level changed : " + nValue[0]);
                } else {
                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("UFA_SetParameter res: " + errmsg);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ntimeout = progress;

                // set timeout parameter : 0~ [10000:default]
                ufa_res = mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.TIMEOUT, ntimeout * 1000);

                if (ufa_res == BioMiniAndroid.ECODE.OK) {
                    int[] nValue = new int[4];
                    ufa_res = mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.TIMEOUT, nValue);

                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("timeout changed : " + nValue[0]);
                } else {
                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("UFA_SetParameter res: " + errmsg);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        fastmode.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    bfastmode = 1;
                    // set fast mode parameter : true or false [true:default]
                    ufa_res = mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.FAST_MODE, bfastmode);

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        TextView tv = findViewById(R.id.txmessage);
                        tv.setText("Fast Mode On");
                    } else {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = findViewById(R.id.txmessage);
                        tv.setText("UFA_SetParameter res: " + errmsg);
                    }
                } else {
                    bfastmode = 0;
                    mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.FAST_MODE, bfastmode);

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        TextView tv = findViewById(R.id.txmessage);
                        tv.setText("Fast Mode Off");
                    } else {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = findViewById(R.id.txmessage);
                        tv.setText("UFA_SetParameter res: " + errmsg);
                    }
                }
            }
        });

        (((Button) findViewById(R.id.btnsuprema))).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle != null) {
                    TextView tv = findViewById(R.id.txmessage);

                    if (nenrolled > 0) {
                        tv.setText("there is one more user. please delete all user before changing template type");
                        return;
                    }

                    ufa_res = mBioMiniHandle.UFA_SetTemplateType(BioMiniAndroid.TEMPLATE_TYPE.SUPREMA);

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        BioMiniAndroid.TEMPLATE_TYPE[] nValue = new BioMiniAndroid.TEMPLATE_TYPE[4];
                        ufa_res = mBioMiniHandle.UFA_GetTemplateType(nValue);
                        tv.setText("TemplateType(" + nValue[0] + ")");

                        ((Button) findViewById(R.id.btnsuprema)).setTextColor(Color.WHITE);
                        ((Button) findViewById(R.id.btntypeiso)).setTextColor(Color.BLACK);
                        ((Button) findViewById(R.id.btntypeansi)).setTextColor(Color.BLACK);
                        g_curTemplateType = ECurrentTemplateType.SUPREMA;
                    }
                }
            }
        });

        (((Button) findViewById(R.id.btntypeiso))).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));

                } else {
                    TextView tv = findViewById(R.id.txmessage);

                    if (nenrolled > 0) {
                        tv.setText("there is one more use. please delete all user before changing template type");
                        return;
                    }

                    ufa_res = mBioMiniHandle.UFA_SetTemplateType(BioMiniAndroid.TEMPLATE_TYPE.ISO19794_2);

                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    tv.setText("UFA_SetTemplateType(ISO) res: " + errmsg);

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        BioMiniAndroid.TEMPLATE_TYPE[] nValue = new BioMiniAndroid.TEMPLATE_TYPE[4];
                        ufa_res = mBioMiniHandle.UFA_GetTemplateType(nValue);
                        tv.setText("TemplateType(" + nValue[0] + ")");

                        ((Button) findViewById(R.id.btnsuprema)).setTextColor(Color.BLACK);
                        ((Button) findViewById(R.id.btntypeiso)).setTextColor(Color.WHITE);
                        ((Button) findViewById(R.id.btntypeansi)).setTextColor(Color.BLACK);
                        g_curTemplateType = ECurrentTemplateType.ISO;
                    }
                }
            }
        });

        (((Button) findViewById(R.id.btntypeansi))).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));

                } else {
                    TextView tv = findViewById(R.id.txmessage);

                    if (nenrolled > 0) {
                        tv.setText("there is one more use. please delete all user before changing template type");
                        return;
                    }

                    ufa_res = mBioMiniHandle.UFA_SetTemplateType(BioMiniAndroid.TEMPLATE_TYPE.ANSI378);

                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    tv.setText("UFA_SetTemplateType(ANSI) res: " + errmsg);

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        BioMiniAndroid.TEMPLATE_TYPE[] nValue = new BioMiniAndroid.TEMPLATE_TYPE[4];
                        ufa_res = mBioMiniHandle.UFA_GetTemplateType(nValue);
                        tv.setText("TemplateType(" + nValue[0] + ")");

                        ((Button) findViewById(R.id.btnsuprema)).setTextColor(Color.BLACK);
                        ((Button) findViewById(R.id.btntypeiso)).setTextColor(Color.BLACK);
                        ((Button) findViewById(R.id.btntypeansi)).setTextColor(Color.WHITE);
                        g_curTemplateType = ECurrentTemplateType.ANSI;
                    }
                }
            }
        });

        findViewById(R.id.btnfinddevice).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));

                } else {
                    enumerate(new IPermissionListener() {
                        @Override
                        public void onPermissionDenied(UsbDevice d) {
                            if (mApplicationContext != null && mUsbManager != null) {
                                PendingIntent pi = PendingIntent.getBroadcast(mApplicationContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                                mApplicationContext.registerReceiver(mPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION));
                                mUsbManager.requestPermission(d, pi);
                            }
                        }
                    });
                }
            }
        });

        findViewById(R.id.checkAutoSleep).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.ENABLE_AUTOSLEEP, 1);
                } else {
                    mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.ENABLE_AUTOSLEEP, 0);
                }
            }
        });

        findViewById(R.id.buttonToggleSleep).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBioMiniHandle.UFA_SetSleep(((ToggleButton) v).isChecked());
            }
        });

        findViewById(R.id.btninit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    // SDK initialization
                    ufa_res = mBioMiniHandle.UFA_Init();
                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        CheckBox cbox = (CheckBox) findViewById(R.id.chkScanningMode);
                        if (mBioMiniHandle.getProductId() == 0x409) {
                            int pnCropMode[] = new int[1];
                            cbox.setVisibility(View.VISIBLE);

                            ufa_res = mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.SCANNING_MODE, pnCropMode);
                            if (ufa_res != BioMiniAndroid.ECODE.OK) {
                                return;
                            } else {
                                if (pnCropMode[0] == BioMiniAndroid.SCANNER_OPTIONS.PLUS2_SCANNING_MODE_CROP.value()) {
                                    cbox.setChecked(true);
                                } else if (pnCropMode[0] == BioMiniAndroid.SCANNER_OPTIONS.PLUS2_SCANNING_MODE_FULL.value()) {
                                    cbox.setChecked(false);
                                }
                            }
                        } else {
                            cbox.setVisibility(View.GONE);
                        }
                    }
                    String errmsg1 = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    String Serial = null;

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        sensitivity.setProgress(7);
                        timeout.setProgress(10);
                        securitylevel.setProgress(4);
                        fastmode.setChecked(true);

                        nsensitivity = 7;
                        ntimeout = 10;
                        nsecuritylevel = 4;
                        bfastmode = 1;

                        mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.SENSITIVITY, nsensitivity);
                        mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.TIMEOUT, ntimeout * 1000);
                        mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.SECURITY_LEVEL, nsecuritylevel);
                        mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.FAST_MODE, bfastmode);
                        ((ToggleButton) findViewById(R.id.buttonToggleSleep)).setChecked(!mBioMiniHandle.UFA_IsAwake());
                        if (g_curTemplateType == ECurrentTemplateType.SUPREMA) {
                            ((Button) findViewById(R.id.btnsuprema)).setTextColor(Color.WHITE);
                            ufa_res = mBioMiniHandle.UFA_SetTemplateType(BioMiniAndroid.TEMPLATE_TYPE.SUPREMA);
                        } else if (g_curTemplateType == ECurrentTemplateType.ISO) {
                            ((Button) findViewById(R.id.btntypeiso)).setTextColor(Color.WHITE);
                            ufa_res = mBioMiniHandle.UFA_SetTemplateType(BioMiniAndroid.TEMPLATE_TYPE.ISO19794_2);
                        } else if (g_curTemplateType == ECurrentTemplateType.ANSI) {
                            ((Button) findViewById(R.id.btntypeansi)).setTextColor(Color.WHITE);
                            ufa_res = mBioMiniHandle.UFA_SetTemplateType(BioMiniAndroid.TEMPLATE_TYPE.ANSI378);

                        }
                        // set callback
                        mBioMiniHandle.UFA_SetCallback(mBioMiniCallbackHandler);
                        Serial = mBioMiniHandle.UFA_GetSerialNumber();
                        // version
                        String sdkversion = mBioMiniHandle.UFA_GetVersionString();
                        TextView tv = findViewById(R.id.txversion);
                        tv.setText(sdkversion);

                        //mBioMiniHandle.UFA_SetCaptureFrame(BioMiniAndroid.FRAME_RATE.HIGH);
                    }
                    // get return code string
                    TextView tv = findViewById(R.id.txmessage);
                    tv.setText("UFA_Init res: " + errmsg1 + " Serial: " + Serial);

                    byte[] CID = new byte[2];
                    mBioMiniHandle.UFA_GetCompanyID(CID);
                    tv.append(" CID : " + new String(CID));
                }
            }
        });

        findViewById(R.id.btnuninit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    // uninitialize SDK
                    ufa_res = mBioMiniHandle.UFA_Uninit();
                    String errmsg1 = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        sensitivity.setProgress(0);
                        timeout.setProgress(0);
                        securitylevel.setProgress(0);
                        fastmode.setChecked(false);

                        ((Button) findViewById(R.id.btnsuprema)).setTextColor(Color.BLACK);
                        ((Button) findViewById(R.id.btntypeiso)).setTextColor(Color.BLACK);
                        ((Button) findViewById(R.id.btntypeansi)).setTextColor(Color.BLACK);

                        nsensitivity = 0;
                        ntimeout = 0;
                        nsecuritylevel = 0;
                        bfastmode = 0;
                    }

                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("UFA_Uninit res: " + errmsg1);
                }
            }
        });

        findViewById(R.id.chkScanningMode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    CheckBox cbox = (CheckBox) findViewById(R.id.chkScanningMode);
                    int[] nVal = new int[1];
                    mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.SCANNING_MODE, nVal);
                    if (cbox.isChecked()) {
                        nVal[0] = BioMiniAndroid.SCANNER_OPTIONS.PLUS2_SCANNING_MODE_CROP.value();
                    } else {
                        nVal[0] = BioMiniAndroid.SCANNER_OPTIONS.PLUS2_SCANNING_MODE_FULL.value();
                    }
                    ufa_res = mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.SCANNING_MODE, nVal[0]);
                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        e(String.valueOf("BioMini SDK Scanning mode changed"));
                    }
                }
            }
        });

        findViewById(R.id.btncapturesingle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.btncapturesingle).setEnabled(false);
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    TextView tv = findViewById(R.id.txmessage);
                    int[] nVal = new int[1];
                    mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.ENABLE_AUTOSLEEP, nVal);
                    if (!mBioMiniHandle.UFA_IsAwake() && nVal[0] == 0) {
                        tv.setText("Fingerprint device is staying in sleep mode!");
                        e(String.valueOf("Fingerprint device is staying in sleep mode!"));
                        ((Button) findViewById(R.id.btncapturesingle)).setEnabled(true);
                        return;
                    }
                    ufa_res = mBioMiniHandle.UFA_CaptureSingle(pImage);
                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);

                    tv.setText("UFA_CaptureSingle res: " + errmsg);

                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        int width = mBioMiniHandle.getImageWidth();
                        int height = mBioMiniHandle.getImageHeight();
                        mBioMiniCallbackHandler.onCaptureCallback(pImage, width, height, 500, true);

                        int[] quality = new int[1];
                        ufa_res = mBioMiniHandle.UFA_GetFPQuality(pImage, width, height, quality, 1);
                        if (ufa_res == BioMiniAndroid.ECODE.OK) {
                            tv.append("\n" + "Fingerprint quality: " + quality[0]);
                            l("Fingerprint quality: " + quality[0]);
                        } else {
                            l("UFA_GetFPQuality failed (" + ufa_res + ")");
                        }

                    }
                }
                findViewById(R.id.btncapturesingle).setEnabled(true);
            }
        });

        findViewById(R.id.btnstartcapturing).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    int[] nVal = new int[1];
                    mBioMiniHandle.UFA_GetParameter(BioMiniAndroid.PARAM.ENABLE_AUTOSLEEP, nVal);
                    if (!mBioMiniHandle.UFA_IsAwake() && nVal[0] == 0) {
                        e(String.valueOf("Fingerprint device is staying in sleep mode!"));
                        tv.setText("Fingerprint device is staying in sleep mode!");
                        return;
                    }
                    // image preview start
                    ufa_res = mBioMiniHandle.UFA_StartCapturing();
                    if (ufa_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);

                        tv.setText("UFA_StartCapturing res: " + errmsg);
                    }
                }
            }
        });

        findViewById(R.id.btnAbort).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BioMiniAndroid.ECODE ufs_res;

                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    // Abort Capturing
                    ufs_res = mBioMiniHandle.UFA_AbortCapturing();
                    // Get Error String
                    if (ufs_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_AbortCapturing res: " + errmsg);
                    } else {
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_AbortCapturing res: OK");


                    }
                }
            }
        });

        findViewById(R.id.btnName).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.custom_dialog, (ViewGroup) findViewById(R.id.layout_root));

                AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
                aDialog.setTitle("Insert your name");
                aDialog.setView(layout);

                aDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText tv = layout.findViewById(R.id.EditTest01);
                        m_strUserName = tv.getText().toString();
                        if (tv.getText().toString().equals("")) {
                            Toast.makeText(mainContext, "please insert User Name Again", Toast.LENGTH_SHORT).show();
                        } else {
                            pEnrolledUser[nenrolled] = tv.getText().toString();
                            isname = true;
                        }
                    }
                });
                aDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog ad = aDialog.create();
                ad.show();
            }
        });

        findViewById(R.id.btndelete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < nenrolled; i++) {
                    Arrays.fill(ptemplate1[i], 0, 1024, (byte) 0);
                    ntemplateSize1[i][0] = 0;
                }

                nenrolled = 0;
            }
        });

        findViewById(R.id.btnenroll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));

                } else {
                    if (nenrolled == 50) {
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("out of memory");
                        return;
                    }

                    if (!isname) {
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("there is no inserted user name");
                        return;
                    }

                    // capture fingerprint image
                    ufa_res = mBioMiniHandle.UFA_CaptureSingle(pImage);

                    if (ufa_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_CaptureSingle res: " + errmsg);
                        return;
                    }

                    int width = mBioMiniHandle.getImageWidth();
                    int height = mBioMiniHandle.getImageHeight();
                    mBioMiniCallbackHandler.onCaptureCallback(pImage, width, height, 500, true);

                    // extract fingerpirnt template from captured image
                    // extracted template is saved in memory (ptemplate1: 2-D byte array)
                    ufa_res = mBioMiniHandle.UFA_ExtractTemplate(ptemplate1[nenrolled], ntemplateSize1[nenrolled], nquality, 1024);

                    if (ufa_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_ExtractTemplate res: " + errmsg);
                    } else {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_ExtractTemplate res: " + errmsg);

                        int[] feature_number = new int[1];
                        ufa_res = mBioMiniHandle.UFA_GetFeatureNumber(ptemplate1[nenrolled], ntemplateSize1[nenrolled][0], feature_number);
                        if (ufa_res == BioMiniAndroid.ECODE.OK) {
                            tv.setText("Number of features : " + feature_number[0]);
                            l("Number of features : " + feature_number[0]);
                        } else {
                            l("UFA_GetFeatureNumber failed (" + ufa_res + ")");
                        }

                        TextView tv2 = (TextView) findViewById(R.id.txmessage);
                        tv2.setText(pEnrolledUser[nenrolled] + " is enrolled");

                        nenrolled++;
                        isname = false;
                    }
                }
            }
        });

        findViewById(R.id.btnverify).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    @SuppressLint("CutPasteId") TextView tv = findViewById(R.id.txmessage);
                    if (nenrolled == 0) {
                        tv.setText("There is no enrolled data");
                        Toast.makeText(mainContext, "no enrolled Data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // capture fingerprint image
                    ufa_res = mBioMiniHandle.UFA_CaptureSingle(pImage);

                    if (ufa_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        tv.setText("UFA_CaptureSingle res: " + errmsg);
                        return;
                    }

                    int width = mBioMiniHandle.getImageWidth(), height = mBioMiniHandle.getImageHeight();
                    mBioMiniCallbackHandler.onCaptureCallback(pImage, width, height, 500, true);

                    int[] quality = new int[1];
                    if (mBioMiniHandle.UFA_GetFPQuality(pImage, width, height, quality, 1) == BioMiniAndroid.ECODE.OK) {
                        tv.setText("Fingerprint quality: " + quality[0]);
                        l("Fingerprint quality: " + quality[0]);
                    }

                    // extract fingerprint template from captured image
                    ufa_res = mBioMiniHandle.UFA_ExtractTemplate(ptemplate2, ntemplateSize2, nquality, 1024);

                    if (ufa_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        tv.setText("UFA_ExtractTemplate res: " + errmsg);
                        return;
                    }

                    int[] feature_number = new int[1];
                    ufa_res = mBioMiniHandle.UFA_GetFeatureNumber(ptemplate1[nenrolled], ntemplateSize1[nenrolled][0], feature_number);
                    if (ufa_res == BioMiniAndroid.ECODE.OK) {
                        l("Number of features : " + feature_number[0]);
                    } else {
                        l("UFA_GetFeatureNumber failed (" + ufa_res + ")");
                    }

                    int[] nVerificationResult = new int[4];
                    nVerificationResult[0] = 0;

                    for (int i = 0; i < nenrolled; i++) {
                        // try 1:1 template matching
                        ufa_res = mBioMiniHandle.UFA_Verify(ptemplate1[i], ntemplateSize1[i][0], ptemplate2, ntemplateSize2[0], nVerificationResult);
                        l("-------> UFA_Verify retValue: " + ufa_res);
                        if (nVerificationResult[0] == 1) {
                            tv.setText("Match with: " + pEnrolledUser[i]);
                            break;
                        }
                    }
                    if (ufa_res != BioMiniAndroid.ECODE.OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        tv.setText("UFA_Verify res: " + errmsg);
                        return;
                    }

                    if (nVerificationResult[0] != 1) {
                        tv.setText("matching result: not matched");

                        TextView tv2 = (TextView) findViewById(R.id.txmessage);
                        tv2.setText("Identification fail");
                    }
                }

            }
        });

        ((Button) findViewById(R.id.btnSaveWSQ)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (m_strUserName.equals("") || m_strUserName.toString() == null) {
                    Toast.makeText(mainContext, "please insert User Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                int nImageWidth = mBioMiniHandle.getImageWidth();
                int nImageHeight = mBioMiniHandle.getImageHeight();
                if (BioMiniAndroid.isSupported(0x16d1, mBioMiniHandle.getProductId())) {
                    Toast.makeText(mainContext, "Invalid Device. ", Toast.LENGTH_SHORT).show();
                }
                // Get UI Handler
                TextView tv = findViewById(R.id.txmessage);
                //Get WSQ Buffer.
                byte[] pwImage = new byte[nImageWidth * nImageHeight * 3];
                int[] pwSize = new int[4];
                // compression ratio 2.25
                ufa_res = mBioMiniHandle.UFA_GetCaptureImageBufferToWSQBufferVarEx(pwImage, pwSize, nImageWidth * nImageHeight * 3, (float) 2.25, 512, 512, 180);
                if (ufa_res == BioMiniAndroid.ECODE.OK) {
                    try {
                        tv.setText("UFA_GetCaptureImageBufferToWSQBuffer : OK");
                        FileOutputStream fos = new FileOutputStream(
                                //new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + m_strUserName.toString() + ".wsq"));
                                new File("sdcard/Pictures/" + m_strUserName + ".wsq"));
                        fos.write(pwImage);
                        fos.close();
                        Toast.makeText(mainContext,
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + m_strUserName.toString() + ".wsq" + " File Saved.",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(mainContext, "Fail to save WSQ formatFile.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tv.setText("UFA_GetCaptureImageBufferToWSQBuffer : Fail(" + ufa_res + ")");
                }

            }
        });

        findViewById(R.id.btnSaveTemplate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_strUserName.equals("") || m_strUserName == null) {
                    Toast.makeText(mainContext, "please insert User Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                TextView MSG = findViewById(R.id.txmessage);
                BioMiniAndroid.ECODE ufa_res;
                byte[] pTemplate = new byte[1024];
                int[] pnTemplateSize = new int[4];
                int[] pnQuality = new int[4];

                ufa_res = mBioMiniHandle.UFA_ExtractTemplate(pTemplate, pnTemplateSize, pnQuality, 1024);
                if (ufa_res != BioMiniAndroid.ECODE.OK) {
                    MSG.setText((mBioMiniHandle.UFA_GetErrorString(ufa_res)));
                    return;
                }

                String str_fullFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + m_strUserName.toString() + ".dat";

                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(new File(str_fullFileName));
                    fos.write(pTemplate, 0, pnTemplateSize[0]);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    l(e.getMessage());
                    return;
                }

                Toast.makeText(mainContext, "Saved Template(.dat)", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnSaveBmp).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get UI Handler
                TextView tv = findViewById(R.id.txmessage);

                if (m_strUserName.equals("")) {
                    Toast.makeText(mainContext, "please insert User Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                ufa_res = mBioMiniHandle.UFA_SaveCaptureImageBufferToBMP(m_strUserName, false);
                if (ufa_res == BioMiniAndroid.ECODE.OK) {
                    tv.setText("UFA_SaveCaptureImageBufferToBMP : OK");
                    Toast.makeText(mainContext,
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + m_strUserName.toString() + ".bmp" + " File Saved.",
                            Toast.LENGTH_SHORT).show();
                } else if (ufa_res == BioMiniAndroid.ECODE.ERR_FILE_EXIST_ALREADY) {
                    Toast.makeText(mainContext, "File is Existed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mainContext, "Fail to Save Fail(" + ufa_res + ").", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.tbtn_AutoRotate).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ToggleButton tbtn_autoRotate = findViewById(v.getId());
                if (mBioMiniHandle != null) {
                    mBioMiniHandle.UFA_SetParameter(BioMiniAndroid.PARAM.AUTO_ROTATE, tbtn_autoRotate.isChecked() ? 1 : 0);
                }

            }
        });

        mListScanners = findViewById(R.id.listScanners);
        mListScanners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < MAX_DEVICES) {
                    Toast.makeText(getApplicationContext(), mListScannerAdapter.getItem(i), Toast.LENGTH_SHORT).show();
                    mBioMiniHandle.UFA_SetDevice(mDeviceList[i]);

                    // initialize UI
                    sensitivity.setProgress(0);
                    timeout.setProgress(0);
                    securitylevel.setProgress(0);
                    fastmode.setChecked(false);

                    ((Button) findViewById(R.id.btnsuprema)).setTextColor(Color.BLACK);
                    ((Button) findViewById(R.id.btntypeiso)).setTextColor(Color.BLACK);
                    ((Button) findViewById(R.id.btntypeansi)).setTextColor(Color.BLACK);

                    nsensitivity = 0;
                    ntimeout = 0;
                    nsecuritylevel = 0;
                    bfastmode = 0;
                } else {
                    Toast.makeText(getApplicationContext(), "Device selection failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private enum ECurrentTemplateType {SUPREMA, ISO, ANSI}


}
