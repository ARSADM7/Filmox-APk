package com.filmox.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class CardAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<Hyper.Item> items;
    private final int posterH;
    public CardAdapter(Context ctx, List<Hyper.Item> items) { this(ctx, items, 168); }
    public CardAdapter(Context ctx, List<Hyper.Item> items, int posterHDp) {
        this.ctx = ctx; this.items = items; this.posterH = posterHDp;
    }
    public int getCount() { return items.size(); }
    public Object getItem(int i) { return items.get(i); }
    public long getItemId(int i) { return i; }
    public View getView(int i, View convert, ViewGroup parent) {
        Hyper.Item it = items.get(i);
        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setPadding(Ui.dp(ctx, 5), Ui.dp(ctx, 5), Ui.dp(ctx, 5), Ui.dp(ctx, 8));
        FrameLayout posterBox = new FrameLayout(ctx);
        RoundedImageView img = new RoundedImageView(ctx, 10);
        img.setBackgroundColor(Ui.PANEL);
        posterBox.addView(img, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(ctx, posterH)));
        if (it.score != null && !it.score.isEmpty()) {
            TextView badge = Ui.tv(ctx, "★ " + it.score, 11, Ui.ACCENT2, true);
            badge.setBackground(Ui.round(0xCC000000, 8, ctx));
            badge.setPadding(Ui.dp(ctx, 6), Ui.dp(ctx, 2), Ui.dp(ctx, 6), Ui.dp(ctx, 2));
            FrameLayout.LayoutParams bl = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bl.setMargins(Ui.dp(ctx, 5), Ui.dp(ctx, 5), 0, 0);
            posterBox.addView(badge, bl);
        }
        cell.addView(posterBox);
        TextView t = Ui.tv(ctx, it.title, 12.5f, Ui.TEXT, false);
        t.setMaxLines(2);
        t.setPadding(0, Ui.dp(ctx, 5), 0, 0);
        cell.addView(t);
        if (it.year != null && !it.year.isEmpty()) {
            cell.addView(Ui.tv(ctx, it.year, 11, Ui.SUB, false));
        }
        Img.load(img, it.poster);
        return cell;
    }
}
