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
    public static final String PREFS_POPUP_SHORTCUT_KEYCODE = "bbcmicro_popup_shortcut_keycode";
    public static final String PREFS_CHAR_PREFIX = "remap_char_int_";
	private static final int MODE_LIST = 0;
    private static final int MODE_REMAP_DIALOG = 1;
    private static final int MODE_SHORTCUT_DIALOG = 2;
    private int mMode = MODE_LIST;
	private static final String TAG = "SettingsActivity";
	private static final int ACTIVITY_REMAP = 0;

	private ListView mBBCKeyList;
	private KeyMap[] mBBCKeyLabels;

	private TextView mKeyCodeView;

    private MaterialDialog mDialog;
	private int mSelectedScanCode;
	private SharedPreferences mPrefs;

    private TextView mShortcutKeyView = null;
    private TextView mKeyView = null;
    private TextView mScanCodeView = null;
    private TextView mScanCodeRemapView = null;
    private int remappedKeyCode = -1;

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
		KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter(this, R.layout.list_row_remap, mBBCKeyLabels);
		mBBCKeyList.setAdapter(bbcKeyAdapter);
		
		mBBCKeyList.setOnItemClickListener(new KeyMapItemClickListener());
	}
	
	private class KeyMapItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                            mMode = MODE_LIST;
                        }

                        @Override
                        public void onNegative(MaterialDialog materialDialog) {
                            mPrefs.edit().remove(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode)).commit();
                            mBBCKeyLabels = BBCUtils.getInstance().getKeyMapsWithRemap(SettingsActivity.this);
                            KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (SettingsActivity.this, R.layout.list_row_remap, mBBCKeyLabels);
                            mBBCKeyList.setAdapter(bbcKeyAdapter);
                            remappedKeyCode = -1;
                            mMode = MODE_LIST;
                        }
                    })
                    .build();

            View dialogContainer = mDialog.getCustomView();

            mKeyView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_key_text_view));
            mKeyView.setText("Key: " + clickedKey.getKeyString());
            mScanCodeView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_scan_code_text_view));
            mScanCodeView.setText("Key Code: 0x" + Integer.toHexString(clickedKey.getScanCode()));
            mScanCodeRemapView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_scan_code_remap_text_view));
            mMode = MODE_REMAP_DIALOG;
            mDialog.show();
		}
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(MODE_REMAP_DIALOG == mMode) {
            if (mDialog != null && mDialog.isShowing()) {
                remappedKeyCode = keyCode;
                if (mScanCodeRemapView != null) {
                    mScanCodeRemapView.setText("Remap Code: 0x" + Integer.toHexString(remappedKeyCode));
                }
                return true;
            }
        }else if(MODE_SHORTCUT_DIALOG == mMode){
            if (mDialog != null && mDialog.isShowing()) {
                remappedKeyCode = keyCode;
                if (mShortcutKeyView != null) {
                    mShortcutKeyView.setText("Remap Code: 0x" + Integer.toHexString(remappedKeyCode));
                }
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

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

                mDialog = new MaterialDialog.Builder(SettingsActivity.this)
                        .title(R.string.title_activity_key_remap)
                        .customView(R.layout.dialog_shorcut_key)
                        .positiveText("Save")
                        .negativeText("Clear")
                        .callback(new MaterialDialog.Callback(){
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                mPrefs.edit().putInt(PREFS_POPUP_SHORTCUT_KEYCODE, remappedKeyCode).commit();
                                mMode = MODE_LIST;
                            }

                            @Override
                            public void onNegative(MaterialDialog materialDialog) {
                                mMode = MODE_LIST;
                            }
                        })
                        .build();

                View dialogContainer = mDialog.getCustomView();

                mShortcutKeyView = TextView.class.cast(dialogContainer.findViewById(R.id.shortcut_keycode_label));
                mShortcutKeyView.setText("No shortcut set");

                mMode = MODE_SHORTCUT_DIALOG;
                mDialog.show();
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