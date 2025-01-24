package com.borjabolufer.mymusicplayer.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.borjabolufer.mymusicplayer.R;
import com.borjabolufer.mymusicplayer.adapter.SongAdapter;
import com.borjabolufer.mymusicplayer.model.Song;
import com.borjabolufer.mymusicplayer.service.MusicService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicService.MusicCallback {
    private MusicService musicService;
    private SeekBar seekBar;
    private ImageButton btnPrevious, btnPlay, btnNext;
    private TextView songTitle, artistName, albumName, yearText;
    private ImageView albumArt;
    private List<Song> songList;
    private RecyclerView rvList;
    private SongAdapter adapter;
    private int currentSongIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        try {
            setupSongList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setupRecyclerView();
        setupMusicService();
    }

    private void initializeViews() {
        seekBar = findViewById(R.id.seekBar);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        albumName = findViewById(R.id.albumName);
        yearText = findViewById(R.id.yearText);
        albumArt = findViewById(R.id.albumArt);
        rvList = findViewById(R.id.rvList);
    }

    private void setupSongList() throws IOException {
        List<Integer> rawSongs = Arrays.asList(
                R.raw.song1, R.raw.song2, R.raw.song3,
                R.raw.song4, R.raw.song5
        );

        songList = new ArrayList<>();
        for (Integer rawId : rawSongs) {
            songList.add(getSongMetadata(rawId));
        }
    }

    private Song getSongMetadata(int resourceId) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + resourceId));

            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);

            return new Song(
                    title != null ? title : "Unknown Title",
                    artist != null ? artist : "Unknown Artist",
                    album != null ? album : "Unknown Album",
                    year != null ? year : "Unknown Year",
                    resourceId
            );
        } finally {
            retriever.release();
        }
    }

    private void setupRecyclerView() {
        adapter = new SongAdapter(this, songList, song -> {
            int newIndex = songList.indexOf(song);
            if (newIndex != -1) {
                currentSongIndex = newIndex;
                musicService.playSong(currentSongIndex);
                updateUI(song);
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);
    }

    private void updateUI(Song song) {
        songTitle.setText(song.getTitle());
        artistName.setText(song.getArtist());
        albumName.setText(song.getAlbum());
        yearText.setText(song.getYear());
        loadAlbumArt(song.getResourceId());
    }

    private void loadAlbumArt(int resourceId) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + resourceId));
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                albumArt.setImageBitmap(bitmap);
            } else {
                albumArt.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            e.printStackTrace();
            albumArt.setImageResource(R.drawable.ic_launcher_foreground);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupMusicService() {
        musicService = new MusicService(this, this);
        musicService.setSongList(songList);

        btnPlay.setOnClickListener(v -> musicService.togglePlayPause());
        btnNext.setOnClickListener(v -> musicService.playNextSong());
        btnPrevious.setOnClickListener(v -> musicService.playPreviousSong());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if (!songList.isEmpty()) {
            updateUI(songList.get(0));
        }
    }

    @Override
    public void onPlaybackStatusChanged(boolean isPlaying) {
        runOnUiThread(() -> {
            btnPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        });
    }

    @Override
    public void onSongChanged(Song song) {
        runOnUiThread(() -> {
            updateUI(song);
            currentSongIndex = songList.indexOf(song);
        });
    }

    @Override
    public void onProgressChanged(int progress, int duration) {
        runOnUiThread(() -> {
            seekBar.setMax(duration);
            seekBar.setProgress(progress);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicService != null) {
            musicService.resumePlayback();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (musicService != null) {
            musicService.pausePlayback();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicService != null) {
            musicService.release();
        }
    }
}