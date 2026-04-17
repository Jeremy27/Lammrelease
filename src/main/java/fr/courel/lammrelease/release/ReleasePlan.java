package fr.courel.lammrelease.release;

import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.model.SemVer;

public record ReleasePlan(
        LammProject project,
        SemVer.Bump bumpType,
        SemVer newVersion,
        String changelog
) {
    public String tag() {
        return "v" + newVersion;
    }

    public String commitMessage() {
        return "Release " + newVersion;
    }

    public String releaseTitle() {
        return project.name() + " " + newVersion;
    }
}
