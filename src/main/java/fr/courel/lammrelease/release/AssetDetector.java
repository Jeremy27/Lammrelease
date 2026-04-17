package fr.courel.lammrelease.release;

import fr.courel.lammrelease.model.LammProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Détecte les artifacts à uploader sur la release : .exe, .zip,
 * et le jar shadé (non "original-*" ni -sources/-javadoc).
 */
public final class AssetDetector {

    private AssetDetector() {}

    public static List<Path> detect(LammProject project) {
        var assets = new ArrayList<Path>();
        collect(project.targetDir(), assets);
        collect(project.directory().resolve("dist"), assets);
        return assets;
    }

    private static void collect(Path dir, List<Path> out) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).filter(AssetDetector::isAsset).forEach(out::add);
        } catch (IOException ignored) {
        }
    }

    private static boolean isAsset(Path path) {
        String name = path.getFileName().toString();
        if (name.startsWith("original-")) return false;
        if (name.endsWith("-sources.jar") || name.endsWith("-javadoc.jar")) return false;
        return name.endsWith(".exe") || name.endsWith(".zip") || name.endsWith(".jar");
    }
}
