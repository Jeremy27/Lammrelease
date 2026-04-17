package fr.courel.lammrelease.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class LammreleaseConfig {

    private static final Path CONFIG_DIR =
            Path.of(System.getProperty("user.home"), ".config", "lammrelease");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");

    private static final String KEY_WORKDIR = "workdir";
    private static final String DEFAULT_WORKDIR =
            Path.of(System.getProperty("user.home"), "Documents", "projets").toString();

    private final Properties props = new Properties();

    public LammreleaseConfig() {
        load();
    }

    public Path workdir() {
        return Path.of(props.getProperty(KEY_WORKDIR, DEFAULT_WORKDIR));
    }

    public void setWorkdir(Path workdir) {
        props.setProperty(KEY_WORKDIR, workdir.toString());
        save();
    }

    private void load() {
        if (!Files.exists(CONFIG_FILE)) return;
        try (var in = Files.newInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Lecture config impossible", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (var out = Files.newOutputStream(CONFIG_FILE)) {
                props.store(out, "Lammrelease config");
            }
        } catch (IOException e) {
            throw new RuntimeException("Écriture config impossible", e);
        }
    }
}
