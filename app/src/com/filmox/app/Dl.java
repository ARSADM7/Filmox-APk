package com.filmox.app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Downloads dentro da app (ficheiros guardados na pasta da app; ver offline). */
public class Dl {
    public static class Entry {
        public long id;
        public String title, poster, fileName, watchPath;
        public int status = -1;      // DownloadManager.STATUS_*
        public long doneBytes = 0, totalBytes = -1;
        public int percent() { return totalBytes > 0 ? (int) (100L * doneBytes / totalBytes) : 0; }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("filmox_dl", Context.MODE_PRIVATE);
    }
    public static File dir(Context c) {
        File d = new File(c.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "filmox");
        if (!d.exists()) d.mkdirs();
        return d;
    }
    public static List<Entry> list(Context c) {
        String all = prefs(c).getString("items", "");
        List<Entry> out = new ArrayList<>();
        if (all.isEmpty()) return out;
        for (String row : all.split("\u0002")) {
            String[] f = row.split("\u0001", -1);
            if (f.length < 5) continue;
            Entry e = new Entry();
            try { e.id = Long.parseLong(f[0]); } catch (Exception ignored) { continue; }
            e.title = f[1]; e.poster = f[2].isEmpty() ? null : f[2];
            e.fileName = f[3]; e.watchPath = f[4];
            out.add(e);
        }
        return out;
    }
    private static void write(Context c, List<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            if (sb.length() > 0) sb.append("\u0002");
            sb.append(e.id).append("\u0001").append(e.title).append("\u0001")
              .append(e.poster == null ? "" : e.poster).append("\u0001")
              .append(e.fileName).append("\u0001").append(e.watchPath);
        }
        prefs(c).edit().putString("items", sb.toString()).apply();
    }
    public static Entry find(Context c, String watchPath) {
        for (Entry e : list(c)) if (e.watchPath.equals(watchPath)) return e;
        return null;
    }
    /** Inicia o download do mp4 assinado. Devolve o id ou -1 em erro. */
    public static long start(Context c, String signedUrl, String title, String poster, String watchPath) {
        String fileName = watchPath.replaceAll("[^A-Za-z0-9._-]", "_") + ".mp4";
        try {
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(signedUrl));
            r.setTitle("Filmox — " + title);
            r.setDescription(watchPath);
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalFilesDir(c, Environment.DIRECTORY_MOVIES, "filmox/" + fileName);
            long id = dm.enqueue(r);
            Entry e = new Entry();
            e.id = id; e.title = title; e.poster = poster; e.fileName = fileName; e.watchPath = watchPath;
            List<Entry> next = new ArrayList<>();
            next.add(e);
            for (Entry x : list(c)) if (!x.watchPath.equals(watchPath)) next.add(x);
            write(c, next);
            return id;
        } catch (Exception ex) {
            return -1;
        }
    }
    public static void refresh(Context c, Entry e) {
        try {
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cur = dm.query(new DownloadManager.Query().setFilterById(e.id));
            if (cur != null && cur.moveToFirst()) {
                e.status = cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                e.doneBytes = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                e.totalBytes = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                cur.close();
            }
        } catch (Exception ignored) { }
    }
    public static void remove(Context c, Entry e) {
        try {
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            dm.remove(e.id);
        } catch (Exception ignored) { }
        try { new File(dir(c), e.fileName).delete(); } catch (Exception ignored) { }
        List<Entry> next = new ArrayList<>();
        for (Entry x : list(c)) if (x.id != e.id) next.add(x);
        write(c, next);
    }
    public static File file(Context c, Entry e) { return new File(dir(c), e.fileName); }
}
