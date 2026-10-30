import com.filmox.app.Hyper;
import java.nio.file.*;
import java.util.List;
public class TestMain {
    static String read(String f) throws Exception { return new String(Files.readAllBytes(Paths.get("samples/" + f)), "UTF-8"); }
    public static void main(String[] a) throws Exception {
        List<Hyper.Item> items = Hyper.parseCards(read("home.html"));
        System.out.println("HOME cards: " + items.size());
        System.out.println("FILMES cards: " + Hyper.parseCards(read("filmes.html")).size());
        System.out.println("LANCAMENTOS cards: " + Hyper.parseCards(read("lancamentos.html")).size());
        System.out.println("BUSCA cards: " + Hyper.parseCards(read("busca.html")).size());
        Hyper.Detail d = Hyper.parseDetail(read("series_visionquest.html"), "series", "visionquest");
        System.out.println("DETALHE serie: '" + d.title + "' | elenco=" + d.cast.size() + " (1o: " + (d.cast.isEmpty()?"-":d.cast.get(0)) + ") | generos=" + d.genres.size() + " | episodios=" + d.episodes.size());
        if (!d.episodes.isEmpty()) {
            Hyper.Episode e = d.episodes.get(0);
            System.out.println("  ep1: T" + e.season + "E" + e.number + " path=" + e.path + " dlId=" + e.downloadId);
        }
        System.out.println("META serie: ano=" + d.year + " nota=" + d.rating + " cert=" + d.certification + " runtime=" + d.runtimeMin);
        System.out.println("SINOPSE: " + d.desc.substring(0, Math.min(80, d.desc.length())) + "...");
        Hyper.Detail f = Hyper.parseDetail(read("filmes_noite-brutal.html"), "filmes", "noite-brutal");
        System.out.println("META filme: ano=" + f.year + " nota=" + f.rating + " cert=" + f.certification + " runtime=" + f.runtimeMin);
        System.out.println("DETALHE filme: '" + f.title + "' | elenco=" + f.cast.size() + " | episodios=" + f.episodes.size() + " (esperado 0)");
        Hyper.Watch w1 = Hyper.parseWatch(read("assistir_filme.html"));
        System.out.println("WATCH filme: assinado=" + (w1.mp4 != null && w1.mp4.contains("X-Amz-Signature")) + " len=" + (w1.mp4==null?0:w1.mp4.length()) + " | audio=" + w1.audio + " | qualidade=" + w1.quality);
        Hyper.Watch w2 = Hyper.parseWatch(read("episodio1.html"));
        System.out.println("WATCH episodio: assinado=" + (w2.mp4 != null && w2.mp4.contains("X-Amz-Signature")) + " len=" + (w2.mp4==null?0:w2.mp4.length()) + " | audio=" + w2.audio + " | qualidade=" + w2.quality);
        String g = read("generos.html");
        java.util.Set<String> gs = new java.util.TreeSet<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("href=\"/genero/([^\"]+)\"").matcher(g);
        while (m.find()) gs.add(m.group(1));
        System.out.println("ELENCO completo filme: " + f.castMembers.size() + (f.castMembers.isEmpty() ? "" : " | 1o=" + f.castMembers.get(0)[0] + " como " + f.castMembers.get(0)[1] + " foto=" + !f.castMembers.get(0)[2].isEmpty()) + " | trailer=" + f.trailer + " | overviewLen=" + f.overview.length() + " | generos=" + f.genres);
        System.out.println("GENEROS no HTML: " + gs.size());
    }
}
