package fr.courel.lammrelease;

import fr.courel.lammrelease.config.LammreleaseConfig;
import fr.courel.lammrelease.model.LammProject;
import fr.courel.lammrelease.ui.MainScreen;
import fr.courel.lammrelease.ui.Navigator;
import fr.courel.lammrelease.ui.ReleaseScreen;
import fr.courel.lammrelease.ui.SettingsScreen;
import fr.courel.lammui.fx.component.LammButtonFx;
import fr.courel.lammui.fx.component.LammChromeFx;
import fr.courel.lammui.fx.theme.LammThemeFx;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.prefs.Preferences;

public final class App extends Application implements Navigator {

    private static final double DEFAULT_WIDTH = 1100;
    private static final double DEFAULT_HEIGHT = 720;
    private static final String PREF_THEME = "theme";
    private static final String PREF_ACCENT = "accent";
    private static final String ACCENT_STEEL = "steel";
    private static final String ACCENT_EMERALD = "emerald";
    private static final String ACCENT_AMBER = "amber";
    private static final Preferences PREFS = Preferences.userNodeForPackage(App.class);

    private final LammreleaseConfig config = new LammreleaseConfig();
    private Stage stage;
    private Scene scene;
    private StackPane content;
    private MainScreen mainScreen;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.initStyle(StageStyle.UNDECORATED);

        var chrome = new LammChromeFx("release");
        chrome.attachTo(primaryStage);

        content = new StackPane();
        VBox.setVgrow(content, Priority.ALWAYS);
        chrome.getChildren().add(content);

        scene = new Scene(chrome, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        LammThemeFx.install(scene);
        applySavedPreferences();

        chrome.addAction(buildSettingsButton());

        mainScreen = new MainScreen(config, this);
        showMain();

        stage.setTitle("Lammrelease");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void showMain() {
        mainScreen.refresh();
        content.getChildren().setAll(mainScreen);
    }

    @Override
    public void showRelease(LammProject project) {
        content.getChildren().setAll(new ReleaseScreen(project, this));
    }

    @Override
    public void showSettings() {
        content.getChildren().setAll(new SettingsScreen(config, this));
    }

    public Stage stage() {
        return stage;
    }

    public void styleDialog(Dialog<?> dialog) {
        var pane = dialog.getDialogPane();
        pane.setGraphic(null);
        pane.getStylesheets().setAll(scene.getStylesheets());
        var sceneRoot = scene.getRoot();
        for (var cls : sceneRoot.getStyleClass()) {
            if ("dark".equals(cls) || cls.startsWith("accent-")) {
                if (!pane.getStyleClass().contains(cls)) {
                    pane.getStyleClass().add(cls);
                }
            }
        }
    }

    private void applySavedPreferences() {
        String theme = PREFS.get(PREF_THEME, "LIGHT");
        LammThemeFx.setMode(scene, "DARK".equals(theme) ? LammThemeFx.Mode.DARK : LammThemeFx.Mode.LIGHT);
        applyAccent(PREFS.get(PREF_ACCENT, ACCENT_STEEL));
    }

    private void applyAccent(String accent) {
        var root = scene.getRoot();
        root.getStyleClass().removeIf(c -> c.startsWith("accent-"));
        if (accent != null && !ACCENT_STEEL.equals(accent)) {
            root.getStyleClass().add("accent-" + accent);
        }
        PREFS.put(PREF_ACCENT, accent == null ? ACCENT_STEEL : accent);
    }

    private void resetWindow() {
        stage.setMaximized(false);
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        var screen = Screen.getPrimary().getVisualBounds();
        stage.setX(screen.getMinX() + (screen.getWidth() - DEFAULT_WIDTH) / 2);
        stage.setY(screen.getMinY() + (screen.getHeight() - DEFAULT_HEIGHT) / 2);
    }

    private void showAbout() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("Lammrelease");
        alert.setContentText(
            "Version " + readVersion()
            + "\nOutil de release pour la suite Lamm"
            + "\n\n© 2026 Jeremy Courel");
        alert.initOwner(stage);
        styleDialog(alert);
        alert.showAndWait();
    }

    private static String readVersion() {
        try (var in = App.class.getResourceAsStream("/version.txt")) {
            if (in == null) return "?";
            return new String(in.readAllBytes()).trim();
        } catch (IOException e) {
            return "?";
        }
    }

    private Button buildSettingsButton() {
        var lightItem = new RadioMenuItem("Mode clair");
        var darkItem = new RadioMenuItem("Mode sombre");
        var themeGroup = new ToggleGroup();
        lightItem.setToggleGroup(themeGroup);
        darkItem.setToggleGroup(themeGroup);
        lightItem.setOnAction(e -> {
            LammThemeFx.setMode(scene, LammThemeFx.Mode.LIGHT);
            PREFS.put(PREF_THEME, "LIGHT");
        });
        darkItem.setOnAction(e -> {
            LammThemeFx.setMode(scene, LammThemeFx.Mode.DARK);
            PREFS.put(PREF_THEME, "DARK");
        });

        var accentSteel = new RadioMenuItem("Bleu acier");
        var accentEmerald = new RadioMenuItem("Émeraude");
        var accentAmber = new RadioMenuItem("Ambre");
        var accentGroup = new ToggleGroup();
        accentSteel.setToggleGroup(accentGroup);
        accentEmerald.setToggleGroup(accentGroup);
        accentAmber.setToggleGroup(accentGroup);
        accentSteel.setOnAction(e -> applyAccent(ACCENT_STEEL));
        accentEmerald.setOnAction(e -> applyAccent(ACCENT_EMERALD));
        accentAmber.setOnAction(e -> applyAccent(ACCENT_AMBER));
        var accentMenu = new Menu("Accent");
        accentMenu.getItems().addAll(accentSteel, accentEmerald, accentAmber);

        var configItem = new MenuItem("Configuration…");
        configItem.setOnAction(e -> showSettings());

        var resetItem = new MenuItem("Réinitialiser la fenêtre");
        resetItem.setOnAction(e -> resetWindow());

        var aboutItem = new MenuItem("À propos…");
        aboutItem.setOnAction(e -> showAbout());

        var menu = new ContextMenu(
            lightItem, darkItem,
            new SeparatorMenuItem(),
            accentMenu,
            new SeparatorMenuItem(),
            configItem,
            resetItem,
            aboutItem
        );

        var btn = new Button();
        btn.getStyleClass().add("lamm-chrome-button");
        btn.setFocusTraversable(false);
        btn.setGraphic(LammChromeFx.settingsIcon());
        btn.setOnAction(e -> {
            boolean dark = LammThemeFx.isDark();
            lightItem.setSelected(!dark);
            darkItem.setSelected(dark);
            String accent = PREFS.get(PREF_ACCENT, ACCENT_STEEL);
            accentSteel.setSelected(ACCENT_STEEL.equals(accent));
            accentEmerald.setSelected(ACCENT_EMERALD.equals(accent));
            accentAmber.setSelected(ACCENT_AMBER.equals(accent));
            menu.show(btn, Side.BOTTOM, 0, 4);
        });
        return btn;
    }
}
