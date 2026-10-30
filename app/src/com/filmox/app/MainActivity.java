package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout content;
    private List<Hyper.Item> heroItems = new ArrayList<>();
    private ImageView heroImg;
    private TextView heroTitle, heroMeta;
    private int heroIdx = 0;
    private LinearLayout heroDots;
    private final Handler handler = new Handler();
    private boolean alive = true;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // barra superior
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(this, 16), Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10));
        TextView logo = new TextView(this);
        logo.setText("FILMOX");
        logo.setTextSize(24); logo.setTextColor(Ui.ACCENT);
        logo.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        top.addView(logo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        android.widget.ImageView favBtn = Ui.icon(this, android.R.drawable.btn_star_big_on, Ui.ACCENT2);
        favBtn.setPadding(Ui.dp(this, 10), Ui.dp(this, 4), Ui.dp(this, 6), Ui.dp(this, 4));
        favBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GridActivity.class);
                i.putExtra("mode", "favorites"); i.putExtra("title", "Os meus favoritos");
                startActivity(i);
            }
        });
        top.addView(favBtn);
        android.widget.ImageView dlBtn = Ui.icon(this, android.R.drawable.stat_sys_download_done, Ui.TEXT);
        dlBtn.setPadding(Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        dlBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startActivity(new Intent(MainActivity.this, DownloadsActivity.class)); }
        });
        top.addView(dlBtn);
        android.widget.ImageView setBtn = Ui.icon(this, android.R.drawable.ic_menu_preferences, Ui.TEXT);
        setBtn.setPadding(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        setBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startActivity(new Intent(MainActivity.this, SettingsActivity.class)); }
        });
        top.addView(setBtn);
        android.widget.ImageView searchBtn = Ui.icon(this, android.R.drawable.ic_menu_search, Ui.TEXT);
        searchBtn.setPadding(Ui.dp(this, 6), Ui.dp(this, 4), Ui.dp(this, 10), Ui.dp(this, 4));
        searchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startActivity(new Intent(MainActivity.this, SearchActivity.class)); }
        });
        top.addView(searchBtn);
        root.addView(top);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, Ui.dp(this, 14));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        root.addView(bottomNav());
        setContentView(root);
        showLoading("A carregar o catálogo…");
        loadHome();
    }

    private View bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(Ui.PANEL);
        nav.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
        int[][] items = {
            {android.R.drawable.ic_menu_view, 0},
            {android.R.drawable.ic_menu_gallery, 1},
            {android.R.drawable.ic_menu_slideshow, 2},
            {android.R.drawable.ic_media_play, 3},
            {android.R.drawable.ic_menu_sort_by_size, 4}
        };
        final String[] labels = {"Início", "Filmes", "Séries", "Animes", "Géneros"};
        final String[] modes = {"home", "filmes", "series", "animes", "generos"};
        for (final int[] it : items) {
            LinearLayout btn = new LinearLayout(this);
            btn.setOrientation(LinearLayout.VERTICAL);
            btn.setGravity(Gravity.CENTER);
            android.widget.ImageView ic = Ui.icon(this, it[0], Ui.TEXT);
            ic.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 2));
            TextView lb = Ui.tv(this, labels[it[1]], 10.5f, Ui.SUB, false);
            lb.setGravity(Gravity.CENTER);
            btn.addView(ic, new LinearLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 24)));
            btn.addView(lb);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String mode = modes[it[1]];
                    if (mode.equals("home")) return;
                    if (mode.equals("generos")) {
                        startActivity(new Intent(MainActivity.this, GenresActivity.class));
                    } else {
                        Intent i = new Intent(MainActivity.this, GridActivity.class);
                        i.putExtra("mode", "section"); i.putExtra("value", mode); i.putExtra("title", labels[it[1]]);
                        startActivity(i);
                    }
                }
            });
            nav.addView(btn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        return nav;
    }

    private void showLoading(String msg) {
        content.removeAllViews();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setPadding(0, Ui.dp(this, 60), 0, 0);
        box.addView(new ProgressBar(this));
        TextView t = Ui.tv(this, msg, 14, Ui.SUB, false); t.setGravity(Gravity.CENTER);
        box.addView(t);
        content.addView(box);
    }

    private void showError(final String msg) {
        content.removeAllViews();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setPadding(Ui.dp(this, 24), Ui.dp(this, 60), Ui.dp(this, 24), 0);
        TextView t = Ui.tv(this, msg, 15, Ui.TEXT, false); t.setGravity(Gravity.CENTER);
        box.addView(t);
        TextView retry = Ui.tv(this, "Tentar outra vez", 15, Ui.TEXT, true);
        retry.setBackground(Ui.round(Ui.ACCENT, 22, this));
        retry.setPadding(Ui.dp(this, 22), Ui.dp(this, 10), Ui.dp(this, 22), Ui.dp(this, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, Ui.dp(this, 16), 0, 0); lp.gravity = Gravity.CENTER_HORIZONTAL;
        retry.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showLoading("A carregar o catálogo…"); loadHome(); }
        });
        box.addView(retry, lp);
        content.addView(box);
    }

    private void loadHome() {
        Bg.run(new Bg.Work<Object[]>() {
            public Object[] run() throws Exception {
                List<Hyper.Item> home = Hyper.home();
                List<Hyper.Item> lanc = new ArrayList<>();
                try { lanc = Hyper.lancamentos(1); } catch (Exception ignored) { }
                return new Object[]{home, lanc};
            }
        }, new Bg.Done<Object[]>() {
            public void ok(Object[] v) {
                @SuppressWarnings("unchecked") List<Hyper.Item> home = (List<Hyper.Item>) v[0];
                @SuppressWarnings("unchecked") List<Hyper.Item> lanc = (List<Hyper.Item>) v[1];
                if (home.isEmpty()) { showError("Sem ligação ou catálogo vazio."); return; }
                buildHome(home, lanc);
            }
            public void fail(Exception e) { showError("Não foi possível carregar.\nVerifica a tua internet e tenta outra vez."); }
        });
    }

    private void buildHome(List<Hyper.Item> home, List<Hyper.Item> lanc) {
        content.removeAllViews();
        heroItems = home.subList(0, Math.min(6, home.size()));
        // HERO
        FrameLayout hero = new FrameLayout(this);
        heroImg = new ImageView(this);
        heroImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hero.addView(heroImg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 300)));
        View shade = new View(this);
        shade.setBackgroundColor(0x66000000);
        hero.addView(shade, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 300)));
        LinearLayout heroBox = new LinearLayout(this);
        heroBox.setOrientation(LinearLayout.VERTICAL);
        heroBox.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 14));
        FrameLayout.LayoutParams hb = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hb.gravity = Gravity.BOTTOM;
        heroTitle = Ui.tv(this, "", 22, Ui.TEXT, true);
        heroTitle.setMaxLines(2);
        heroBox.addView(heroTitle);
        heroMeta = Ui.tv(this, "", 13, Ui.ACCENT2, false);
        heroBox.addView(heroMeta);
        LinearLayout heroBtns = new LinearLayout(this);
        heroBtns.setGravity(Gravity.CENTER_VERTICAL);
        TextView watch = Ui.tv(this, "▶  Assistir agora", 14.5f, Ui.TEXT, true);
        watch.setBackground(Ui.round(Ui.ACCENT, 10, this));
        watch.setPadding(Ui.dp(this, 16), Ui.dp(this, 9), Ui.dp(this, 16), Ui.dp(this, 9));
        watch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (heroItems.isEmpty()) return;
                Hyper.Item it = heroItems.get(heroIdx);
                if ("filmes".equals(it.type)) {
                    PlayerActivity.play(MainActivity.this, Hyper.movieWatchPath(it.slug),
                            it.title, it.title, it.poster, it.type, it.slug);
                } else {
                    openDetail(it);
                }
            }
        });
        heroBtns.addView(watch);
        TextView det = Ui.tv(this, "Detalhes", 14.5f, Ui.TEXT, false);
        det.setBackground(Ui.roundStroke(0x00000000, 10, Ui.ACCENT, this));
        det.setPadding(Ui.dp(this, 16), Ui.dp(this, 9), Ui.dp(this, 16), Ui.dp(this, 9));
        det.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { if (!heroItems.isEmpty()) openDetail(heroItems.get(heroIdx)); }
        });
        LinearLayout.LayoutParams dl2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dl2.setMargins(Ui.dp(this, 10), 0, 0, 0);
        heroBtns.addView(det, dl2);
        LinearLayout.LayoutParams wl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wl.setMargins(0, Ui.dp(this, 10), 0, 0);
        heroBox.addView(heroBtns, wl);
        hero.addView(heroBox, hb);
        hero.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { if (!heroItems.isEmpty()) openDetail(heroItems.get(heroIdx)); }
        });
        heroDots = new LinearLayout(this);
        heroDots.setGravity(Gravity.CENTER);
        heroDots.setPadding(0, Ui.dp(this, 8), 0, 0);
        for (int i = 0; i < heroItems.size(); i++) {
            View d = new View(this);
            d.setBackground(Ui.round(Ui.PANEL2, 4, this));
            LinearLayout.LayoutParams dp3 = new LinearLayout.LayoutParams(Ui.dp(this, 7), Ui.dp(this, 7));
            dp3.setMargins(Ui.dp(this, 3), 0, Ui.dp(this, 3), 0);
            heroDots.addView(d, dp3);
        }
        content.addView(hero);
        content.addView(heroDots);
        updateHero();

        buildContinueRow();
        if (!lanc.isEmpty()) addRow("Lançamentos", lanc, "lancamentos", null, "Lançamentos");
        addRow("Em destaque", home, null, null, null);
        addRow("Filmes populares", filter(home, "filmes"), "section", "filmes", "Filmes");
        addRow("Séries populares", filter(home, "series"), "section", "series", "Séries");
        addRow("Animes", filter(home, "animes"), "section", "animes", "Animes");
        addRow("Doramas", filter(home, "doramas"), "section", "doramas", "Doramas");
        addRow("Novelas", filter(home, "novelas"), "section", "novelas", "Novelas");
    }

    private List<Hyper.Item> filter(List<Hyper.Item> all, String type) {
        List<Hyper.Item> out = new ArrayList<>();
        for (Hyper.Item i : all) if (type.equals(i.type)) out.add(i);
        return out;
    }

    private void buildContinueRow() {
        final List<Hist.Entry> hist = Hist.list(this);
        if (hist.isEmpty()) return;
        TextView h = Ui.tv(this, "▶ Continuar a assistir", 17, Ui.TEXT, true);
        h.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 8));
        h.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startActivity(new Intent(MainActivity.this, ContinueActivity.class)); }
        });
        content.addView(h);
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), 0);
        for (final Hist.Entry e : hist) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
            FrameLayout thumbBox = new FrameLayout(this);
            RoundedImageView im = new RoundedImageView(this, 10);
            im.setBackgroundColor(Ui.PANEL);
            thumbBox.addView(im, new FrameLayout.LayoutParams(Ui.dp(this, 168), Ui.dp(this, 96)));
            Img.load(im, e.poster == null ? null : e.poster.replace("/w500/", "/w780/"));
            LinearLayout track = new LinearLayout(this);
            track.setBackgroundColor(0xAA22222E);
            FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(Ui.dp(this, 168), Ui.dp(this, 4));
            tp.gravity = Gravity.BOTTOM;
            View fill = new View(this);
            fill.setBackgroundColor(Ui.ACCENT);
            track.addView(fill, new LinearLayout.LayoutParams(Ui.dp(this, 168) * e.percent() / 100, Ui.dp(this, 4)));
            thumbBox.addView(track, tp);
            TextView playIc = Ui.tv(this, "▶", 15, Ui.TEXT, true);
            playIc.setBackground(Ui.round(0x99000000, 16, this));
            playIc.setPadding(Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4));
            FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pp.gravity = Gravity.CENTER;
            thumbBox.addView(playIc, pp);
            card.addView(thumbBox);
            TextView tt = Ui.tv(this, e.title == null ? "" : e.title, 12, Ui.TEXT, false);
            tt.setMaxLines(1);
            LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(Ui.dp(this, 168), ViewGroup.LayoutParams.WRAP_CONTENT);
            tl.setMargins(0, Ui.dp(this, 4), 0, 0);
            card.addView(tt, tl);
            card.addView(Ui.tv(this, "Faltam " + Math.max(0, (e.durMs - e.posMs) / 60000) + " min", 11, Ui.SUB, false));
            card.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PlayerActivity.play(MainActivity.this, e.watchPath, e.label, e.title, e.poster, e.type, e.slug);
                }
            });
            card.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Hist.remove(MainActivity.this, e.watchPath);
                    android.widget.Toast.makeText(MainActivity.this, "Removido de Continuar a assistir", android.widget.Toast.LENGTH_SHORT).show();
                    loadHomeRefresh();
                    return true;
                }
            });
            row.addView(card);
        }
        hs.addView(row);
        content.addView(hs);
    }

    private void loadHomeRefresh() { showLoading("A carregar o catálogo…"); loadHome(); }

    private void addRow(String label, List<Hyper.Item> items, final String gridMode, final String gridValue, final String gridTitle) {
        if (items.isEmpty()) return;
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 8));
        TextView h = Ui.tv(this, label, 17, Ui.TEXT, true);
        header.addView(h, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (gridMode != null) {
            TextView all = Ui.tv(this, "Ver todos ›", 13.5f, Ui.ACCENT, false);
            all.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, GridActivity.class);
                    i.putExtra("mode", gridMode);
                    if (gridValue != null) i.putExtra("value", gridValue);
                    i.putExtra("title", gridTitle == null ? label : gridTitle);
                    startActivity(i);
                }
            });
            header.addView(all);
        }
        content.addView(header);
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), 0);
        for (final Hyper.Item it : items) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
            RoundedImageView img = new RoundedImageView(this, 10);
            img.setBackgroundColor(Ui.PANEL);
            card.addView(img, new LinearLayout.LayoutParams(Ui.dp(this, 108), Ui.dp(this, 158)));
            Img.load(img, it.poster);
            TextView t = Ui.tv(this, it.title, 12, Ui.TEXT, false);
            t.setMaxLines(2);
            t.setPadding(0, Ui.dp(this, 4), 0, 0);
            LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(Ui.dp(this, 108), ViewGroup.LayoutParams.WRAP_CONTENT);
            card.addView(t, tl);
            String meta = (it.score == null || it.score.isEmpty() ? "" : "★ " + it.score + "  ") + (it.year == null ? "" : it.year);
            card.addView(Ui.tv(this, meta, 11, Ui.SUB, false));
            card.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { openDetail(it); }
            });
            row.addView(card);
        }
        hs.addView(row);
        content.addView(hs);
    }

    private void updateHero() {
        if (heroItems.isEmpty()) return;
        if (heroDots != null) {
            for (int i = 0; i < heroDots.getChildCount(); i++) {
                View d = heroDots.getChildAt(i);
                boolean active = i == heroIdx;
                d.setBackground(Ui.round(active ? Ui.ACCENT : Ui.PANEL2, 4, this));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) d.getLayoutParams();
                lp.width = Ui.dp(this, active ? 18 : 7);
                d.setLayoutParams(lp);
            }
        }
        Hyper.Item it = heroItems.get(heroIdx);
        Img.load(heroImg, it.backdrop());
        heroTitle.setText(it.title);
        heroMeta.setText((it.score == null || it.score.isEmpty() ? "" : "★ " + it.score + "   ")
                + (it.year == null ? "" : it.year) + "   " + labelType(it.type));
        handler.removeCallbacks(heroTick);
        if (alive && heroItems.size() > 1) handler.postDelayed(heroTick, 5000);
    }
    private final Runnable heroTick = new Runnable() {
        public void run() {
            if (!alive || heroItems.isEmpty()) return;
            heroIdx = (heroIdx + 1) % heroItems.size();
            updateHero();
        }
    };
    private String labelType(String t) {
        if ("filmes".equals(t)) return "Filme";
        if ("series".equals(t)) return "Série";
        if ("animes".equals(t)) return "Anime";
        if ("doramas".equals(t)) return "Dorama";
        if ("novelas".equals(t)) return "Novela";
        return t;
    }
    private void openDetail(Hyper.Item it) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("type", it.type); i.putExtra("slug", it.slug);
        i.putExtra("title", it.title); i.putExtra("poster", it.poster);
        i.putExtra("score", it.score); i.putExtra("year", it.year);
        startActivity(i);
    }
    @Override protected void onDestroy() { alive = false; handler.removeCallbacks(heroTick); super.onDestroy(); }
}
