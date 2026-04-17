package fr.courel.lammrelease.process;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitClient {

    private final Path workingDir;

    public GitClient(Path workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isClean() throws IOException {
        var r = ProcessRunner.run(workingDir, List.of("git", "status", "--porcelain"));
        return r.ok() && r.stdout().trim().isEmpty();
    }

    /** Retourne "owner/repo" à partir de l'URL du remote origin, ou null. */
    public String githubRepo() throws IOException {
        var r = ProcessRunner.run(workingDir, List.of("git", "remote", "get-url", "origin"));
        if (!r.ok()) return null;
        return parseGithubRepo(r.stdout().trim());
    }

    static String parseGithubRepo(String url) {
        if (url == null || url.isBlank()) return null;
        Pattern p = Pattern.compile("github\\.com[:/]([^/]+)/([^/]+?)(?:\\.git)?/?$");
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) + "/" + m.group(2) : null;
    }

    public String lastTag() throws IOException {
        var r = ProcessRunner.run(workingDir,
                List.of("git", "describe", "--tags", "--abbrev=0"));
        return r.ok() ? r.stdout().trim() : null;
    }

    /** Liste les commits entre lastTag et HEAD (ou tous si pas de tag). */
    public String commitsSince(String tag) throws IOException {
        List<String> cmd = tag != null
                ? List.of("git", "log", tag + "..HEAD", "--pretty=format:- %s")
                : List.of("git", "log", "--pretty=format:- %s");
        var r = ProcessRunner.run(workingDir, cmd);
        return r.ok() ? r.stdout() : "";
    }

    public void add(Path relative, Consumer<String> log) throws IOException {
        fail(ProcessRunner.run(workingDir, log, List.of("git", "add", relative.toString())), "git add");
    }

    public void commit(String message, Consumer<String> log) throws IOException {
        fail(ProcessRunner.run(workingDir, log, List.of("git", "commit", "-m", message)), "git commit");
    }

    public void tag(String tag, Consumer<String> log) throws IOException {
        fail(ProcessRunner.run(workingDir, log, List.of("git", "tag", tag)), "git tag");
    }

    public void push(Consumer<String> log) throws IOException {
        fail(ProcessRunner.run(workingDir, log, List.of("git", "push")), "git push");
    }

    public void pushTag(String tag, Consumer<String> log) throws IOException {
        fail(ProcessRunner.run(workingDir, log, List.of("git", "push", "origin", tag)), "git push tag");
    }

    private static void fail(ProcessRunner.Result r, String step) throws IOException {
        if (!r.ok()) throw new IOException(step + " a échoué (code " + r.exitCode() + ")");
    }
}
