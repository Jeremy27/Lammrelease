package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.config.LammreleaseConfig;
import fr.courel.lammui.component.LammButton;
import fr.courel.lammui.component.LammCard;
import fr.courel.lammui.component.LammDialog;
import fr.courel.lammui.component.LammLabel;
import fr.courel.lammui.component.LammTextField;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SettingsScreen extends JPanel {

    private final LammreleaseConfig config;
    private final Navigator nav;
    private final LammTextField workdirField;

    public SettingsScreen(LammreleaseConfig config, Navigator nav) {
        super(new BorderLayout());
        this.config = config;
        this.nav = nav;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        var card = new LammCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setTitle("Paramètres");

        card.add(new LammLabel("Dossier racine scanné pour détecter les projets Lamm.",
                LammLabel.Style.CAPTION, true));
        card.add(Box.createVerticalStrut(8));

        workdirField = new LammTextField("Dossier de travail", 40);
        workdirField.setText(config.workdir().toString());
        workdirField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(workdirField);
        card.add(Box.createVerticalStrut(16));

        card.add(new LammLabel("Releases GitHub : l'authentification est déléguée à la CLI `gh`.",
                LammLabel.Style.CAPTION, true));
        card.add(Box.createVerticalStrut(16));

        var actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        var save = new LammButton("Enregistrer");
        save.addActionListener(_ -> save());
        var back = LammButton.flat("Retour");
        back.addActionListener(_ -> nav.showMain());
        actions.add(save);
        actions.add(back);
        card.add(actions);

        var top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        add(top, BorderLayout.CENTER);
    }

    private void save() {
        Path path = Path.of(workdirField.getText().trim());
        if (!Files.isDirectory(path)) {
            LammDialog.error(SwingUtilities.getWindowAncestor(this) instanceof JFrame f ? f : null,
                    "Dossier invalide", "Le chemin indiqué n'est pas un dossier existant.");
            return;
        }
        config.setWorkdir(path);
        nav.showMain();
    }
}
