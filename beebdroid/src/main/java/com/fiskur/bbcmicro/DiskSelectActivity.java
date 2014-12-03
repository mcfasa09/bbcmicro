package com.fiskur.bbcmicro;

import org.json.JSONArray;
import org.json.JSONException;

import com.littlefluffytoys.beebdroid.Beebdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DiskSelectActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "DiskSelectActivity";
	public static final String INTENT_EXTRA_FILEPATH = "extra_filepath";
	public static final String PREFS_DISKS_JSON_ARRAY = "bbcmicro_saved_disks_json_array";
	private TextView mNoDiskLabel;
	private ListView mDiskList;
	private SharedPreferences mPrefs;
	private String[] mPaths;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_select);
		
		mPrefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS, MODE_PRIVATE);
		String disksStr = mPrefs.getString(PREFS_DISKS_JSON_ARRAY, null);
		
		mDiskList = (ListView) findViewById(R.id.disk_select_list);
		
		
		if(disksStr == null){
			mNoDiskLabel = (TextView) findViewById(R.id.no_disks_available_label);
			mNoDiskLabel.setVisibility(View.VISIBLE);
			mDiskList.setVisibility(View.GONE);
		}else{
			mDiskList = (ListView) findViewById(R.id.disk_select_list);
			try {
				JSONArray disksJSONArray = new JSONArray(disksStr);
				int numberDisks = disksJSONArray.length();
				String[] titles = new String[numberDisks];
				mPaths = new String[numberDisks];
				for(int i = 0 ; i < numberDisks ; i++){
					String path = disksJSONArray.getString(i);
					mPaths[i] = path;
					String title;
					if(path.indexOf('/') > -1){
						title = path.substring(path.lastIndexOf('/') + 1, path.length());
						if(title.indexOf('.') > -1){
							title = title.substring(0, title.indexOf('.'));
						}
					}else{
						title = path;
					}
					titles[i] = title;
				}
				ArrayAdapter<String> titlesAdapter = new ArrayAdapter<String>(this, R.layout.list_row_disk, titles);
				mDiskList.setAdapter(titlesAdapter);
				mDiskList.setOnItemClickListener(this);
			} catch (JSONException e) {
				l(e.toString());
			}
		}
	}
	
	private void l(String message){
		Log.d(TAG, message);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		String chosenPath = mPaths[position];
		Intent filepathIntent = new Intent();
		filepathIntent.putExtra(INTENT_EXTRA_FILEPATH, chosenPath);
		DiskSelectActivity.this.setResult(RESULT_OK, filepathIntent);
		DiskSelectActivity.this.finish();
	}
}