# Lammrelease

Application desktop pour automatiser les releases des projets de la suite **Lamm**.

## Fonctionnalités

- Scan du dossier de travail, détection automatique des projets `fr.courel/lamm*`
- Affichage de la version locale + dernière release GitHub publiée
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
- Swing + [LammUI](../LammUI)
- Aucune API REST maison : délégation à `git` et `gh` CLI

## Prérequis

- Java 25+
- Maven 3.9+
- `git` CLI
- `gh` CLI [authentifié](https://cli.github.com/manual/gh_auth_login)
- LammUI installé localement (`cd ../LammUI && mvn install`)

## Build

```bash
mvn package
```

## Lancement

```bash
java -jar target/lammrelease-X.Y.Z.jar
```

## Configuration

Au premier lancement, le dossier de travail par défaut est `~/Documents/projets`. Modifiable via l'écran **Paramètres** et persisté dans `~/.config/lammrelease/config.properties`.

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
├── ui/         # MainScreen, ReleaseScreen, SettingsScreen, AppHeader
└── App.java
```
