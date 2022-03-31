package com.majestyInc.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.internal.Constants;

import java.util.ArrayList;

import static com.majestyInc.musicplayer.ApplicationClass.ACTION_CANCEL;
import static com.majestyInc.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.majestyInc.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.majestyInc.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.majestyInc.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.majestyInc.musicplayer.player.PlaynPause;
import static com.majestyInc.musicplayer.player.songs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {


    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFIles> musicFIles = new ArrayList<>();
    Uri uri;
    int position =-1;
    int post =+1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";


    @Override
    public void onCreate() {
        super.onCreate();

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    public class MyBinder extends Binder
   {
       MusicService getService()
       {
           return MusicService.this;
       }
   }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition !=-1)
        {
            playMedia(myPosition);
        }

        if(actionName !=null )
        {
            switch (actionName)
            {
                case "playPause" :
                    playNPause();
                    break;

                case "next" :
                   nextBtnClicked();
                    break;

                case "previous" :
                   prevBtnClicked();
                    break;

                case "cancel":
                    if(mediaPlayer.isPlaying())
                    {
                        mediaPlayer.pause();
                        stopForeground(true);
                    }
                    else
                        {
                            stopForeground(true);
                        }

                    break;
            }


        }

        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFIles = songs;
        position = startPosition;
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFIles !=null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start()
    {
        mediaPlayer.start();
    }



  boolean isPlaying()
    {

        if(mediaPlayer!=null){
            return mediaPlayer.isPlaying();

        }
     else{
        return mediaPlayer.isPlaying();}
    }

    void stop()
    {
        mediaPlayer.stop();
    }
    void release()
    {
        mediaPlayer.release();
    }
    int getDuration()
    {
       return mediaPlayer.getDuration();
    }

    void seekTo(int position)
    {
        mediaPlayer.seekTo(position);
    }
    void createMediaPlayer(int positionInner)
    {
        position = positionInner;
        uri = Uri.parse(musicFIles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE)
                .edit();
        editor.putString(MUSIC_FILE,uri.toString());
        editor.putString(ARTIST_NAME, musicFIles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFIles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }
    int getCurrentPosition()
    {
        return mediaPlayer.getCurrentPosition();
    }

    void pause()
    {
        mediaPlayer.pause();
    }

    void onCompleted()
    {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying !=null ) {
            actionPlaying.nextBtnClicked();
            if(mediaPlayer!=null){
              createMediaPlayer(position);
              mediaPlayer.start();
              PlaynPause.setImageResource(R.drawable.ic_pause);
              onCompleted();
            }
        }

    }

    void setCallBack(ActionPlaying actionPlaying)
    {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseBtn)
    {
        Intent intent = new Intent(this,player.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);

        Intent prevIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        Intent pauseIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        Intent nextIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_CANCEL);
        PendingIntent deletePending = PendingIntent.getBroadcast(this,0,deleteIntent,PendingIntent.FLAG_UPDATE_CURRENT);




        byte[] picture = null;
        picture = getAlbumArt(musicFIles.get(position).getPath());

        Bitmap thumb = null;

        if(picture != null)
        {
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else
        {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.opt3);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFIles.get(position).getTitle())
                .setContentText(musicFIles.get(position).getArtist())
                .addAction(R.drawable.ic_back,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.ic_forward,"Next",nextPending)
                .addAction(R.drawable.ic_cancel,"cancel",deletePending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(2,notification);




    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;

    }


    public void nextBtnClicked()
    {
        if(actionPlaying !=null )
        {
            actionPlaying.nextBtnClicked();
        }
    }

    public void playNPause()
    {
        if(actionPlaying !=null )
        {
            actionPlaying.playPausedBtnClicked();
        }

    }


    public void prevBtnClicked()
    {
        if(actionPlaying !=null )
        {
            actionPlaying.prevBtnClicked();
        }
    }



}
