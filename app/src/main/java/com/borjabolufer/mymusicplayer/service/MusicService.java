package com.borjabolufer.mymusicplayer.service;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.borjabolufer.mymusicplayer.model.Song;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicService {
    private MediaPlayer mediaPlayer;
    private final Context context;
    private List<Song> songList;
    private int currentSongIndex = 0;
    private MusicCallback musicCallback;
    private ScheduledExecutorService executorService;

    public interface MusicCallback {
        void onPlaybackStatusChanged(boolean isPlaying);

        void onSongChanged(Song song);

        void onProgressChanged(int progress, int duration);
    }

    public MusicService(Context context, MusicCallback callback) {
        this.context = context;
        this.musicCallback = callback;
        setupMediaPlayer();
    }

    public void setSongList(List<Song> songs) {
        this.songList = songs;
        if (!songList.isEmpty()) {
            setupMediaPlayer();
        }
    }

    private void setupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (songList != null && !songList.isEmpty()) {
            try {
                mediaPlayer = MediaPlayer.create(context, songList.get(currentSongIndex).getResourceId());
                if (mediaPlayer != null){
                    mediaPlayer.setOnCompletionListener(mp -> playNextSong());
                    startProgressUpdate();
                    notifyCurrentSong();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void startProgressUpdate() {
        if (executorService != null) {
            executorService.shutdown();
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                musicCallback.onProgressChanged(
                        mediaPlayer.getCurrentPosition(),
                        mediaPlayer.getDuration()
                );
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    public void playSong(int songIndex) {
        if (songList == null || songList.isEmpty() || songIndex < 0 || songIndex >= songList.size()) {
            return;
        }

        currentSongIndex = songIndex;

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        try {
            mediaPlayer = MediaPlayer.create(context, songList.get(currentSongIndex).getResourceId());
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> playNextSong());
                mediaPlayer.start();
                startProgressUpdate();
                notifyCurrentSong();
                musicCallback.onPlaybackStatusChanged(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumePlayback() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
                startProgressUpdate();
                musicCallback.onPlaybackStatusChanged(true);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // Si hay un error al resumir, intentamos recrear el MediaPlayer
                setupMediaPlayer();
                mediaPlayer.start();
                startProgressUpdate();
                musicCallback.onPlaybackStatusChanged(true);
            }
        }
    }

    public void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
                if (executorService != null) {
                    executorService.shutdown();
                }
                musicCallback.onPlaybackStatusChanged(false);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    // Método auxiliar para verificar el estado del MediaPlayer
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    // Método auxiliar para obtener la posición actual
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    // Método auxiliar para obtener la duración total
    public int getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
            musicCallback.onPlaybackStatusChanged(mediaPlayer.isPlaying());
        }
    }

    public void playNextSong() {
        if (songList == null || songList.isEmpty()) return;
        currentSongIndex = (currentSongIndex + 1) % songList.size();
        setupMediaPlayer();
        if (mediaPlayer != null){
            mediaPlayer.start();
            musicCallback.onPlaybackStatusChanged(true);
        }
    }

    public void playPreviousSong() {
        if (songList == null || songList.isEmpty()) return;
        currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
        setupMediaPlayer();
        if (mediaPlayer != null){
            mediaPlayer.start();
            musicCallback.onPlaybackStatusChanged(true);
        }
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
        }
    }

    private void notifyCurrentSong() {
        if (musicCallback != null && !songList.isEmpty()) {
            musicCallback.onSongChanged(songList.get(currentSongIndex));
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}