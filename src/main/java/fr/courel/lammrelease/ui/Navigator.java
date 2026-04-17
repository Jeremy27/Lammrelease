package fr.courel.lammrelease.ui;

import fr.courel.lammrelease.model.LammProject;

public interface Navigator {
    void showMain();
    void showRelease(LammProject project);
    void showSettings();
}
