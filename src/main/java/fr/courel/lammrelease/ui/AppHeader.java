package fr.courel.lammrelease.ui;

import fr.courel.lammui.component.LammHeader;
import fr.courel.lammui.component.LammSwitch;
import fr.courel.lammui.component.LammTitle;
import fr.courel.lammui.theme.LammTheme;

import javax.swing.*;
import java.awt.*;

public final class AppHeader {

    private AppHeader() {}

    public static JPanel build() {
        var header = new LammHeader();
        header.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        var titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(new LammTitle("release", 22f));
        header.add(titlePanel, BorderLayout.WEST);

        var themeSwitch = new LammSwitch("Dark");
        themeSwitch.setOnGradient(true);
        themeSwitch.addPropertyChangeListener("selected", _ -> {
            LammTheme.toggle();
            themeSwitch.setLabel(LammTheme.isDark() ? "Light" : "Dark");
        });
        var switchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        switchWrapper.setOpaque(false);
        switchWrapper.add(themeSwitch);
        header.add(switchWrapper, BorderLayout.EAST);

        return header;
    }
}
