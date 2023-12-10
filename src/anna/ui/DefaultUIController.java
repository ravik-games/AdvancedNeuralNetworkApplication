package anna.ui;

import anna.Application;
import anna.network.DataTypes;
import anna.network.NeuralNetwork;
import anna.ui.tabs.UIDataController;
import anna.ui.tabs.UIManagementController;
import anna.ui.tabs.UIOutputController;
import anna.ui.tabs.UIStructureController;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultUIController implements UIController {
    //Master class for all UI stuff

    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization
    protected Application application;

    public HBox rootPane, sideMenuPane;
    public TabPane tabPane;
    public StackPane menuStackPane;
    public Separator menuSeparator;
    public Tab dataTab, architectureTab, managementTab, resultsTab;


    public NeuralNetwork.NetworkArguments lastArguments;

    public UIDataController dataController;
    public UIStructureController structureController;
    public UIManagementController managementController;
    public UIOutputController outputController;

    protected boolean sideMenuOpen;

    //Initialize components
    public void initialize(){
        // Set invisible for fade in animation
        rootPane.setOpacity(0);

        // Load tabs
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editor/DataTab.fxml"), bundle);
            dataTab.setContent(loader.load());
            dataController = loader.getController();

            loader = new FXMLLoader(getClass().getResource("/fxml/editor/ArchitectureTab.fxml"), bundle);
            architectureTab.setContent(loader.load());
            structureController = loader.getController();

            loader = new FXMLLoader(getClass().getResource("/fxml/editor/ManagementTab.fxml"), bundle);
            managementTab.setContent(loader.load());
            managementController = loader.getController();

            loader = new FXMLLoader(getClass().getResource("/fxml/editor/ResultsTab.fxml"), bundle);
            resultsTab.setContent(loader.load());
            outputController = loader.getController();

        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.fileLoadingError"), true);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE ,"Couldn't load .fxml files. See stack trace:\n" + sw);
        }

        // Set correct order for side menu animation
        menuStackPane.setViewOrder(1);

        //Setup all hints
        //setupHints();

        // Wait for initialization to fade in
        Platform.runLater(() -> fadeNode(rootPane, 200, false));
    }

    //Interface methods implementations
    @Override
    public void clearResults(int chartXLength) {
        outputController.clearCharts();
        outputController.setChartXLength(chartXLength);
        outputController.initializeMatrixClasses();
    }

    @Override
    public void updateResults(boolean clear, int epoch, List<DataTypes.Evaluation> trainEvaluation, List<DataTypes.Evaluation> testEvaluation) {
        outputController.lastTrainEvaluation = trainEvaluation;
        outputController.lastTestEvaluation = testEvaluation;
        outputController.updateChart(clear, epoch + 1);
        outputController.updateStatistics(clear);
        outputController.updateSingleClassMatrix();
        outputController.updateFullMatrix(true);
    }

    public void cycleFadeNode(Node node, double ms, Runnable onChange) {
        // Configure fade animation
        FadeTransition fade = new FadeTransition(Duration.millis(ms / 2), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setAutoReverse(true);
        fade.setCycleCount(2);
        fade.play();

        fade.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (fade.getDuration().equals(newValue)) {
                onChange.run();
            }
        });
    }

    public Transition fadeNode(Node node, double ms, boolean disappear) {
        // Configure fade animation
        FadeTransition fade = new FadeTransition(Duration.millis(ms), node);
        fade.setFromValue(disappear ? 1 : 0);
        fade.setToValue(disappear ? 0 : 1);
        fade.play();

        return fade;
    }

    @Override
    public void simulationClassificationResult(double[] outputValues, String outputCategory) {
        outputController.simulationClassificationResult(outputValues, outputCategory);
    }

    @Override
    public void simulationPredictionResult(double outputValue) {
        outputController.simulationPredictionResult(outputValue);
    }

    @Override
    public void setMain(Application application) {
        this.application = application;
        dataController.setReferences(application, application.getDataMaster(), this);
        structureController.setReferences(application, application.getDataMaster(), this);
        managementController.setReferences(application, application.getDataMaster(), this, structureController);
        outputController.setReferences(application, application.getDataMaster(), this);
    }

    public void setupHints() {
        String style = """
                -fx-font-family: Inter;
                -fx-background-color: #ffffff;
                -fx-opacity: 1;
                -fx-text-fill: #000000;
                -fx-font-size: 16;""";

        Tooltip autoPartition = new Tooltip(bundle.getString("tab.data"));
        autoPartition.setStyle(style);
        //Tooltip.install(autoPartitionInfo, autoPartition);
    }

    protected void animateSideMenu(boolean hide) {
        // Prepare animation
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), sideMenuPane);
        transition.setFromX(hide ? 0 : -205);
        transition.setToX(hide ? -205 : 0);

        transition.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Add effects to tab pane
            tabPane.setEffect(new GaussianBlur(hide ? (((double) 200 - newValue.toMillis()) / (double) 200) * 10 : (newValue.toMillis() / (double) 200) * 10));
        });

        transition.setOnFinished(event -> {
            sideMenuOpen = !hide;
            if (hide) {
                sideMenuPane.setVisible(false);
                menuSeparator.setVisible(true);
                tabPane.setEffect(null);
            }
        });
        if (!hide) {
            sideMenuPane.setVisible(true);
            menuSeparator.setVisible(false);
        }

        transition.play();

    }
    public void openSideMenu() {
        animateSideMenu(sideMenuOpen);
    }
    public void closeSideMenu() {
        if (sideMenuOpen) {
            animateSideMenu(true);
        }
    }

    public void openDataTab() {
        if (tabPane.getSelectionModel().getSelectedIndex() != 0)
            cycleFadeNode(tabPane, 400, () -> tabPane.getSelectionModel().select(0));
    }

    public void openArchitectureTab() {
        if (tabPane.getSelectionModel().getSelectedIndex() != 1)
            cycleFadeNode(tabPane, 400, () -> tabPane.getSelectionModel().select(1));
    }

    public void openManagementTab() {
        if (tabPane.getSelectionModel().getSelectedIndex() != 2)
            cycleFadeNode(tabPane, 400, () -> tabPane.getSelectionModel().select(2));
    }

    public void openResultsTab() {
        if (tabPane.getSelectionModel().getSelectedIndex() != 3)
            cycleFadeNode(tabPane, 400, () -> tabPane.getSelectionModel().select(3));
    }

    // Fade out and load main menu
    public void returnToMenu() {
        fadeNode(rootPane, 200, true).setOnFinished(event -> application.loadMainMenu());
    }
}
