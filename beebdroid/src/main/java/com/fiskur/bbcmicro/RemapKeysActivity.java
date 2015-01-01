package com.fiskur.bbcmicro;

import android.content.DialogInterface;
import android.content.SharedPreferences;
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

public class RemapKeysActivity extends ActionBarActivity {
    public static final String EXTRA_GAME_TITLE = "com.fiskur.bbcmicro.KEYMAP_GAME_TITLE";
    public static final String PREFS_POPUP_SHORTCUT_KEYCODE = "bbcmicro_popup_shortcut_keycode";
    public static final String PREFS_CHAR_PREFIX = "remap_char_int_";
	private static final String TAG = "RemapKeysActivity";
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

    private String mGameTitle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_GAME_TITLE)){
            mGameTitle = getIntent().getStringExtra(EXTRA_GAME_TITLE);
            mPrefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS + mGameTitle.toUpperCase(), MODE_PRIVATE);
        }else{
            mPrefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS, MODE_PRIVATE);
        }

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

            mDialog = new MaterialDialog.Builder(RemapKeysActivity.this)
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

                            mBBCKeyLabels = BBCUtils.getInstance().getKeyMapsWithRemap(RemapKeysActivity.this);
                            KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (RemapKeysActivity.this, R.layout.list_row_remap, mBBCKeyLabels);
                            mBBCKeyList.setAdapter(bbcKeyAdapter);
                            remappedKeyCode = -1;
                        }

                        @Override
                        public void onNegative(MaterialDialog materialDialog) {
                            mPrefs.edit().remove(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode)).commit();
                            mBBCKeyLabels = BBCUtils.getInstance().getKeyMapsWithRemap(RemapKeysActivity.this);
                            KeyMapAdapter bbcKeyAdapter = new KeyMapAdapter (RemapKeysActivity.this, R.layout.list_row_remap, mBBCKeyLabels);
                            mBBCKeyList.setAdapter(bbcKeyAdapter);
                            remappedKeyCode = -1;
                        }
                    })
                    .build();

            View dialogContainer = mDialog.getCustomView();

            mKeyView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_key_text_view));
            mKeyView.setText("Key: " + clickedKey.getKeyString());
            mScanCodeView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_scan_code_text_view));
            mScanCodeView.setText("Key Code: 0x" + Integer.toHexString(clickedKey.getScanCode()));
            mScanCodeRemapView = TextView.class.cast(dialogContainer.findViewById(R.id.remap_scan_code_remap_text_view));

            int remapCode = mPrefs.getInt(PREFS_CHAR_PREFIX + Integer.toHexString(mSelectedScanCode), -1);
            if(remapCode == -1){
                mScanCodeRemapView.setText("Remap Code: none");
            }else{
                mScanCodeRemapView.setText("Remap Code: 0x" + Integer.toHexString(remapCode));
            }

            mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    L.l("Key Code: 0x" + Integer.toHexString(keyCode));
                    remappedKeyCode = keyCode;
                    mScanCodeRemapView.setText("Remap Code: 0x" + Integer.toHexString(remappedKeyCode));
                    return false;
                }
            });

            mDialog.show();
		}
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
                mDialog = new MaterialDialog.Builder(RemapKeysActivity.this)
                        .title(R.string.title_activity_key_remap)
                        .customView(R.layout.dialog_shorcut_key)
                        .positiveText("Save")
                        .negativeText("Clear")
                        .callback(new MaterialDialog.Callback(){
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                mPrefs.edit().putInt(PREFS_POPUP_SHORTCUT_KEYCODE, remappedKeyCode).commit();
                            }

                            @Override
                            public void onNegative(MaterialDialog materialDialog) {
                                mPrefs.edit().remove(PREFS_POPUP_SHORTCUT_KEYCODE).commit();
                            }
                        })
                        .build();

                View dialogContainer = mDialog.getCustomView();

                mShortcutKeyView = TextView.class.cast(dialogContainer.findViewById(R.id.shortcut_keycode_label));
                int shortcut = mPrefs.getInt(PREFS_POPUP_SHORTCUT_KEYCODE, -1);
                if(shortcut == -1){
                    mShortcutKeyView.setText("No shortcut set");
                }else{
                    mShortcutKeyView.setText("Key Code: 0x" + Integer.toHexString(shortcut));
                }


                mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        L.l("Key Code: 0x" + Integer.toHexString(keyCode));
                        remappedKeyCode = keyCode;
                        mShortcutKeyView.setText("Key Code: 0x" + Integer.toHexString(keyCode));
                        return false;
                    }
                });

                mDialog.show();
				break;
			case R.id.action_wipe:
                Toast.makeText(RemapKeysActivity.this, "All saved disks and keymappings wiped", Toast.LENGTH_LONG).show();
                mPrefs.edit().clear().commit();
                finish();
				break;
            default:
                finish();
		}
		return true;
	}
}