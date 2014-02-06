package com.fiskur.bbcmicro;

import com.fiskur.bbcmicro.BBCUtils.KeyMap;
import com.littlefluffytoys.beebdroid.Beebdroid;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class SettingsActivity extends Activity {
	
	private static final String TAG = "SettingsActivity";
	private static final int ACTIVITY_REMAP = 0;
	public static final String PREFS_CHAR_PREFIX = "remap_char_int_";
	private ListView mBBCKeyList;
	private KeyMap[] mBBCKeyLabels;
	
	private TextView mKeyCodeView;
	
	private int mSelectedScanCode;
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mPrefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS, MODE_PRIVATE);

		mBBCKeyLabels = BBCUtils.getInstance().getKeyMaps();
		
		mBBCKeyList = (ListView) findViewById(R.id.settings_keymap_list);
		KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (this,android.R.layout.simple_list_item_1, mBBCKeyLabels);
		mBBCKeyList.setAdapter(bbcKeyAdapter);
		
		mBBCKeyList.setOnItemClickListener(new KeyMapItemClickListener());
	}
	
	private class KeyMapItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			l("List item clicked");
			KeyMap clickedKey = mBBCKeyLabels[position];
			mSelectedScanCode = clickedKey.getScanCode();
			Intent keyMapIntent = new Intent(SettingsActivity.this, KeyRemapActivity.class);
			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_KEY_STRING, Character.toString(clickedKey.getKey()));
			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_SCAN_INT, clickedKey.getScanCode());
			startActivityForResult(keyMapIntent, ACTIVITY_REMAP);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data != null && data.hasExtra(KeyRemapActivity.RESULT_EXTRA_REMAP_KEY)){
			int remapInt = data.getIntExtra(KeyRemapActivity.RESULT_EXTRA_REMAP_KEY, -1);
			if(remapInt != -1){
				mPrefs.edit().putInt(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode), remapInt).commit();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mKeyCodeView != null){
			mKeyCodeView.setText(Integer.toBinaryString(keyCode));
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void l(String message){
		Log.d(TAG, message);
	}
}
