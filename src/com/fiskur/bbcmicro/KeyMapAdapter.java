package com.fiskur.bbcmicro;

import com.fiskur.bbcmicro.BBCUtils.KeyMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class KeyMapAdapter extends ArrayAdapter<BBCUtils.KeyMap> {
	private static final String TAG = "KeyMapAdapter";
	private Context mContext; 
	private int mLayoutResourceId;    
	private BBCUtils.KeyMap[] mData = null;
	private LayoutInflater mInflater;

	public KeyMapAdapter(Context context, int layoutResourceId, BBCUtils.KeyMap[] data){
		super(context, layoutResourceId, data);
		mLayoutResourceId = layoutResourceId;
		mContext = context;
		mData = data;
		
		mInflater = ((Activity)context).getLayoutInflater();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = mInflater.inflate(mLayoutResourceId, parent, false);
		TextView textView = (TextView) row.findViewById(android.R.id.text1);
		KeyMap map = mData[position];
		int scanCode = map.getScanCode();
		String key = "" + map.getKey();
		if(Integer.toHexString(scanCode).equals("49")){
			key = "Enter";
		}
		String label = key  + " [0x" + Integer.toHexString(scanCode) + "]";
		l("Row: " + label);
		textView.setText(label);
		return row;
	}
	
	private void l(String message){
		Log.d(TAG, message);
	}
}
