package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity {
    private final List<Hyper.Item> items = new ArrayList<>();
    private CardAdapter adapter;
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 8));
        final EditText input = new EditText(this);
        input.setHint("Procurar filmes, séries, animes…");
        input.setHintTextColor(Ui.SUB);
        input.setTextColor(Ui.TEXT);
        input.setTextSize(15.5f);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setBackground(Ui.round(Ui.PANEL2, 22, this));
        input.setPadding(Ui.dp(this, 16), Ui.dp(this, 9), Ui.dp(this, 16), Ui.dp(this, 9));
        bar.addView(input, new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button go = new Button(this);
        go.setText("Buscar");
        go.setTextColor(Ui.TEXT);
        go.setBackground(Ui.round(Ui.ACCENT, 22, this));
        LinearLayout.LayoutParams gl = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        gl.setMargins(Ui.dp(this, 8), 0, 0, 0);
        bar.addView(go, gl);
        root.addView(bar);
        status = Ui.tv(this, "Escreve o nome e toca em Buscar.", 14, Ui.SUB, false);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, Ui.dp(this, 14), 0, 0);
        root.addView(status);
        GridView grid = new GridView(this);
        grid.setNumColumns(3);
        grid.setVerticalSpacing(Ui.dp(this, 4));
        grid.setHorizontalSpacing(Ui.dp(this, 4));
        grid.setPadding(Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 8));
        adapter = new CardAdapter(this, items);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Hyper.Item it = items.get(pos);
                Intent i = new Intent(SearchActivity.this, DetailActivity.class);
                i.putExtra("type", it.type); i.putExtra("slug", it.slug);
                i.putExtra("title", it.title); i.putExtra("poster", it.poster);
                i.putExtra("score", it.score); i.putExtra("year", it.year);
                startActivity(i);
            }
        });
        root.addView(grid, new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
        View.OnClickListener run = new View.OnClickListener() {
            public void onClick(View v) { doSearch(input.getText().toString().trim()); }
        };
        go.setOnClickListener(run);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int action, KeyEvent e) {
                if (action == EditorInfo.IME_ACTION_SEARCH) { doSearch(input.getText().toString().trim()); return true; }
                return false;
            }
        });
        input.requestFocus();
    }

    private void doSearch(final String q) {
        if (q.isEmpty()) return;
        status.setText("A procurar “" + q + "”…");
        Bg.run(new Bg.Work<List<Hyper.Item>>() {
            public List<Hyper.Item> run() throws Exception { return Hyper.search(q); }
        }, new Bg.Done<List<Hyper.Item>>() {
            public void ok(List<Hyper.Item> v) {
                items.clear(); items.addAll(v); adapter.notifyDataSetChanged();
                status.setText(v.isEmpty() ? "Nada encontrado para “" + q + "”." : v.size() + " resultado(s)");
            }
            public void fail(Exception e) { status.setText("Erro de ligação. Tenta outra vez."); }
        });
    }
}
