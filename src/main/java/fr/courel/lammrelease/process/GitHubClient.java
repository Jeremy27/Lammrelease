package fr.courel.lammrelease.process;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Délègue les opérations GitHub à la CLI `gh` (auth déjà gérée par l'utilisateur).
 */
public final class GitHubClient {

    private final Path workingDir;

    public GitHubClient(Path workingDir) {
        this.workingDir = workingDir;
    }

    /** Retourne le tag de la dernière release publiée, ou null. */
    public String latestReleaseTag() throws IOException {
        var r = ProcessRunner.run(workingDir,
                List.of("gh", "release", "view", "--json", "tagName", "-q", ".tagName"));
        if (!r.ok()) return null;
        String out = r.stdout().trim();
        return out.isEmpty() ? null : out;
    }

    /** Crée une release GitHub avec le tag et le corps donnés, et attache les artifacts. */
    public void createRelease(String tag, String title, String body, List<Path> assets, Consumer<String> log)
            throws IOException {
        var cmd = new ArrayList<>(List.of("gh", "release", "create", tag,
                "--title", title,
                "--notes", body));
        for (Path asset : assets) {
            cmd.add(asset.toString());
        }
        var r = ProcessRunner.run(workingDir, log, cmd);
        if (!r.ok()) {
            throw new IOException("gh release create a échoué (code " + r.exitCode() + ")");
        }
    }

    public boolean isAvailable() {
        try {
            return ProcessRunner.run(workingDir, List.of("gh", "--version")).ok();
        } catch (IOException e) {
            return false;
        }
    }
}
