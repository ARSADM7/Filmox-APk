package com.filmox.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

public class SplashActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Ui.BG);
        TextView logo = new TextView(this);
        logo.setText("FILMOX");
        logo.setTextSize(42); logo.setTextColor(Ui.ACCENT);
        logo.setLetterSpacing(0.16f);
        logo.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);
        View line = new View(this);
        line.setBackground(Ui.round(Ui.ACCENT, 2, this));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 3));
        lineParams.setMargins(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
        root.addView(line, lineParams);
        TextView sub = Ui.tv(this, "Filmes · Séries · Animes", 14, Ui.TEXT, true);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);
        TextView tagline = Ui.tv(this, "O teu próximo mundo começa aqui", 11.5f, Ui.SUB, false);
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, Ui.dp(this, 6), 0, 0);
        root.addView(tagline);
        setContentView(root);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1100);
    }
}
