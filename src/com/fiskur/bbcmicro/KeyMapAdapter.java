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
	
	static class ViewHolder{
		public TextView labelText;
		public TextView keyCodeText;
		public TextView remapText;
	}

	public KeyMapAdapter(Context context, int layoutResourceId, BBCUtils.KeyMap[] data){
		super(context, layoutResourceId, data);
		mLayoutResourceId = layoutResourceId;
		mContext = context;
		mData = data;
		
		mInflater = ((Activity)context).getLayoutInflater();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if(row == null){
			row = mInflater.inflate(mLayoutResourceId, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.labelText = (TextView) row.findViewById(R.id.list_row_charcter_label);
			viewHolder.keyCodeText = (TextView) row.findViewById(R.id.list_row_keycode_label);
			viewHolder.remapText = (TextView) row.findViewById(R.id.list_row_keycode_remap_label);
			row.setTag(viewHolder);
		}
		
		ViewHolder holder = (ViewHolder) row.getTag();
		KeyMap map = mData[position];
		int scanCode = map.getScanCode();
		String key = "" + map.getKey();
		if(Integer.toHexString(scanCode).equals("49")){
			key = "Enter";
		}else if(Integer.toHexString(scanCode).equals("62")){
			key = "Space";
		}
		holder.labelText.setText(key);
		
		String keyCodeLabelStr = "Keycode: 0x" + Integer.toHexString(scanCode);
		holder.keyCodeText.setText(keyCodeLabelStr);
		
		int remapCode = map.getRemapCode();
		if(remapCode != -1){
			String remapLabelStr = "Remap: 0x" + Integer.toHexString(remapCode);
			holder.remapText.setText(remapLabelStr);
		}else{
			holder.remapText.setText("Remap: none");
		}
		return row;
	}

	private void l(String message){
		Log.d(TAG, message);
	}
}
