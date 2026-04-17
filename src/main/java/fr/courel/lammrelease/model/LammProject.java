package fr.courel.lammrelease.model;

import java.nio.file.Path;

public record LammProject(
        String name,
        Path directory,
        Path pomFile,
        String groupId,
        String artifactId,
        SemVer version,
        String githubRepo
) {
    public Path targetDir() {
        return directory.resolve("target");
    }
}
