package com.majestyInc.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.makeramen.roundedimageview.RoundedImageView;


import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


import static com.majestyInc.musicplayer.AlbumDetailsAdapter.albums;

import static com.majestyInc.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.majestyInc.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.majestyInc.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.majestyInc.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.majestyInc.musicplayer.MainActivity.musicFiles;
import static com.majestyInc.musicplayer.MainActivity.repeatBln;
import static com.majestyInc.musicplayer.MainActivity.shuffleBln;
import static com.majestyInc.musicplayer.MusicAdapter.mFilles;

public class player extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    private ConstraintLayout constraintLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String Keeper = "";
    public static ImageButton PlaynPause;
    private ImageButton Forward;
    private ImageButton Backward;
    private  ImageButton Repeat;
    private ImageButton Shuffle;
    private SeekBar seekBar;
    private TextView CurrentTime;
    private TextView TotalTime;
    public TextView SongName;
    public TextView ArtistName;
    private RoundedImageView SongPicure;
    private Button ArtificialIntelligence;
    private String mode = "OFF";
     static MediaPlayer mediaPlayer;
    static ArrayList<MusicFIles> songs = new ArrayList<>();
    int Position;
    static Uri uri;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    public static String name, artist;
    private ImageButton back;
    AdView adview, adview1;
    private InterstitialAd mInterstitialAd;
    MusicService musicService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);



        /*  CheckVoiceCommandPermission();

         */

        ArtistName = findViewById(R.id.artistName);
        PlaynPause = findViewById(R.id.playButton);
        Forward = findViewById(R.id.nextButton);
        Backward = findViewById(R.id.previousButton);
        Repeat = findViewById(R.id.repeatButton);
        Shuffle = findViewById(R.id.shuffleButton);
        seekBar = findViewById(R.id.SeekBar);
        CurrentTime = findViewById(R.id.currentTime);
        TotalTime = findViewById(R.id.totalTime);
        SongName = findViewById(R.id.songName);
        back = findViewById(R.id.back);
        adview = findViewById(R.id.adView);
        adview1 = findViewById(R.id.adView1);


        back.setClickable(true);
        SongPicure = findViewById(R.id.albumArt);
        /* ArtificialIntelligence = findViewById(R.id.AI);

         */



        getIntentMethod();



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3471834375937115/6378669612");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);
        adview1.loadAd(adRequest);




        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {

                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {

                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }

                });
                Intent home = new Intent(player.this, MainActivity.class);
                startActivity(home);

            }
        });





        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {

                    musicService.seekTo(progress * 1000);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });




        player.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    CurrentTime.setText(FormattedTime(currentPosition));


                }
                handler.postDelayed(this, 1000);
            }
        });


        constraintLayout = findViewById(R.id.constraintLayout);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(player.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());



        /*

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matchesFound = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null) {

                    if (mode.equals("ON")) {


                        Keeper = matchesFound.get(0);

                        if (Keeper.equals("pause")) {
                            playPausedBtnClicked();
                            Toast.makeText(player.this, "Command = " + Keeper, Toast.LENGTH_LONG).show();
                        } else if (Keeper.equals("play")) {
                            playPausedBtnClicked();
                            Toast.makeText(player.this, "Command = " + Keeper, Toast.LENGTH_LONG).show();
                        } else if (Keeper.equals("play next song")) {
                            nextBtnClicked();
                            Toast.makeText(player.this, "Command = " + Keeper, Toast.LENGTH_LONG).show();
                        } else if (Keeper.equals("play previous song")) {
                            prevBtnClicked();
                            Toast.makeText(player.this, "Command = " + Keeper, Toast.LENGTH_LONG).show();
                        }


                    }

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

         */



/*
        constraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        Keeper = "";
                        break;

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                }
                return false;

            }
        });

 */

