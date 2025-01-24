package com.borjabolufer.mymusicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.borjabolufer.mymusicplayer.R;
import com.borjabolufer.mymusicplayer.model.Song;

import java.io.IOException;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private final List<Song> songList;
    private final Context context;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public SongAdapter(Context context, List<Song> songList, OnItemClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        if (holder.tvSongTitle != null) holder.tvSongTitle.setText(song.getTitle());
        if (holder.tvArtistName != null) holder.tvArtistName.setText(song.getArtist());
        if (holder.tvAlbum != null) holder.tvAlbum.setText(song.getAlbum());
        if (holder.tvYear != null) holder.tvYear.setText(song.getYear());

        // Cargar la imagen del álbum
        loadAlbumArt(holder.ivAlbumArt, song.getResourceId());

        // Configurar el clic del elemento
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(song));
    }

    private void loadAlbumArt(ImageView imageView, int resourceId) {
        if (imageView == null) return;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, Uri.parse("android.resource://" +
                    context.getPackageName() + "/" + resourceId));
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAlbumArt;
        TextView tvSongTitle, tvArtistName, tvAlbum, tvYear;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.songImageView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtistName = itemView.findViewById(R.id.tvSongArtist);
            tvAlbum = itemView.findViewById(R.id.tvAlbumName);
            tvYear = itemView.findViewById(R.id.tvYear);
        }
    }
}