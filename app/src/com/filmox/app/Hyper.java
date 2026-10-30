package com.filmox.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Cliente + parser do site Hyper (fonte de conteúdo). Java puro: corre no Android e no JVM de teste. */
public class Hyper {
    public static final String BASE = "https://hyper.hyperapps.site";

    public static class Item {
        public String type, slug, title, poster, score, year, path;
        public String backdrop() { return poster == null ? null : poster.replace("/w500/", "/w1280/"); }
    }
    public static class Episode {
        public int season, number;
        public String path, downloadId;
    }
    public static class Detail {
        public String title, desc, image, type, slug;
        public String year = "", rating = "", certification = "";
        public int runtimeMin = 0;
        public String overview = "", trailer = "";
        public List<String[]> castMembers = new ArrayList<>(); // nome, personagem, foto
        public List<String> cast = new ArrayList<>();
        public List<String> genres = new ArrayList<>();
        public List<String> genreSlugs = new ArrayList<>();
        public List<Episode> episodes = new ArrayList<>();
        public boolean isSeries() { return !episodes.isEmpty(); }
    }
    public static class Watch {
        public String mp4, audio, quality; // audio: Dublado/Legendado; quality: 1080p/720p
    }

    public static String fetch(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) Filmox/1.0");
        c.setConnectTimeout(20000); c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);
        BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192]; int n;
        while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
        r.close();
        return sb.toString();
    }

    private static final Pattern CARD = Pattern.compile(
        "<a class=\"card\" href=\"/(filmes|series|animes|doramas|novelas)/([^\"]+)\"[^>]*>(.*?)</a>", Pattern.DOTALL);
    private static final Pattern IMG = Pattern.compile("src=\"(https://image\\.tmdb\\.org[^\"]+)\"");
    private static final Pattern TITLE = Pattern.compile("<span class=\"title\">(.*?)<span class=\"year\">", Pattern.DOTALL);
    private static final Pattern YEAR = Pattern.compile("<span class=\"year\">(\\d{4})</span>");
    private static final Pattern SCORE = Pattern.compile("★\\s*<!--\\s*-->\\s*([0-9.]+)|★\\s*([0-9.]+)");

    public static List<Item> parseCards(String html) {
        Map<String, Item> map = new LinkedHashMap<>();
        Matcher m = CARD.matcher(html);
        while (m.find()) {
            String path = "/" + m.group(1) + "/" + m.group(2);
            if (map.containsKey(path)) continue;
            String block = m.group(3);
            Item it = new Item();
            it.type = m.group(1); it.slug = m.group(2); it.path = path;
            Matcher im = IMG.matcher(block); it.poster = im.find() ? im.group(1) : null;
            Matcher ti = TITLE.matcher(block);
            it.title = ti.find() ? clean(ti.group(1)) : titleFromSlug(it.slug);
            Matcher ye = YEAR.matcher(block); it.year = ye.find() ? ye.group(1) : "";
            Matcher sc = SCORE.matcher(block);
            it.score = sc.find() ? (sc.group(1) != null ? sc.group(1) : sc.group(2)) : "";
            map.put(path, it);
        }
        return new ArrayList<>(map.values());
    }

    public static List<Item> home() throws Exception { return parseCards(fetch(BASE + "/")); }
    public static List<Item> section(String type, int page) throws Exception {
        return parseCards(fetch(BASE + "/" + type + "?page=" + page));
    }
    public static List<Item> lancamentos(int page) throws Exception {
        return parseCards(fetch(BASE + "/lancamentos?page=" + page));
    }
    public static List<Item> genre(String slug, int page) throws Exception {
        return parseCards(fetch(BASE + "/genero/" + slug + "?page=" + page));
    }
    public static List<Item> search(String q) throws Exception {
        return parseCards(fetch(BASE + "/busca?q=" + URLEncoder.encode(q, "UTF-8")));
    }

    public static List<String[]> genres() throws Exception {
        String html = fetch(BASE + "/generos");
        List<String[]> out = new ArrayList<>();
        Matcher m = Pattern.compile("href=\"/genero/([^\"]+)\"[^>]*>([^<]{2,40})<").matcher(html);
        Map<String, Boolean> seen = new LinkedHashMap<>();
        while (m.find()) {
            if (seen.containsKey(m.group(1))) continue;
            seen.put(m.group(1), Boolean.TRUE);
            out.add(new String[]{m.group(1), clean(m.group(2))});
        }
        if (out.isEmpty()) { // nomes a partir do slug
            Matcher m2 = Pattern.compile("href=\"/genero/([^\"]+)\"").matcher(html);
            while (m2.find()) {
                if (seen.containsKey(m2.group(1))) continue;
                seen.put(m2.group(1), Boolean.TRUE);
                out.add(new String[]{m2.group(1), titleFromSlug(m2.group(1))});
            }
        }
        return out;
    }

    private static String og(String html, String prop) {
        Matcher m = Pattern.compile("<meta property=\"og:" + prop + "\" content=\"([^\"]*)\"").matcher(html);
        return m.find() ? decode(m.group(1)) : "";
    }

    public static Detail detail(String type, String slug) throws Exception {
        return parseDetail(fetch(BASE + "/" + type + "/" + slug), type, slug);
    }
    public static Detail parseDetail(String htmlRaw, String type, String slug) {
        String html = unescapeRsc(htmlRaw);
        Detail d = new Detail();
        d.type = type; d.slug = slug;
        d.title = og(html, "title");
        d.desc = og(html, "description");
        d.image = og(html, "image");
        Matcher ym = Pattern.compile("\"year\":(\\d{4})").matcher(html);
        if (ym.find()) d.year = ym.group(1);
        Matcher rm = Pattern.compile("\"rating\":([0-9]+(?:\\.[0-9]+)?)").matcher(html);
        if (rm.find() && !"0".equals(rm.group(1)) && !"0.0".equals(rm.group(1))) d.rating = rm.group(1);
        Matcher tm = Pattern.compile("\"runtime\":(\\d+)").matcher(html);
        if (tm.find()) { try { d.runtimeMin = Integer.parseInt(tm.group(1)); } catch (Exception ignored) { } }
        Matcher km = Pattern.compile("\"certification\":\"([^\"]*)\"").matcher(html);
        if (km.find()) d.certification = km.group(1);
        Matcher ov = Pattern.compile("\"overview\":\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(html);
        if (ov.find()) d.overview = ov.group(1).replace("\\n", " ").replace("\\\"", "\"").trim();
        Matcher trm = Pattern.compile("\"trailer\":\"([A-Za-z0-9_-]{6,})\"").matcher(html);
        if (trm.find()) d.trailer = trm.group(1);
        Matcher cmm = Pattern.compile("\\{\"name\":\"((?:[^\"\\\\]|\\\\.)*)\",\"slug\":\"[^\"]*\",\"character\":(?:\"((?:[^\"\\\\]|\\\\.)*)\"|null),\"photo\":(?:\"([^\"]*)\"|null)").matcher(html);
        while (cmm.find() && d.castMembers.size() < 20) {
            String ch = cmm.group(2) == null ? "" : cmm.group(2);
            String ph = cmm.group(3) == null ? "" : cmm.group(3);
            d.castMembers.add(new String[]{ cmm.group(1), ch, ph });
        }
        Matcher gj = Pattern.compile("\"genres\":\\[(.*?)\\]").matcher(html);
        if (gj.find()) {
            Matcher one = Pattern.compile("\\{\"name\":\"([^\"]+)\",\"slug\":\"([^\"]+)\"\\}").matcher(gj.group(1));
            List<String> gn = new ArrayList<>(); List<String> gs = new ArrayList<>();
            while (one.find()) { gn.add(one.group(1)); gs.add(one.group(2)); }
            if (!gn.isEmpty()) { d.genres = gn; d.genreSlugs = gs; }
        }
        Matcher cm = Pattern.compile("href=\"/pessoa/([^\"]+)\"").matcher(html);
        Map<String, Boolean> seenC = new LinkedHashMap<>();
        while (cm.find()) {
            if (!seenC.containsKey(cm.group(1))) { seenC.put(cm.group(1), Boolean.TRUE); d.cast.add(titleFromSlug(cm.group(1))); }
        }
        Matcher gm = Pattern.compile("href=\"/genero/([^\"]+)\"").matcher(html);
        Map<String, Boolean> seenG = new LinkedHashMap<>();
        while (gm.find()) {
            if (!seenG.containsKey(gm.group(1)) && !d.genreSlugs.contains(gm.group(1))) { seenG.put(gm.group(1), Boolean.TRUE); d.genres.add(titleFromSlug(gm.group(1))); d.genreSlugs.add(gm.group(1)); }
        }
        // episódios + ids de download
        Map<String, String> dl = new LinkedHashMap<>();
        Matcher dm = Pattern.compile("/api/download/episode/(\\d+)").matcher(html);
        List<String> dlIds = new ArrayList<>();
        while (dm.find()) dlIds.add(dm.group(1));
        Matcher em = Pattern.compile("href=\"/(?:series|animes|doramas|novelas)/[^\\\"]*/temporada/(\\d+)/episodio/(\\d+)\"").matcher(html);
        Map<String, Episode> eps = new LinkedHashMap<>();
        int idx = 0;
        while (em.find()) {
            String key = em.group(1) + "x" + em.group(2);
            if (eps.containsKey(key)) continue;
            Episode e = new Episode();
            e.season = Integer.parseInt(em.group(1)); e.number = Integer.parseInt(em.group(2));
            e.path = "/" + type + "/" + slug + "/temporada/" + e.season + "/episodio/" + e.number;
            e.downloadId = idx < dlIds.size() ? dlIds.get(idx) : null;
            idx++;
            eps.put(key, e);
        }
        d.episodes.addAll(eps.values());
        return d;
    }

    private static final Pattern MP4 = Pattern.compile("(https?://[^\"'\\<> ]+?\\.mp4[^\"'\\<> ]*)");
    private static final Pattern SRC_URL = Pattern.compile("\"url\":\"(https?://[^\"]+?\\.mp4[^\"]*)\"");
    private static final Pattern SRC_QUAL = Pattern.compile("\"quality\":\"([^\"]*)\"");
    private static final Pattern SRC_AUDIO = Pattern.compile("\"audio_label\":\"([^\"]*)\"");

    /** As páginas RSC escapam & como \u0026 e as aspas com barra — sem desescapar, o URL fica truncado e sem assinatura. */
    static String unescapeRsc(String h) {
        return h.replace("\\\\\"", "\"").replace("\\\"", "\"").replace("\\\\u0026", "&")
                .replace("\\u0026", "&").replace("\u0026", "&").replace("\\/", "/");
    }

    public static Watch watch(String watchPath) throws Exception {
        return parseWatch(fetch(BASE + watchPath));
    }
    public static Watch parseWatch(String htmlRaw) {
        String html = unescapeRsc(htmlRaw);
        Watch w = new Watch();
        // 1) Fonte oficial: array "sources" com URL ASSINADO (válido ~1h)
        int si = html.indexOf("\"sources\":[");
        if (si >= 0) {
            int end = html.indexOf("],\"title\"", si);
            String block = end > si ? html.substring(si, end) : html.substring(si, Math.min(html.length(), si + 6000));
            Matcher um = SRC_URL.matcher(block);
            String first = null;
            while (um.find()) {
                String u = um.group(1);
                if (first == null) first = u;
                if (u.contains("X-Amz-Signature")) { w.mp4 = u; break; }
            }
            if (w.mp4 == null) w.mp4 = first;
            Matcher qm = SRC_QUAL.matcher(block);
            if (qm.find()) w.quality = qm.group(1);
            Matcher am = SRC_AUDIO.matcher(block);
            if (am.find()) w.audio = am.group(1);
        }
        // 2) Fallback: qualquer .mp4 no HTML já desescapado, preferindo assinado
        if (w.mp4 == null) {
            Matcher m = MP4.matcher(html);
            String best = null;
            while (m.find()) {
                String u = m.group(1).replace("&amp;", "&");
                if (best == null) best = u;
                if (u.contains("X-Amz-Signature")) { best = u; break; }
            }
            w.mp4 = best;
        }
        if (w.audio == null || w.audio.isEmpty()) {
            w.audio = html.contains("Dublado") ? "Dublado" : (html.contains("Legendado") ? "Legendado" : "");
        }
        return w;
    }

    public static String movieWatchPath(String slug) { return "/filmes/" + slug + "/assistir"; }

    public static String titleFromSlug(String slug) {
        String[] parts = slug.split("-");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        return sb.toString();
    }

    static String clean(String s) {
        return decode(s.replaceAll("<[^>]+>", "")).trim();
    }
    static String decode(String s) {
        return s.replace("&amp;", "&").replace("&quot;", "\"").replace("&#x27;", "'")
                .replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&#39;", "'").replace("&nbsp;", " ");
    }
}
