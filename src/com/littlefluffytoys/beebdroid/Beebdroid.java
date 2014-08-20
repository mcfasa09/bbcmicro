package com.littlefluffytoys.beebdroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import com.fiskur.bbcmicro.BBCUtils;
import com.fiskur.bbcmicro.BBCUtils.KeyMap;
import com.fiskur.bbcmicro.CatlogueActivity;
import com.fiskur.bbcmicro.DiskSelectActivity;
import com.fiskur.bbcmicro.ExplorerActivity;
import com.fiskur.bbcmicro.FiskurAboutActivity;
import com.fiskur.bbcmicro.R;
import com.fiskur.bbcmicro.SetShortcutActivity;
import com.fiskur.bbcmicro.SettingsActivity;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.graphics.Bitmap;

public class Beebdroid extends Activity {

	private static final String TAG = "Beebdroid";

	// Constants
	public static final String BBC_MICRO_PREFS = "fiskur_bbc_miro_prefs";
	private static final int ACTIVITY_RESULT_FILE_EXPLORER = 9000;
	private static final int ACTIVITY_RESULT_WEB_CATALOGUE = 9001;
	private static final int ACTIVITY_RESULT_SETTINGS = 9002;
	private static final int ACTIVITY_RESULT_LOAD_DISK = 9003;
	private static final int EMULATOR_CYCLE_MS = 20;
	private static final int SOFT_UPKEY_WAIT_MS = 50;

	// JNI interface
	public native void bbcInit(ByteBuffer mem, ByteBuffer roms, byte[] audiob, int flags);

	public native void bbcBreak(int flags);

	public native void bbcExit();

	public native int bbcRun();

	public native int bbcInitGl(int width, int height);

	public native void bbcLoadDisc(ByteBuffer disc, int autoboot);

	public native void bbcSetTriggers(short[] pc_triggers);

	public native void bbcKeyEvent(int scancode, int flags, int down);

	public native int bbcSerialize(byte[] buffer);

	public native void bbcDeserialize(byte[] buffer);

	public native int bbcGetThumbnail(Bitmap bmp);

	// Load native library
	static {
		System.loadLibrary("bbcmicro");
	}

	// Emulator
	private BBCUtils mBBCUtils;
	private Model mBBCModel;
	private ByteBuffer mDiskImageByteBuffer;

	private Handler mBBCMainLoopHandler = new Handler();
	private BeebView beebView;

	// Keyboard
	private KeyCharacterMap map = KeyCharacterMap.load(0);
	private Handler mSoftKeyboardHandler;
	private long mSoftKeyboardUpdateMS = 120;
	private ArrayList<Character> mCharactersList = new ArrayList<Character>();
	private EditText mInvisibleEditText = null;
	private InvisibleTextWatcher mInvisibleTextWatcher;
	private boolean mShiftKeyDown;
	private int mShortcutKeycode = -1;
	private boolean mDiskLoaded = false;
	
	//Hardware keyboard remapping
	private Map<Integer, Integer> mRemapMap = null;

	// Audio
	private boolean mAudioPlaying;
	private AudioTrack mAudioTrack;
	private byte[] mAudioBuffer;

	// Basic String from file
	private boolean mProcessingBasic = false;
	private long mStartBasicInput;
	private String mBasicSource;
	
	//Google Drive
//	private GoogleDriveListener mDriveHandler;
//	private GoogleApiClient mGoogleApiClient;
//	private DriveFolder mLastDriveFolder;
//	private static final int REQUEST_CODE_OPENER = 9763;
//	private static final int RESOLVE_CONNECTION_REQUEST_CODE = 9876;
//	private String mDriveFileName;
	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getActionBar().setTitle(null);
		
		//mDriveHandler = new GoogleDriveListener();

		int screenOrientation = getResources().getConfiguration().orientation;

		if (Configuration.ORIENTATION_LANDSCAPE == screenOrientation) {
			getActionBar().hide();
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}

		setContentView(R.layout.activity_beebdroid);

		mBBCUtils = BBCUtils.getInstance();

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgressBar.setMax(100);
		beebView = (BeebView) findViewById(R.id.beeb);

