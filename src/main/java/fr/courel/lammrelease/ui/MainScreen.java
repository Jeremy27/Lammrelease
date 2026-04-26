package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.config.LammreleaseConfig;
import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.process.GitHubClient;
import fr.courel.lammrelease.scan.ProjectScanner;
import fr.courel.lammui.fx.component.LammButtonFx;
import fr.courel.lammui.fx.component.LammCardFx;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MainScreen extends VBox {

    private static final double TILE_WIDTH = 320;

    private final LammreleaseConfig config;
    private final Navigator nav;
    private final TilePane tiles;
    private final Label hint;
    private final Map<String, String> tagCache = new ConcurrentHashMap<>();

    public MainScreen(LammreleaseConfig config, Navigator nav) {
        this.config = config;
        this.nav = nav;
        setSpacing(12);
        setPadding(new Insets(20, 32, 20, 32));

        hint = new Label();
        var refresh = new LammButtonFx("Actualiser");
        refresh.setOnAction(e -> refresh());
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        var topBar = new HBox(8, hint, spacer, refresh);
        topBar.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(topBar);

        tiles = new TilePane(16, 16);
        tiles.setPrefColumns(3);
        tiles.setPrefTileWidth(TILE_WIDTH);
        tiles.setTileAlignment(Pos.TOP_LEFT);

        var scroll = new ScrollPane(tiles);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);
    }

    public void refresh() {
        hint.setText("Projets détectés dans " + config.workdir());
        tiles.getChildren().clear();
        List<LammProject> projects = new ProjectScanner().scan(config.workdir());
        if (projects.isEmpty()) {
            tiles.getChildren().add(new Label("Aucun projet Lamm détecté dans ce dossier."));
            return;
        }
        for (var p : projects) {
            tiles.getChildren().add(buildTile(p));
        }
    }

    private LammCardFx buildTile(LammProject project) {
        var card = new LammCardFx(project.name());
        card.setPrefWidth(TILE_WIDTH);

        var versionLabel = new Label("Version locale : " + project.version());
        String remote = project.githubRepo() != null ? project.githubRepo() : "— (pas de remote GitHub)";
        var githubLabel = new Label("GitHub : " + remote);
        githubLabel.setWrapText(true);
        String cached = tagCache.get(project.name());
        var publishedLabel = new Label("Dernière release : " + (cached != null ? cached : "…"));

        var info = new VBox(4, versionLabel, githubLabel, publishedLabel);
        VBox.setVgrow(info, Priority.ALWAYS);

        var releaseBtn = LammButtonFx.primary("Release");
        releaseBtn.setDisable(project.githubRepo() == null);
        releaseBtn.setOnAction(e -> nav.showRelease(project));
        var actionRow = new HBox(releaseBtn);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(info, actionRow);

        if (cached == null && project.githubRepo() != null) {
            loadTagAsync(project, publishedLabel);
        }
        return card;
    }

    private void loadTagAsync(LammProject project, Label label) {
        var task = new Task<String>() {
            @Override
            protected String call() {
                try {
                    return new GitHubClient(project.directory()).latestReleaseTag();
                } catch (Exception e) {
                    return null;
                }
            }
        };
        task.setOnSucceeded(e -> {
            String tag = task.getValue();
            String display = tag != null ? tag : "aucune";
            tagCache.put(project.name(), display);
            label.setText("Dernière release : " + display);
        });
        task.setOnFailed(e -> Platform.runLater(() -> label.setText("Dernière release : —")));
        var t = new Thread(task, "lammrelease-tag-fetch-" + project.name());
        t.setDaemon(true);
        t.start();
    }
}
