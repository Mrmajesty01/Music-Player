package com.majestyInc.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static com.majestyInc.musicplayer.bottomFragment.playNpause;


public class  MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final int REQUEST_CODE=1;
  static ArrayList<MusicFIles> musicFiles = new ArrayList<>();
  static Boolean shuffleBln = false, repeatBln = false;
  static  ArrayList<MusicFIles> albums = new ArrayList<>();
  private String MY_SORT_PREF = "SortOrder";
  public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
  public static final String MUSIC_FILE = "STORED_MUSIC";
  public static boolean SHOW_MINI_PLAYER = false;
  public static String PATH_TO_FRAG = null;
  public static String SONG_TO_FRAG = null;
  public static String ARTIST_TO_FRAG = null;
  public static final String ARTIST_NAME = "ARTIST NAME";
  public static final String SONG_NAME = "SONG NAME";
  MusicService musicService;
  bottomFragment bottomFragment;

    AdView adview;



    private final PhoneStateListener phoneListener = new PhoneStateListener(){

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {

            if(state == TelephonyManager.CALL_STATE_RINGING )
            {
                if(musicService!=null){

                    musicService.playNPause();
                }

            }

            if(state == TelephonyManager.CALL_STATE_OFFHOOK)
            {
                if(musicService!=null) {
                    musicService.playNPause();
                }
               
            }
            super.onCallStateChanged(state, phoneNumber);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);







        final TelephonyManager telephonyManager =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if(telephonyManager !=null)
        {
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }




        adview = findViewById(R.id.adView);








        Permission();
        initViewPager();
        /*PermissionAudio();

         */

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},1);

        }


        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);




    }

    private void Permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        }
        else {


            musicFiles = getAllAudio(this);
            initViewPager();



        }
    }



  /*  private void PermissionAudio() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        }
        else {




        }

    }

   */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){


                Toast.makeText(this, "Permission Granted!!!", Toast.LENGTH_SHORT).show();

                musicFiles = getAllAudio(this);
                initViewPager();

            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
               /* ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);

                */

            }
        }
    }




    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
       ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);



    }




    public static class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }

        void addFragments(Fragment fragment, String title){

            fragments.add(fragment);
            titles.add(title);
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);

        }
    }


    public  ArrayList<MusicFIles> getAllAudio(Context context){

       SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE);
       String SortOrder = preferences.getString("sorting", "sortByDate");
       ArrayList<String> duplicate = new ArrayList<>();
       albums.clear();
        ArrayList<MusicFIles> audioList = new ArrayList<>();

        String order = null;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch (SortOrder)
        {
            case "sortByName":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC" ;
                break;


            case "sortByDate":
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC" ;
                break;

            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + " DESC" ;
                break;

        }

        String [] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,//for path
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
    };

        Cursor cursor = context.getContentResolver().query(uri,projection,null,null, order);
        if(cursor !=null){
        while (cursor.moveToNext()){
            String album = cursor.getString(0);
            String title = cursor.getString(1);
            String duration = cursor.getString(2);
            String path = cursor.getString(3);
            String artist= cursor.getString(4);
            String id = cursor.getString(5);


            MusicFIles musicFIles = new MusicFIles(path, title, artist, album, duration,id);
                audioList.add(musicFIles);

                if(!duplicate.contains(album)){

                    albums.add(musicFIles);
                    duplicate.add(album);

                }

        }
         cursor.close();
        }
        return audioList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText){

        String userInput = newText.toLowerCase();

        ArrayList<MusicFIles> myFiles = new ArrayList<>();

        for (MusicFIles songs : musicFiles)
        {
            if(songs.getTitle().toLowerCase().contains(userInput))
            {
                myFiles.add(songs);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE).edit();
        switch (item.getItemId()){
            case R.id.byName :
                editor.putString("sorting","sortByName");
                editor.apply();
                this.recreate();
                break;

            case R.id.byDate :
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.recreate();
                break;

            case R.id.bySize :
                editor.putString("sorting","sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
        String path = preferences.getString(MUSIC_FILE,null);
        String artist = preferences.getString(ARTIST_NAME,null);
        String song = preferences.getString(SONG_NAME,null);



        if(path!=null)
        {
            SHOW_MINI_PLAYER = true;
            PATH_TO_FRAG = path;
            ARTIST_TO_FRAG = artist;
            SONG_TO_FRAG = song;


        }
        else
            {
                SHOW_MINI_PLAYER = false;
                PATH_TO_FRAG = null;
                ARTIST_TO_FRAG = null;
                SONG_TO_FRAG = null;
            }



    }

}