		if (isHardwareKeyboardAvailable()) {
			l("Using physical keyboard");
			initKeyboardRemapping();
		} else {
			l("Using soft keyboard");

			beebView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showInvisibleEditTextKeyboard();
				}
			});

			// Inject a single character key-down into the BBC emulator - then
			// 50ms
			// later inject the key-up event
			mSoftKeyboardHandler = new Handler();
			mSoftKeyboardHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mCharactersList.size() > 0) {
						char character = mCharactersList.remove(0);

						final int shiftScanCode = mBBCUtils.getShiftScanCode(character);
						final int shiftDown;
						final int scanCode;
						if (shiftScanCode != -1) {
							scanCode = shiftScanCode;
							shiftDown = 1;
						} else {
							scanCode = mBBCUtils.getScanCode(character);
							shiftDown = 0;
						}

						bbcKeyEvent(scanCode, shiftDown, 1);

						mSoftKeyboardHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								bbcKeyEvent(scanCode, shiftDown, 0);
							}
						}, SOFT_UPKEY_WAIT_MS);
					}
					if (mProcessingBasic && mBasicSource != null && mBasicSource.length() > 0) {
						processBasicSourceCode();
					}

					mSoftKeyboardHandler.postDelayed(this, mSoftKeyboardUpdateMS);
				}
			}, mSoftKeyboardUpdateMS);
		}

		// TODO - move this whole class into a Fragment and use new mechanism to
		// restore state - getLastNonConfigurationInstance is deprecated.
		Beebdroid prev = (Beebdroid) getLastNonConfigurationInstance();
		if (prev == null) {
			mAudioBuffer = new byte[2000 * 2];
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 31250, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 16384, AudioTrack.MODE_STREAM);
			mBBCModel = new Model();
			mBBCModel.loadRoms(this, Model.SupportedModels[1]);
			bbcInit(mBBCModel.mem, mBBCModel.roms, mAudioBuffer, 1);
			processDiskViaIntent();
		} else {
			mBBCModel = prev.mBBCModel;
			mAudioTrack = prev.mAudioTrack;
			mAudioBuffer = prev.mAudioBuffer;
			bbcInit(mBBCModel.mem, mBBCModel.roms, mAudioBuffer, 0);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		checkScreenAutoRotate();
		
		mBBCMainLoopHandler.postDelayed(bbcEmulatorRunnable, EMULATOR_CYCLE_MS);

		int screenOrientation = getResources().getConfiguration().orientation;
		if (Configuration.ORIENTATION_PORTRAIT == screenOrientation) {
			// Portrait -
			mInvisibleEditText = (EditText) findViewById(R.id.invisible_edit);
			
			if (isHardwareKeyboardAvailable()) {
				// Is this a risky strategy?
				if(mInvisibleEditText != null){
					ViewGroup manager = (ViewGroup) mInvisibleEditText.getParent();
					if(manager.findViewById(R.id.invisible_edit) != null){
						manager.removeView(mInvisibleEditText);
					}
				}
				return;
			}
			//Need to check keycodes here - this doesn't work...
			/*
			mInvisibleEditText.setKeyListener(new KeyListener() {
				
				@Override
				public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
					l("EditText onKeyUp");
					Beebdroid.this.onKeyUp(keyCode, event);
					return true;
				}
				
				@Override
				public boolean onKeyOther(View view, Editable text, KeyEvent event) {
					return false;
				}
				
				@Override
				public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
					l("EditText onKeyDown keyCode: " + keyCode);
					Beebdroid.this.onKeyDown(keyCode, event);
					return true;
				}
				
				@Override
				public int getInputType() {
					return 0;
				}
				
				@Override
				public void clearMetaKeyState(View view, Editable content, int states) {
					l("clearMetaKeyState");
				}
			});
			*/
			
			mInvisibleEditText.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (EditorInfo.IME_NULL == actionId) {
						mCharactersList.add('\n');
					}

					return false;
				}
			});

			
			if (mInvisibleTextWatcher == null) {
				mInvisibleTextWatcher = new InvisibleTextWatcher();
			} else {
				mInvisibleEditText.removeTextChangedListener(mInvisibleTextWatcher);
			}

			mInvisibleEditText.addTextChangedListener(mInvisibleTextWatcher);
			
			showInvisibleEditTextKeyboard();
		} else {
			getActionBar().hide();
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		
		//See if a popup shortcut has been set
		SharedPreferences prefs = getSharedPreferences(BBC_MICRO_PREFS, MODE_PRIVATE);
		if(prefs.contains(SetShortcutActivity.PREFS_POPUP_SHORTCUT_KEYCODE)){
			mShortcutKeycode = prefs.getInt(SetShortcutActivity.PREFS_POPUP_SHORTCUT_KEYCODE, -1);
		}
	}

	// This runnable drives the native emulation code
	private Runnable bbcEmulatorRunnable = new Runnable() {
		@Override
		public void run() {
			// Execute next BBC cycle in 1/50th of a second:
			long now = android.os.SystemClock.uptimeMillis();
			mBBCMainLoopHandler.postAtTime(bbcEmulatorRunnable, now + EMULATOR_CYCLE_MS);

			if (beebView.gl == null) {
				if (beebView.egl == null) {
					// no surface yet
					return;
				}
				beebView.initgl();
				bbcInitGl(beebView.width, beebView.height);
			}

			// BBC cycle
			bbcRun();
		}
	};

	/**
	 * Attempt to show the soft keyboard.
	 */
	private void showInvisibleEditTextKeyboard() {
		if (mInvisibleEditText == null) {
			return;
		}
		mInvisibleEditText.requestFocus();
		mInvisibleEditText.postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.showSoftInput(mInvisibleEditText, 0);
			}
		}, 200);
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		l("onKeyDown " + keycode);
		if(keycode == mShortcutKeycode){
			l("showLoadDiskPopup()");
//			Intent loadDiskIntent = new Intent(Beebdroid.this, DiskSelectActivity.class);
//			startActivityForResult(loadDiskIntent, ACTIVITY_RESULT_LOAD_DISK);
			Intent webCatalogueIntent = new Intent(Beebdroid.this, CatlogueActivity.class);
			startActivityForResult(webCatalogueIntent, ACTIVITY_RESULT_WEB_CATALOGUE);
			return true;
		}
		if (keycode == KeyEvent.KEYCODE_SHIFT_LEFT || keycode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
			mShiftKeyDown = true;
		}
		int bbcKeycode;
		if(mRemapMap != null && mRemapMap.containsKey(keycode)){
			l("Using remap for key: " + keycode);
			bbcKeycode = mRemapMap.get(keycode);
		}else{
			bbcKeycode = BBCUtils.lookupKeycode(mShiftKeyDown, keycode);
		}

		bbcKeyEvent(bbcKeycode, mShiftKeyDown ? 1 : 0, 1);
		return super.onKeyDown(keycode, event);
	}

	@Override
	public boolean onKeyUp(int keycode, KeyEvent event) {
		if(keycode == mShortcutKeycode){
			return true;
		}
		if (keycode == KeyEvent.KEYCODE_SHIFT_LEFT || keycode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
			mShiftKeyDown = false;
		}
		int bbcKeycode;
		if(mRemapMap != null && mRemapMap.containsKey(keycode)){
			bbcKeycode = mRemapMap.get(keycode);
		}else{
			bbcKeycode = BBCUtils.lookupKeycode(mShiftKeyDown, keycode);
		}

		bbcKeyEvent(bbcKeycode, mShiftKeyDown ? 1 : 0, 0);
		return super.onKeyUp(keycode, event);
	}

	// Open BBC disk from browser/file explorer
	private void processDiskViaIntent() {
		Intent intent = getIntent();
		Uri dataUri = intent.getData();

		if (dataUri != null) {
			if (dataUri.getScheme().equals("basic")) {
				l("Load basic file");
			}else if (dataUri.getScheme().equals("file")) {
				ZipInputStream in = null;
				try {
					InputStream input = new FileInputStream(dataUri.getPath());
					if (dataUri.getLastPathSegment().endsWith(".zip")) {
						in = new ZipInputStream(input);
						in.getNextEntry();
						input = in;
					}
					byte[] diskBytes = BBCUtils.readInputStream(input);
					mDiskImageByteBuffer = ByteBuffer.allocateDirect(diskBytes.length);
					mDiskImageByteBuffer.put(diskBytes);
					bbcLoadDisc(mDiskImageByteBuffer, 1);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return this;
	}

	private class InvisibleTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			l("onTextChanged: " + s);
			if (s.length() > 1) {
				char lastChar = s.charAt(s.length() - 1);
				l("onTextChanged - adding char: " + lastChar);
				mCharactersList.add(lastChar);
				
				//This doesn't fix keyboard issue
				if(Character.isDigit(lastChar)){
					mInvisibleEditText.setText("0");
				}else{
					mInvisibleEditText.setText("_");
				}
				
				mInvisibleEditText.setSelection(1);
			}
			if (s.length() == 0) {
				// send backspace command
				l("onTextChanged delete action");
				bbcKeyEvent(BBCUtils.BeebKeys.BBCKEY_DELETE, 0, 1);
				mSoftKeyboardHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						bbcKeyEvent(BBCUtils.BeebKeys.BBCKEY_DELETE, 0, 0);
					}
				}, SOFT_UPKEY_WAIT_MS);
				mInvisibleEditText.setText("_");
				mInvisibleEditText.setSelection(1);
			}
		}
	}

	private void l(String message) {
		Log.d(TAG, message);
	}

	@Override
	public void onPause() {
		super.onPause();
		mBBCMainLoopHandler.removeCallbacks(bbcEmulatorRunnable);
	}

	@Override
	public void onStop() {
		super.onStop();
		bbcExit();
		mAudioTrack.stop();
		mAudioPlaying = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_RESULT_FILE_EXPLORER:
			if (data == null) {
				return;
			}
			if (data.hasExtra(ExplorerActivity.INTENT_EXTRA_CONTENTS)) {
				l("intent has string extra");
				final String basicString = data.getStringExtra(ExplorerActivity.INTENT_EXTRA_CONTENTS);
				l("Basic string:\n" + basicString);
				mBasicSource = basicString;
				processBasicSourceCode();
			} else if (data.hasExtra(ExplorerActivity.INTENT_EXTRA_FILEPATH)) {
				l("intent has filepath extra");
				String filePath = data.getStringExtra(ExplorerActivity.INTENT_EXTRA_FILEPATH);
				l("Disk image to load: " + filePath);
				loadLocalDisk(filePath, true);
			}
			break;
//		case REQUEST_CODE_OPENER:
//			if(data != null){
//				DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//				
//				if(driveId != null){
//					mProgressBar.setVisibility(View.VISIBLE);
//					DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);
//					mLastDriveFolder = Drive.DriveApi.getFolder(mGoogleApiClient, driveId);
//					
//					//This is bad - synchronous call on event thread:
//					mDriveFileName = driveFile.getMetadata(mGoogleApiClient).await().getMetadata().getTitle();
//
//					driveFile.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, mDriveHandler).addResultCallback(mDriveHandler);
//				}else{
//					Toast.makeText(Beebdroid.this, "Google Drive error: no DriveFile ", Toast.LENGTH_LONG).show();
//				}
//			}
//			break;
		case ACTIVITY_RESULT_SETTINGS:
			initKeyboardRemapping();
			break;
		case ACTIVITY_RESULT_LOAD_DISK:
			if(data != null && data.hasExtra(DiskSelectActivity.INTENT_EXTRA_FILEPATH)){
				String filePath = data.getStringExtra(DiskSelectActivity.INTENT_EXTRA_FILEPATH);
				loadLocalDisk(filePath, true);
			}
			break;
		default:
			l("onActivityResult - unrecognised");
		}
	}

	private void initKeyboardRemapping() {
		SharedPreferences prefs = getSharedPreferences(BBC_MICRO_PREFS, MODE_PRIVATE);
		KeyMap[] keys = mBBCUtils.getKeyMaps();
		mRemapMap = new HashMap<Integer, Integer>();
		for(KeyMap key : keys){
			int bbcKeyCode = key.getScanCode();
			String prefKey = SettingsActivity.PREFS_CHAR_PREFIX + Integer.toHexString(bbcKeyCode);
			if(prefs.contains(prefKey)){
				int remappedKeyCode = prefs.getInt(prefKey, -1);
				if(remappedKeyCode != -1){
					l("Found remapping: " + key.getKeyString() + " - " + key.getScanCode() + " is remapped to: " + remappedKeyCode);
					mRemapMap.put(remappedKeyCode, bbcKeyCode);
				}
			}
		}
	}

	private void processBasicSourceCode() {
		char character = mBasicSource.charAt(0);
		character = Character.toLowerCase(character);
		mCharactersList.add(character);
		mBasicSource = mBasicSource.substring(1, mBasicSource.length());
		if(mBasicSource.length() == 0){
			mProcessingBasic = false;
			long basicInputMS = System.currentTimeMillis() - mStartBasicInput;
			l("Basic loading took " + (basicInputMS/1000) + "seconds");
		}
	}

	private void loadLocalDisk(String path, boolean bootIt) {
		mDiskImageByteBuffer = mBBCUtils.loadFile(new File(path));
		mDiskLoaded = true;
		if (bootIt) {
			bbcBreak(0);
		}
		bbcLoadDisc(mDiskImageByteBuffer, 1);
	}
	
	private void loadByteArray(byte[] bytes, boolean bootIt){
		mDiskImageByteBuffer = mBBCUtils.loadBytes(bytes);
		mDiskLoaded = true;
		if (bootIt) {
			bbcBreak(0);
		}
		bbcLoadDisc(mDiskImageByteBuffer, 1);
	}
	
	@Override
	public void onBackPressed() {
		if(mDiskLoaded){
			//TODO - handle escape (key needs to be remapped?)
			super.onBackPressed();
		}else{
			super.onBackPressed();
		}
	}

	// Native JNI Callback
	public void videoCallback() {
		if (beebView.egl != null) {
			beebView.egl.eglSwapBuffers(beebView.display, beebView.surface);
		}
	}

	public void audioCallback(int pos, int cb) {
		mAudioTrack.write(mAudioBuffer, pos, cb);

		if (!mAudioPlaying) {
			mAudioTrack.play();
			mAudioPlaying = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_open_local:
			Intent fileExplorerIntent = new Intent(Beebdroid.this, ExplorerActivity.class);
			startActivityForResult(fileExplorerIntent, ACTIVITY_RESULT_FILE_EXPLORER);
			break;
		case R.id.action_web_catalogue:
			Intent webCatalogueIntent = new Intent(Beebdroid.this, CatlogueActivity.class);
			startActivityForResult(webCatalogueIntent, ACTIVITY_RESULT_WEB_CATALOGUE);
			break;
		case R.id.action_reset:
			mBasicSource = "";
			mDiskLoaded = false;
			bbcBreak(0);
			break;
//		case R.id.action_google_drive:
//			launchGoogleDrive();
//			break;
		case R.id.action_settings:
			Intent settingsIntent = new Intent(Beebdroid.this, SettingsActivity.class);
			startActivityForResult(settingsIntent, ACTIVITY_RESULT_SETTINGS);
			break;
		case R.id.action_hide_actionbar:
			if (getActionBar().isShowing()) {
				getActionBar().hide();
			} else {
				getActionBar().show();
			}
			break;
		case R.id.action_about:
			startActivity(new Intent(Beebdroid.this, FiskurAboutActivity.class));
			break;
		}

		return true;
	}
	
	private void checkScreenAutoRotate(){
		 if (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) != 1) {
		   Toast.makeText(this, "Screen auto-rotate is disabled", Toast.LENGTH_LONG).show();
		 }
	}

	private boolean isHardwareKeyboardAvailable() {
		return getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
	}
	
