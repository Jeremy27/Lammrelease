package fr.courel.lammrelease.release;

import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.process.GitClient;
import fr.courel.lammrelease.process.GitHubClient;
import fr.courel.lammrelease.process.MavenRunner;
import fr.courel.lammrelease.scan.PomReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Orchestre une release de bout en bout pour un projet Lamm.
 */
public final class ReleasePipeline {

    @FunctionalInterface
    public interface Listener {
        void onEvent(ReleaseStep step, Status status, String line);
        enum Status { START, LOG, DONE, ERROR }
    }

    public void run(ReleasePlan plan, Listener listener) throws IOException {
        LammProject project = plan.project();
        Path dir = project.directory();

        // 1. git status clean
        step(listener, ReleaseStep.CHECK_CLEAN, () -> {
            if (!new GitClient(dir).isClean()) {
                throw new IOException("git status non clean — commit ou stash avant de releaser");
            }
        });

        // 2. Bump pom.xml
        step(listener, ReleaseStep.BUMP_VERSION, () ->
                PomReader.writeVersion(project.pomFile(), plan.newVersion().toString()));

        // 3. Build
        BiConsumer<ReleaseStep, String> logger = (s, l) ->
                listener.onEvent(s, Listener.Status.LOG, l);
        step(listener, ReleaseStep.BUILD, () ->
                new MavenRunner(dir).cleanPackage(line -> logger.accept(ReleaseStep.BUILD, line)));

        // 4. Commit + tag local
        step(listener, ReleaseStep.COMMIT, () -> {
            var git = new GitClient(dir);
            git.add(Path.of("pom.xml"), line -> logger.accept(ReleaseStep.COMMIT, line));
            git.commit(plan.commitMessage(), line -> logger.accept(ReleaseStep.COMMIT, line));
            git.tag(plan.tag(), line -> logger.accept(ReleaseStep.COMMIT, line));
        });

        // 5. Push commits + tag
        step(listener, ReleaseStep.PUSH, () -> {
            var git = new GitClient(dir);
            git.push(line -> logger.accept(ReleaseStep.PUSH, line));
            git.pushTag(plan.tag(), line -> logger.accept(ReleaseStep.PUSH, line));
        });

        // 6. Release GitHub + upload assets (détectés APRÈS build) — sauf si la CI s'en charge
        if (project.hasCiRelease()) {
            listener.onEvent(ReleaseStep.GITHUB_RELEASE, Listener.Status.LOG,
                    "Délégué au workflow GitHub Actions (déclenché par le push du tag)");
        } else {
            step(listener, ReleaseStep.GITHUB_RELEASE, () -> {
                List<Path> assets = AssetDetector.detect(project);
                assets.forEach(a -> logger.accept(ReleaseStep.GITHUB_RELEASE, "asset: " + a));
                var gh = new GitHubClient(dir);
                gh.createRelease(plan.tag(), plan.releaseTitle(), plan.changelog(), assets,
                        line -> logger.accept(ReleaseStep.GITHUB_RELEASE, line));
            });
        }
    }

    private void step(Listener listener, ReleaseStep step, IoRunnable action) throws IOException {
        listener.onEvent(step, Listener.Status.START, null);
        try {
            action.run();
            listener.onEvent(step, Listener.Status.DONE, null);
        } catch (IOException e) {
            listener.onEvent(step, Listener.Status.ERROR, e.getMessage());
            throw e;
        }
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }
}
