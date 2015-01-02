package com.fiskur.bbcmicro;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;


public class PackagedGamesActivity extends ActionBarActivity {
    private static final String TAG = "PackagedGamesActivity";

    public static final String EXTRA_PACKAGED_GAME = "com.fiskur.bbcmicro.EXTRA_PACKAGED_GAME";

    private String[] mGames;
    private ListView mGamesList;

    boolean mAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaged_games);

        Toolbar toolbar = (Toolbar)findViewById(R.id.material_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGamesList = ListView.class.cast(findViewById(R.id.packaged_games_list));

        loadHighlights();

        mGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mAll){
                    String selectedGame = mGames[position];
                    gameSelected("games/all/" + selectedGame);
                }else {
                    if (position == mGames.length) {
                        loadAll();
                    } else {
                        String selectedGame = mGames[position];
                        gameSelected("games/highlights/" + selectedGame);
                    }
                }
            }
        });
    }

    private void l(String message){
        Log.d(TAG, message);
    }

    private void gameSelected(String gamePath){
        Intent packagedGameIntent = new Intent();
        packagedGameIntent.putExtra(EXTRA_PACKAGED_GAME, gamePath);
        setResult(RESULT_OK, packagedGameIntent);
        PackagedGamesActivity.this.finish();
    }

    private void loadHighlights(){
        mAll = false;
        AssetManager assetManager = getAssets();

        try {
            mGames = assetManager.list("games/highlights");
            PackagedGamesAdapter gamesAdapter = new PackagedGamesAdapter(this, R.layout.list_row_game, mGames);
            mGamesList.setAdapter(gamesAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAll(){
        mAll = true;
        AssetManager assetManager = getAssets();

        try {
            mGames = assetManager.list("games/all");
            PackagedGamesAdapter gamesAdapter = new PackagedGamesAdapter(PackagedGamesActivity.this, R.layout.list_row_game, mGames);
            mGamesList.setAdapter(gamesAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mAll) {
            loadHighlights();
        }else{
            finish();
        }
        return true;
    }
}
