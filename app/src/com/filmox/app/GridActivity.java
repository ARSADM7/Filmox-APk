package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class GridActivity extends Activity {
    private String mode, value, title;
    private int page = 1;
    private boolean loading = false, ended = false;
    private final List<Hyper.Item> items = new ArrayList<>();
    private final List<Hyper.Item> loadedOrder = new ArrayList<>();
    private String sortMode = "recentes";
    private String yearFilter = "";
    private CardAdapter adapter;
    private LinearLayout statusBox;
    private LinearLayout bannerBox;
    private boolean bannerDone = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        mode = getIntent().getStringExtra("mode");
        value = getIntent().getStringExtra("value");
        title = getIntent().getStringExtra("title");
        if (title == null) title = "Filmox";

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
        top.addView(Ui.tv(this, title, "section".equals(mode) ? 23 : 19, Ui.TEXT, true));
        root.addView(top);

        if ("section".equals(mode)) {
            root.addView(filterBar());
            bannerBox = new LinearLayout(this);
            bannerBox.setOrientation(LinearLayout.VERTICAL);
            root.addView(bannerBox);
        } else if (canPaginate() || "search".equals(mode)) {
            root.addView(sortBar());
        }
        statusBox = new LinearLayout(this);
        statusBox.setGravity(Gravity.CENTER);
        statusBox.setPadding(0, Ui.dp(this, 22), 0, 0);
        statusBox.addView(new ProgressBar(this));
        root.addView(statusBox);

        GridView grid = new GridView(this);
        grid.setNumColumns("section".equals(mode) ? 4 : 3);
        grid.setVerticalSpacing(Ui.dp(this, 4));
        grid.setHorizontalSpacing(Ui.dp(this, 4));
        grid.setPadding(Ui.dp(this, 6), 0, Ui.dp(this, 6), Ui.dp(this, 8));
        adapter = new CardAdapter(this, items, "section".equals(mode) ? 118 : 168);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Hyper.Item it = items.get(pos);
                Intent i = new Intent(GridActivity.this, DetailActivity.class);
                i.putExtra("type", it.type); i.putExtra("slug", it.slug);
                i.putExtra("title", it.title); i.putExtra("poster", it.poster);
                i.putExtra("score", it.score); i.putExtra("year", it.year);
                startActivity(i);
            }
        });
        if (canPaginate()) {
            grid.setOnScrollListener(new AbsListView.OnScrollListener() {
                public void onScrollStateChanged(AbsListView v, int s) { }
                public void onScroll(AbsListView v, int first, int count, int total) {
                    if (!loading && !ended && total > 0 && first + count >= total - 6) loadPage(page + 1);
                }
            });
        }
        root.addView(grid, new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        if ("favorites".equals(mode)) {
            items.addAll(Fav.list(this));
            adapter.notifyDataSetChanged();
            statusBox.removeAllViews();
            if (items.isEmpty()) {
                TextView t = Ui.tv(this, "Ainda não tens favoritos.\nToca na estrela num filme ou série para guardar aqui.", 14.5f, Ui.SUB, false);
                t.setGravity(Gravity.CENTER);
                statusBox.addView(t);
            }
        } else {
            loadPage(1);
        }
    }

    private View sortBar() {
        HorizontalScrollView hs = new android.widget.HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(Ui.dp(this, 12), Ui.dp(this, 2), Ui.dp(this, 12), Ui.dp(this, 6));
        String[][] opts = {{"recentes", "Recentes"}, {"nota", "★ Melhores"}, {"az", "A–Z"}, {"ano", "Ano"}};
        for (final String[] o : opts) {
            final TextView chip = Ui.chip(this, o[1]);
            LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cl.setMargins(0, 0, Ui.dp(this, 8), 0);
            chip.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { sortMode = o[0]; applySort(); }
            });
            row.addView(chip, cl);
        }
        hs.addView(row);
        return hs;
    }

    private void applySort() {
        List<Hyper.Item> base = new ArrayList<>();
        for (Hyper.Item i : loadedOrder) {
            if (yearFilter.isEmpty() || yearFilter.equals(i.year)) base.add(i);
        }
        if ("nota".equals(sortMode)) {
            java.util.Collections.sort(base, new java.util.Comparator<Hyper.Item>() {
                public int compare(Hyper.Item a, Hyper.Item b) {
                    return Float.compare(scoreOf(b), scoreOf(a));
                }
            });
        } else if ("az".equals(sortMode)) {
            java.util.Collections.sort(base, new java.util.Comparator<Hyper.Item>() {
                public int compare(Hyper.Item a, Hyper.Item b) {
                    return a.title.compareToIgnoreCase(b.title);
                }
            });
        } else if ("ano".equals(sortMode)) {
            java.util.Collections.sort(base, new java.util.Comparator<Hyper.Item>() {
                public int compare(Hyper.Item a, Hyper.Item b) {
                    return yearOf(b) - yearOf(a);
                }
            });
        }
        items.clear(); items.addAll(base);
        adapter.notifyDataSetChanged();
    }
    private float scoreOf(Hyper.Item i) {
        try { return i.score == null || i.score.isEmpty() ? 0f : Float.parseFloat(i.score); }
        catch (Exception e) { return 0f; }
    }
    private int yearOf(Hyper.Item i) {
        try { return i.year == null || i.year.isEmpty() ? 0 : Integer.parseInt(i.year); }
        catch (Exception e) { return 0; }
    }

    private View filterBar() {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(Ui.dp(this, 12), Ui.dp(this, 2), Ui.dp(this, 12), Ui.dp(this, 8));
        String[] labels = {"[ Género ]", "[ Ano ]", "[ Ordenação ]"};
        for (int i = 0; i < labels.length; i++) {
            final int which = i;
            TextView chip = Ui.chip(this, labels[i]);
            chip.setBackground(Ui.roundStroke(Ui.PANEL, 14, Ui.ACCENT, this));
            chip.setTextColor(Ui.ACCENT2);
            LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cl.setMargins(0, 0, Ui.dp(this, 8), 0);
            chip.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (which == 0) pickGenre();
                    else if (which == 1) pickYear();
                    else pickSort();
                }
            });
            row.addView(chip, cl);
        }
        hs.addView(row);
        return hs;
    }

    private void pickSort() {
        final String[] names = {"Recentes", "★ Melhores", "A–Z", "Ano (mais novos)"};
        final String[] keys = {"recentes", "nota", "az", "ano"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Ordenação")
                .setItems(names, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) {
                        sortMode = keys[w]; applySort();
                    }
                }).show();
    }

    private void pickYear() {
        java.util.TreeSet<Integer> years = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
        for (Hyper.Item i : loadedOrder) { int y = yearOf(i); if (y > 0) years.add(y); }
        final List<String> opts = new ArrayList<>();
        opts.add("Todos os anos");
        for (Integer y : years) opts.add(String.valueOf(y));
        new android.app.AlertDialog.Builder(this)
                .setTitle("Filtrar por ano (itens carregados)")
                .setItems(opts.toArray(new String[0]), new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) {
                        yearFilter = w == 0 ? "" : opts.get(w);
                        applySort();
                        if (!yearFilter.isEmpty()) {
                            android.widget.Toast.makeText(GridActivity.this,
                                    "A mostrar " + items.size() + " de " + yearFilter + " (carrega mais com scroll para filtrar mais)",
                                    android.widget.Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    private void pickGenre() {
        Bg.run(new Bg.Work<List<String[]>>() {
            public List<String[]> run() throws Exception { return Hyper.genres(); }
        }, new Bg.Done<List<String[]>>() {
            public void ok(final List<String[]> gs) {
                String[] names = new String[gs.size()];
                for (int i = 0; i < gs.size(); i++) names[i] = gs.get(i)[1];
                new android.app.AlertDialog.Builder(GridActivity.this)
                        .setTitle("Género")
                        .setItems(names, new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface d, int w) {
                                Intent i = new Intent(GridActivity.this, GridActivity.class);
                                i.putExtra("mode", "genre");
                                i.putExtra("value", gs.get(w)[0]);
                                i.putExtra("title", gs.get(w)[1]);
                                startActivity(i);
                            }
                        }).show();
            }
            public void fail(Exception e) {
                android.widget.Toast.makeText(GridActivity.this, "Erro ao carregar géneros.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildBanner(final Hyper.Item it) {
        // Continuar a assistir (só tipos de série)
        if (!"filmes".equals(value)) {
            List<Hist.Entry> mine = new ArrayList<>();
            for (Hist.Entry e : Hist.list(this)) if (value.equals(e.type)) mine.add(e);
            if (!mine.isEmpty()) {
                TextView h = Ui.tv(this, "Continuar a assistir", 16, Ui.TEXT, true);
                h.setPadding(Ui.dp(this, 14), Ui.dp(this, 6), Ui.dp(this, 14), Ui.dp(this, 6));
                bannerBox.addView(h);
                HorizontalScrollView hs = new HorizontalScrollView(this);
                hs.setHorizontalScrollBarEnabled(false);
                LinearLayout row = new LinearLayout(this);
                row.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), Ui.dp(this, 4));
                for (final Hist.Entry e : mine) {
                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
                    RoundedImageView im = new RoundedImageView(this, 10);
                    im.setBackgroundColor(Ui.PANEL);
                    card.addView(im, new LinearLayout.LayoutParams(Ui.dp(this, 150), Ui.dp(this, 84)));
                    Img.load(im, e.poster == null ? null : e.poster.replace("/w500/", "/w780/"));
                    TextView tt = Ui.tv(this, e.title == null ? "" : e.title, 11.5f, Ui.TEXT, false);
                    tt.setMaxLines(1);
                    card.addView(tt, new LinearLayout.LayoutParams(Ui.dp(this, 150), ViewGroup.LayoutParams.WRAP_CONTENT));
                    card.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            PlayerActivity.play(GridActivity.this, e.watchPath, e.label, e.title, e.poster, e.type, e.slug);
                        }
                    });
                    row.addView(card);
                }
                hs.addView(row);
                bannerBox.addView(hs);
            }
        }
        // Banner de destaque
        TextView h2 = Ui.tv(this, title + " em destaque", 16, Ui.TEXT, true);
        h2.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 6));
        bannerBox.addView(h2);
        android.widget.FrameLayout fb = new android.widget.FrameLayout(this);
        RoundedImageView bd = new RoundedImageView(this, 12);
        bd.setBackgroundColor(Ui.PANEL);
        fb.addView(bd, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 168)));
        Img.load(bd, it.backdrop());
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 10));
        android.widget.FrameLayout.LayoutParams ip = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ip.gravity = Gravity.BOTTOM;
        TextView tt = Ui.tv(this, it.title, 17, Ui.TEXT, true);
        tt.setShadowLayer(6, 0, 2, 0xCC000000);
        info.addView(tt);
        String meta = (it.year == null || it.year.isEmpty() ? "" : it.year)
                + (it.score == null || it.score.isEmpty() ? "" : "   ★ " + it.score);
        TextView mm = Ui.tv(this, meta, 12.5f, Ui.ACCENT2, false);
        info.addView(mm);
        TextView watch = Ui.bigButton(this, "▶ Assistir Agora");
        watch.setTextSize(13.5f);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(Ui.dp(this, 150), Ui.dp(this, 38));
        wp.setMargins(0, Ui.dp(this, 6), 0, 0);
        watch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(GridActivity.this, DetailActivity.class);
                i.putExtra("type", it.type); i.putExtra("slug", it.slug);
                i.putExtra("title", it.title); i.putExtra("poster", it.poster);
                i.putExtra("score", it.score); i.putExtra("year", it.year);
                startActivity(i);
            }
        });
        info.addView(watch, wp);
        fb.addView(info, ip);
        LinearLayout.LayoutParams fbp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 168));
        fbp.setMargins(Ui.dp(this, 12), 0, Ui.dp(this, 12), Ui.dp(this, 6));
        bannerBox.addView(fb, fbp);
        fb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { watch.performClick(); }
        });
    }

    private boolean canPaginate() {
        return "section".equals(mode) || "genre".equals(mode) || "lancamentos".equals(mode);
    }

    private void loadPage(final int p) {
        loading = true;
        Bg.run(new Bg.Work<List<Hyper.Item>>() {
            public List<Hyper.Item> run() throws Exception {
                if ("section".equals(mode)) return Hyper.section(value, p);
                if ("genre".equals(mode)) return Hyper.genre(value, p);
                if ("lancamentos".equals(mode)) return Hyper.lancamentos(p);
                if ("search".equals(mode)) return Hyper.search(value);
                return new ArrayList<>();
            }
        }, new Bg.Done<List<Hyper.Item>>() {
            public void ok(List<Hyper.Item> v) {
                loading = false;
                statusBox.removeAllViews();
                if (v.isEmpty()) {
                    ended = true;
                    if (items.isEmpty()) {
                        TextView t = Ui.tv(GridActivity.this, "Nada encontrado aqui.", 14.5f, Ui.SUB, false);
                        t.setGravity(Gravity.CENTER);
                        statusBox.addView(t);
                    }
                } else {
                    page = p;
                    loadedOrder.addAll(v);
                    applySort();
                    if ("section".equals(mode) && !bannerDone && !loadedOrder.isEmpty()) {
                        bannerDone = true;
                        buildBanner(loadedOrder.get(0));
                    }
                }
            }
            public void fail(Exception e) {
                loading = false;
                statusBox.removeAllViews();
                TextView t = Ui.tv(GridActivity.this, "Erro de ligação. Volta atrás e tenta outra vez.", 14.5f, Ui.SUB, false);
                t.setGravity(Gravity.CENTER);
                statusBox.addView(t);
            }
        });
    }
}
