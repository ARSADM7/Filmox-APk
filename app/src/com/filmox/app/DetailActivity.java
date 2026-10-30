package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends Activity {
    private String type, slug, title0, poster0;
    private Hyper.Detail detail;
    private LinearLayout episodeBox;
    private Spinner seasonSpinner;
    private TextView metaView, favBtn;
    private boolean favState;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        type = getIntent().getStringExtra("type");
        slug = getIntent().getStringExtra("slug");
        title0 = getIntent().getStringExtra("title");
        poster0 = getIntent().getStringExtra("poster");
        showLoading();
        Bg.run(new Bg.Work<Hyper.Detail>() {
            public Hyper.Detail run() throws Exception { return Hyper.detail(type, slug); }
        }, new Bg.Done<Hyper.Detail>() {
            public void ok(Hyper.Detail v) { detail = v; build(); }
            public void fail(Exception e) { showError(); }
        });
    }

    private void showLoading() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        root.setGravity(Gravity.CENTER);
        android.widget.ProgressBar pb = new android.widget.ProgressBar(this);
        root.addView(pb);
        TextView t = Ui.tv(this, "A carregar…", 14, Ui.SUB, false);
        t.setPadding(0, Ui.dp(this, 10), 0, 0);
        root.addView(t);
        setContentView(root);
    }

    private void showError() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        root.setGravity(Gravity.CENTER);
        root.setPadding(Ui.dp(this, 24), 0, Ui.dp(this, 24), 0);
        TextView t = Ui.tv(this, "Não foi possível carregar esta página.\nVerifica a internet e tenta outra vez.", 15, Ui.TEXT, false);
        t.setGravity(Gravity.CENTER);
        root.addView(t);
        TextView btn = Ui.bigButton(this, "Tentar outra vez");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 200), Ui.dp(this, 46));
        lp.setMargins(0, Ui.dp(this, 16), 0, 0);
        btn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { recreate(); } });
        root.addView(btn, lp);
        setContentView(root);
    }

    private void build() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        // Backdrop com título por cima (modelo das referências)
        FrameLayout header = new FrameLayout(this);
        ImageView bd = new ImageView(this);
        bd.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bd.setBackgroundColor(Ui.PANEL);
        header.addView(bd, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 250)));
        Img.load(bd, detail.image != null && !detail.image.isEmpty() ? detail.image : poster0);
        View shade = new View(this);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ 0x00000000, 0x660A0A10, Ui.BG });
        shade.setBackground(gd);
        FrameLayout.LayoutParams shp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 250));
        header.addView(shade, shp);
        TextView title = Ui.tv(this, detail.title.isEmpty() ? title0 : detail.title, 24, Color.WHITE, true);
        title.setShadowLayer(6, 0, 2, 0xCC000000);
        title.setMaxLines(2);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.gravity = Gravity.BOTTOM;
        tp.setMargins(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 10));
        header.addView(title, tp);
        col.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 250)));

        LinearLayout pad = new LinearLayout(this);
        pad.setOrientation(LinearLayout.VERTICAL);
        pad.setPadding(Ui.dp(this, 16), Ui.dp(this, 8), Ui.dp(this, 16), Ui.dp(this, 24));
        col.addView(pad);

        // Meta: ano - nota - duracao - classificacao (+ qualidade/audio via watch)
        String yr = !detail.year.isEmpty() ? detail.year : getIntent().getStringExtra("year");
        String rt = "";
        if (!detail.rating.isEmpty()) rt = "★ " + detail.rating + "   ";
        else {
            String sc = getIntent().getStringExtra("score");
            if (sc != null && !sc.isEmpty()) rt = "★ " + sc + "   ";
        }
        String dur = "";
        if (detail.runtimeMin > 0) {
            dur = detail.isSeries() ? detail.runtimeMin + " min/ep   "
                    : (detail.runtimeMin / 60) + "h " + (detail.runtimeMin % 60) + "m   ";
        }
        String cert = detail.certification.isEmpty() ? "" : "[" + detail.certification + "+]   ";
        final String metaBase = (yr == null || yr.isEmpty() ? "" : yr + "   ") + rt + dur + cert
                + (detail.isSeries() ? detail.episodes.size() + " episódio(s)" : labelType(type));
        metaView = Ui.tv(this, metaBase, 13.5f, Ui.ACCENT2, false);
        pad.addView(metaView);

        // Botoes: Play grande + Favorito + Baixar
        LinearLayout btns = new LinearLayout(this);
        btns.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, Ui.dp(this, 12), 0, 0);
        TextView playBtn = Ui.bigButton(this, detail.isSeries() ? "▶  Continuar / Assistir" : "▶  Assistir");
        playBtn.setTextSize(16.5f);
        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (detail.isSeries()) {
                    Hyper.Episode next = nextEpisode();
                    if (next != null) playPath(next.path, seriesTitle() + " T" + next.season + "E" + next.number);
                    else Toast.makeText(DetailActivity.this, "Sem episódios nesta página.", Toast.LENGTH_LONG).show();
                } else {
                    playPath(Hyper.movieWatchPath(slug), detailTitle());
                }
            }
        });
        btns.addView(playBtn, new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 1));
        favBtn = squareBtn(isFav() ? "★" : "☆");
        favBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { toggleFav(); }
        });
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(Ui.dp(this, 50), Ui.dp(this, 50));
        fp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        btns.addView(favBtn, fp);
        if (!detail.isSeries()) {
            TextView dlBtn = squareBtn("⬇");
            dlBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    downloadPath(Hyper.movieWatchPath(slug), detailTitle());
                }
            });
            btns.addView(dlBtn, (LinearLayout.LayoutParams) fp);
        }
        pad.addView(btns, bp);

        // Trailer
        if (!detail.trailer.isEmpty()) {
            TextView tr = Ui.chip(this, "▶  Trailer (YouTube)");
            LinearLayout.LayoutParams trp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            trp.setMargins(0, Ui.dp(this, 12), 0, 0);
            tr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=" + detail.trailer)));
                    } catch (Exception e) {
                        Toast.makeText(DetailActivity.this, "Não foi possível abrir o YouTube.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            pad.addView(tr, trp);
        }

        // Generos
        if (!detail.genres.isEmpty()) {
            HorizontalScrollView hs = new HorizontalScrollView(this);
            hs.setHorizontalScrollBarEnabled(false);
            LinearLayout row = new LinearLayout(this);
            for (int i = 0; i < detail.genres.size(); i++) {
                final String gName = detail.genres.get(i);
                final String gSlug = i < detail.genreSlugs.size() ? detail.genreSlugs.get(i) : "";
                TextView chip = Ui.chip(this, gName);
                LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cl.setMargins(0, Ui.dp(this, 12), Ui.dp(this, 8), 0);
                chip.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (gSlug.isEmpty()) return;
                        Intent it = new Intent(DetailActivity.this, GridActivity.class);
                        it.putExtra("mode", "genre");
                        it.putExtra("value", gSlug);
                        it.putExtra("title", gName);
                        startActivity(it);
                    }
                });
                row.addView(chip, cl);
            }
            hs.addView(row);
            pad.addView(hs);
        }

        // Sinopse expansivel
        String synopsis = !detail.overview.isEmpty() ? detail.overview : detail.desc;
        if (!synopsis.isEmpty()) {
            TextView sh = Ui.tv(this, "Sinopse", 17, Ui.TEXT, true);
            sh.setPadding(0, Ui.dp(this, 18), 0, Ui.dp(this, 6));
            pad.addView(sh);
            final TextView st = Ui.tv(this, synopsis, 14.5f, Ui.SUB, false);
            st.setMaxLines(3);
            pad.addView(st);
            final TextView more = Ui.tv(this, "MAIS ▾", 12.5f, Ui.ACCENT, true);
            more.setPadding(0, Ui.dp(this, 4), 0, 0);
            more.setOnClickListener(new View.OnClickListener() {
                boolean open = false;
                public void onClick(View v) {
                    open = !open;
                    st.setMaxLines(open ? Integer.MAX_VALUE : 3);
                    more.setText(open ? "MENOS ▴" : "MAIS ▾");
                }
            });
            pad.addView(more);
        }

        // Elenco com fotos
        if (!detail.castMembers.isEmpty() || !detail.cast.isEmpty()) {
            TextView ch = Ui.tv(this, "Elenco", 17, Ui.TEXT, true);
            ch.setPadding(0, Ui.dp(this, 20), 0, Ui.dp(this, 8));
            pad.addView(ch);
            HorizontalScrollView hs = new HorizontalScrollView(this);
            hs.setHorizontalScrollBarEnabled(false);
            LinearLayout row = new LinearLayout(this);
            if (!detail.castMembers.isEmpty()) {
                for (String[] m : detail.castMembers) {
                    LinearLayout person = new LinearLayout(this);
                    person.setOrientation(LinearLayout.VERTICAL);
                    person.setGravity(Gravity.CENTER_HORIZONTAL);
                    person.setPadding(Ui.dp(this, 6), 0, Ui.dp(this, 6), 0);
                    RoundedImageView ph = new RoundedImageView(this, 28);
                    ph.setBackgroundColor(Ui.PANEL);
                    person.addView(ph, new LinearLayout.LayoutParams(Ui.dp(this, 58), Ui.dp(this, 58)));
                    if (!m[2].isEmpty()) Img.load(ph, m[2]);
                    else ph.setImageDrawable(null);
                    TextView nm = Ui.tv(this, m[0], 11.5f, Ui.TEXT, false);
                    nm.setMaxLines(2); nm.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams nl = new LinearLayout.LayoutParams(Ui.dp(this, 74), ViewGroup.LayoutParams.WRAP_CONTENT);
                    nl.setMargins(0, Ui.dp(this, 4), 0, 0);
                    person.addView(nm, nl);
                    if (!m[1].isEmpty()) {
                        TextView cr = Ui.tv(this, m[1], 10.5f, Ui.SUB, false);
                        cr.setMaxLines(1); cr.setGravity(Gravity.CENTER);
                        person.addView(cr, new LinearLayout.LayoutParams(Ui.dp(this, 74), ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    row.addView(person);
                }
            } else {
                for (String name : detail.cast) {
                    LinearLayout person = new LinearLayout(this);
                    person.setOrientation(LinearLayout.VERTICAL);
                    person.setGravity(Gravity.CENTER_HORIZONTAL);
                    person.setPadding(Ui.dp(this, 6), 0, Ui.dp(this, 6), 0);
                    TextView av = Ui.tv(this, initials(name), 15, Ui.ACCENT, true);
                    av.setGravity(Gravity.CENTER);
                    av.setBackground(Ui.round(Ui.PANEL, 28, this));
                    person.addView(av, new LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52)));
                    TextView nm = Ui.tv(this, name, 11.5f, Ui.TEXT, false);
                    nm.setMaxLines(2); nm.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams nl = new LinearLayout.LayoutParams(Ui.dp(this, 74), ViewGroup.LayoutParams.WRAP_CONTENT);
                    nl.setMargins(0, Ui.dp(this, 4), 0, 0);
                    person.addView(nm, nl);
                    row.addView(person);
                }
            }
            hs.addView(row);
            pad.addView(hs);
        }

        // Episodios
        if (detail.isSeries()) {
            TextView eh = Ui.tv(this, "Episódios", 17, Ui.TEXT, true);
            eh.setPadding(0, Ui.dp(this, 20), 0, Ui.dp(this, 6));
            pad.addView(eh);
            final List<Integer> seasons = new ArrayList<>();
            for (Hyper.Episode e : detail.episodes) if (!seasons.contains(e.season)) seasons.add(e.season);
            if (seasons.size() > 1) {
                List<String> labels = new ArrayList<>();
                for (int sn : seasons) labels.add("Temporada " + sn);
                seasonSpinner = new Spinner(this);
                ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
                seasonSpinner.setAdapter(aa);
                seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                        fillEpisodes(seasons.get(pos));
                    }
                    public void onNothingSelected(AdapterView<?> p) { }
                });
                pad.addView(seasonSpinner);
            }
            episodeBox = new LinearLayout(this);
            episodeBox.setOrientation(LinearLayout.VERTICAL);
            pad.addView(episodeBox);
            fillEpisodes(seasons.isEmpty() ? 1 : seasons.get(0));
        }

        // Qualidade/audio (filmes): buscar watch uma vez
        if (!detail.isSeries()) {
            Bg.run(new Bg.Work<Hyper.Watch>() {
                public Hyper.Watch run() throws Exception { return Hyper.watch(Hyper.movieWatchPath(slug)); }
            }, new Bg.Done<Hyper.Watch>() {
                public void ok(Hyper.Watch w) {
                    String extra = "";
                    if (w.quality != null && !w.quality.isEmpty()) extra += w.quality + "   ";
                    if (w.audio != null && !w.audio.isEmpty()) extra += w.audio + "   ";
                    if (!extra.isEmpty() && metaView != null) metaView.setText(metaBase + extra.trim());
                }
                public void fail(Exception e) { }
            });
        }

        // Semelhantes
        final LinearLayout similarBox = new LinearLayout(this);
        similarBox.setOrientation(LinearLayout.VERTICAL);
        pad.addView(similarBox);
        Bg.run(new Bg.Work<List<Hyper.Item>>() {
            public List<Hyper.Item> run() throws Exception {
                if (!detail.genreSlugs.isEmpty()) {
                    List<Hyper.Item> g = Hyper.genre(detail.genreSlugs.get(0), 1);
                    if (g.size() > 1) return g;
                }
                return Hyper.section(type, 1);
            }
        }, new Bg.Done<List<Hyper.Item>>() {
            public void ok(List<Hyper.Item> v) {
                List<Hyper.Item> rel = new ArrayList<>();
                for (Hyper.Item i : v) {
                    if (!i.slug.equals(slug)) rel.add(i);
                    if (rel.size() >= 12) break;
                }
                if (rel.isEmpty()) return;
                TextView h = Ui.tv(DetailActivity.this, "Semelhantes", 17, Ui.TEXT, true);
                h.setPadding(0, Ui.dp(DetailActivity.this, 22), 0, Ui.dp(DetailActivity.this, 8));
                similarBox.addView(h);
                HorizontalScrollView hs2 = new HorizontalScrollView(DetailActivity.this);
                hs2.setHorizontalScrollBarEnabled(false);
                LinearLayout row2 = new LinearLayout(DetailActivity.this);
                for (final Hyper.Item it : rel) {
                    LinearLayout card = new LinearLayout(DetailActivity.this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(Ui.dp(DetailActivity.this, 5), 0, Ui.dp(DetailActivity.this, 5), 0);
                    RoundedImageView im = new RoundedImageView(DetailActivity.this, 10);
                    im.setBackgroundColor(Ui.PANEL);
                    card.addView(im, new LinearLayout.LayoutParams(Ui.dp(DetailActivity.this, 100), Ui.dp(DetailActivity.this, 148)));
                    Img.load(im, it.poster);
                    TextView tt = Ui.tv(DetailActivity.this, it.title, 12, Ui.TEXT, false);
                    tt.setMaxLines(2);
                    LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(Ui.dp(DetailActivity.this, 100), ViewGroup.LayoutParams.WRAP_CONTENT);
                    tl.setMargins(0, Ui.dp(DetailActivity.this, 4), 0, 0);
                    card.addView(tt, tl);
                    card.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { openDetail(it); }
                    });
                    row2.addView(card);
                }
                hs2.addView(row2);
                similarBox.addView(hs2);
            }
            public void fail(Exception e) { }
        });

        sv.addView(col);
        setContentView(sv);
        favState = isFav();
    }

    private TextView squareBtn(String label) {
        TextView b = Ui.tv(this, label, 19, Ui.ACCENT, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.roundStroke(Ui.PANEL, 12, Ui.ACCENT, this));
        return b;
    }

    private String detailTitle() { return detail != null && !detail.title.isEmpty() ? detail.title : title0; }
    private String seriesTitle() { return detailTitle(); }

    private Hyper.Episode nextEpisode() {
        if (detail.episodes.isEmpty()) return null;
        for (Hyper.Episode e : detail.episodes) {
            Hist.Entry h = Hist.get(this, e.path);
            if (h == null || !h.finished()) return e;
        }
        return detail.episodes.get(0);
    }

    private void fillEpisodes(int season) {
        episodeBox.removeAllViews();
        Map<String, String> byKey = new LinkedHashMap<>();
        for (Hyper.Episode e : detail.episodes) byKey.put(e.season + "x" + e.number, e.downloadId);
        for (final Hyper.Episode e : detail.episodes) {
            if (e.season != season) continue;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.round(Ui.PANEL, 10, this));
            row.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, Ui.dp(this, 8), 0, 0);
            TextView play = Ui.tv(this, "▶", 15, Ui.ACCENT, true);
            play.setBackground(Ui.round(Ui.PANEL2, 16, this));
            play.setGravity(Gravity.CENTER);
            row.addView(play, new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 34)));
            Hist.Entry seen = Hist.get(this, e.path);
            String suffix = "";
            if (seen != null) suffix = seen.finished() ? "    ✓ visto" : "    · " + seen.percent() + "% visto";
            TextView name = Ui.tv(this, "Episódio " + e.number + suffix, 15, Ui.TEXT, false);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            np.setMargins(Ui.dp(this, 10), 0, 0, 0);
            row.addView(name, np);
            TextView dl = Ui.tv(this, "⬇", 17, Ui.ACCENT2, false);
            dl.setPadding(Ui.dp(this, 8), Ui.dp(this, 6), Ui.dp(this, 8), Ui.dp(this, 6));
            dl.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    downloadPath(e.path, seriesTitle() + " T" + e.season + "E" + e.number);
                }
            });
            row.addView(dl);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    playPath(e.path, seriesTitle() + " T" + e.season + "E" + e.number);
                }
            });
            episodeBox.addView(row, rp);
        }
        if (episodeBox.getChildCount() == 0) {
            episodeBox.addView(Ui.tv(this, "Sem episódios listados nesta página.", 13.5f, Ui.SUB, false));
        }
    }

    private void playPath(String watchPath, String label) {
        PlayerActivity.play(this, watchPath, label, detailTitle(), poster0, type, slug);
    }

    private void downloadPath(final String watchPath, final String label) {
        if (Dl.find(this, watchPath) != null) {
            Toast.makeText(this, "Já está nos Downloads ⬇", Toast.LENGTH_LONG).show();
            return;
        }
        if (SettingsActivity.wifiOnly(this)) {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni == null || ni.getType() != ConnectivityManager.TYPE_WIFI) {
                    Toast.makeText(this, "Definição activa: downloads só por Wi-Fi.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception ignored) { }
        }
        Toast.makeText(this, "A preparar o download…", Toast.LENGTH_SHORT).show();
        Bg.run(new Bg.Work<Hyper.Watch>() {
            public Hyper.Watch run() throws Exception { return Hyper.watch(watchPath); }
        }, new Bg.Done<Hyper.Watch>() {
            public void ok(Hyper.Watch w) {
                if (w.mp4 == null) {
                    Toast.makeText(DetailActivity.this, "Ficheiro não encontrado nesta página.", Toast.LENGTH_LONG).show();
                    return;
                }
                long id = Dl.start(DetailActivity.this, w.mp4, label, poster0, watchPath);
                Toast.makeText(DetailActivity.this,
                        id > 0 ? "Download iniciado — vê o progresso em Downloads ⬇" : "Não foi possível iniciar o download.",
                        Toast.LENGTH_LONG).show();
            }
            public void fail(Exception e) {
                Toast.makeText(DetailActivity.this, "Erro ao preparar o download.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private Hyper.Item mkItem(String t, String sl, String ti, String po, String sc, String yr) {
        Hyper.Item it = new Hyper.Item();
        it.type = t; it.slug = sl; it.title = ti; it.poster = po; it.score = sc; it.year = yr;
        it.path = "/" + t + "/" + sl;
        return it;
    }
    private boolean isFav() {
        return Fav.hasItem(this, "/" + type + "/" + slug);
    }
    private void toggleFav() {
        Hyper.Item it = mkItem(type, slug, detailTitle(), poster0,
                getIntent().getStringExtra("score"), getIntent().getStringExtra("year"));
        Fav.toggleSimple(this, it);
        favBtn.setText(isFav() ? "★" : "☆");
    }

    private void openDetail(Hyper.Item it) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("type", it.type); i.putExtra("slug", it.slug);
        i.putExtra("title", it.title); i.putExtra("poster", it.poster);
        i.putExtra("score", it.score); i.putExtra("year", it.year);
        startActivity(i);
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        String r = parts.length > 0 && !parts[0].isEmpty() ? parts[0].substring(0, 1) : "?";
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) r += parts[parts.length - 1].substring(0, 1);
        return r.toUpperCase();
    }

    private String labelType(String t) {
        if ("filmes".equals(t)) return "Filme";
        if ("series".equals(t)) return "Série";
        if ("animes".equals(t)) return "Anime";
        if ("doramas".equals(t)) return "Dorama";
        if ("novelas".equals(t)) return "Novela";
        return t;
    }
}
