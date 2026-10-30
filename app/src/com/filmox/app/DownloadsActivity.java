package com.filmox.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/** Página de Downloads: estado, ver offline, apagar. */
public class DownloadsActivity extends Activity {
    private LinearLayout listBox;
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        public void run() { render(); handler.postDelayed(this, 2000); }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(this, 6), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 6));
        TextView back = Ui.tv(this, "‹", 30, Ui.TEXT, true);
        back.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), Ui.dp(this, 4));
        back.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        top.addView(back);
        top.addView(Ui.tv(this, "Downloads", 19, Ui.TEXT, true));
        root.addView(top);
        TextView hint = Ui.tv(this, "Vídeos guardados no dispositivo para ver sem internet. Pasta: Download/FILMOX/", 12.5f, Ui.SUB, false);
        hint.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 6));
        root.addView(hint);
        ScrollView sv = new ScrollView(this);
        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setPadding(Ui.dp(this, 14), Ui.dp(this, 6), Ui.dp(this, 14), Ui.dp(this, 16));
        sv.addView(listBox);
        root.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); render(); handler.postDelayed(tick, 2000); }
    @Override protected void onPause() { super.onPause(); handler.removeCallbacks(tick); }

    private void render() {
        listBox.removeAllViews();
        List<Dl.Entry> items = Dl.list(this);
        Collections.sort(items, new Comparator<Dl.Entry>() {
            public int compare(Dl.Entry a, Dl.Entry b) {
                if (a.isSeries() == b.isSeries()) return 0;
                return a.isSeries() ? 1 : -1;
            }
        });
        if (items.isEmpty()) {
            TextView e = Ui.tv(this, "Sem downloads ainda.\nAbre um filme ou episódio e escolhe Baixar.", 14.5f, Ui.SUB, false);
            e.setPadding(0, Ui.dp(this, 30), 0, 0);
            listBox.addView(e);
            return;
        }
        String lastGroup = "";
        for (final Dl.Entry e : items) {
            Dl.refresh(this, e);
            String group = e.isSeries() ? "Séries" : "Filmes";
            if (!group.equals(lastGroup)) {
                TextView heading = Ui.tv(this, group, 16, Ui.ACCENT2, true);
                heading.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 8));
                listBox.addView(heading);
                lastGroup = group;
            }
            final File f = Dl.file(this, e);
            final String offlineUri = Dl.downloadUri(this, e);
            final boolean done = e.status == DownloadManager.STATUS_SUCCESSFUL && offlineUri != null;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.round(Ui.PANEL, 12, this));
            row.setPadding(Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, Ui.dp(this, 10));
            RoundedImageView im = new RoundedImageView(this, 8);
            im.setBackgroundColor(Ui.PANEL2);
            row.addView(im, new LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 76)));
            Img.load(im, e.poster);
            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 6), 0);
            TextView tt = Ui.tv(this, e.title, 15, Ui.TEXT, true);
            tt.setMaxLines(2);
            mid.addView(tt);
            String state;
            if (done) {
                state = "✓ Pronto — " + ((e.totalBytes > 0 ? e.totalBytes : f.length()) / 1048576) + " MB · ver offline";
            } else if (e.status == DownloadManager.STATUS_RUNNING) {
                state = "A baixar… " + e.percent() + "%  (" + (e.doneBytes / 1048576) + "/" + (e.totalBytes / 1048576) + " MB)";
            } else if (e.status == DownloadManager.STATUS_PAUSED || e.status == DownloadManager.STATUS_PENDING) {
                state = "Em espera…";
            } else if (e.status == DownloadManager.STATUS_FAILED) {
                state = "✗ Falhou — apaga e tenta outra vez";
            } else if (f.exists()) {
                state = "✓ Pronto — " + ((e.totalBytes > 0 ? e.totalBytes : f.length()) / 1048576) + " MB · ver offline";
            } else {
                state = "Estado desconhecido";
            }
            mid.addView(Ui.tv(this, state, 12.5f, done ? Ui.ACCENT2 : Ui.SUB, false));
            if (e.status == DownloadManager.STATUS_RUNNING) {
                LinearLayout track = new LinearLayout(this);
                track.setBackgroundColor(Ui.PANEL2);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(Ui.dp(this, 150), Ui.dp(this, 4));
                tp.setMargins(0, Ui.dp(this, 6), 0, 0);
                View fill = new View(this);
                fill.setBackgroundColor(Ui.ACCENT);
                track.addView(fill, new LinearLayout.LayoutParams(Math.max(1, Ui.dp(this, 150) * e.percent() / 100), Ui.dp(this, 4)));
                mid.addView(track, tp);
            }
            row.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            if (done || offlineUri != null) {
                TextView play = Ui.tv(this, "VER", 11, Ui.ACCENT, true);
                play.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
                play.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(DownloadsActivity.this, PlayerActivity.class);
                        i.putExtra("file", offlineUri);
                        i.putExtra("title", e.title);
                        startActivity(i);
                    }
                });
                row.addView(play);
            }
            row.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(DownloadsActivity.this)
                            .setTitle("Apagar download?")
                            .setMessage(e.title)
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Apagar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    Dl.remove(DownloadsActivity.this, e);
                                    Toast.makeText(DownloadsActivity.this, "Download apagado", Toast.LENGTH_SHORT).show();
                                    render();
                                }
                            }).show();
                    return true;
                }
            });
            listBox.addView(row, rp);
        }
    }
}
