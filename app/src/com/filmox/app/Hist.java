package com.filmox.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

/** Histórico de visualização: guarda posição para "Continuar a assistir". */
public class Hist {
    public static class Entry {
        public String watchPath, title, poster, type, slug, label;
        public long posMs, durMs;
        public int percent() { return durMs > 0 ? (int) (100L * posMs / durMs) : 0; }
        public boolean finished() { return durMs > 0 && posMs >= durMs * 95 / 100; }
    }
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("filmox_hist", Context.MODE_PRIVATE);
    }
    public static List<Entry> list(Context c) {
        String all = prefs(c).getString("items", "");
        List<Entry> out = new ArrayList<>();
        if (all.isEmpty()) return out;
        for (String row : all.split("\u0002")) {
            String[] f = row.split("\u0001", -1);
            if (f.length < 8) continue;
            Entry e = new Entry();
            e.watchPath = f[0]; e.title = f[1]; e.poster = f[2].isEmpty() ? null : f[2];
            e.type = f[3]; e.slug = f[4]; e.label = f[5];
            try { e.posMs = Long.parseLong(f[6]); e.durMs = Long.parseLong(f[7]); } catch (Exception ignored) { }
            out.add(e);
        }
        return out;
    }
    public static Entry get(Context c, String watchPath) {
        for (Entry e : list(c)) if (e.watchPath.equals(watchPath)) return e;
        return null;
    }
    public static void save(Context c, Entry e) {
        if (e.finished()) { remove(c, e.watchPath); return; }
        List<Entry> cur = list(c);
        List<Entry> next = new ArrayList<>();
        next.add(e);
        for (Entry x : cur) if (!x.watchPath.equals(e.watchPath)) next.add(x);
        if (next.size() > 20) next = next.subList(0, 20);
        write(c, next);
    }
    public static void remove(Context c, String watchPath) {
        List<Entry> next = new ArrayList<>();
        for (Entry x : list(c)) if (!x.watchPath.equals(watchPath)) next.add(x);
        write(c, next);
    }
    private static void write(Context c, List<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            if (sb.length() > 0) sb.append("\u0002");
            sb.append(e.watchPath).append("\u0001").append(e.title).append("\u0001")
              .append(e.poster == null ? "" : e.poster).append("\u0001")
              .append(e.type).append("\u0001").append(e.slug).append("\u0001")
              .append(e.label).append("\u0001").append(e.posMs).append("\u0001").append(e.durMs);
        }
        prefs(c).edit().putString("items", sb.toString()).apply();
    }
}
