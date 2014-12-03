package com.fiskur.bbcmicro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fiskur.bbcmicro.BBCUtils.KeyMap;
import com.littlefluffytoys.beebdroid.Beebdroid;

public class SettingsActivity extends ActionBarActivity {
	
	private static final String TAG = "SettingsActivity";
	private static final int ACTIVITY_REMAP = 0;
	public static final String PREFS_CHAR_PREFIX = "remap_char_int_";
	private ListView mBBCKeyList;
	private KeyMap[] mBBCKeyLabels;

	private TextView mKeyCodeView;

    private MaterialDialog mDialog;
	private int mSelectedScanCode;
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
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
//			KeyMap clickedKey = mBBCKeyLabels[position];
//			mSelectedScanCode = clickedKey.getScanCode();
//			Intent keyMapIntent = new Intent(SettingsActivity.this, KeyRemapActivity.class);
//			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_KEY_STRING, clickedKey.getKeyString());
//			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_SCAN_INT, clickedKey.getScanCode());
//			keyMapIntent.putExtra(KeyRemapActivity.EXTRA_SCAN_REMAP_INT, clickedKey.getRemapCode());
//			startActivityForResult(keyMapIntent, ACTIVITY_REMAP);

            KeyMap clickedKey = mBBCKeyLabels[position];
            mSelectedScanCode = clickedKey.getScanCode();

            mDialog = new MaterialDialog.Builder(SettingsActivity.this)
                    .title(R.string.title_activity_key_remap)
                    .customView(R.layout.dialog_key_remap)
                    .positiveText("Save")
                    .negativeText("Clear")
                    .callback(new MaterialDialog.Callback(){
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            if(remappedKeyCode != -1){
                                mPrefs.edit().putInt(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode), remappedKeyCode).commit();
                            }else{
                                mPrefs.edit().remove(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode)).commit();
                            }

                            mBBCKeyLabels = BBCUtils.getInstance().getKeyMapsWithRemap(SettingsActivity.this);
                            KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (SettingsActivity.this, R.layout.list_row_remap, mBBCKeyLabels);
                            mBBCKeyList.setAdapter(bbcKeyAdapter);
                            remappedKeyCode = -1;
                        }

                        @Override
                        public void onNegative(MaterialDialog materialDialog) {
                            //do nothing
                        }
                    })
                    .build();

            View dialogContainer = mDialog.getCustomView();

            mKeyView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_key_text_view));
            mKeyView.setText("Key: " + clickedKey.getKeyString());
            mScanCodeView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_scan_code_text_view));
            mScanCodeView.setText("Key Code: 0x" + Integer.toHexString(clickedKey.getScanCode()));
            mScanCodeRemapView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_scan_code_remap_text_view));

            mDialog.show();
		}
	}

    private TextView mKeyView = null;
    private TextView mScanCodeView = null;
    private TextView mScanCodeRemapView = null;
    int remappedKeyCode = -1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mDialog.isShowing()){
            remappedKeyCode = keyCode;
            if(mScanCodeRemapView != null) {
                mScanCodeRemapView.setText("Remap Code: 0x" + Integer.toHexString(remappedKeyCode));
            }
        }

        return super.onKeyDown(keyCode, event);
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
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(mKeyCodeView != null){
//			mKeyCodeView.setText(Integer.toBinaryString(keyCode));
//		}
//		return super.onKeyDown(keyCode, event);
//	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
