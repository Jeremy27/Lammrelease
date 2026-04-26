# Lammrelease

Application desktop pour automatiser les releases des projets de la suite **Lamm**.

## Fonctionnalités

- Scan du dossier de travail, détection automatique des projets `fr.courel/lamm*`
- Affichage en tuiles avec version locale + dernière release GitHub publiée (fetch async)
- Écran de release avec :
  - Choix du bump SemVer (Patch / Minor / Major)
  - Changelog pré-rempli depuis `git log` (commits depuis le dernier tag), éditable
  - Checklist des étapes avec statut live
  - Logs en temps réel du build
- Pipeline 6 étapes :
  1. Vérification `git status` clean
  2. Bump de la `<version>` dans le `pom.xml`
  3. `mvn clean package`
  4. Commit + tag local
  5. Push commits + tag
  6. `gh release create` avec upload automatique des assets

## Stack

- Java 25
- JavaFX + [LammUI 2.0+](https://github.com/Jeremy27/LammUI)
- Stage `UNDECORATED` avec chrome custom (`LammChromeFx`)
- Aucune API REST maison : délégation à `git` et `gh` CLI

## Prérequis

- Java 25+
- Maven 3.9+
- `git` CLI
- `gh` CLI [authentifié](https://cli.github.com/manual/gh_auth_login)
- Accès à GitHub Packages pour tirer LammUI (cf. section ci-dessous)

## Accès à LammUI depuis GitHub Packages

LammUI est publiée sur `https://maven.pkg.github.com/Jeremy27/LammUI`. Maven a besoin d'un token pour la télécharger. Dans `~/.m2/settings.xml` :

```xml
<servers>
  <server>
    <id>github-lammui</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_PAT_WITH_READ_PACKAGES</password>
  </server>
</servers>
```

## Build

```bash
mvn package
```

## Lancement

```bash
java -jar target/lammrelease-X.Y.Z.jar
```

## Configuration

Au premier lancement, le dossier de travail par défaut est `~/Documents/projets`. Modifiable via le menu **Configuration…** (cog en haut à droite) et persisté dans `~/.config/lammrelease/config.properties`.

Les préférences UI (mode clair/sombre, accent) sont persistées via `java.util.prefs.Preferences`.

## Détection des assets uploadés sur la release

Sont pris en compte dans `target/` et `dist/` :
- fichiers `.exe`, `.zip`, `.jar`

Exclusions : `original-*`, `*-sources.jar`, `*-javadoc.jar`.

## Structure

```
src/main/java/fr/courel/lammrelease/
├── config/     # LammreleaseConfig (workdir persistant)
├── model/      # SemVer, LammProject
├── scan/       # PomReader, ProjectScanner
├── process/    # GitClient, MavenRunner, GitHubClient (gh), ProcessRunner
├── release/    # ReleasePipeline, ReleasePlan, AssetDetector, ChangelogBuilder
├── ui/         # MainScreen (tuiles), ReleaseScreen, SettingsScreen
├── App.java    # Application JavaFX, navigateur de screens
└── Main.java   # launcher (JavaFX 11+ refuse de démarrer une Application directement)
```
