package com.fiskur.bbcmicro;

import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.view.MenuItem;

public class FiskurAboutActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fiskur_about);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			TextView aboutView = (TextView) findViewById(R.id.about_view);
			aboutView.setText(aboutView.getText().toString() + " (Version: " + version + ")");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
