package com.fiskur.bbcmicro;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class KeyRemapActivity extends ActionBarActivity {

	public static final String EXTRA_KEY_STRING = "key_remap_extra";
	public static final String EXTRA_SCAN_INT = "key_scancode_extra";
	public static final String EXTRA_SCAN_REMAP_INT = "key_scancode_remap_extra";
	public static final String RESULT_EXTRA_REMAP_KEY = "key_result_remap_key_extra";
	
	private TextView mKeyView;
	private TextView mScanCodeView;
	private TextView mScanCodeRemapView;
	private Button mClearRemapButton;
	private Button mSetRemapButton;
	
	int remappedKeyCode = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_key_remap);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mKeyView = (TextView) findViewById(R.id.remap_key_text_view);

		if(getIntent().hasExtra(EXTRA_KEY_STRING)){
			String key = getIntent().getStringExtra(EXTRA_KEY_STRING);
			mKeyView.setText("Key: " + key);
		}
		
		mScanCodeView = (TextView) findViewById(R.id.remap_scan_code_text_view);

		if(getIntent().hasExtra(EXTRA_SCAN_INT)){
			int scanCode = getIntent().getIntExtra(EXTRA_SCAN_INT, -1);
			mScanCodeView.setText("Key Code: 0x" + Integer.toHexString(scanCode));
		}
		
		mScanCodeRemapView = (TextView) findViewById(R.id.remap_scan_code_remap_text_view);
		
		if(getIntent().hasExtra(EXTRA_SCAN_REMAP_INT)){
			int remapCode = getIntent().getIntExtra(EXTRA_SCAN_REMAP_INT, -1);
			if(remapCode != -1){
				mScanCodeRemapView.setText("Remap Code: 0x" + Integer.toHexString(remapCode));
			}
		}
		
		mClearRemapButton = (Button) findViewById(R.id.remap_clear_button);
		mClearRemapButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				 returnIntent.putExtra(RESULT_EXTRA_REMAP_KEY, -1);
				 setResult(RESULT_OK,returnIntent);     
				 finish();
			}
		});
		
		mSetRemapButton = (Button) findViewById(R.id.remap_set_button);
		mSetRemapButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				 returnIntent.putExtra(RESULT_EXTRA_REMAP_KEY,remappedKeyCode);
				 setResult(RESULT_OK,returnIntent);     
				 finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		remappedKeyCode = keyCode;
		mScanCodeRemapView.setText("Remap Code: 0x" + Integer.toHexString(remappedKeyCode));
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
