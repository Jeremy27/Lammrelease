package fr.courel.lammrelease.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lit les balises racine d'un pom.xml via regex.
 * Suffisant pour les projets Lamm (pas d'héritage de parent pom).
 */
public final class PomReader {

    private PomReader() {}

    public record PomCoords(String groupId, String artifactId, String version) {}

    public static PomCoords read(Path pom) throws IOException {
        String xml = Files.readString(pom);
        String body = stripBuildSection(xml);
        return new PomCoords(
                extract("groupId", body),
                extract("artifactId", body),
                extract("version", body)
        );
    }

    public static void writeVersion(Path pom, String newVersion) throws IOException {
        String xml = Files.readString(pom);
        String rootVersionPattern =
                "(<project[^>]*>[\\s\\S]*?<artifactId>[^<]+</artifactId>\\s*)<version>[^<]+</version>";
        Matcher m = Pattern.compile(rootVersionPattern).matcher(xml);
        if (!m.find()) {
            throw new IOException("Balise <version> racine introuvable dans " + pom);
        }
        String updated = m.replaceFirst(Matcher.quoteReplacement(m.group(1) + "<version>" + newVersion + "</version>"));
        Files.writeString(pom, updated);
    }

    private static String stripBuildSection(String xml) {
        return xml.replaceAll("(?s)<build>.*?</build>", "")
                  .replaceAll("(?s)<dependencies>.*?</dependencies>", "")
                  .replaceAll("(?s)<properties>.*?</properties>", "");
    }

    private static String extract(String tag, String body) {
        Matcher m = Pattern.compile("<" + tag + ">([^<]+)</" + tag + ">").matcher(body);
        return m.find() ? m.group(1).trim() : null;
    }
}
