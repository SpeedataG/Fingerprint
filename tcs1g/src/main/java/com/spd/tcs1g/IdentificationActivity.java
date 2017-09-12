/* 
 * File: 		IdentificationActivity.java
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Engine.Candidate;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.UareUGlobal;

import java.text.DecimalFormat;

public class IdentificationActivity extends Activity 
{
	private Button m_back;
	private String m_deviceName = "";

	private String m_enginError;
	private Reader m_reader = null;
	private int m_DPI = 0;
	private Bitmap m_bitmap = null;
	private ImageView m_imgView;
	private TextView m_selectedDevice;
	private TextView m_title;
	private boolean m_reset = false;


	private TextView m_text;
	private TextView m_text_conclusion;
	private String m_textString;
	private String m_text_conclusionString;
	private Engine m_engine = null;
	private Candidate[] results = null;
	private Fmd m_fmd1 = null;
	private Fmd m_fmd2 = null;
	private Fmd m_fmd3 = null;
	private Fmd m_fmd4 = null;
	private boolean m_first = true;
	private int m_score = -1;
	private Reader.CaptureResult cap_result = null;

	private void initializeActivity()
	{
		m_title = (TextView) findViewById(R.id.title);
		m_title.setText("Identification");

		m_enginError = "";

		m_selectedDevice = (TextView) findViewById(R.id.selected_device);
		m_deviceName = getIntent().getExtras().getString("device_name");

		m_selectedDevice.setText("Device: " + m_deviceName);

		m_imgView = (ImageView) findViewById(R.id.bitmap_image);
		m_bitmap = Globals.GetLastBitmap();
		if (m_bitmap == null) m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
		m_imgView.setImageBitmap(m_bitmap);
		m_back = (Button) findViewById(R.id.back);

		m_back.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				onBackPressed();
			}
		});

		m_text = (TextView) findViewById(R.id.text);
		m_text_conclusion = (TextView) findViewById(R.id.text_conclusion);
		UpdateGUI();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_engine);
		m_textString = "Place your thumb on the reader";
		initializeActivity();
		// initiliaze dp sdk
		try 
		{
			Context applContext = getApplicationContext();
			m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
			m_reader.Open(Priority.EXCLUSIVE);
			m_DPI = Globals.GetFirstDPI(m_reader);
			m_engine = UareUGlobal.GetEngine(); 
			
		} catch (Exception e)
		{
			Log.w("UareUSampleJava", "error during init of reader");
			m_deviceName = "";
			onBackPressed();
			return;
		}


		// loop capture on a separate thread to avoid freezing the UI
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				m_reset = false;
				while (!m_reset)
				{
					try 
					{
						cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
					}
					catch (Exception e)
					{
						if(!m_reset)
						{
							Log.w("UareUSampleJava", "error during capture: " + e.toString());
							m_deviceName = "";
							onBackPressed();
						}
					}

					// an error occurred
					if (cap_result == null || cap_result.image == null) continue;

					try 
					{
						m_enginError = "";

						// save bitmap image locally
						m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());
						if (m_fmd1 == null)
						{
							m_fmd1 = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
						}
						else if (m_fmd2 == null)
						{
							m_fmd2 = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
						}
						else if (m_fmd3 == null)
						{
							m_fmd3 = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
						}
						else if (m_fmd4 == null)
						{
							m_fmd4 = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
						}
						else 
						{
							Fmd m_temp = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
							Fmd[] m_fmds_temp = new Fmd[] {m_fmd1, m_fmd2, m_fmd3, m_fmd4}; 
							results = m_engine.Identify(m_temp, 0, m_fmds_temp, 100000, 2);

							if (results.length != 0)
							{
								m_score = m_engine.Compare(m_fmds_temp[results[0].fmd_index], 0, m_temp, 0);
							}
							else 
							{
								m_score = -1;
							}
							m_fmd1 = null;
							m_fmd2 = null;
							m_fmd3 = null;
							m_fmd4 = null;
						}
					} 
					catch (Exception e) 
					{
						m_enginError = e.toString();
						Log.w("UareUSampleJava", "Engine error: " + e.toString());
					}
					
					m_text_conclusionString = Globals.QualityToString(cap_result);

					if(!m_enginError.isEmpty())
					{
						m_text_conclusionString = "Engine: " + m_enginError;
					}
					if (m_fmd1 == null)
					{
						if (!m_first)
						{
							if (m_text_conclusionString.length() == 0)
							{
								String conclusion = "";
								if (results.length > 0)
								{
									switch (results[0].fmd_index)
									{
									case 0:
										conclusion = "Thumb matched";
										break;
									case 1: 
										conclusion = "Index finger matched";
										break;
									case 2:
										conclusion = "Middle finger matched";
										break;
									case 3:
										conclusion = "Ring finger matched";
										break;
									}
								}
								else
								{
									conclusion = "No match found";
								}
								m_text_conclusionString = conclusion; 
								if (m_score != -1)
								{
									DecimalFormat formatting = new DecimalFormat("##.######");
									m_text_conclusionString = m_text_conclusionString + " (Dissimilarity Score: " + String.valueOf(m_score)+ ", False match rate: " + Double.valueOf(formatting.format((double)m_score/0x7FFFFFFF)) + ")";
								}
							}
						}

						m_textString = "Place your thumb on the reader";
					}
					else if (m_fmd2 == null)
					{
						m_first = false;
						m_textString = "Place your index finger on the reader";
					}
					else if (m_fmd3 == null)
					{
						m_first = false;
						m_textString = "Place your middle finger on the reader";
					}
					else if (m_fmd4 == null)
					{
						m_first = false;
						m_textString = "Place your ring finger on the reader";
					}
					else
					{
						m_textString = "Place any finger on the reader";
					}

					
					runOnUiThread(new Runnable()
					{
						@Override public void run() 
						{
							UpdateGUI();
						}
					});
				}
			}
		}).start();
	}

	
	public void UpdateGUI()
	{
		m_imgView.setImageBitmap(m_bitmap);
		m_imgView.invalidate();
		m_text_conclusion.setText(m_text_conclusionString);
		m_text.setText(m_textString);
	}

	
	@Override
	public void onBackPressed()
	{
		try 
		{
			m_reset = true;
			try { m_reader.CancelCapture(); } catch (Exception e) {}
			m_reader.Close();
		}
		catch (Exception e) 
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
		setContentView(R.layout.activity_engine);
		initializeActivity();
	}
}
