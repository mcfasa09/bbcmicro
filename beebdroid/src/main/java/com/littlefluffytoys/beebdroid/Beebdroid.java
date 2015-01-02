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
import com.fiskur.bbcmicro.L;
import com.fiskur.bbcmicro.PackagedGamesActivity;
import com.fiskur.bbcmicro.R;
import com.fiskur.bbcmicro.RemapKeysActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.graphics.Bitmap;

public class Beebdroid extends ActionBarActivity {

	private static final String TAG = "Beebdroid";

    private boolean mShowingToolbar = false;
    private Menu mMenu = null;
    private String mGameTitle = null;
    private boolean mPaused = false;

	public static final String BBC_MICRO_PREFS = "fiskur_bbc_micro_prefs";
	private static final int ACTIVITY_RESULT_FILE_EXPLORER = 9000;
	private static final int ACTIVITY_RESULT_WEB_CATALOGUE = 9001;
	private static final int ACTIVITY_RESULT_SETTINGS = 9002;
	private static final int ACTIVITY_RESULT_LOAD_DISK = 9003;
    private static final int ACTIVITY_RESULT_PACKAGED_CATALOGUE = 9004;
	private static final int EMULATOR_CYCLE_MS = 20;

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
	private ArrayList<Character> mCharactersList = new ArrayList<Character>();
	private boolean mShiftKeyDown;
	private int mShortcutKeycode = -1;
	private boolean mDiskLoaded = false;
	
	//Hardware keyboard remapping
	private Map<Integer, Integer> mRemapMap = null;

