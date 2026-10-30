package com.filmox.app;

import android.os.Handler;
import android.os.Looper;

public class Bg {
    public interface Work<T> { T run() throws Exception; }
    public interface Done<T> { void ok(T v); void fail(Exception e); }
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    public static <T> void run(final Work<T> w, final Done<T> d) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    final T v = w.run();
                    MAIN.post(new Runnable() { public void run() { d.ok(v); } });
                } catch (final Exception e) {
                    MAIN.post(new Runnable() { public void run() { d.fail(e); } });
                }
            }
        }).start();
    }
}
