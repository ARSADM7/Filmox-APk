package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity {
    private final List<Hyper.Item> allItems = new ArrayList<>();
    private final List<Hyper.Item> visibleItems = new ArrayList<>();
    private CardAdapter adapter;
    private TextView status;
    private EditText input;
    private LinearLayout suggestions;
    private String activeType = "all";
    private String lastQuery = "";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 8));
        TextView back = Ui.tv(this, "‹", 30, Ui.TEXT, true);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        top.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.addView(Ui.tv(this, "Pesquisar", 21, Ui.TEXT, true));
        heading.addView(Ui.tv(this, "Encontra o próximo título para ver", 11.5f, Ui.SUB, false));
        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        hl.leftMargin = Ui.dp(this, 8);
        top.addView(heading, hl);
        root.addView(top);

        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setBackground(Ui.roundStroke(Ui.PANEL2, 16, 0x554B4355, this));
        searchBar.setPadding(Ui.dp(this, 12), Ui.dp(this, 2), Ui.dp(this, 8), Ui.dp(this, 2));
        ImageView searchIcon = Ui.icon(this, android.R.drawable.ic_menu_search, Ui.ACCENT);
        searchBar.addView(searchIcon, new LinearLayout.LayoutParams(Ui.dp(this, 22), Ui.dp(this, 22)));
        input = new EditText(this);
        input.setHint("Filmes, séries e animes...");
        input.setHintTextColor(Ui.SUB);
        input.setTextColor(Ui.TEXT);
        input.setTextSize(15);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setPadding(Ui.dp(this, 10), Ui.dp(this, 11), Ui.dp(this, 4), Ui.dp(this, 11));
        searchBar.addView(input, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageView clear = Ui.icon(this, android.R.drawable.ic_menu_close_clear_cancel, Ui.SUB);
        clear.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                input.setText("");
                allItems.clear(); visibleItems.clear(); adapter.notifyDataSetChanged();
                status.setText("Pesquisa limpa.");
                showSuggestions();
            }
        });
        searchBar.addView(clear, new LinearLayout.LayoutParams(Ui.dp(this, 38), Ui.dp(this, 42)));
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMargins(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 10));
        root.addView(searchBar, sbp);

        HorizontalScrollView filters = new HorizontalScrollView(this);
        filters.setHorizontalScrollBarEnabled(false);
        LinearLayout filterRow = new LinearLayout(this);
        filterRow.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 8));
        addFilter(filterRow, "Tudo", "all");
        addFilter(filterRow, "Filmes", "filmes");
        addFilter(filterRow, "Séries", "series");
        addFilter(filterRow, "Animes", "animes");
        filters.addView(filterRow);
        root.addView(filters);

        status = Ui.tv(this, "Pesquisa filmes, séries e animes pelo nome.", 13, Ui.SUB, false);
        status.setPadding(Ui.dp(this, 16), Ui.dp(this, 5), Ui.dp(this, 16), Ui.dp(this, 8));
        root.addView(status);

        suggestions = new LinearLayout(this);
        suggestions.setOrientation(LinearLayout.VERTICAL);
        root.addView(suggestions);

        GridView grid = new GridView(this);
        grid.setNumColumns(3);
        grid.setVerticalSpacing(Ui.dp(this, 5));
        grid.setHorizontalSpacing(Ui.dp(this, 3));
        grid.setPadding(Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 12));
        adapter = new CardAdapter(this, visibleItems, 174);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Hyper.Item it = visibleItems.get(pos);
                Intent i = new Intent(SearchActivity.this, DetailActivity.class);
                i.putExtra("type", it.type); i.putExtra("slug", it.slug);
                i.putExtra("title", it.title); i.putExtra("poster", it.poster);
                i.putExtra("score", it.score); i.putExtra("year", it.year);
                startActivity(i);
            }
        });
        root.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int action, KeyEvent e) {
                if (action == EditorInfo.IME_ACTION_SEARCH || (e != null && e.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    doSearch(input.getText().toString().trim()); return true;
                }
                return false;
            }
        });
        showSuggestions();
    }

    private void addFilter(LinearLayout row, String label, final String key) {
        final TextView chip = Ui.chip(this, label);
        chip.setTextColor("all".equals(key) ? Ui.ACCENT2 : Ui.TEXT);
        chip.setBackground(Ui.roundStroke(Ui.PANEL2, 18, "all".equals(key) ? Ui.ACCENT : 0x554B4355, this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 36));
        lp.setMargins(0, 0, Ui.dp(this, 8), 0);
        chip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activeType = key;
                refreshVisible();
                // update selected appearance
                LinearLayout parent = (LinearLayout) rowParent(v);
                for (int n=0; n<parent.getChildCount(); n++) {
                    TextView c = (TextView) parent.getChildAt(n);
                    boolean selected = c == v;
                    c.setTextColor(selected ? Ui.ACCENT2 : Ui.TEXT);
                    c.setBackground(Ui.roundStroke(Ui.PANEL2, 18, selected ? Ui.ACCENT : 0x554B4355, SearchActivity.this));
                }
            }
        });
        row.addView(chip, lp);
    }

    private ViewGroup rowParent(View v) { return (ViewGroup) v.getParent(); }

    private void doSearch(final String q) {
        if (q.isEmpty()) {
            status.setText("Escreve o nome de um filme, série ou anime.");
            showSuggestions();
            return;
        }
        lastQuery = q;
        status.setText("A procurar “" + q + "”…");
        suggestions.removeAllViews();
        Bg.run(new Bg.Work<List<Hyper.Item>>() {
            public List<Hyper.Item> run() throws Exception { return Hyper.search(q); }
        }, new Bg.Done<List<Hyper.Item>>() {
            public void ok(List<Hyper.Item> v) {
                allItems.clear(); allItems.addAll(v);
                saveRecent(q);
                refreshVisible();
                status.setText(visibleItems.isEmpty()
                        ? "Não encontrámos resultados para “" + q + "”."
                        : visibleItems.size() + " resultado(s) para “" + q + "”.");
            }
            public void fail(Exception e) { status.setText("Falha na ligação. Verifica a internet e tenta novamente."); }
        });
    }

    private void refreshVisible() {
        visibleItems.clear();
        for (Hyper.Item it : allItems) {
            if ("all".equals(activeType) || activeType.equals(it.type)) visibleItems.add(it);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        if (!allItems.isEmpty() && visibleItems.isEmpty()) status.setText("Não há resultados nesta categoria.");
    }

    private void showSuggestions() {
        suggestions.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("filmox_search", MODE_PRIVATE);
        String recent = prefs.getString("recent", "");
        if (!recent.isEmpty()) addSuggestionGroup("Pesquisas recentes", recent.split("\\n"));
        addSuggestionGroup("Sugestões de pesquisa", new String[]{"Ação", "Comédia", "Animação", "Aventura", "Drama"});
    }

    private void addSuggestionGroup(String title, String[] values) {
        if (values.length == 0) return;
        TextView h = Ui.tv(this, title, 14.5f, Ui.TEXT, true);
        h.setPadding(Ui.dp(this, 16), Ui.dp(this, 7), Ui.dp(this, 16), Ui.dp(this, 6));
        suggestions.addView(h);
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), Ui.dp(this, 8));
        for (final String value : values) {
            if (value.trim().isEmpty()) continue;
            TextView chip = Ui.chip(this, value);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, Ui.dp(this, 7), 0);
            chip.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { input.setText(value); input.setSelection(input.length()); doSearch(value); }
            });
            row.addView(chip, lp);
        }
        hs.addView(row);
        suggestions.addView(hs);
    }

    private void saveRecent(String q) {
        SharedPreferences p = getSharedPreferences("filmox_search", MODE_PRIVATE);
        String old = p.getString("recent", "");
        String[] parts = old.isEmpty() ? new String[0] : old.split("\\n");
        StringBuilder out = new StringBuilder(q);
        int added = 1;
        for (String x : parts) {
            if (!x.equalsIgnoreCase(q) && added < 6) { out.append("\n").append(x); added++; }
        }
        p.edit().putString("recent", out.toString()).apply();
    }
}