	// Audio
	private boolean mAudioPlaying;
	private AudioTrack mAudioTrack;
	private byte[] mAudioBuffer;

	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_beebdroid);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);

        int screenOrientation = getResources().getConfiguration().orientation;
        if (Configuration.ORIENTATION_LANDSCAPE == screenOrientation) {
        }

		mBBCUtils = BBCUtils.getInstance();

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgressBar.setMax(100);
		beebView = (BeebView) findViewById(R.id.beeb);

        beebView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getSupportActionBar().isShowing()){
                    hideToolbar();
                }else{
                    showToolbar();
                }
            }
        });

        initKeyboardRemapping();

		// TODO - move this whole class into a Fragment and use new mechanism to
		// restore state - getLastNonConfigurationInstance is deprecated.
		Beebdroid prev = (Beebdroid) getLastCustomNonConfigurationInstance();
		if (prev == null) {
            L.l("No prev Beebdroid instance");
			mAudioBuffer = new byte[2000 * 2];
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 31250, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 16384, AudioTrack.MODE_STREAM);
			mBBCModel = new Model();
			mBBCModel.loadRoms(this, Model.SupportedModels[1]);
			bbcInit(mBBCModel.mem, mBBCModel.roms, mAudioBuffer, 1);
			processDiskViaIntent();
		} else {
            L.l("Beebdroid instance found - reloading state");
			mBBCModel = prev.mBBCModel;
			mAudioTrack = prev.mAudioTrack;
			mAudioBuffer = prev.mAudioBuffer;
			bbcInit(mBBCModel.mem, mBBCModel.roms, mAudioBuffer, 0);
		}

        isHardwareKeyboardAvailable();
	}

	@Override
	public void onResume() {
		super.onResume();
        L.l("onResume()");
		checkScreenAutoRotate();

        if(mShowingToolbar){
            showToolbar();
        }else{
            hideToolbar();
        }
		
		mBBCMainLoopHandler.postDelayed(bbcEmulatorRunnable, EMULATOR_CYCLE_MS);

		//See if a popup shortcut has been set
		SharedPreferences prefs = getSharedPreferences(BBC_MICRO_PREFS, MODE_PRIVATE);
		if(prefs.contains(RemapKeysActivity.PREFS_POPUP_SHORTCUT_KEYCODE)){
			mShortcutKeycode = prefs.getInt(RemapKeysActivity.PREFS_POPUP_SHORTCUT_KEYCODE, -1);
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
            if(!mPaused){
                bbcRun();
            }

		}
	};

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		l("onKeyDown " + keycode);
		if(keycode == mShortcutKeycode){
			l("showLoadDiskPopup()");
			Intent webCatalogueIntent = new Intent(Beebdroid.this, CatlogueActivity.class);
			startActivityForResult(webCatalogueIntent, ACTIVITY_RESULT_WEB_CATALOGUE);
			return true;
		}
		if (keycode == KeyEvent.KEYCODE_SHIFT_LEFT || keycode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
			mShiftKeyDown = true;
		}
		int bbcKeycode;
		if(mRemapMap != null && mRemapMap.containsKey(keycode)){
			//l("Using remap for key: " + keycode);
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
    public Object onRetainCustomNonConfigurationInstance(){
        return this;
    }

	private void l(String message) {
		Log.d(TAG, message);
	}

	@Override
	public void onPause() {
        l("onPause()");
		super.onPause();
		mBBCMainLoopHandler.removeCallbacks(bbcEmulatorRunnable);
	}

	@Override
	public void onStop() {
        l("onStop()");
		super.onStop();
		bbcExit();
		mAudioTrack.stop();
		mAudioPlaying = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
        case ACTIVITY_RESULT_PACKAGED_CATALOGUE:
            l("ACTIVITY_RESULT_PACKAGED_CATALOGUE");
            if (data == null) {
                l("No data");
                return;
            }
            if(data.hasExtra(PackagedGamesActivity.EXTRA_PACKAGED_GAME)){
                String game = data.getStringExtra(PackagedGamesActivity.EXTRA_PACKAGED_GAME);
                l("Load game: " + game);
                updateGameMappingMenuItem(game);
                ZipInputStream in = null;
                try {
                    InputStream input = getAssets().open(game);
                    in = new ZipInputStream(input);
                    in.getNextEntry();
                    input = in;
                    byte[] diskBytes = BBCUtils.readInputStream(input);
                    mDiskImageByteBuffer = ByteBuffer.allocateDirect(diskBytes.length);
                    mDiskImageByteBuffer.put(diskBytes);
                    bbcLoadDisc(mDiskImageByteBuffer, 1);
                    mDiskLoaded = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            break;
		case ACTIVITY_RESULT_WEB_CATALOGUE:
			if (data == null) {
				return;
			}
			if(data.hasExtra(CatlogueActivity.EXTRA_ZIP_PATH)){
				String zipPath = data.getStringExtra(CatlogueActivity.EXTRA_ZIP_PATH);

                updateGameMappingMenuItem(zipPath);

				ZipInputStream in = null;
				try {
					InputStream input = new FileInputStream(zipPath);
						in = new ZipInputStream(input);
						in.getNextEntry();
						input = in;
					byte[] diskBytes = BBCUtils.readInputStream(input);
					mDiskImageByteBuffer = ByteBuffer.allocateDirect(diskBytes.length);
					mDiskImageByteBuffer.put(diskBytes);
					bbcLoadDisc(mDiskImageByteBuffer, 1);
                    mDiskLoaded = true;
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
			break;
		case ACTIVITY_RESULT_FILE_EXPLORER:
			if (data == null) {
				return;
			}
			if (data.hasExtra(ExplorerActivity.INTENT_EXTRA_CONTENTS)) {
				//Not supported:
				//l("intent has string extra");
				//final String basicString = data.getStringExtra(ExplorerActivity.INTENT_EXTRA_CONTENTS);
				//l("Basic string:\n" + basicString);
			} else if (data.hasExtra(ExplorerActivity.INTENT_EXTRA_FILEPATH)) {
				String filePath = data.getStringExtra(ExplorerActivity.INTENT_EXTRA_FILEPATH);
				loadLocalDisk(filePath, true);
			}
			break;
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
        L.l("initKeyboardRemapping()");

        if(mDiskLoaded &&  mGameTitle != null){
            SharedPreferences gamePrefs = getSharedPreferences(BBC_MICRO_PREFS + mGameTitle.toUpperCase(), MODE_PRIVATE);
            if(!gamePrefs.getAll().isEmpty()){
                initKeyMapping(gamePrefs);
            }else{
                initKeyMapping(getSharedPreferences(BBC_MICRO_PREFS, MODE_PRIVATE));
            }
        }else {
            initKeyMapping(getSharedPreferences(BBC_MICRO_PREFS, MODE_PRIVATE));
        }
	}

    private void initKeyMapping(SharedPreferences prefs){
        KeyMap[] keys = mBBCUtils.getKeyMaps();
        mRemapMap = new HashMap<Integer, Integer>();
        for (KeyMap key : keys) {
            int bbcKeyCode = key.getScanCode();
            String prefKey = RemapKeysActivity.PREFS_CHAR_PREFIX + Integer.toHexString(bbcKeyCode);
            if (prefs.contains(prefKey)) {
                int remappedKeyCode = prefs.getInt(prefKey, -1);
                if (remappedKeyCode != -1) {
                    l("Found remapping: " + key.getKeyString() + " - " + key.getScanCode() + " is remapped to: " + remappedKeyCode);
                    mRemapMap.put(remappedKeyCode, bbcKeyCode);
                }
            }
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
        mMenu = menu;
		return true;
	}

    private void updateGameMappingMenuItem(String game){
        if(mMenu == null){
            return;
        }
        if(game.indexOf("/") > -1 && game.endsWith(".zip")){
            mGameTitle = game.substring(game.lastIndexOf("/") + 1, game.indexOf(".zip"));
        }else if(game.endsWith(".zip")){
            mGameTitle = game.substring(0, game.indexOf(".zip"));
        }else{
            mGameTitle = "Unknown";
        }

        MenuItem gameMappingMenuItem = mMenu.findItem(R.id.action_game_mapping);
        gameMappingMenuItem.setTitle("" + mGameTitle + " Key Mapping");
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.action_pause:
                mPaused = !mPaused;
                if(mPaused){
                    item.setTitle("Continue");
                }else{
                    item.setTitle("Pause");
                }
                break;
		case R.id.action_open_local:
			Intent fileExplorerIntent = new Intent(Beebdroid.this, ExplorerActivity.class);
			startActivityForResult(fileExplorerIntent, ACTIVITY_RESULT_FILE_EXPLORER);
			break;
		case R.id.action_web_catalogue:
			Intent webCatalogueIntent = new Intent(Beebdroid.this, CatlogueActivity.class);
			startActivityForResult(webCatalogueIntent, ACTIVITY_RESULT_WEB_CATALOGUE);
			break;
        case R.id.action_packaged_catalogue:
            Intent packagedCatalogueIntent = new Intent(Beebdroid.this, PackagedGamesActivity.class);
            startActivityForResult(packagedCatalogueIntent, ACTIVITY_RESULT_PACKAGED_CATALOGUE);
            break;
		case R.id.action_reset:
			mDiskLoaded = false;
			bbcBreak(0);
			break;
		case R.id.action_global_mapping:
			Intent globalMappingIntent = new Intent(Beebdroid.this, RemapKeysActivity.class);
			startActivityForResult(globalMappingIntent, ACTIVITY_RESULT_SETTINGS);
			break;
        case R.id.action_game_mapping:
            Intent gameMappingIntent = new Intent(Beebdroid.this, RemapKeysActivity.class);
            gameMappingIntent.putExtra(RemapKeysActivity.EXTRA_GAME_TITLE, mGameTitle);
            startActivityForResult(gameMappingIntent, ACTIVITY_RESULT_SETTINGS);
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

    //This method is broken on Lollipop and/or with the folio case for Nexus 9...
	private boolean isHardwareKeyboardAvailable() {
        int keyboardConfig = getResources().getConfiguration().keyboard;
        boolean hasHardwareKeys = keyboardConfig == Configuration.KEYBOARD_QWERTY;
        L.l("hasHardwareKeys = " + hasHardwareKeys);
		return hasHardwareKeys;
	}

    private void hideToolbar(){
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mShowingToolbar = false;
    }

    private void showToolbar(){
        getSupportActionBar().show();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mShowingToolbar = true;
    }
}