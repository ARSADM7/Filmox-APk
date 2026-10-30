package com.filmox.app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Downloads offline organizados no armazenamento partilhado quando o Android permite. */
public class Dl {
    public static class Entry {
        public long id;
        public String title, poster, fileName, watchPath;
        public int status = -1;
        public long doneBytes = 0, totalBytes = -1;
        public int percent() { return totalBytes > 0 ? (int) (100L * doneBytes / totalBytes) : 0; }
        public boolean isSeries() { return fileName != null && fileName.startsWith("Séries/"); }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("filmox_dl", Context.MODE_PRIVATE);
    }

    /** Raiz dos downloads. Android 10+: /Download/FILMOX. Android antigo: pasta privada segura. */
    public static File dir(Context c) {
        File d;
        if (Build.VERSION.SDK_INT >= 29) {
            d = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FILMOX");
        } else {
            d = new File(c.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "FILMOX");
        }
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private static String safe(String value) {
        if (value == null) return "Sem título";
        String s = value.replaceAll("[\\/:*?\"<>|]", "_").replaceAll("\\s+", " ").trim();
        return s.length() > 90 ? s.substring(0, 90).trim() : (s.isEmpty() ? "Sem título" : s);
    }

    private static String relativePath(String title, String watchPath) {
        Matcher m = Pattern.compile("(?i)\\sT(\\d+)E(\\d+)").matcher(title == null ? "" : title);
        if (m.find()) {
            int season = Integer.parseInt(m.group(1));
            int episode = Integer.parseInt(m.group(2));
            String series = safe(title.substring(0, m.start()).trim());
            return "Séries/" + series + "/Temporada " + season + "/E" + String.format(java.util.Locale.US, "%02d", episode)
                    + " - " + safe(title.substring(m.end()).trim().isEmpty() ? "Episódio" : title.substring(m.end()).trim())
                    + " - " + Integer.toHexString(watchPath.hashCode()) + ".mp4";
        }
        return "Filmes/" + safe(title) + " - " + Integer.toHexString((watchPath == null ? title : watchPath).hashCode()) + ".mp4";
    }

    public static List<Entry> list(Context c) {
        String all = prefs(c).getString("items", "");
        List<Entry> out = new ArrayList<>();
        if (all.isEmpty()) return out;
        for (String row : all.split("\\u0002")) {
            String[] f = row.split("\\u0001", -1);
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
            if (sb.length() > 0) sb.append("\\u0002");
            sb.append(e.id).append("\\u0001").append(e.title).append("\\u0001")
              .append(e.poster == null ? "" : e.poster).append("\\u0001")
              .append(e.fileName).append("\\u0001").append(e.watchPath);
        }
        prefs(c).edit().putString("items", sb.toString()).apply();
    }

    public static Entry find(Context c, String watchPath) {
        for (Entry e : list(c)) if (e.watchPath.equals(watchPath)) return e;
        return null;
    }

    /** Inicia o download e guarda-o em Download/FILMOX/Filmes ou Series. */
    public static long start(Context c, String signedUrl, String title, String poster, String watchPath) {
        String relative = relativePath(title, watchPath);
        try {
            if (Build.VERSION.SDK_INT < 29) {
                File destination = new File(dir(c), relative);
                File parent = destination.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) return -1;
            }
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) return -1;
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(signedUrl));
            r.setTitle("FILMOX — " + title);
            r.setDescription("FILMOX / " + relative.replace('/', ' '));
            r.setMimeType("video/mp4");
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            if (Build.VERSION.SDK_INT >= 29) {
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FILMOX/" + relative);
            } else {
                r.setDestinationInExternalFilesDir(c, Environment.DIRECTORY_MOVIES, "FILMOX/" + relative);
            }
            long id = dm.enqueue(r);
            Entry e = new Entry();
            e.id = id; e.title = title; e.poster = poster; e.fileName = relative; e.watchPath = watchPath;
            List<Entry> next = new ArrayList<>();
            next.add(e);
            for (Entry x : list(c)) if (!x.watchPath.equals(watchPath)) next.add(x);
            write(c, next);
            return id;
        } catch (Exception ex) { return -1; }
    }

    public static void refresh(Context c, Entry e) {
        Cursor cur = null;
        try {
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) return;
            cur = dm.query(new DownloadManager.Query().setFilterById(e.id));
            if (cur != null && cur.moveToFirst()) {
                e.status = cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                e.doneBytes = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                e.totalBytes = cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            }
        } catch (Exception ignored) { } finally { if (cur != null) cur.close(); }
    }

    public static void remove(Context c, Entry e) {
        try {
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) dm.remove(e.id);
        } catch (Exception ignored) { }
        try { File f = file(c, e); if (f.exists()) f.delete(); } catch (Exception ignored) { }
        try { File old = new File(new File(c.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "filmox"), e.fileName); if (old.exists()) old.delete(); } catch (Exception ignored) { }
        List<Entry> next = new ArrayList<>();
        for (Entry x : list(c)) if (x.id != e.id) next.add(x);
        write(c, next);
    }

    /** URI devolvido pelo DownloadManager: funciona também com o armazenamento protegido do Android 10+. */
    public static String downloadUri(Context c, Entry e) {
        try {
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = dm == null ? null : dm.getUriForDownloadedFile(e.id);
            if (uri != null) return uri.toString();
        } catch (Exception ignored) { }
        File old = new File(new File(c.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "filmox"), e.fileName);
        return old.exists() ? Uri.fromFile(old).toString() : null;
    }

    public static File file(Context c, Entry e) {
        // Compatibilidade com downloads da versão 1.3 já existentes no telemóvel.
        File legacy = new File(new File(c.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "filmox"), e.fileName);
        return legacy.exists() ? legacy : new File(dir(c), e.fileName);
    }
}
