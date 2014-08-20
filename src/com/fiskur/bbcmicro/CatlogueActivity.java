package com.fiskur.bbcmicro;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class CatlogueActivity extends Activity {
	
	String mSaveDirStr = Environment.getExternalStorageDirectory() + "/FirskurBBCMicro/"; 
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_catlogue);
		
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
		    getActionBar().hide();
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}else{
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		
		mWebView = (WebView) findViewById(R.id.web_view);
		mWebView.setBackgroundColor(Color.argb(1, 0, 0, 0));
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		mWebView.setWebViewClient(new CatalogueWebViewClient());
		mWebView.loadUrl("http://fiskur.eu/apps/bbcmicrocat/android.php");
	}
	
	private class CatalogueWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if(!url.endsWith(".zip")){
	    		return false;
	    	}else{
	    		Log.d("CatlogueActivity", "Download .zip: " + url);
	    		checkSaveDirectory();
	    		return false;
	    	}
	    }
	    
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	mWebView.setVisibility(View.VISIBLE);
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		CatlogueActivity.this.finish();
		return true;
	}
	
	@Override
	public void onBackPressed() {
		if(mWebView.canGoBack()){
			mWebView.goBack();
		}else{
			CatlogueActivity.this.finish();
		}
	}
	
	private void checkSaveDirectory(){
		l("checkSaveDirectory()");
		File saveDirFile = new File(mSaveDirStr);
		if(saveDirFile.exists()){
			l("Save directory already exists");
			return;
		}else{
			l("Making save directory");
			saveDirFile.mkdir();
		}
	}
	
	private void l(String message){
		Log.d("CATACT", ">> " + message);
	}
}
