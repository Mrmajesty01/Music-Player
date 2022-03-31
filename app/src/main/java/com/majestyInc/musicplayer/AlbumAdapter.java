package com.majestyInc.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder> {

    private Context mContext;
    private ArrayList<MusicFIles> albums;
    View view;


    public AlbumAdapter(Context mContext, ArrayList<MusicFIles> albums) {
        this.mContext = mContext;
        this.albums = albums;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {


        holder.AlbumName.setText(albums.get(position).getAlbum());
        byte [] image = getAlbumArt(albums.get(position).getPath());

       if(image != null)
        {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.albumPic);
        }
        else {
            Glide.with(mContext)
                    .load(R.drawable.opt3)
                    .into(holder.albumPic);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, AlbumDetails.class);
                intent.putExtra("albumName", albums.get(position).getAlbum());
                mContext.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView albumPic;
        TextView AlbumName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            albumPic = itemView.findViewById(R.id.albumPic);
            AlbumName = itemView.findViewById(R.id.AlbumName);
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
