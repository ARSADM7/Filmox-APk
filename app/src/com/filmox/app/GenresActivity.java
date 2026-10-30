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
        top.setPadding(Ui.dp(this, 8), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 12));
        TextView back = Ui.tv(this, "‹", 30, Ui.TEXT, true);
        back.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 12), 0);
        back.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        top.addView(back);
        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.addView(Ui.tv(this, "Explorar géneros", 21, Ui.TEXT, true));
        titleBox.addView(Ui.tv(this, "Escolhe um estilo e descobre novos títulos", 11.5f, Ui.SUB, false));
        top.addView(titleBox);
        root.addView(top);

        GridView grid = new GridView(this);
        grid.setNumColumns(2);
        grid.setVerticalSpacing(Ui.dp(this, 12));
        grid.setHorizontalSpacing(Ui.dp(this, 8));
        grid.setPadding(Ui.dp(this, 12), Ui.dp(this, 4), Ui.dp(this, 12), Ui.dp(this, 12));
        adapter = new BaseAdapter() {
            public int getCount() { return genres.size(); }
            public Object getItem(int p) { return genres.get(p); }
            public long getItemId(int p) { return p; }
            public View getView(int pos, View cv, ViewGroup parent) {
                final String[] g = genres.get(pos);
                FrameLayout tile = new FrameLayout(GenresActivity.this);
                RoundedImageView img = new RoundedImageView(GenresActivity.this, 14);
                img.setBackgroundColor(Ui.PANEL);
                tile.addView(img, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(GenresActivity.this, 128)));
                View shade = new View(GenresActivity.this);
                android.graphics.drawable.GradientDrawable fade = new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0x11000000, 0x22000000, 0xE6000000});
                fade.setCornerRadius(Ui.dp(GenresActivity.this, 14));
                shade.setBackground(fade);
                tile.addView(shade, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(GenresActivity.this, 128)));
                
                TextView name = Ui.tv(GenresActivity.this, g[1], 16, Ui.TEXT, true);
                name.setShadowLayer(6, 0, 2, 0xCC000000);
                FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                np.gravity = Gravity.BOTTOM;
                np.setMargins(Ui.dp(GenresActivity.this, 12), 0, Ui.dp(GenresActivity.this, 10), Ui.dp(GenresActivity.this, 12));
                name.setBackground(Ui.round(0x33000000, 8, GenresActivity.this));
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
