package fr.courel.lammrelease.release;

import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.process.GitClient;

import java.io.IOException;

public final class ChangelogBuilder {

    private ChangelogBuilder() {}

    public static String build(LammProject project) {
        try {
            var git = new GitClient(project.directory());
            String lastTag = git.lastTag();
            String commits = git.commitsSince(lastTag);
            if (commits == null || commits.isBlank()) {
                return "_Aucun commit depuis la dernière release._";
            }
            String header = lastTag != null
                    ? "## Changements depuis " + lastTag + "\n\n"
                    : "## Changements\n\n";
            return header + commits;
        } catch (IOException e) {
            return "_Changelog indisponible : " + e.getMessage() + "_";
        }
    }
}
