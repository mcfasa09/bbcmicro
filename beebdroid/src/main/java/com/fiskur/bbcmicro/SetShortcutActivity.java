package com.fiskur.bbcmicro;


import com.littlefluffytoys.beebdroid.Beebdroid;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.SharedPreferences;

public class SetShortcutActivity extends Activity {
	public static final String PREFS_POPUP_SHORTCUT_KEYCODE = "bbcmicro_popup_shortcut_keycode";
	private SharedPreferences mPrefs;
	private TextView mShortcutLabel;
	private Button mClearButton;
	private Button mSaveButton;
	private int mShortcutKeycode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_shortcut);
		
		mShortcutLabel = (TextView) findViewById(R.id.shortcut_keycode_label);
		
		mPrefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS, MODE_PRIVATE);
		mShortcutKeycode = mPrefs.getInt(PREFS_POPUP_SHORTCUT_KEYCODE, -1);
		
		if(mShortcutKeycode == -1){
			mShortcutLabel.setText("No shortcut set");
		}else{
			mShortcutLabel.setText("0x" + Integer.toHexString(mShortcutKeycode));
		}
		
		
		
		mClearButton = (Button) findViewById(R.id.shortcut_clear_button);
		mClearButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPrefs.edit().remove(PREFS_POPUP_SHORTCUT_KEYCODE).commit();
				SetShortcutActivity.this.finish();
			}
		});
		
		mSaveButton = (Button) findViewById(R.id.shortcut_set_button);
		mSaveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPrefs.edit().putInt(PREFS_POPUP_SHORTCUT_KEYCODE, mShortcutKeycode).commit();
				SetShortcutActivity.this.finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mShortcutLabel != null){
			mShortcutKeycode = keyCode;
			mShortcutLabel.setText("0x" + Integer.toHexString(keyCode));
		}
		return super.onKeyDown(keyCode, event);
	}
}
