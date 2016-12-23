package com.example.bartek.musicplayer;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView rvFacourites;
    private ArrayList<Song> songFavourites;
    private Toolbar toolbar;
    SharedPreferences prefs;


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
            return null;
        }

        @Override
        public void onBindViewHolder(RVAdapter.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
