package com.example.bartek.musicplayer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView rvFacourites;
    private ArrayList<Song> songFavourites;
    private Toolbar toolbar;
    SharedPreferences prefs;

    private MusicPlayer musicPlayer;
    private Intent playIntent;
    private boolean musicBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayer.MusicBinder musicBinder = (MusicPlayer.MusicBinder) service;

            musicPlayer = musicBinder.getService();

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);


        rvFacourites = (RecyclerView) findViewById(R.id.rvFavourites);
        toolbar = (Toolbar) findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        songFavourites = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor cursor = resolver.query(mediaUri, null, null, null, null);
        prefs = getSharedPreferences(Song.SHARED_FAVOURITES, MODE_PRIVATE);
        Map<String, ?> values = prefs.getAll();


        Log.e("ilosc sharedprefs", String.valueOf(prefs.getAll().size()));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                );
                for (Map.Entry<String, ?> prefData : values.entrySet()) {
                    //Log.e("prefData key", String.valueOf(prefData.getKey()));
                    //Log.e("song key", String.valueOf(song.getId()));
                    if (prefData.getKey().equals(String.valueOf(song.getId()))) {
                        Log.e("prefData key", String.valueOf(prefData.getKey()));
                        Log.e("song key", String.valueOf(song.getId()));
                        songFavourites.add(song);
                    }
                }
            } while (cursor.moveToNext());
            Log.e("countFavourites", String.valueOf(songFavourites.size()));
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (playIntent == null) {
            playIntent = new Intent(getApplicationContext(), MusicPlayer.class);
            bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            if (isMyServiceRunning(MusicPlayer.class)) {
                startService(playIntent);
            }
        }

        rvFacourites.setAdapter(new RVAdapter());
        rvFacourites.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            private Button bPlay;
            private ImageView imgFavourites;
            private TextView tvArtistName;
            private TextView tvSongName;

            public ViewHolder(View itemView) {
                super(itemView);

                bPlay = (Button) itemView.findViewById(R.id.bPlay);
                imgFavourites = (ImageView) itemView.findViewById(R.id.imgFavourites);
                tvArtistName = (TextView) itemView.findViewById(R.id.tvArtistName);
                tvSongName = (TextView) itemView.findViewById(R.id.tvSongName);

            }
        }


        @Override
        public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View songView = inflater.inflate(R.layout.item_list_rv_favourites, parent, false);
            ViewHolder viewHolder = new ViewHolder(songView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RVAdapter.ViewHolder holder, int position) {

            final int pos = position;

            holder.tvArtistName.setText(songFavourites.get(pos).getArtist());
            holder.tvSongName.setText(songFavourites.get(pos).getTitle());

            holder.bPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    musicPlayer.setList(songFavourites);
                    musicPlayer.setSong(pos);
                    try {
                        musicPlayer.playSong();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

            holder.imgFavourites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(String.valueOf(songFavourites.get(pos).getId()));
                    editor.apply();
                    songFavourites.remove(pos);
                    rvFacourites.removeViewAt(pos);
                    RVAdapter.super.notifyItemRemoved(pos);
                    RVAdapter.super.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return songFavourites.size();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
