/* 
 * File: 		GetCapabilitiesActivity.java
 * Created:		2013/05/03
 * 
 * copyright (c) 2013 DigitalPersona Inc.
 */

package com.spd.tcs1g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.Capabilities;
import com.digitalpersona.uareu.Reader.Priority;

public class GetCapabilitiesActivity extends Activity 
{
	private Button m_back;
	private ListView m_capabilities;
	private TextView m_title;
	private String m_deviceName = "";

	private Reader reader = null;
	private Bundle savedInstanceState = null;

	Capabilities caps = null;
	Reader.Description descr = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_list);

		m_title = (TextView) findViewById(R.id.title);
		m_title.setText("Get Capabilities");
		m_deviceName = getIntent().getExtras().getString("device_name");
		m_back = (Button) findViewById(R.id.back);

		m_back.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) 
			{
				onBackPressed();
			}
		});
		// initiliaze dp sdk
		try 
		{
			Context applContext = getApplicationContext();
			reader = Globals.getInstance().getReader(m_deviceName, applContext);
			reader.Open(Priority.EXCLUSIVE);
			caps = reader.GetCapabilities();
			descr = reader.GetDescription();
			reader.Close();
		} 
		catch (Exception e) 
		{
			Log.w("UareUSampleJava", "error during init of reader");
			Intent i = new Intent();
			i.putExtra("device_name", "");
			setResult(Activity.RESULT_OK, i);
			finish();
			return;
		}

		String[] values = new String[20 + caps.resolutions.length];
		values[0] = "Name: " + String.valueOf(descr.name);
		values[1] = "Serial Number: " + String.valueOf(descr.serial_number);
		values[2] = "Product Id: " + String.valueOf(descr.id.product_id);
		values[3] = "Product Name: " + String.valueOf(descr.id.product_name);
		values[4] = "Vendor Id: " + String.valueOf(descr.id.vendor_id);
		values[5] = "Vendor Name: " + String.valueOf(descr.id.vendor_name);
		values[6] = "Modality: " + String.valueOf(descr.modality).replace('_', ' ');
		values[7] = "Technology: " + String.valueOf(descr.technology).replace('_', ' ');
		values[8] = "Firmware Version: v" + descr.version.firmware_version.major + "." + descr.version.firmware_version.minor + "." + descr.version.firmware_version.maintenance;
		values[9] = "Hardware Version: v" + descr.version.hardware_version.major + "." + descr.version.hardware_version.minor + "." + descr.version.hardware_version.maintenance;
		values[10] = "Can Capture: " + String.valueOf(caps.can_capture);
		values[11] = "Can Extract Features: " + String.valueOf(caps.can_extract_features);
		values[12] = "Can Identify: " + String.valueOf(caps.can_identify);
		values[13] = "Can Match: " + String.valueOf(caps.can_match);
		values[14] = "Can Stream: " + String.valueOf(caps.can_stream);
		values[15] = "Has Calibration: " + String.valueOf(caps.has_calibration);
		values[16] = "Has Fingerprint Storage: " + String.valueOf(caps.has_fingerprint_storage);
		values[17] = "Has Power Management: " + String.valueOf(caps.has_power_management);
		values[18] = "Indicator Type: " + String.format("0x%08X", caps.indicator_type);//String.valueOf(caps.indicator_type);
		values[19] = "PIV Compliant: " + String.valueOf(caps.piv_compliant);

		for(int nCount = 0; nCount < caps.resolutions.length; nCount++)
		{
			values[20 + nCount] = "Resolution (" + (nCount + 1) + " of " + caps.resolutions.length + "): " + String.valueOf(caps.resolutions[nCount]);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, values);

		m_capabilities = (ListView) findViewById(R.id.list);
		m_capabilities.setAdapter(adapter); 
	}

	@Override
	public void onBackPressed()
	{
		Intent i = new Intent();
		i.putExtra("device_name", m_deviceName);
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	// called when orientation has changed to manually destroy and recreate activity
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		onCreate(savedInstanceState);
		super.onConfigurationChanged(newConfig);
	}
}
