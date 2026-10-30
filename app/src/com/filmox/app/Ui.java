package com.filmox.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Ui {
    public static final int BG = Color.rgb(11, 10, 16);
    public static final int PANEL = Color.rgb(25, 23, 32);
    public static final int PANEL2 = Color.rgb(37, 34, 46);
    public static final int ACCENT = Color.rgb(255, 121, 15);
    public static final int ACCENT2 = Color.rgb(255, 185, 112);
    public static final int TEXT = Color.WHITE;
    public static final int SUB = Color.rgb(154, 160, 174);

    public static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }
    public static ImageView icon(Context c, int resource, int tint) {
        ImageView image = new ImageView(c);
        image.setImageResource(resource);
        image.setColorFilter(tint);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return image;
    }
    public static TextView tv(Context c, String text, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(text); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }
    public static GradientDrawable round(int color, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(c, radiusDp));
        return g;
    }
    public static GradientDrawable roundStroke(int color, float radiusDp, int strokeColor, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(c, radiusDp));
        g.setStroke(dp(c, 1.5f), strokeColor);
        return g;
    }

    public static TextView bigButton(Context c, String text) {
        TextView t = tv(c, text, 15.5f, Color.WHITE, true);
        t.setBackground(round(ACCENT, 12, c));
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        return t;
    }
    public static TextView chip(Context c, String text) {
        TextView t = tv(c, text, 12, TEXT, false);
        t.setBackground(roundStroke(PANEL2, 14, 0x553F3949, c));
        t.setPadding(dp(c, 10), dp(c, 5), dp(c, 10), dp(c, 5));
        t.setGravity(Gravity.CENTER);
        return t;
    }
}
