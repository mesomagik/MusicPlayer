package com.example.bartek.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bartek.musicplayer.MusicPlayer.MusicBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity {

    private MusicPlayer musicPlayer;
    private Intent playIntent;
    private boolean musicBound = false;

    private SharedPreferences prefs;
    private Toolbar toolbar;
    private RecyclerView rvSongs;
    private ArrayList<Song> songList;
    private TextView tvSongNamePlayer;
    private Button bNext;
    private Button bPrev;
    private Button bPlay;
    private ImageView imgFavourite;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;

            musicPlayer = binder.getService();
            musicPlayer.setList(songList);

            if (musicPlayer != null) {
                Log.i("service-bind", "Service is bonded successfully!");
            }

            RVAdapter rvAdapter = new RVAdapter(getApplicationContext(), songList, musicPlayer);
            rvSongs.setAdapter(rvAdapter);
            rvSongs.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(Song.SHARED_FAVOURITES, MODE_PRIVATE);

        tvSongNamePlayer = (TextView) findViewById(R.id.tvSongNamePlayer);
        bNext = (Button) findViewById(R.id.bNext);
        bPrev = (Button) findViewById(R.id.bPrev);
        bPlay = (Button) findViewById(R.id.bPlay);
        imgFavourite = (ImageView) findViewById(R.id.imgFavourite);
        imgFavourite.setVisibility(View.INVISIBLE);

        songList = new ArrayList<>();

        ContentResolver resolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor musicCursor = resolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                Song song = new Song(
                        musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                );
                Log.e("art", String.valueOf(musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media._ID))));
                songList.add(song);
            } while (musicCursor.moveToNext());
        }

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        rvSongs = (RecyclerView) findViewById(R.id.rvSongs);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgFavourite.setVisibility(View.VISIBLE);
                if (isFavourite(songList.get(musicPlayer.getSongNum()).getId())) {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_gold));
                } else {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_black));
                }
                if (musicPlayer.getSongNum() != songList.size() - 1) {
                    musicPlayer.setSong(musicPlayer.getSongNum() + 1);
                } else {
                    musicPlayer.setSong(0);
                }
                try {
                    musicPlayer.playSong();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tvSongNamePlayer.setText(songList.get(musicPlayer.getSongNum()).getTitle());
            }
        });

        bPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgFavourite.setVisibility(View.VISIBLE);
                if (isFavourite(songList.get(musicPlayer.getSongNum()).getId())) {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_gold));
                } else {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_black));
                }
                if (musicPlayer.getSongNum() != 0) {
                    musicPlayer.setSong(musicPlayer.getSongNum() - 1);
                } else {
                    musicPlayer.setSong(songList.size());
                }
                try {
                    musicPlayer.playSong();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tvSongNamePlayer.setText(songList.get(musicPlayer.getSongNum()).getTitle());
            }
        });

        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgFavourite.setVisibility(View.VISIBLE);
                if (isFavourite(songList.get(musicPlayer.getSongNum()).getId())) {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_gold));
                } else {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_black));
                }
                if (musicPlayer.isPlaying()) {
                    bPlay.setText("Pause");
                    musicPlayer.setPause();
                } else {
                    bPlay.setText("Play");
                    try {
                        musicPlayer.playSong();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        imgFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ilosc sharedprefs", String.valueOf(prefs.getAll().size()));
                if (isFavourite(songList.get(musicPlayer.getSongNum()).getId())) {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_black));
                    prefs.edit();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(String.valueOf(songList.get(musicPlayer.getSongNum()).getId()));
                    editor.apply();
                } else {
                    imgFavourite.setImageDrawable(getResources().getDrawable(R.drawable.star_gold));
                    prefs.edit();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(String.valueOf(songList.get(musicPlayer.getSongNum()).getId()), songList.get(musicPlayer.getSongNum()).getId());
                    editor.apply();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(),FavouritesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (playIntent == null) {
            playIntent = new Intent(getApplicationContext(), MusicPlayer.class);

            if (connection != null) {
                Log.e("connection", "is not null");
            }

            bindService(playIntent, connection, Context.BIND_AUTO_CREATE);
            Log.e("service", "start");
            startService(playIntent);


            if (musicPlayer == null) {
                Log.e("onStart check musicPl", "null");
            }
        }
    }

    public boolean isFavourite(long id) {
        SharedPreferences prefs = getSharedPreferences(Song.SHARED_FAVOURITES, MODE_PRIVATE);
        Map<String, ?> values = prefs.getAll();

        for (Map.Entry<String, ?> prefData : values.entrySet()) {
            if (prefData.getKey().equals(String.valueOf(id))) {
                return true;
            }
        }
        return false;
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

        private ArrayList<Song> mSongList;
        private Context mContext;
        private MusicPlayer mMusicPlayer;


        public RVAdapter(Context context, ArrayList<Song> songList, MusicPlayer musicPlayer) {
            mSongList = songList;
            mContext = context;

            if (musicPlayer == null) {
                Log.e("constructor", "null");
            }
            mMusicPlayer = musicPlayer;
        }

        private Context getContext() {
            return mContext;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tvSongName;
            public TextView tvArtist;
            public Button bPlay;

            public ViewHolder(View items) {

                super(items);
                tvSongName = (TextView) items.findViewById(R.id.tvSongName);
                bPlay = (Button) items.findViewById(R.id.bPlay);
                tvArtist = (TextView) items.findViewById(R.id.tvArtist);
            }
        }

        @Override
        public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            View songView = layoutInflater.inflate(R.layout.item_list_rv, parent, false);
            ViewHolder view = new ViewHolder(songView);

            return view;
        }

        @Override
        public void onBindViewHolder(RVAdapter.ViewHolder holder, final int position) {

            final Integer pos = position;

            holder.tvSongName.setText(mSongList.get(pos).getTitle());
            holder.tvArtist.setText(mSongList.get(pos).getArtist());

            holder.bPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    imgFavourite.setVisibility(View.VISIBLE);
                    if (mMusicPlayer == null) {
                        Log.e("position", pos.toString());
                    }

                    mMusicPlayer.setSong(pos);
                    try {
                        mMusicPlayer.playSong();
                        tvSongNamePlayer.setText(songList.get(pos).getTitle());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return songList.size();
        }


    }
}