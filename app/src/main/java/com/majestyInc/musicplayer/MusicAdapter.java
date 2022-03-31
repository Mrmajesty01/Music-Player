package com.majestyInc.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;



import static com.majestyInc.musicplayer.player.artist;
import static com.majestyInc.musicplayer.player.name;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<MusicFIles>mFilles;
    private InterstitialAd mInterstitialAd;
    MusicService musicService;
    MediaPlayer player;

    MusicAdapter(Context mContext, ArrayList<MusicFIles> mFilles){




        this.mContext = mContext;
        this.mFilles = mFilles;
    }

    AdRequest adRequest = new AdRequest.Builder().build();

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.songName.setText(mFilles.get(position).getTitle());
        byte [] image = getAlbumArt(mFilles.get(position).getPath());

        if(image != null)
        {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.songImage);
        }
        else {
            Glide.with(mContext)
                    .load(R.drawable.opt3)
                    .into(holder.songImage);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext,player.class);
                intent.putExtra("position",position);
                mContext.startActivity(intent);

            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.deletepopup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                        case R.id.delete:
                        Toast.makeText(mContext, "Delete Clicked!!!", Toast.LENGTH_SHORT).show();
                        delete(position,view);
                        break;
                        case  R.id.playnexxt:



                        break;
                    }
                    return true;
                }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return mFilles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView songName;
        ImageView songImage, delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            songImage = itemView.findViewById(R.id.songImage);
            songName = itemView.findViewById(R.id.songName);
            delete = itemView.findViewById(R.id.Delete);


        }
    }


   private byte[] getAlbumArt(String uri){
       MediaMetadataRetriever retriever = new MediaMetadataRetriever();
       retriever.setDataSource(uri);
       byte[] art = retriever.getEmbeddedPicture();
       retriever.release();
       return art;

    }
public void delete(int position, View v) {

    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFilles.get(position).getId()));
    File file = new File(mFilles.get(position).getPath());
    boolean deleted = file.delete();
    if (deleted) {
        mContext.getContentResolver().delete(uri, null, null);
        mFilles.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mFilles.size());
        Snackbar.make(v, "File Deleted! ", Snackbar.LENGTH_LONG)
                .show();

    }
    else
        {
            // if song is in sdcard and API level is =>19
            Snackbar.make(v, "File Can't Be Deleted! ", Snackbar.LENGTH_LONG)
                    .show();

        }
}

    void updateList(ArrayList<MusicFIles> musicFIlesArrayList)
    {
        mFilles= new ArrayList<>();
        mFilles.addAll(musicFIlesArrayList);
        notifyDataSetChanged();
    }

}
