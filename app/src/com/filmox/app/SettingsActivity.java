package com.filmox.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Definições simples e funcionais. */
public class SettingsActivity extends Activity {
    public static boolean wifiOnly(android.content.Context c) {
        return c.getSharedPreferences("filmox_prefs", MODE_PRIVATE).getBoolean("wifi_only", false);
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final SharedPreferences prefs = getSharedPreferences("filmox_prefs", MODE_PRIVATE);
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
        top.addView(Ui.tv(this, "Definições", 19, Ui.TEXT, true));
        root.addView(top);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.dp(this, 16), Ui.dp(this, 8), Ui.dp(this, 16), Ui.dp(this, 16));

        // Wi-Fi apenas
        LinearLayout r1 = new LinearLayout(this);
        r1.setGravity(Gravity.CENTER_VERTICAL);
        r1.setBackground(Ui.round(Ui.PANEL, 12, this));
        r1.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        LinearLayout c1 = new LinearLayout(this);
        c1.setOrientation(LinearLayout.VERTICAL);
        c1.addView(Ui.tv(this, "Downloads só por Wi-Fi", 15.5f, Ui.TEXT, true));
        c1.addView(Ui.tv(this, "Poupa dados móveis (Movitel/Vodacom)", 12.5f, Ui.SUB, false));
        r1.addView(c1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Switch sw = new Switch(this);
        sw.setChecked(prefs.getBoolean("wifi_only", false));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean on) {
                prefs.edit().putBoolean("wifi_only", on).apply();
            }
        });
        r1.addView(sw);
        col.addView(r1);

        col.addView(actionRow("Limpar histórico (Continuar a assistir)", new View.OnClickListener() {
            public void onClick(View v) {
                confirm("Limpar histórico?", new Runnable() {
                    public void run() {
                        getSharedPreferences("filmox_hist", MODE_PRIVATE).edit().clear().apply();
                        Toast.makeText(SettingsActivity.this, "Histórico limpo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }));
        col.addView(actionRow("Limpar favoritos", new View.OnClickListener() {
            public void onClick(View v) {
                confirm("Limpar favoritos?", new Runnable() {
                    public void run() {
                        getSharedPreferences("filmox_favs", MODE_PRIVATE).edit().clear().apply();
                        Toast.makeText(SettingsActivity.this, "Favoritos limpos", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }));

        TextView about = Ui.tv(this, "Filmox 1.3\nCatálogo e vídeos: hyper.hyperapps.site\nOs downloads ficam em Download/FILMOX no Android 10 ou superior. Em versões antigas ficam na pasta privada da app.", 12.5f, Ui.SUB, false);
        about.setPadding(0, Ui.dp(this, 22), 0, 0);
        col.addView(about);
        root.addView(col);
        setContentView(root);
    }

    private View actionRow(String label, View.OnClickListener click) {
        TextView t = Ui.tv(this, label, 15.5f, Ui.TEXT, false);
        t.setBackground(Ui.round(Ui.PANEL, 12, this));
        t.setPadding(Ui.dp(this, 14), Ui.dp(this, 13), Ui.dp(this, 14), Ui.dp(this, 13));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, Ui.dp(this, 10), 0, 0);
        t.setLayoutParams(lp);
        t.setOnClickListener(click);
        return t;
    }

    private void confirm(String title, final Runnable yes) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) { yes.run(); }
                }).show();
    }
}
