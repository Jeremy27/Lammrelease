package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.model.SemVer;
import fr.courel.lammrelease.release.ChangelogBuilder;
import fr.courel.lammrelease.release.ReleasePipeline;
import fr.courel.lammrelease.release.ReleasePlan;
import fr.courel.lammrelease.release.ReleaseStep;
import fr.courel.lammui.component.LammButton;
import fr.courel.lammui.component.LammCard;
import fr.courel.lammui.component.LammDialog;
import fr.courel.lammui.component.LammLabel;
import fr.courel.lammui.component.LammScrollPane;
import fr.courel.lammui.component.LammTextArea;
import fr.courel.lammui.theme.LammColors;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ReleaseScreen extends JPanel {

    private final LammProject project;
    private final Navigator nav;

    private final JRadioButton patchRadio = new JRadioButton("Patch", true);
    private final JRadioButton minorRadio = new JRadioButton("Minor");
    private final JRadioButton majorRadio = new JRadioButton("Major");

    private final LammLabel newVersionLabel;
    private final LammTextArea changelogArea;
    private final LammTextArea logsArea;
    private final Map<ReleaseStep, JLabel> stepLabels = new EnumMap<>(ReleaseStep.class);
    private final LammButton launchBtn;

    public ReleaseScreen(LammProject project, Navigator nav) {
        super(new BorderLayout(12, 12));
        this.project = project;
        this.nav = nav;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        newVersionLabel = new LammLabel(computeNewVersion().toString(), LammLabel.Style.SUBTITLE);
        changelogArea = new LammTextArea("Changelog", 8, 40);
        changelogArea.setText(ChangelogBuilder.build(project));
        logsArea = new LammTextArea("Logs", 12, 60);
        logsArea.setEditable(false);

        launchBtn = new LammButton("Lancer la release");
        launchBtn.addActionListener(_ -> launch());

        add(buildLeft(), BorderLayout.WEST);
        add(buildRight(), BorderLayout.CENTER);
    }

    private JPanel buildLeft() {
        var col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setPreferredSize(new Dimension(360, 0));

        col.add(configCard());
        col.add(Box.createVerticalStrut(12));
        col.add(stepsCard());
        col.add(Box.createVerticalStrut(12));

        var actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(launchBtn);
        var back = LammButton.flat("Retour");
        back.addActionListener(_ -> nav.showMain());
        actions.add(back);
        col.add(actions);
        col.add(Box.createVerticalGlue());

        return col;
    }

    private JPanel configCard() {
        var card = new LammCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setTitle(project.name());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(new LammLabel("Version actuelle : " + project.version(), LammLabel.Style.BODY));
        card.add(Box.createVerticalStrut(8));
        card.add(new LammLabel("Type de bump", LammLabel.Style.CAPTION, true));

        var bumpRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bumpRow.setOpaque(false);
        bumpRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        var group = new ButtonGroup();
        for (var radio : List.of(patchRadio, minorRadio, majorRadio)) {
            radio.setOpaque(false);
            radio.addActionListener(_ -> newVersionLabel.setText(computeNewVersion().toString()));
            group.add(radio);
            bumpRow.add(radio);
        }
        card.add(bumpRow);

        card.add(Box.createVerticalStrut(8));
        var nextRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nextRow.setOpaque(false);
        nextRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        nextRow.add(new LammLabel("Nouvelle version :", LammLabel.Style.BODY, true));
        nextRow.add(newVersionLabel);
        card.add(nextRow);

        return card;
    }

    private JPanel stepsCard() {
        var card = new LammCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setTitle("Étapes");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (var step : ReleaseStep.values()) {
            boolean delegated = step == ReleaseStep.GITHUB_RELEASE && project.hasCiRelease();
            var label = new JLabel(delegated
                    ? "↗  " + step.label() + " (via CI)"
                    : "○  " + step.label());
            label.setForeground(LammColors.textSecondary());
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            stepLabels.put(step, label);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    private JPanel buildRight() {
        var col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        var changelogCard = new LammCard();
        changelogCard.setLayout(new BorderLayout());
        changelogCard.setTitle("Changelog");
        changelogArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        changelogCard.add(new LammScrollPane(changelogArea), BorderLayout.CENTER);
        col.add(changelogCard);
        col.add(Box.createVerticalStrut(12));

        var logsCard = new LammCard();
        logsCard.setLayout(new BorderLayout());
        logsCard.setTitle("Logs");
        logsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        logsCard.add(new LammScrollPane(logsArea), BorderLayout.CENTER);
        col.add(logsCard);

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
        launchBtn.setEnabled(false);
        logsArea.setText("");
        stepLabels.forEach((step, l) -> {
            boolean delegated = step == ReleaseStep.GITHUB_RELEASE && project.hasCiRelease();
            l.setText(delegated ? "↗  " + step.label() + " (via CI)" : "○  " + step.label());
            l.setForeground(LammColors.textSecondary());
        });

        SemVer newVersion = computeNewVersion();
        var plan = new ReleasePlan(project, selectedBump(), newVersion, changelogArea.getText());

        new Thread(() -> runPipeline(plan), "release-pipeline").start();
    }

    private void runPipeline(ReleasePlan plan) {
        try {
            new ReleasePipeline().run(plan, (step, status, line) -> SwingUtilities.invokeLater(() -> {
                JLabel label = stepLabels.get(step);
                switch (status) {
                    case START -> {
                        label.setText("⟳  " + step.label());
                        label.setForeground(LammColors.PRIMARY);
                    }
                    case DONE -> {
                        label.setText("✓  " + step.label());
                        label.setForeground(LammColors.SUCCESS);
                    }
                    case ERROR -> {
                        label.setText("✗  " + step.label() + " — " + line);
                        label.setForeground(LammColors.ERROR);
                    }
                    case LOG -> {
                        if (line != null) appendLog(line);
                    }
                }
            }));
            SwingUtilities.invokeLater(() -> {
                LammDialog.success(parentFrame(), "Release terminée",
                        plan.project().name() + " " + plan.newVersion() + " publiée.");
                launchBtn.setEnabled(true);
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                appendLog("ERREUR : " + e.getMessage());
                LammDialog.error(parentFrame(), "Échec de la release", e.getMessage());
                launchBtn.setEnabled(true);
            });
        }
    }

    private void appendLog(String line) {
        logsArea.getTextArea().append(line + "\n");
        var area = logsArea.getTextArea();
        area.setCaretPosition(area.getDocument().getLength());
    }

    private JFrame parentFrame() {
        return SwingUtilities.getWindowAncestor(this) instanceof JFrame f ? f : null;
    }

}
