package com.yzq.android.musicplayer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZQ on 2016/11/8.
 */

public class MusicList extends AppCompatActivity {

    private ListView musicList;

    private ProgressDialog pd;
    private MyHandler handler = new MyHandler();

    final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/";
    private List<MusicItem> songList = new ArrayList<MusicItem>();
    private ArrayList<MusicItem> music_list = new ArrayList<MusicItem>();
    private String mp3Pattern = ".mp3";

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MusicAdapter musicAdapter = new MusicAdapter(MusicList.this, songList);
            musicList.setAdapter(musicAdapter);
            pd.dismiss();
        }
    }

    Runnable newRunnable = new Runnable() {
        @Override
        public void run() {
            getPlayList();
            handler.sendMessage(new Message());
        }
    };

    private void getPlayList() {
        if (!songList.isEmpty()) {
            songList.clear();
        }
        if (!music_list.isEmpty()) {
            music_list.clear();
        }
        if (MEDIA_PATH != null) {
            File home = new File(MEDIA_PATH);
            File[] listFiles = home.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void addSongToList(File song) {
        long Filesize = song.length()/1024;
        if(song.getName().endsWith(mp3Pattern) && Filesize > 1024) {
            MusicItem newSong = new MusicItem(song.getName().substring(0, (song.getName().length()-4)), song.getPath());
            songList.add(newSong);
            music_list.add(newSong);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_list_activity_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiclist);

        musicList = (ListView) findViewById(R.id.music_list);

        Bundle extras = getIntent().getExtras();
        if ((ArrayList<MusicItem>) extras.getSerializable("musicList") != null) {
            music_list = (ArrayList<MusicItem>) extras.getSerializable("musicList");
            List<MusicItem> myList = (ArrayList<MusicItem>) extras.getSerializable("musicList");
            MusicAdapter musicAdapter = new MusicAdapter(MusicList.this, myList);
            musicList.setAdapter(musicAdapter);
        }

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("musicList", music_list);
                intent.putExtra("currentMusic", i);
                MusicList.this.setResult(RESULT_OK, intent);
                handler.removeCallbacksAndMessages(null);
                MusicList.this.finish();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                pd = ProgressDialog.show(MusicList.this, "Scanning", "Loading, Please wait...");
                new Thread(newRunnable).start();
        }
        return super.onOptionsItemSelected(item);
    }
}
