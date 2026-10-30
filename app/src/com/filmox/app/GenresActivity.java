package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Géneros em mosaicos com imagem (primeiro título de cada género). */
public class GenresActivity extends Activity {
    private final List<String[]> genres = new ArrayList<>();
    private BaseAdapter adapter;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 16), Ui.dp(this, 10));
        TextView back = Ui.tv(this, "‹", 30, Ui.TEXT, true);
        back.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 12), 0);
        back.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        top.addView(back);
        top.addView(Ui.tv(this, "Géneros", 23, Ui.TEXT, true));
        root.addView(top);

        GridView grid = new GridView(this);
        grid.setNumColumns(2);
        grid.setVerticalSpacing(Ui.dp(this, 10));
        grid.setHorizontalSpacing(Ui.dp(this, 10));
        grid.setPadding(Ui.dp(this, 12), Ui.dp(this, 4), Ui.dp(this, 12), Ui.dp(this, 12));
        adapter = new BaseAdapter() {
            public int getCount() { return genres.size(); }
            public Object getItem(int p) { return genres.get(p); }
            public long getItemId(int p) { return p; }
            public View getView(int pos, View cv, ViewGroup parent) {
                final String[] g = genres.get(pos);
                FrameLayout tile = new FrameLayout(GenresActivity.this);
                RoundedImageView img = new RoundedImageView(GenresActivity.this, 12);
                img.setBackgroundColor(Ui.PANEL);
                tile.addView(img, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(GenresActivity.this, 96)));
                TextView dot = Ui.tv(GenresActivity.this, "●", 10, Ui.ACCENT, false);
                FrameLayout.LayoutParams dp2 = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dp2.gravity = Gravity.TOP | Gravity.RIGHT;
                dp2.setMargins(0, Ui.dp(GenresActivity.this, 6), Ui.dp(GenresActivity.this, 8), 0);
                tile.addView(dot, dp2);
                TextView name = Ui.tv(GenresActivity.this, g[1], 16.5f, Ui.TEXT, true);
                name.setShadowLayer(6, 0, 2, 0xCC000000);
                FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                np.gravity = Gravity.BOTTOM;
                np.setMargins(Ui.dp(GenresActivity.this, 10), 0, Ui.dp(GenresActivity.this, 10), Ui.dp(GenresActivity.this, 8));
                tile.addView(name, np);
                tile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(GenresActivity.this, GridActivity.class);
                        i.putExtra("mode", "genre");
                        i.putExtra("value", g[0]);
                        i.putExtra("title", g[1]);
                        startActivity(i);
                    }
                });
                final RoundedImageView fimg = img;
                Bg.run(new Bg.Work<Hyper.Item>() {
                    public Hyper.Item run() throws Exception {
                        List<Hyper.Item> items = Hyper.genre(g[0], 1);
                        return items.isEmpty() ? null : items.get(0);
                    }
                }, new Bg.Done<Hyper.Item>() {
                    public void ok(Hyper.Item it) { if (it != null) Img.load(fimg, it.backdrop()); }
                    public void fail(Exception e) { }
                });
                return tile;
            }
        };
        grid.setAdapter(adapter);
        root.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        final LinearLayout rootRef = root;
        Bg.run(new Bg.Work<List<String[]>>() {
            public List<String[]> run() throws Exception { return Hyper.genres(); }
        }, new Bg.Done<List<String[]>>() {
            public void ok(List<String[]> v) { genres.addAll(v); adapter.notifyDataSetChanged(); }
            public void fail(Exception e) {
                TextView t = Ui.tv(GenresActivity.this, "Erro ao carregar os géneros.", 14, Ui.SUB, false);
                t.setPadding(Ui.dp(GenresActivity.this, 16), Ui.dp(GenresActivity.this, 16), 0, 0);
                rootRef.addView(t, 1);
            }
        });
    }
}
