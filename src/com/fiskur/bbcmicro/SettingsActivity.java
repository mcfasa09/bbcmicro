package com.fiskur.bbcmicro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fiskur.bbcmicro.BBCUtils.KeyMap;
import com.littlefluffytoys.beebdroid.Beebdroid;

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
		
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mPrefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS, MODE_PRIVATE);

		mBBCKeyLabels = BBCUtils.getInstance().getKeyMapsWithRemap(this);
		
		mBBCKeyList = (ListView) findViewById(R.id.settings_keymap_list);
		KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (this, R.layout.list_row_remap, mBBCKeyLabels);
		mBBCKeyList.setAdapter(bbcKeyAdapter);
		
		mBBCKeyList.setOnItemClickListener(new KeyMapItemClickListener());
	}
	
	private class KeyMapItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			KeyMap clickedKey = mBBCKeyLabels[position];
			mSelectedScanCode = clickedKey.getScanCode();
			Intent keyMapIntent = new Intent(SettingsActivity.this, KeyRemapActivity.class);
			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_KEY_STRING, clickedKey.getKeyString());
			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_SCAN_INT, clickedKey.getScanCode());
			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_SCAN_REMAP_INT, clickedKey.getRemapCode());
			startActivityForResult(keyMapIntent, ACTIVITY_REMAP);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data != null && data.hasExtra(KeyRemapActivity.RESULT_EXTRA_REMAP_KEY)){
			int remapInt = data.getIntExtra(KeyRemapActivity.RESULT_EXTRA_REMAP_KEY, -1);
			if(remapInt != -1){
				mPrefs.edit().putInt(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode), remapInt).commit();
			}else{
				mPrefs.edit().remove(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode)).commit();
			}
			mBBCKeyLabels = BBCUtils.getInstance().getKeyMapsWithRemap(this);
			KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (this, R.layout.list_row_remap, mBBCKeyLabels);
			mBBCKeyList.setAdapter(bbcKeyAdapter);
			
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_disk_popup_shortcut:
				Intent setPopupShortcutIntent = new Intent(SettingsActivity.this, SetShortcutActivity.class);
				startActivity(setPopupShortcutIntent);
				break;
			case R.id.action_wipe:
				Toast.makeText(SettingsActivity.this, "All saved disks and keymappings wiped", Toast.LENGTH_LONG).show();
				mPrefs.edit().clear().commit();
				finish();
				break;
				default:
					finish();
		}
		
		return true;
	}
}