/*

        ArtificialIntelligence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mode.equals("ON")) {
                    mode = "OFF";
                    ArtificialIntelligence.setText("Voice Enable Mode - OFF");
                    PlaynPause.setVisibility(view.VISIBLE);
                    Forward.setVisibility(view.VISIBLE);
                    Backward.setVisibility(view.VISIBLE);
                    Repeat.setVisibility(view.VISIBLE);
                    Shuffle.setVisibility(view.VISIBLE);
                } else {
                    mode = "ON";
                    ArtificialIntelligence.setText("Voice Enable Mode - ON");
                    Forward.setVisibility(view.GONE);
                    Repeat.setVisibility(view.GONE);
                    Shuffle.setVisibility(view.GONE);
                    Backward.setVisibility(View.GONE);

                }

            }
        });

 */


        Shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (shuffleBln) {
                    shuffleBln = false;
                    Shuffle.setImageResource(R.drawable.ic_shuffle);
                } else {
                    shuffleBln = true;
                    Shuffle.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });

        Repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatBln) {
                    repeatBln = false;
                    Repeat.setImageResource(R.drawable.ic_repeat);
                } else {
                    repeatBln = true;
                    Repeat.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });


        PlaynPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {

                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {

                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }

                });


                playPausedBtnClicked();
            }
        });

        Forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {

                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {

                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }

                });

                nextBtnClicked();
            }
        });


        Backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {

                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {

                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }

                });


                prevBtnClicked();
            }
        });


    }




    private String FormattedTime(int currentPosition) {

        String totalOut = "";
        String totalNew = "";

        String seconds = String.valueOf(currentPosition % 60);
        String minutes = String.valueOf(currentPosition / 60);

        totalOut = minutes + ":" + seconds;

        totalNew = minutes + ":" + "0" + seconds;

        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }

    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());


        int durationTime = Integer.parseInt(songs.get(Position).getDuration()) / 1000;
        TotalTime.setText(FormattedTime(durationTime));

        byte[] art = retriever.getEmbeddedPicture();



        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            animation(this, SongPicure, bitmap);}
        else  {

            Glide.with(getApplicationContext())
                    .load(R.drawable.opt3)
                    .into(SongPicure);
        }


    }

    private void getIntentMethod() {
        Position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");

        if (sender != null && sender.equals("albumDetails")) {
            songs = albums;
        } else {

            songs = mFilles;

        }

        if (songs != null) {

            PlaynPause.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(songs.get(Position).getPath());
        }



        Intent intent = new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",Position);
        startService(intent);

    }


    /*public void CheckVoiceCommandPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(player.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }

        }
    }

     */


    @Override
    protected void onPostResume() {

        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        prevThreadBtn();
        nextThreadBtn();

        super.onPostResume();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicService !=null)
        {
         PlaynPause.setImageResource(R.drawable.ic_pause);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void playThreadBtn() {

        playThread = new Thread() {

            @Override
            public void run() {
                super.run();
                PlaynPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPausedBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPausedBtnClicked() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {

        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        if (musicService.isPlaying()) {
            PlaynPause.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);

            player.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

                    }
                    handler.postDelayed(this, 1000);
                }
            });

        } else {
            musicService.showNotification(R.drawable.ic_pause);
            PlaynPause.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);


            player.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }


    private void prevThreadBtn() {

        prevThread = new Thread() {

            @Override
            public void run() {
                super.run();
                Backward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void nextThreadBtn() {

        nextThread = new Thread() {

            @Override
            public void run() {
                super.run();
                Forward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {

        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBln && !repeatBln) {
                Position = getRandom(songs.size() - 1);
            } else if (!shuffleBln && !repeatBln) {
                Position = ((Position + 1) % songs.size());
            }
            //position will be position which means repeat button is on
            uri = Uri.parse(songs.get(Position).getPath());
            musicService.createMediaPlayer(Position);
            metaData(uri);
            SongName.setText(songs.get(Position).getTitle());
            ArtistName.setText(songs.get(Position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);

            player.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            PlaynPause.setImageResource(R.drawable.ic_pause);
            musicService.start();

        } else {
            musicService.stop();
            musicService.release();
            if (shuffleBln && !repeatBln) {
                Position = getRandom(songs.size() - 1);
            } else if (!shuffleBln && !repeatBln) {
                Position = ((Position + 1) % songs.size());
            }

            uri = Uri.parse(songs.get(Position).getPath());
           musicService.createMediaPlayer(Position);
            metaData(uri);
            SongName.setText(songs.get(Position).getTitle());
            ArtistName.setText(songs.get(Position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);

            player.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

                    }
                    handler.postDelayed(this, 1000);
                }
            });

            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_play);
            PlaynPause.setImageResource(R.drawable.ic_play);

        }


    }


    public void prevBtnClicked() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {

        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBln && !repeatBln) {
                Position = getRandom(songs.size() - 1);
            } else if (!shuffleBln && !repeatBln) {
                Position = ((Position - 1) < 0 ? (songs.size() - 1) : (Position - 1));
            }


            uri = Uri.parse(songs.get(Position).getPath());
           musicService.createMediaPlayer(Position);
            metaData(uri);
            SongName.setText(songs.get(Position).getTitle());
            ArtistName.setText(songs.get(Position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);

            player.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

                    }
                    handler.postDelayed(this, 1000);
                }
            });

          musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            PlaynPause.setImageResource(R.drawable.ic_pause);
            musicService.start();

        } else {
            musicService.stop();
            musicService.release();

            if (shuffleBln && !repeatBln) {
                Position = getRandom(songs.size() - 1);
            } else if (!shuffleBln && !repeatBln) {
                Position = ((Position - 1) < 0 ? (songs.size() - 1) : (Position - 1));
            }


            uri = Uri.parse(songs.get(Position).getPath());
           musicService.createMediaPlayer(Position);
            metaData(uri);
            SongName.setText(songs.get(Position).getTitle());
            ArtistName.setText(songs.get(Position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);

            player.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);

                    }
                    handler.postDelayed(this, 1000);
                }
            });

           musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_play);
            PlaynPause.setImageResource(R.drawable.ic_play);

        }

    }


    public void animation(final Context context, final ImageView imageView, final Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);


    }



    public int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }




    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder)service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        musicService.start();
        SongName.setText(songs.get(Position).getTitle());
        ArtistName.setText(songs.get(Position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);


    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)  {
        musicService = null;

    }









}


