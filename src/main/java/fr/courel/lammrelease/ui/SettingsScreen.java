package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.App;
import fr.courel.lammrelease.config.LammreleaseConfig;
import fr.courel.lammui.fx.component.LammButtonFx;
import fr.courel.lammui.fx.component.LammCardFx;
import fr.courel.lammui.fx.component.LammTextFieldFx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SettingsScreen extends VBox {

    private final LammreleaseConfig config;
    private final Navigator nav;
    private final LammTextFieldFx workdirField;

    public SettingsScreen(LammreleaseConfig config, Navigator nav) {
        this.config = config;
        this.nav = nav;
        setPadding(new Insets(20, 32, 20, 32));
        setSpacing(16);

        var card = new LammCardFx("Configuration");
        card.setSpacing(12);

        card.getChildren().add(new Label("Dossier racine scanné pour détecter les projets Lamm."));

        workdirField = new LammTextFieldFx("Dossier de travail");
        workdirField.setText(config.workdir().toString());
        card.getChildren().add(workdirField);

        card.getChildren().add(new Label("Releases GitHub : l'authentification est déléguée à la CLI `gh`."));

        var save = LammButtonFx.primary("Enregistrer");
        save.setOnAction(e -> save());
        var back = new LammButtonFx("Retour");
        back.setOnAction(e -> nav.showMain());
        var actions = new HBox(8, save, back);
        actions.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(actions);

        getChildren().add(card);
    }

    private void save() {
        Path path = Path.of(workdirField.getText().trim());
        if (!Files.isDirectory(path)) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dossier invalide");
            alert.setContentText("Le chemin indiqué n'est pas un dossier existant.");
            if (nav instanceof App app) {
                alert.initOwner(app.stage());
                app.styleDialog(alert);
            }
            alert.showAndWait();
            return;
        }
        config.setWorkdir(path);
        nav.showMain();
    }
}
