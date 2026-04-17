package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.config.LammreleaseConfig;
import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.process.GitHubClient;
import fr.courel.lammrelease.scan.ProjectScanner;
import fr.courel.lammui.component.LammButton;
import fr.courel.lammui.component.LammCard;
import fr.courel.lammui.component.LammLabel;
import fr.courel.lammui.component.LammScrollPane;
import fr.courel.lammui.theme.LammColors;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class MainScreen extends JPanel {

    private final LammreleaseConfig config;
    private final Navigator nav;
    private final JPanel grid;

    public MainScreen(LammreleaseConfig config, Navigator nav) {
        super(new BorderLayout(0, 0));
        this.config = config;
        this.nav = nav;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        add(topBar(), BorderLayout.NORTH);

        grid = new JPanel();
        grid.setOpaque(false);
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        add(new LammScrollPane(grid), BorderLayout.CENTER);

        refresh();
    }

    private JPanel topBar() {
        var bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        var left = new LammLabel("Projets détectés dans " + config.workdir(), LammLabel.Style.CAPTION, true);
        bar.add(left, BorderLayout.WEST);

        var right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        var refresh = LammButton.flat("Actualiser");
        refresh.addActionListener(_ -> refresh());
        var settings = LammButton.flat("Paramètres");
        settings.addActionListener(_ -> nav.showSettings());
        right.add(refresh);
        right.add(settings);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    public void refresh() {
        grid.removeAll();
        List<LammProject> projects = new ProjectScanner().scan(config.workdir());
        if (projects.isEmpty()) {
            var empty = new LammLabel("Aucun projet Lamm détecté dans ce dossier.",
                    LammLabel.Style.BODY, true);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            grid.add(empty);
        } else {
            for (var p : projects) {
                grid.add(projectCard(p));
                grid.add(Box.createVerticalStrut(12));
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel projectCard(LammProject project) {
        var card = new LammCard();
        card.setLayout(new BorderLayout(16, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setTitle(project.name());

        var infoCol = new JPanel();
        infoCol.setOpaque(false);
        infoCol.setLayout(new BoxLayout(infoCol, BoxLayout.Y_AXIS));
        infoCol.add(new LammLabel("Version locale : " + project.version(), LammLabel.Style.BODY));

        String remote = project.githubRepo() != null ? project.githubRepo() : "— (pas de remote GitHub)";
        infoCol.add(new LammLabel("GitHub : " + remote, LammLabel.Style.CAPTION, true));

        String published = fetchLatestReleaseTag(project);
        infoCol.add(new LammLabel("Dernière release : " + (published != null ? published : "aucune"),
                LammLabel.Style.CAPTION, true));

        card.add(infoCol, BorderLayout.CENTER);

        var actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        var releaseBtn = new LammButton("Release");
        releaseBtn.setEnabled(project.githubRepo() != null);
        releaseBtn.addActionListener(_ -> nav.showRelease(project));
        actions.add(releaseBtn);
        card.add(actions, BorderLayout.EAST);

        return card;
    }

    private String fetchLatestReleaseTag(LammProject project) {
        if (project.githubRepo() == null) return null;
        try {
            return new GitHubClient(project.directory()).latestReleaseTag();
        } catch (Exception e) {
            return null;
        }
    }
}
