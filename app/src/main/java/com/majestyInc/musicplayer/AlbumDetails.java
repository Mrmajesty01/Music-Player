package com.majestyInc.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;

import static com.majestyInc.musicplayer.MainActivity.musicFiles;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView imageView;
    String AlbumName;
    ArrayList<MusicFIles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;
    AdView adview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        adview = findViewById(R.id.adView);
        recyclerView = findViewById(R.id.recyclerAlbum);
        imageView = findViewById(R.id.AlbPic);
        AlbumName = getIntent().getStringExtra("albumName");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);



        int j = 0;

        for (int i =0; i< musicFiles.size(); i++ )
        {


            if(AlbumName.equals(musicFiles.get(i).getAlbum()))


            {
                albumSongs.add(j, musicFiles.get(i));

                j++;
            }

        }

        byte [] albumArt = getAlbumArt(albumSongs.get(0).getPath());

        if(imageView !=null)
        {
            Glide.with(this)
                    .load(albumArt)
                    .into(imageView);
        }

        else
            {
                Glide.with(this)
                        .load(R.drawable.opt3)
                        .into(imageView);
            }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(!(albumSongs.size() <1) )
        {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;

    }
}
