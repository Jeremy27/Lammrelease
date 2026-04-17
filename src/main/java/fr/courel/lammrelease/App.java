package fr.courel.lammrelease;

import fr.courel.lammrelease.config.LammreleaseConfig;
import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.ui.AppHeader;
import fr.courel.lammrelease.ui.MainScreen;
import fr.courel.lammrelease.ui.Navigator;
import fr.courel.lammrelease.ui.ReleaseScreen;
import fr.courel.lammrelease.ui.SettingsScreen;
import fr.courel.lammui.component.LammFrame;

import javax.swing.*;
import java.awt.*;

public final class App implements Navigator {

    private static final String CARD_MAIN = "main";
    private static final String CARD_RELEASE = "release";
    private static final String CARD_SETTINGS = "settings";

    private final LammreleaseConfig config = new LammreleaseConfig();
    private final LammFrame frame = new LammFrame("Lammrelease");
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private MainScreen mainScreen;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().start());
    }

    private void start() {
        frame.setLayout(new BorderLayout());
        frame.add(AppHeader.build(), BorderLayout.NORTH);
        center.setOpaque(false);
        frame.add(center, BorderLayout.CENTER);

        mainScreen = new MainScreen(config, this);
        center.add(mainScreen, CARD_MAIN);
        center.add(new SettingsScreen(config, this), CARD_SETTINGS);

        frame.centerOnScreen(1100, 720);
        frame.setVisible(true);
    }

    @Override
    public void showMain() {
        mainScreen.refresh();
        cards.show(center, CARD_MAIN);
    }

    @Override
    public void showRelease(LammProject project) {
        for (var comp : center.getComponents()) {
            if (CARD_RELEASE.equals(comp.getName())) center.remove(comp);
        }
        var screen = new ReleaseScreen(project, this);
        screen.setName(CARD_RELEASE);
        center.add(screen, CARD_RELEASE);
        cards.show(center, CARD_RELEASE);
    }

    @Override
    public void showSettings() {
        cards.show(center, CARD_SETTINGS);
    }
}
