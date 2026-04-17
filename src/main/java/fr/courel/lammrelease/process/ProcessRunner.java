package fr.courel.lammrelease.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public final class ProcessRunner {

    private ProcessRunner() {}

    public record Result(int exitCode, String stdout) {
        public boolean ok() { return exitCode == 0; }
    }

    /** Exécute une commande et stream chaque ligne au consumer. Bloque jusqu'à la fin. */
    public static Result run(Path workingDir, Consumer<String> onLine, List<String> command) throws IOException {
        var pb = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(workingDir.toFile());
        Process process = pb.start();
        var sb = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
                if (onLine != null) onLine.accept(line);
            }
        }
        try {
            int code = process.waitFor();
            return new Result(code, sb.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrompu", e);
        }
    }

    public static Result run(Path workingDir, List<String> command) throws IOException {
        return run(workingDir, null, command);
    }
}
