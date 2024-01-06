package anna.ui;

import anna.Application;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultUIMenuController {

    public VBox appDescriptionVBox;
    public Label appDescriptionLabel;
    public HBox networkLoadButtonBox;
    public TabPane menuTabPane;
    public ChoiceBox<String> languageChoiceBox;

    private Application main;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    public void initialize() {
        // Set invisible for fade in animation
        menuTabPane.setOpacity(0);

        // Workaround for a scene builder bug, where it cant show FXML expression bindings. It can be replaced in FXML as maxWidth="${appDescriptionVBox.width}", but then it makes working in scene builder very inconvenient
        appDescriptionLabel.setMaxWidth(appDescriptionVBox.getMaxWidth());

        // Set up language selector
        // There are strange bug with ChoiceBox russian characters, so as workaround names are loaded from the bundle
        languageChoiceBox.getItems().addAll(bundle.getString("menu.language.russian"), bundle.getString("menu.language.english"));

        languageChoiceBox.setValue(bundle.getString(Locale.getDefault().getLanguage().equalsIgnoreCase("ru") ? "menu.language.russian" : "menu.language.english"));
        languageChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Locale.setDefault(newValue.intValue() == 0 ? new Locale("ru") : Locale.ENGLISH);
            PopupController.errorMessage("INFO", "", bundle.getString("menu.language.warning"));
        });

        // Set Load button disabled and add tooltip "In development" TODO Remove when loading feature is done
        setupHint(networkLoadButtonBox, bundle.getString("general.inDevelopment"));

        // Wait for initialization to fade in
        Platform.runLater(() -> sceneFade(false));
    }

    public void setMain(Application application) {
        main = application;
    }

    public void createNewNetwork() {
        changeTab(4);
    }
    public void loadNetwork() {
        /*String path = PopupController.openExplorer();
        if (path == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetNetworkFile"));
        }
        */
    }
    public void returnToMenu() {
        changeTab(0);
    }
    public void openTutorialTab() {
        changeTab(1);
    }
    public void openSettingsTab() {
        changeTab(2);
    }
    public void openAboutTab() {
        changeTab(3);
    }
    public void exit() {
        Platform.exit();
    }

    private void changeTab(int tabID) {
        // Configure fade animation
        FadeTransition transition = new FadeTransition(Duration.millis(200), menuTabPane);
        transition.setFromValue(1);
        transition.setToValue(0);
        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        transition.play();

        //Change tab
        transition.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (transition.getDuration().equals(newValue)) {
                menuTabPane.getSelectionModel().select(tabID);
            }
        });
    }

    //Open browser links
    public void openFeedbackForm() {
        PopupController.openURI("https://forms.yandex.ru/u/6443d915d046880af1ef091f/");
    }
    public void openVK() {
        PopupController.openURI("https://vk.com/ravikgames");
    }
    public void openGitHub() {
        PopupController.openURI("https://github.com/ravik-games/AdvancedNeuralNetworkApplication");
    }
    public void openMAN() {
        PopupController.openURI("https://sevman.edusev.ru/");
    }

    // Get file from path
    private File getFileFromPath(String path){
        File result;
        result = new File(path);
        int index = result.getName().lastIndexOf(".");
        if(index <= 0 || !result.getName().substring(index + 1).equals("csv")) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING ,"Selected file is not valid");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.fileNotValid"));
        }
        return result;
    }
    private Transition sceneFade(boolean fadeOut) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), menuTabPane);
        fade.setFromValue(fadeOut ? 1 : 0);
        fade.setToValue(fadeOut ? 0 : 1);
        fade.play();
        return fade;
    }
    public void openAdvancedMode() {
        sceneFade(true).setOnFinished(event -> main.startNetworkEditor(true));
    }
    public void openSimpleMode() {
        sceneFade(true).setOnFinished(event -> main.startNetworkEditor(false));
    }

    // Configure and set up hint
    public void setupHint(Node node, String text) {
        String style = """
                -fx-font-family: Inter;
                -fx-background-color: #ffffff;
                -fx-background-radius: 2.5;
                -fx-opacity: 1;
                -fx-text-fill: #000000;
                -fx-font-size: 14;
                -fx-border-color: #4769ff;
                -fx-border-width: 1;
                -fx-border-radius: 2.5;
                """;

        Tooltip tooltip = new Tooltip(text);
        tooltip.setMaxWidth(400);
        tooltip.setWrapText(true);
        tooltip.setStyle(style);
        tooltip.setShowDuration(Duration.seconds(10));
        tooltip.setShowDelay(Duration.millis(500));
        Tooltip.install(node, tooltip);
    }
}
