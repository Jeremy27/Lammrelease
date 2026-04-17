package fr.courel.lammrelease.release;

public enum ReleaseStep {
    CHECK_CLEAN("Vérification git clean"),
    BUMP_VERSION("Mise à jour version pom"),
    BUILD("Build Maven"),
    COMMIT("Commit + tag"),
    PUSH("Push remote"),
    GITHUB_RELEASE("Création release GitHub + upload");

    private final String label;

    ReleaseStep(String label) { this.label = label; }

    public String label() { return label; }
}
