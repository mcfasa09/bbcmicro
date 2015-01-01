package com.fiskur.bbcmicro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class CatlogueActivity extends ActionBarActivity implements Listener<byte[]> {
	public static final String EXTRA_ZIP_PATH = "com.fiskur.bbcmicro.EXTRA_ZIP_PATH";
	private RequestQueue mQueue;
	private String mFilename;
	private String mSaveDirStr = Environment.getExternalStorageDirectory() + "/FirskurBBCMicro/"; 
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.activity_catlogue);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);
		
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
		    getSupportActionBar().hide();
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}else{
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mWebView = (WebView) findViewById(R.id.web_view);
		mWebView.setBackgroundColor(Color.argb(1, 0, 0, 0));
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		mWebView.setWebViewClient(new CatalogueWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress) {
                CatlogueActivity.this.setTitle("Loading...");
                //CatlogueActivity.this.setProgress(progress * 100);
                if(progress == 100) {
                    CatlogueActivity.this.setTitle("Web Catalogue");
                }
            }
        });
		mWebView.loadUrl("http://fiskur.eu/apps/bbcmicrocat/android.php");

        super.onCreate(savedInstanceState);
	}
	
	private class CatalogueWebViewClient extends WebViewClient {

	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if(!url.endsWith(".zip")){
	    		return false;
	    	}else{
	    		Log.d("CatlogueActivity", "Download .zip: " + url);
	    		mFilename = url.substring(url.lastIndexOf('/') + 1, url.length());
	    		l("Filename is: " + mFilename);
	    		ByteRequest byteReq = new ByteRequest(Method.GET, url, CatlogueActivity.this, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						l("Error downloading byte[]: " + error.toString());
						showError("Error downloading disk: " + error.getMessage());
					}
				});
	    		if(mQueue == null){
	    			mQueue = Volley.newRequestQueue(CatlogueActivity.this);
	    		}
	    		mQueue.add(byteReq);
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

	@Override
	public void onResponse(byte[] response) {
		l("Returned byte[] size: " + response.length);
		checkSaveDirectory();
		try {
			saveFile(mSaveDirStr + mFilename, response);
			Intent zipDownloadedIntent = new Intent();
			zipDownloadedIntent.putExtra(EXTRA_ZIP_PATH, mSaveDirStr + mFilename);
			setResult(RESULT_OK, zipDownloadedIntent);
			CatlogueActivity.this.finish();
		} catch (IOException e) {
			showError("Could not save disk: " + e.toString());
		}
	}
	
	private void showError(String message){
		l("ERROR: " + message) ;
		Toast.makeText(CatlogueActivity.this, message, Toast.LENGTH_LONG).show();
	}
	
	public void saveFile(String fileName, byte[] data) throws IOException{
        l("Saving " + data.length + " bytes into " + fileName);
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.close();
    }
}