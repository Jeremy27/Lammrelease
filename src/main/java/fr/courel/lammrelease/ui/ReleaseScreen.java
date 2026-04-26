package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.App;
import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.model.SemVer;
import fr.courel.lammrelease.release.ChangelogBuilder;
import fr.courel.lammrelease.release.ReleasePipeline;
import fr.courel.lammrelease.release.ReleasePlan;
import fr.courel.lammrelease.release.ReleaseStep;
import fr.courel.lammui.fx.component.LammButtonFx;
import fr.courel.lammui.fx.component.LammCardFx;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public final class ReleaseScreen extends HBox {

    private final LammProject project;
    private final Navigator nav;

    private final RadioButton patchRadio = new RadioButton("Patch");
    private final RadioButton minorRadio = new RadioButton("Minor");
    private final RadioButton majorRadio = new RadioButton("Major");

    private final Label newVersionLabel;
    private final TextArea changelogArea = new TextArea();
    private final TextArea logsArea = new TextArea();
    private final Map<ReleaseStep, Label> stepLabels = new EnumMap<>(ReleaseStep.class);
    private final LammButtonFx launchBtn;

    public ReleaseScreen(LammProject project, Navigator nav) {
        this.project = project;
        this.nav = nav;
        setSpacing(12);
        setPadding(new Insets(20, 32, 20, 32));

        newVersionLabel = new Label(computeNewVersion().toString());
        newVersionLabel.setStyle("-fx-font-weight: 600;");

        changelogArea.setText(ChangelogBuilder.build(project));
        changelogArea.setPrefRowCount(8);

        logsArea.setEditable(false);
        logsArea.setPrefRowCount(14);

        launchBtn = LammButtonFx.accent("Lancer la release");
        launchBtn.setOnAction(e -> launch());

        getChildren().addAll(buildLeft(), buildRight());
    }

    private VBox buildLeft() {
        var col = new VBox(12);
        col.setPrefWidth(380);
        col.setMinWidth(360);

        col.getChildren().addAll(configCard(), stepsCard());

        var back = new LammButtonFx("Retour");
        back.setOnAction(e -> nav.showMain());
        var actions = new HBox(8, launchBtn, back);
        actions.setAlignment(Pos.CENTER_LEFT);
        col.getChildren().add(actions);

        return col;
    }

    private LammCardFx configCard() {
        var card = new LammCardFx(project.name());
        card.setSpacing(8);

        card.getChildren().add(new Label("Version actuelle : " + project.version()));

        var bumpHeader = new Label("Type de bump");
        bumpHeader.setStyle("-fx-text-fill: -lamm-text-secondary; -fx-font-size: 12;");
        card.getChildren().add(bumpHeader);

        var group = new ToggleGroup();
        patchRadio.setToggleGroup(group);
        minorRadio.setToggleGroup(group);
        majorRadio.setToggleGroup(group);
        patchRadio.setSelected(true);
        group.selectedToggleProperty().addListener((obs, ov, nv) ->
            newVersionLabel.setText(computeNewVersion().toString()));
        var bumpRow = new HBox(12, patchRadio, minorRadio, majorRadio);
        bumpRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(bumpRow);

        var nextRow = new HBox(6, new Label("Nouvelle version :"), newVersionLabel);
        nextRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(nextRow);

        return card;
    }

    private LammCardFx stepsCard() {
        var card = new LammCardFx("Étapes");
        card.setSpacing(4);
        for (var step : ReleaseStep.values()) {
            boolean delegated = step == ReleaseStep.GITHUB_RELEASE && project.hasCiRelease();
            var label = new Label(delegated
                ? "↗  " + step.label() + " (via CI)"
                : "○  " + step.label());
            label.setStyle("-fx-text-fill: -lamm-text-secondary;");
            stepLabels.put(step, label);
            card.getChildren().add(label);
        }
        return card;
    }

    private VBox buildRight() {
        var col = new VBox(12);
        HBox.setHgrow(col, Priority.ALWAYS);

        var changelogCard = new LammCardFx("Changelog");
        VBox.setVgrow(changelogArea, Priority.ALWAYS);
        changelogCard.getChildren().add(changelogArea);

        var logsCard = new LammCardFx("Logs");
        VBox.setVgrow(logsArea, Priority.ALWAYS);
        logsCard.getChildren().add(logsArea);
        VBox.setVgrow(logsCard, Priority.ALWAYS);

        col.getChildren().addAll(changelogCard, logsCard);
        return col;
    }

    private SemVer computeNewVersion() {
        return project.version().bump(selectedBump());
    }

    private SemVer.Bump selectedBump() {
        if (majorRadio.isSelected()) return SemVer.Bump.MAJOR;
        if (minorRadio.isSelected()) return SemVer.Bump.MINOR;
        return SemVer.Bump.PATCH;
    }

    private void launch() {
        launchBtn.setDisable(true);
        logsArea.clear();
        stepLabels.forEach((step, l) -> {
            boolean delegated = step == ReleaseStep.GITHUB_RELEASE && project.hasCiRelease();
            l.setText(delegated ? "↗  " + step.label() + " (via CI)" : "○  " + step.label());
            l.setStyle("-fx-text-fill: -lamm-text-secondary;");
        });

        SemVer newVersion = computeNewVersion();
        var plan = new ReleasePlan(project, selectedBump(), newVersion, changelogArea.getText());

        var t = new Thread(() -> runPipeline(plan), "release-pipeline");
        t.setDaemon(true);
        t.start();
    }

    private void runPipeline(ReleasePlan plan) {
        try {
            new ReleasePipeline().run(plan, (step, status, line) -> Platform.runLater(() -> {
                Label label = stepLabels.get(step);
                switch (status) {
                    case START -> {
                        label.setText("⟳  " + step.label());
                        label.setStyle("-fx-text-fill: -lamm-primary;");
                    }
                    case DONE -> {
                        label.setText("✓  " + step.label());
                        label.setStyle("-fx-text-fill: -lamm-success;");
                    }
                    case ERROR -> {
                        label.setText("✗  " + step.label() + " — " + line);
                        label.setStyle("-fx-text-fill: -lamm-error;");
                    }
                    case LOG -> {
                        if (line != null) appendLog(line);
                    }
                }
            }));
            Platform.runLater(() -> {
                showInfo("Release terminée",
                    plan.project().name() + " " + plan.newVersion() + " publiée.");
                launchBtn.setDisable(false);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                appendLog("ERREUR : " + e.getMessage());
                showError("Échec de la release", e.getMessage());
                launchBtn.setDisable(false);
            });
        }
    }

    private void appendLog(String line) {
        logsArea.appendText(line + "\n");
    }

    private void showInfo(String header, String content) {
        showAlert(Alert.AlertType.INFORMATION, header, content);
    }

    private void showError(String header, String content) {
        showAlert(Alert.AlertType.ERROR, header, content);
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        var alert = new Alert(type);
        alert.setTitle(header);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (nav instanceof App app) {
            alert.initOwner(app.stage());
            app.styleDialog(alert);
        }
        alert.showAndWait();
    }
}
