package fr.courel.lammrelease.process;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public final class MavenRunner {

    private final Path workingDir;

    public MavenRunner(Path workingDir) {
        this.workingDir = workingDir;
    }

    public void cleanPackage(Consumer<String> onLine) throws IOException {
        var r = ProcessRunner.run(workingDir, onLine, List.of("mvn", "-B", "clean", "package"));
        if (!r.ok()) {
            throw new IOException("mvn clean package a échoué (code " + r.exitCode() + ")");
        }
    }
}
