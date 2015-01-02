package com.fiskur.bbcmicro;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

public class PackagedGamesAdapter extends ArrayAdapter<String> {
    private static final String TAG = "PackagedGamesAdapter";
    private int mLayoutResourceId;
    private LayoutInflater mInflater;
    private String[] mData;

    static class ViewHolder{
        public TextView labelText;
    }

    public PackagedGamesAdapter(Context context, int layoutResourceId, String[] data){
        super(context, layoutResourceId, data);
        mLayoutResourceId = layoutResourceId;
        mData = new String[data.length + 1];

        for(int i = 0 ; i < data.length ; i++){
            mData[i] = data[i];
        }

        mData[mData.length-1] = "View all games";


        Arrays.sort(mData, new Comparator<String>() {
            @Override
            public int compare(String entry1, String entry2) {
                return entry1.compareTo(entry2);
            }
        });



        mInflater = ((Activity)context).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if(row == null){
            row = mInflater.inflate(mLayoutResourceId, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.labelText = (TextView) row.findViewById(R.id.list_row_label);
            row.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) row.getTag();
        String game = mData[position];

        if(game.indexOf(".zip") > 0){
            game = game.substring(0, game.indexOf(".zip"));
        }

        game = decodeCamel(game);

        holder.labelText.setText(game);
        return row;
    }

    private String decodeCamel(String game){
        char[] letters = game.toCharArray();

        StringBuilder sb = new StringBuilder();

        for(int i = 0 ; i < letters.length ; i++){
            sb.append(letters[i]);
            int next = i + 1;

            if(next < letters.length && Character.isUpperCase(letters[next])){
                if(letters[i] == '3' && letters[next] == 'D'){
                    //3D
                }else{
                    sb.append(" ");
                }
            }
        }

        return sb.toString();
    }
}