//	private void launchGoogleDrive(){
//		if (mGoogleApiClient == null) {
//			GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
//			builder.addApi(Drive.API);
//			builder.addScope(Drive.SCOPE_FILE);
//			builder.addConnectionCallbacks(mDriveHandler);
//			builder.addOnConnectionFailedListener(mDriveHandler);
//			mGoogleApiClient = builder.build();
//		}
//		if(mGoogleApiClient.isConnected()){
//			mDriveHandler.showPicker();
//		}else{
//			mGoogleApiClient.connect();
//		}
//	}
	
//	private class GoogleDriveListener implements ConnectionCallbacks, OnConnectionFailedListener, DownloadProgressListener, OnContentsOpenedCallback{
//		
//		GoogleDriveListener(){
//
//		}
//		
//		public void showPicker(){
//			OpenFileActivityBuilder activityBuilder = Drive.DriveApi.newOpenFileActivityBuilder();
//			activityBuilder.setMimeType(new String[] { "application/octet-stream", "application/zip", "text/plain" });
//			
//			//This doesn't work...
//			if(mLastDriveFolder != null){
//				activityBuilder.setActivityStartFolder(mLastDriveFolder.getDriveId());
//			}
//			
//			IntentSender intentSender = activityBuilder.build(mGoogleApiClient);
//			try {
//				startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
//			} catch (SendIntentException e) {
//				l("Unable to send intent");
//			}
//		}
//
//		@Override
//		public void onConnected(Bundle bundle) {
//			showPicker();
//		}
//
//		@Override
//		public void onDisconnected() {
//			
//		}
//
//		@Override
//		public void onConnectionFailed(ConnectionResult connectionResult) {
//			l("onConnectionFailed");
//			if (connectionResult.hasResolution()) {
//				try {
//					l("Trying to resolve issue...");
//					connectionResult.startResolutionForResult(Beebdroid.this, RESOLVE_CONNECTION_REQUEST_CODE);
//				} catch (IntentSender.SendIntentException e) {
//					l("SendIntentException: " + e.getMessage());
//				}
//			} else {
//				GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), Beebdroid.this, 0).show();
//			}
//		}
//
//		@Override
//		public void onOpen(ContentsResult contentsResult) {
//			if (!contentsResult.getStatus().isSuccess()) {
//				Toast.makeText(Beebdroid.this, "Error retrieving disk", Toast.LENGTH_LONG).show();
//				return;
//			}
//
//			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//
//			int bufferSize = 1024;
//			byte[] buffer = new byte[bufferSize];
//			int len = 0;
//			
//			if(mDriveFileName.toLowerCase(Locale.getDefault()).endsWith(".txt") || mDriveFileName.toLowerCase(Locale.getDefault()).endsWith(".bas")){
//				//Assume it a Basic text file
//				try {
//					InputStream is = contentsResult.getContents().getInputStream();
//					while ((len = is.read(buffer)) != -1) {
//						byteBuffer.write(buffer, 0, len);
//					}
//				} catch (IOException e) {
//					l(e.toString());
//					Toast.makeText(Beebdroid.this, "Error loading disk: " + e.toString(), Toast.LENGTH_LONG).show();
//				}
//				
//				byte[] txtFileContents = byteBuffer.toByteArray();
//				mBasicSource = new String(txtFileContents);
//				mProcessingBasic = true;
//				mStartBasicInput = System.currentTimeMillis();
//				processBasicSourceCode();
//			}else if(mDriveFileName.toLowerCase(Locale.getDefault()).endsWith(".zip")){
//				//Assume it's a zipped disk
//				InputStream is = contentsResult.getContents().getInputStream();
//				ZipInputStream zipIn = new ZipInputStream(is);
//				try {
//					zipIn.getNextEntry();
//					is = zipIn;
//					while ((len = is.read(buffer)) != -1) {
//						byteBuffer.write(buffer, 0, len);
//					}
//				} catch (IOException e) {
//					l(e.toString());
//					Toast.makeText(Beebdroid.this, "Error unzipping disk: " + e.toString(), Toast.LENGTH_LONG).show();
//				}
//				byte[] diskContents = byteBuffer.toByteArray();
//				loadByteArray(diskContents, true);
//			}else{
//				//Assume it's a disk
//				try {
//					InputStream is = contentsResult.getContents().getInputStream();
//					while ((len = is.read(buffer)) != -1) {
//						byteBuffer.write(buffer, 0, len);
//					}
//				} catch (IOException e) {
//					l(e.toString());
//					Toast.makeText(Beebdroid.this, "Error loading disk: " + e.toString(), Toast.LENGTH_LONG).show();
//				}
//				
//				byte[] diskContents = byteBuffer.toByteArray();
//				loadByteArray(diskContents, true);
//			}
//			
//			mProgressBar.setProgress(0);
//			mProgressBar.setVisibility(View.GONE);
//		}
//
//		@Override
//		public void onProgress(long bytesDownloaded, long bytesExpected) {
//			int progress = (int) (bytesDownloaded * 100 / bytesExpected);
//			l(String.format("Loading progress: %d percent", progress));
//			mProgressBar.setProgress(progress);
//		}
//	}
}