package fr.courel.lammrelease.scan;

import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.model.SemVer;
import fr.courel.lammrelease.process.GitClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scanne un dossier parent et détecte les projets Lamm.
 * Critères : pom.xml présent, groupId = fr.courel, artifactId préfixé par "lamm".
 */
public final class ProjectScanner {

    public List<LammProject> scan(Path workdir) {
        if (!Files.isDirectory(workdir)) return List.of();
        var results = new ArrayList<LammProject>();
        try (var stream = Files.list(workdir)) {
            stream.filter(Files::isDirectory).forEach(dir -> tryLoad(dir).ifPresent(results::add));
        } catch (IOException e) {
            return List.of();
        }
        results.sort(Comparator.comparing(LammProject::name));
        return results;
    }

    private java.util.Optional<LammProject> tryLoad(Path dir) {
        Path pom = dir.resolve("pom.xml");
        if (!Files.isRegularFile(pom)) return java.util.Optional.empty();
        try {
            var coords = PomReader.read(pom);
            if (coords.groupId() == null || coords.artifactId() == null || coords.version() == null) {
                return java.util.Optional.empty();
            }
            if (!"fr.courel".equals(coords.groupId()) || !coords.artifactId().startsWith("lamm")) {
                return java.util.Optional.empty();
            }
            SemVer version;
            try {
                version = SemVer.parse(coords.version());
            } catch (IllegalArgumentException e) {
                return java.util.Optional.empty();
            }
            String repo = new GitClient(dir).githubRepo();
            return java.util.Optional.of(new LammProject(
                    dir.getFileName().toString(),
                    dir,
                    pom,
                    coords.groupId(),
                    coords.artifactId(),
                    version,
                    repo
            ));
        } catch (IOException e) {
            return java.util.Optional.empty();
        }
    }
}
