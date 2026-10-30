package com.filmox.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Fav {
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("filmox_favs", Context.MODE_PRIVATE);
    }
    public static List<Hyper.Item> list(Context c) {
        // ordem de inserção guardada numa única string para ser determinística
        String all = prefs(c).getString("items", "");
        List<Hyper.Item> out = new ArrayList<>();
        if (all.isEmpty()) return out;
        for (String row : all.split("\u0002")) {
            String[] f = row.split("\u0001", -1);
            if (f.length < 5) continue;
            Hyper.Item it = new Hyper.Item();
            it.path = f[0]; it.title = f[1]; it.poster = f[2].isEmpty() ? null : f[2];
            it.type = f[3]; it.slug = f[4]; it.score = ""; it.year = "";
            out.add(it);
        }
        return out;
    }
    public static void toggleSimple(Context c, Hyper.Item it) {
        List<Hyper.Item> cur = list(c);
        boolean found = false;
        List<Hyper.Item> next = new ArrayList<>();
        for (Hyper.Item i : cur) { if (i.path.equals(it.path)) { found = true; } else next.add(i); }
        if (!found) next.add(0, it);
        StringBuilder sb = new StringBuilder();
        for (Hyper.Item i : next) {
            if (sb.length() > 0) sb.append("\u0002");
            sb.append(i.path).append("\u0001").append(i.title).append("\u0001")
              .append(i.poster == null ? "" : i.poster).append("\u0001")
              .append(i.type).append("\u0001").append(i.slug);
        }
        prefs(c).edit().putString("items", sb.toString()).apply();
    }
    public static boolean hasItem(Context c, String path) {
        for (Hyper.Item i : list(c)) if (i.path.equals(path)) return true;
        return false;
    }
}
