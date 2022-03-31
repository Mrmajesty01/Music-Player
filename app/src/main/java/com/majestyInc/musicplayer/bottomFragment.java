package com.majestyInc.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;
import static com.majestyInc.musicplayer.MainActivity.ARTIST_NAME;
import static com.majestyInc.musicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.majestyInc.musicplayer.MainActivity.MUSIC_LAST_PLAYED;
import static com.majestyInc.musicplayer.MainActivity.PATH_TO_FRAG;
import static com.majestyInc.musicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.majestyInc.musicplayer.MainActivity.SONG_NAME;
import static com.majestyInc.musicplayer.MainActivity.SONG_TO_FRAG;
import static com.majestyInc.musicplayer.MusicService.MUSIC_FILE;


public class bottomFragment extends Fragment implements ServiceConnection {




    public static ImageView playNpause;
    ImageView songArt, nextbtn;
    TextView songname,artist;
    FrameLayout miniplayer;
    MusicService musicService;

    View view;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    ArrayList<MusicFIles> musicFIles = new ArrayList<>();
    MusicFIles mFilles;


    public bottomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bottom, container, false);
        artist = view.findViewById(R.id.ArtistName);
        songname = view.findViewById(R.id.songName);
        songArt = view.findViewById(R.id.songArt);
        nextbtn = view.findViewById(R.id.nextbtn);
        playNpause = view.findViewById(R.id.playpausebtn);
        miniplayer = view.findViewById(R.id.framelayout);


        miniplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {




                }}
        });

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicService !=null)
                {
                    musicService.nextBtnClicked();


                    if(getActivity() !=null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
                                .edit();
                        editor.putString(MUSIC_FILE, musicService.musicFIles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFIles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFIles.get(musicService.position).getTitle());
                        editor.apply();


                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE,null);
                        String artistName = preferences.getString(ARTIST_NAME,null);
                        String song = preferences.getString(SONG_NAME,null);


                        if(path!=null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistName;
                            SONG_TO_FRAG = song;


                        }
                        else
                        {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_TO_FRAG = null;
                        }

                        if(SHOW_MINI_PLAYER) {
                            if (PATH_TO_FRAG != null) {

                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if (art != null) {
                                    Glide.with(getContext()).load(art).into(songArt);}
                                else {
                                    Glide.with(getContext()).load(R.drawable.opt3).into(songArt);
                                }
                                songname.setText(SONG_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);




                            }

                        }
                    }
                }

            }
        });



        playNpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (musicService != null) {
                    musicService.playNPause();


                    if (musicService.isPlaying()) {
                        playNpause.setImageResource(R.drawable.pause);
                    } else {
                        playNpause.setImageResource(R.drawable.play);
                    }
                }

            }
        });

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();

//
//        if (musicService !=null) {
//
//
//            playNpause.setImageResource(R.drawable.pause);
//        }
//        else
//        {
//            playNpause.setImageResource(R.drawable.play);}
    }


    @Override
    public void onResume() {
        super.onResume();

        if (SHOW_MINI_PLAYER) {


            if (PATH_TO_FRAG != null) {

                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null) {
                    Glide.with(getContext()).load(art).into(songArt);
                } else {
                    Glide.with(getContext()).load(R.drawable.opt3).into(songArt);
                }


                songname.setText(SONG_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);
                Intent intent = new Intent(getContext(), MusicService.class);
                if (getContext() != null) {
                    getContext().bindService(intent, this, BIND_AUTO_CREATE);
                }

            }




            check();


            }



        }



  private void check()
  {





      if (musicService !=null && musicService.isPlaying())
      {

          playNpause.setImageResource(R.drawable.pause);
      }

      else
      {
          playNpause.setImageResource(R.drawable.play);
      }
  }


    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;

        musicService = binder.getService();

    }




    @Override
    public void onServiceDisconnected(ComponentName name) {

        musicService = null;

    }

    @Override
    public void onPause() {
        super.onPause();

        if(getContext() !=null)
        {
            getContext().unbindService(this);
        }
    }

}
