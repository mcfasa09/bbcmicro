package com.fiskur.bbcmicro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.littlefluffytoys.beebdroid.Beebdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ExplorerActivity extends Activity {
	private static final String TAG = "FilesActivity";
	public static final int MODE_SAVE = 1;
	public static final int MODE_OPEN = 2;
	public static final String MODE = "mode";
	private int mMode;

	public static final int RESULT_FILES_ACTIVITY = 321;
	public static final String INTENT_EXTRA_CONTENTS = "extra_contents";
	public static final String INTENT_EXTRA_FILENAME = "extra_filename";
	public static final String INTENT_EXTRA_FILEPATH = "extra_filepath";

	private TableLayout mFilenameTable;
	private EditText mFilenameEdit;
	private ListView mFilesList;
	private List<String> item = null;
	private List<String> path = null;
	private static final String root = "/";

	private static String mCurrentDirectory = root;
	private static String mFilename = "";
	private String mContent;

	private ProgressBar mProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mMode = getIntent().getIntExtra(MODE, MODE_OPEN);

		mProgress = (ProgressBar) findViewById(R.id.progress);
		mProgress.setIndeterminate(true);

		mFilenameTable = (TableLayout) findViewById(R.id.filename_table);
		mFilenameEdit = (EditText) findViewById(R.id.filename_edit);

		mFilenameEdit.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int action, KeyEvent arg2) {
				mFilename = mFilenameEdit.getText().toString();
				if (mFilename.length() == 0) {
					showError("Enter filename");
					return true;
				}

				if (mContent != null) {
					Log.d("FilesActivity", "Save file to: " + mCurrentDirectory + " with filename: " + mFilename);
					new SaveFile(mCurrentDirectory + "/" + mFilename, mContent).execute("");
				} else {
					Intent filenameIntent = new Intent();
					filenameIntent.putExtra(INTENT_EXTRA_FILENAME, "Could not save file - no content");
					ExplorerActivity.this.setResult(RESULT_FILES_ACTIVITY, filenameIntent);
					ExplorerActivity.this.finish();
				}

				return true;
			}

		});

		if (MODE_OPEN == mMode) {
			mFilenameTable.setVisibility(View.GONE);
		} else {
			// we're saving
			mContent = getIntent().getStringExtra(INTENT_EXTRA_CONTENTS);
		}

		mFilesList = (ListView) findViewById(R.id.file_list);
		mFilesList.setOnItemClickListener(mItemClickListener);
		
		getDir(mCurrentDirectory);
		mFilenameEdit.setText(mFilename);
		Log.d("FilesActivity", "Resetting folder to " + mCurrentDirectory + " and filename to " + mFilename);

	}

	private void showError(String error) {
		Toast.makeText(ExplorerActivity.this, error, Toast.LENGTH_LONG).show();
	}

	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			File file = new File(path.get(position));
			if (file.isDirectory()) {
				if (file.canRead()) {
					mCurrentDirectory = file.getAbsolutePath();
					getDir(path.get(position));
				} else {
					showError(file.getName() + " folder can't be opened");
				}
			} else {
				Log.d("FilesActivity", "File chosen: " + file.getAbsolutePath());
				switch (mMode) {
				case MODE_OPEN:
					mFilename = file.getName();
					if(mFilename.endsWith(".ssd") || mFilename.endsWith(".SSD")){
						String filePath = file.getPath();
						savePath(filePath);
						Intent filepathIntent = new Intent();
						filepathIntent.putExtra(INTENT_EXTRA_FILEPATH, filePath);
						ExplorerActivity.this.setResult(RESULT_FILES_ACTIVITY, filepathIntent);
						ExplorerActivity.this.finish();
					}else{
						new OpenFile(file.getAbsolutePath()).execute("");
					}
					
					break;
				case MODE_SAVE:
					mFilenameEdit.setText(file.getName());
					break;
				}
			}
		}
	};
	
	private void savePath(String path){
		SharedPreferences prefs = getSharedPreferences(Beebdroid.BBC_MICRO_PREFS, MODE_PRIVATE);
		String disksStr = prefs.getString(DiskSelectActivity.PREFS_DISKS_JSON_ARRAY, null);
		try {
		JSONArray disksJSONArray = null;
		if(disksStr == null){
			disksJSONArray = new JSONArray();
		}else{
			disksJSONArray = new JSONArray(disksStr);
		}
		
		disksJSONArray.put(path);
		prefs.edit().putString(DiskSelectActivity.PREFS_DISKS_JSON_ARRAY, disksJSONArray.toString()).commit();
		} catch (JSONException e) {
			l(e.toString());
		}
	}

	private class OpenFile extends AsyncTask<String, String, String> {

		private String mPath;
		private String mContents;
		private boolean error;

		public OpenFile(String path) {
			mPath = path;
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... arg0) {
			try {
				mContents = getStringFromFile(mPath);
			} catch (Exception e) {
				error = true;
				mContents = e.toString();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mProgress.setVisibility(View.GONE);
			if (error) {
				showError("Could not open file");
			} else {
				Intent contentIntent = new Intent();
				contentIntent.putExtra(INTENT_EXTRA_CONTENTS, mContents);

				ExplorerActivity.this.setResult(RESULT_FILES_ACTIVITY, contentIntent);
				ExplorerActivity.this.finish();
			}
			super.onPostExecute(result);
		}

		private String getStringFromFile(String filePath) throws Exception {
			File fl = new File(filePath);
			FileInputStream fin = new FileInputStream(fl);
			String ret = convertStreamToString(fin);
			fin.close();
			return ret;
		}

		private String convertStreamToString(InputStream is) throws Exception {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		}
	}

	private class SaveFile extends AsyncTask<String, String, String> {

		private String mFilepath;
		private String mContent;
		boolean mFileCreated;

		public SaveFile(String filepath, String content) {
			mFilepath = filepath;
			mContent = content;
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... arg0) {
			File saveFile = new File(mFilepath);
			if (saveFile.exists()) {
				boolean deleted = saveFile.delete();
				if(!deleted){
					mFileCreated = false;
					return null;
				}
			}
				FileWriter out;
				try {
					File newFile = new File(mFilepath);
					mFileCreated = newFile.createNewFile();
					if (mFileCreated) {
						out = new FileWriter(newFile);
						out.write(mContent);
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mProgress.setVisibility(View.GONE);
			if (mFileCreated) {
				Intent filenameIntent = new Intent();
				filenameIntent.putExtra(INTENT_EXTRA_FILENAME, mFilenameEdit.getText().toString());
				ExplorerActivity.this.setResult(RESULT_FILES_ACTIVITY, filenameIntent);
				ExplorerActivity.this.finish();
			} else {
				showError("Could not save file");
			}

			super.onPostExecute(result);
		}
	}

	private void getDir(String dirPath) {

		Log.d("FilesActivity", "Location: " + dirPath);

		item = new ArrayList<String>();
		path = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			path.add(file.getPath());

			if (file.isDirectory()) {
				item.add(file.getName() + "/");
			} else {
				item.add(file.getName());
			}
		}

		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, item);
		mFilesList.setAdapter(fileList);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		ExplorerActivity.this.finish();
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void l(String message){
		Log.d(TAG, message);
	}
}