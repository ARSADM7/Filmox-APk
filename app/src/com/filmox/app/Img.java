package com.filmox.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Carregador de imagens simples com cache em memória. */
public class Img {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(48 * 1024) {
        protected int sizeOf(String k, Bitmap b) { return b.getByteCount() / 1024; }
    };
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void load(final ImageView iv, final String url) {
        if (url == null || url.isEmpty()) { iv.setImageBitmap(null); return; }
        iv.setTag(url);
        Bitmap hit = CACHE.get(url);
        if (hit != null) { iv.setImageBitmap(hit); return; }
        iv.setImageBitmap(null);
        POOL.execute(new Runnable() {
            public void run() {
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                    c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) Filmox/1.0");
                    c.setConnectTimeout(15000); c.setReadTimeout(20000);
                    InputStream in = c.getInputStream();
                    final Bitmap b = BitmapFactory.decodeStream(in);
                    in.close();
                    if (b == null) return;
                    CACHE.put(url, b);
                    MAIN.post(new Runnable() {
                        public void run() { if (url.equals(iv.getTag())) iv.setImageBitmap(b); }
                    });
                } catch (Exception ignored) { }
            }
        });
    }
}
