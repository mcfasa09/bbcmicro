package com.fiskur.bbcmicro;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;

public class SettingsActivity extends Activity {
	
	private static final String TAG = "SettingsActivity";
	private Spinner mBBCKeySpinner;
	private String[] mBBCKeyLabels;
	
	private TextView mKeyCodeView;
	private Button mAddButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mBBCKeyLabels = BBCUtils.getInstance().getBBCKeyLabels();
		
		mBBCKeySpinner = (Spinner) findViewById(R.id.settings_bbc_keys_spinner);
		ArrayAdapter<String> bbcKeyAdapter =new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,mBBCKeyLabels);
		mBBCKeySpinner.setAdapter(bbcKeyAdapter);
		
		mKeyCodeView = (TextView) findViewById(R.id.settings_keycode_text_view);
		
		mAddButton = (Button) findViewById(R.id.settings_add_remap_button);
		mAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				l("Add remap...");
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		mKeyCodeView.setText(Integer.toBinaryString(keyCode));
		return super.onKeyDown(keyCode, event);
	}
	
	private void l(String message){
		Log.d(TAG, message);
	}
}
