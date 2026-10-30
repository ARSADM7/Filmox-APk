package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Página "Continuar a assistir": lista completa com tempo restante. */
public class ContinueActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(this, 6), Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 10));
        TextView back = Ui.tv(this, "‹", 30, Ui.TEXT, true);
        back.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), Ui.dp(this, 4));
        back.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        top.addView(back);
        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.addView(Ui.tv(this, "Continuar a assistir", 21, Ui.TEXT, true));
        titleBox.addView(Ui.tv(this, "Retoma de onde paraste", 11.5f, Ui.SUB, false));
        top.addView(titleBox);
        root.addView(top);

        ScrollView sv = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.dp(this, 14), Ui.dp(this, 6), Ui.dp(this, 14), Ui.dp(this, 16));
        List<Hist.Entry> hist = Hist.list(this);
        if (hist.isEmpty()) {
            TextView e = Ui.tv(this, "Ainda não começaste nenhum vídeo.\nQuando vires um filme ou episódio, ele aparece aqui para continuares.", 14.5f, Ui.SUB, false);
            e.setPadding(0, Ui.dp(this, 30), 0, 0);
            col.addView(e);
        }
        for (final Hist.Entry e : hist) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.roundStroke(Ui.PANEL, 14, 0x332F2B38, this));
            row.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, Ui.dp(this, 10));
            FrameLayout thumbBox = new FrameLayout(this);
            RoundedImageView im = new RoundedImageView(this, 8);
            im.setBackgroundColor(Ui.PANEL2);
            thumbBox.addView(im, new FrameLayout.LayoutParams(Ui.dp(this, 118), Ui.dp(this, 68)));
            Img.load(im, e.poster == null ? null : e.poster.replace("/w500/", "/w780/"));
            TextView playIc = Ui.tv(this, "▶", 13, Ui.TEXT, true);
            playIc.setBackground(Ui.round(0x99000000, 14, this));
            playIc.setPadding(Ui.dp(this, 7), Ui.dp(this, 3), Ui.dp(this, 7), Ui.dp(this, 3));
            FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pp.gravity = Gravity.CENTER;
            thumbBox.addView(playIc, pp);
            row.addView(thumbBox);

            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 6), 0);
            TextView tt = Ui.tv(this, e.title == null ? "" : e.title, 15.5f, Ui.TEXT, true);
            tt.setMaxLines(1);
            mid.addView(tt);
            if (e.label != null && !e.label.equals(e.title)) {
                TextView lb = Ui.tv(this, e.label, 12.5f, Ui.SUB, false);
                lb.setMaxLines(1);
                mid.addView(lb);
            }
            long remainMin = Math.max(0, (e.durMs - e.posMs) / 60000);
            mid.addView(Ui.tv(this, "Faltam " + remainMin + " min  ·  " + e.percent() + "% visto", 12.5f, Ui.ACCENT2, false));
            LinearLayout track = new LinearLayout(this);
            track.setBackgroundColor(Ui.PANEL2);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(Ui.dp(this, 130), Ui.dp(this, 4));
            tp.setMargins(0, Ui.dp(this, 6), 0, 0);
            View fill = new View(this);
            fill.setBackgroundColor(Ui.ACCENT);
            track.addView(fill, new LinearLayout.LayoutParams(Ui.dp(this, 130) * e.percent() / 100, Ui.dp(this, 4)));
            mid.addView(track, tp);
            row.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView del = Ui.tv(this, "REMOVER", 9.5f, Ui.SUB, true);
            del.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
            del.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Hist.remove(ContinueActivity.this, e.watchPath);
                    recreate();
                }
            });
            row.addView(del);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PlayerActivity.play(ContinueActivity.this, e.watchPath, e.label, e.title, e.poster, e.type, e.slug);
                }
            });
            col.addView(row, rp);
        }
        sv.addView(col);
        root.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }
}
