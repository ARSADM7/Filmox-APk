package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

public class PlayerActivity extends Activity {
    public static void play(final android.content.Context ctx, final String watchPath, final String label,
                            final String hTitle, final String hPoster, final String hType, final String hSlug) {
        android.widget.Toast.makeText(ctx, "A preparar o vídeo…", android.widget.Toast.LENGTH_SHORT).show();
        Bg.run(new Bg.Work<Hyper.Watch>() {
            public Hyper.Watch run() throws Exception { return Hyper.watch(watchPath); }
        }, new Bg.Done<Hyper.Watch>() {
            public void ok(Hyper.Watch w) {
                if (w.mp4 == null) {
                    android.widget.Toast.makeText(ctx, "Vídeo não encontrado nesta página.", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(ctx, PlayerActivity.class);
                i.putExtra("url", w.mp4);
                i.putExtra("watchPath", watchPath);
                i.putExtra("title", label + (w.audio == null || w.audio.isEmpty() ? "" : " · " + w.audio)
                        + (w.quality == null || w.quality.isEmpty() ? "" : " · " + w.quality));
                i.putExtra("hTitle", hTitle); i.putExtra("hPoster", hPoster);
                i.putExtra("hType", hType); i.putExtra("hSlug", hSlug);
                Hist.Entry prev = Hist.get(ctx, watchPath);
                if (prev != null && prev.posMs > 30000) i.putExtra("startPos", (int) prev.posMs);
                ctx.startActivity(i);
            }
            public void fail(Exception e) {
                android.widget.Toast.makeText(ctx, "Erro ao preparar o vídeo.", android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private VideoView vv;
    private android.widget.ProgressBar spin;
    private String watchPath;
    private boolean retried = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        final String url = getIntent().getStringExtra("url");
        watchPath = getIntent().getStringExtra("watchPath");
        String title = getIntent().getStringExtra("title");
        if (title != null) setTitle(title);
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(0xFF000000);
        vv = new VideoView(this);
        RelativeLayout.LayoutParams vl = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        vl.addRule(RelativeLayout.CENTER_IN_PARENT);
        root.addView(vv, vl);
        spin = new ProgressBar(this);
        RelativeLayout.LayoutParams sl = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        sl.addRule(RelativeLayout.CENTER_IN_PARENT);
        root.addView(spin, sl);
        setContentView(root);
        String localFile = getIntent().getStringExtra("file");
        if (localFile != null) {
            MediaController mc2 = new MediaController(this);
            mc2.setAnchorView(vv);
            vv.setMediaController(mc2);
            vv.setVideoPath(localFile);
            vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) { spin.setVisibility(View.GONE); vv.start(); }
            });
            vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int w, int e) {
                    Toast.makeText(PlayerActivity.this,
                            "Não foi possível reproduzir este ficheiro.", Toast.LENGTH_LONG).show();
                    finish();
                    return true;
                }
            });
            return;
        }
        if (url == null) { finish(); return; }
        MediaController mc = new MediaController(this);
        mc.setAnchorView(vv);
        vv.setMediaController(mc);
        vv.setVideoURI(Uri.parse(url));
        final int startPos = getIntent().getIntExtra("startPos", 0);
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                spin.setVisibility(View.GONE);
                if (startPos > 0) vv.seekTo(startPos);
                vv.start();
                if (!loopStarted) { loopStarted = true; saveLoop.run(); }
            }
        });
        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (watchPath != null && !retried) {
                    retried = true;
                    final int pos = vv.getCurrentPosition();
                    spin.setVisibility(View.VISIBLE);
                    Toast.makeText(PlayerActivity.this, "A renovar o vídeo…", Toast.LENGTH_SHORT).show();
                    Bg.run(new Bg.Work<Hyper.Watch>() {
                        public Hyper.Watch run() throws Exception { return Hyper.watch(watchPath); }
                    }, new Bg.Done<Hyper.Watch>() {
                        public void ok(Hyper.Watch w) {
                            if (w.mp4 != null) {
                                vv.setVideoURI(Uri.parse(w.mp4));
                                if (pos > 0) vv.seekTo(pos);
                                vv.start();
                            } else { erroFinal(url); }
                        }
                        public void fail(Exception e) { erroFinal(url); }
                    });
                } else {
                    erroFinal(url);
                }
                return true;
            }
        });
        vv.requestFocus();
    }

    private boolean loopStarted = false;
    private final Runnable saveLoop = new Runnable() {
        public void run() {
            saveProgress();
            new android.os.Handler().postDelayed(this, 5000);
        }
    };
    private void saveProgress() {
        if (vv == null || watchPath == null) return;
        try {
            int pos = vv.getCurrentPosition(), dur = vv.getDuration();
            if (pos <= 0 || dur <= 0) return;
            Hist.Entry e = new Hist.Entry();
            e.watchPath = watchPath;
            e.title = getIntent().getStringExtra("hTitle");
            if (e.title == null) e.title = getIntent().getStringExtra("title");
            e.poster = getIntent().getStringExtra("hPoster");
            e.type = getIntent().getStringExtra("hType");
            e.slug = getIntent().getStringExtra("hSlug");
            e.label = getIntent().getStringExtra("title");
            e.posMs = pos; e.durMs = dur;
            Hist.save(PlayerActivity.this, e);
        } catch (Exception ignored) { }
    }
    @Override protected void onPause() { saveProgress(); super.onPause(); }

    private void erroFinal(String url) {
        spin.setVisibility(View.GONE);
        Toast.makeText(PlayerActivity.this,
                "Não foi possível reproduzir aqui. A abrir no navegador…", Toast.LENGTH_LONG).show();
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { }
        finish();
    }
}
