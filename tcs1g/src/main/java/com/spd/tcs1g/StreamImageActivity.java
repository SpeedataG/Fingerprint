/* 
 * File: 		StreamImageActivity.java
 * Created:		2013/05/03
 * 
 * copyright (c) 2013 DigitalPersona Inc.
 */

package com.spd.tcs1g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.CaptureResult;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.UareUException;

public class StreamImageActivity extends Activity
{
	private Button m_back;
	private String m_deviceName = "";

	private Reader m_reader = null;
	private int m_DPI = 0;
	private Bitmap m_bitmap = null;
	private ImageView m_imgView;
	private TextView m_selectedDevice;
	private TextView m_title;
	private boolean m_reset = false;
	private CountDownTimer m_timer = null;

	private void initializeActivity()
	{
		m_title = (TextView) findViewById(R.id.title);
		m_title.setText("Stream Image");
		m_selectedDevice = (TextView) findViewById(R.id.selected_device);
		m_deviceName = getIntent().getExtras().getString("device_name");

		m_selectedDevice.setText("Device: " + m_deviceName);
		m_imgView = (ImageView) findViewById(R.id.bitmap_image);
		m_imgView.setImageBitmap(m_bitmap);
		m_bitmap = Globals.GetLastBitmap();
		if (m_bitmap == null) BitmapFactory.decodeResource(getResources(), R.drawable.black);
		m_back = (Button) findViewById(R.id.back);

		m_back.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v)
			{
				onBackPressed(); 
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture_stream);

		initializeActivity();

		// initiliaze dp sdk
		try 
		{
			Context applContext = getApplicationContext();
			m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
			m_reader.Open(Priority.EXCLUSIVE);
			m_DPI = Globals.GetFirstDPI(m_reader);
			m_reader.StartStreaming();
		} 
		catch (Exception e) 
		{
			Log.w("UareUSampleJava", "error during capture");
			m_deviceName = "";
			onBackPressed();
		}

		m_reset = false;

		// updates UI continuously
		m_timer = new CountDownTimer(25, 25)
		{
			public void onTick(long millisUntilFinished) {}
			public void onFinish()
			{
				try 
				{
					if (!m_reset)
					{
						CaptureResult res = m_reader.GetStreamImage(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI);
						if (res != null && res.image != null)
						{
							m_bitmap = Globals.GetBitmapFromRaw(res.image.getViews()[0].getImageData(), res.image.getViews()[0].getWidth(), res.image.getViews()[0].getHeight());
							m_imgView.setImageBitmap(m_bitmap);
							m_imgView.invalidate();
							m_timer.start();
							return;
						}
					}
				}
				catch (Exception e)
				{
					if(!m_reset)
					{
						Log.w("UareUSampleJava", "error during streaming");
						m_deviceName = "";
						onBackPressed();
					}
					return;
				}
			}
		}.start();
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			m_reset = true;
			if (m_reader != null)
			{
				try { m_reader.StopStreaming(); } catch (Exception e) {}
				m_reader.Close();
			}
		}
		catch (UareUException e)
		{
			Log.w("UareUSampleJava", "error during reader shutdown");
		}

		Intent i = new Intent();
		i.putExtra("device_name", m_deviceName);
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	// called when orientation has changed to manually destroy and recreate activity
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_capture_stream);
		initializeActivity();
	}
}
