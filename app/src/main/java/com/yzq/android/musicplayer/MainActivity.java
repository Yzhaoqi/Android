package com.yzq.android.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Button play, stop, quit;
    private SeekBar seekBar;
    private MusicService musicService;
    private TextView current, duration, filepath, status, load_file;
    private ImageView logo;
    private FrameLayout frame;
    private MusicHandler musicHandler;
    private MusicThread musicThread;

    private String address;
    private ArrayList<MusicItem> musicList;
    private int num;

    private Thread thread;
    private float degree = 0;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");

    private DrawCover dc = new DrawCover();

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder)(iBinder)).getService();
            if (musicList != null && !musicList.isEmpty()) {
                Initial();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    class MusicHandler extends Handler {
        MusicHandler(){}
        @Override
        public void handleMessage(Message msg) {
            if (musicService != null && musicService.isValid()) {
                UpdateMusicMessage(musicService.getCurrentMusic());
                seekBar.setMax(musicService.getDuration());
                seekBar.setProgress(musicService.getCurrentPosition());
                Date date = new Date(musicService.getCurrentPosition());
                current.setText(time.format(date));
                degree = (float) ((degree+0.08)%360);
                frame.setRotation(degree);
            }
        }
    }

    class MusicThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    musicHandler.sendMessage(new Message());
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showPhonePermissions() {
        int permissionsCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionsCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission Needed")
                        .setMessage("Rationale")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            }
                        });
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);

        play = (Button)findViewById(R.id.play);
        stop = (Button)findViewById(R.id.stop);
        quit = (Button)findViewById(R.id.quit);
        seekBar = (SeekBar)findViewById(R.id.seek);
        current = (TextView)findViewById(R.id.current);
        duration = (TextView)findViewById(R.id.duration);
        filepath = (TextView)findViewById(R.id.source);
        status = (TextView)findViewById(R.id.status);
        load_file = (TextView)findViewById(R.id.load_file);
        frame = (FrameLayout)findViewById(R.id.frame);
        logo = (ImageView)findViewById(R.id.logo);

        musicHandler = new MusicHandler();
        musicThread = new MusicThread();
        thread = new Thread(musicThread);
        thread.start();
        showPhonePermissions();
        read_music_from_file();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar.setEnabled(true);
                if (play.getText().equals("PLAY")) {
                    musicService.play();
                    play.setText("PAUSE");
                    status.setText("PLAYING");
                } else {
                    musicService.pause();
                    play.setText("PLAY");
                    status.setText("PAUSE");
                }
            }
        });

        stop.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.stop();
                play.setText("PLAY");
                status.setText("STOP");
                seekBar.setEnabled(false);
            }
        }));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b)
                    musicService.seek(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store_music_to_file();
                musicHandler.removeCallbacks(musicThread);
                unbindService(sc);
                try {
                    MainActivity.this.finish();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        store_music_to_file();
        musicHandler.removeCallbacks(musicThread);
        unbindService(sc);
        try {
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            musicList = (ArrayList<MusicItem>)data.getSerializableExtra("musicList");
            num = data.getIntExtra("currentMusic", 0);
            Initial();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load:
                Intent intent = new Intent("com.yzq.Android.MUSICLIST");
                intent.putExtra("musicList", musicList);
                startActivityForResult(intent, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Initial() {
        address = musicList.get(num).getPath();
        Uri uri = Uri.parse(address);
        String mineType = URLConnection.guessContentTypeFromName(address);
        filepath.setText(address);
        play.setText("PLAY");
        status.setText("IDLE");
        if (mineType != null && mineType.startsWith("audio")) {
            dc.setCover(this, logo, uri);
            frame.setRotation(0);
            degree = 0;
            musicService.load(musicList, num);
            Date date = new Date(musicService.getDuration());
            duration.setText(time.format(date));
            load_file.setText("Loaded media file:");
            play.setEnabled(true);
            stop.setEnabled(true);
        } else {
            load_file.setText("Error file open:");
            play.setEnabled(false);
            stop.setEnabled(false);
        }
    }

    private void UpdateMusicMessage(String path) {
        if (address != path) {
            address = path;
            Uri uri = Uri.parse(address);
            String mineType = URLConnection.guessContentTypeFromName(address);
            filepath.setText(address);
            if (mineType != null && mineType.startsWith("audio")) {
                dc.setCover(this, logo, uri);
                frame.setRotation(0);
                degree = 0;
                Date date = new Date(musicService.getDuration());
                duration.setText(time.format(date));
                load_file.setText("Loaded media file:");
            }
        }
    }

    private void store_music_to_file() {
        FileOutputStream fos, numos;
        try {
            fos = openFileOutput("musicList", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(musicList);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            numos = openFileOutput("number", Context.MODE_PRIVATE);
            numos.write(num);
            numos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void read_music_from_file() {
        FileInputStream fis;
        try {
            fis = openFileInput("musicList");
            ObjectInputStream ois = new ObjectInputStream(fis);
            musicList = (ArrayList<MusicItem>)ois.readObject();
            ois.close();
            fis.close();

            fis = openFileInput("number");
            num = fis.read();
            Log.v("the number is", String.valueOf(num));
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}