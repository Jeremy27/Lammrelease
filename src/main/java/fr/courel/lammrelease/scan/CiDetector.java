package fr.courel.lammrelease.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Détecte si un projet a un workflow GitHub Actions déclenché sur tag push.
 * Si oui, on considère que la CI se charge de publier (release/package) et
 * on peut skipper l'étape GITHUB_RELEASE côté Lammrelease.
 */
public final class CiDetector {

    private static final Pattern TAG_TRIGGER = Pattern.compile(
            "on:\\s*[\\s\\S]*?push:\\s*[\\s\\S]*?tags:",
            Pattern.CASE_INSENSITIVE);

    private CiDetector() {}

    public static boolean hasTagTriggeredWorkflow(Path projectDir) {
        Path workflows = projectDir.resolve(".github/workflows");
        if (!Files.isDirectory(workflows)) return false;
        try (Stream<Path> files = Files.list(workflows)) {
            return files
                    .filter(p -> {
                        String n = p.getFileName().toString();
                        return n.endsWith(".yml") || n.endsWith(".yaml");
                    })
                    .anyMatch(CiDetector::triggersOnTag);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean triggersOnTag(Path file) {
        try {
            String content = Files.readString(file);
            return TAG_TRIGGER.matcher(content).find();
        } catch (IOException e) {
            return false;
        }
    }
}
