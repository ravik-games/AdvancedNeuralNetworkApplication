package anna.ui;

import anna.Application;
import anna.network.DataTypes;
import anna.network.NeuralNetwork;
import anna.ui.tabs.UIDataController;
import anna.ui.tabs.UINetworkController;
import anna.ui.tabs.UIOutputController;
import anna.ui.tabs.UIStructureController;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DefaultUIController implements UIController {
    //Master class for all UI stuff

    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization
    protected Application application;

    public CheckBox autoOpenResults, autoDatasetCheckBox, previewAutoDatasetCheckBox;
    public Button inputNeuronButton, layerAddButton, inputNeuronRemoveButton, inputNeuronAutoButton, layerRemoveButton, startSimulatorButton, newWindowChartButton,
            newWindowMatrixButton, loadTrainDataButton, loadTestDataButton;
    public VBox inputVBox, hyperparametersVBox, statVBox;
    public HBox architectureHBox, simulatorHBox, rootPane, sideMenuPane;
    public TabPane tabPane, dataLoaderPane;
    public Canvas graphicOutput;
    public TextField trainingPartField, testingPartField, updateResultsEpoch, trainDataPath, testDataPath, loadArchitecturePath, loadWeightsPath, loadHyperparametersPath,
            loadNeuralNetworkPath, saveArchitecturePath, saveWeightsPath, saveHyperparametersPath, saveNeuralNetworkPath;
    public TableView<List<String>> trainDataTable, testDataTable, generalDataTable;
    public Label trainDataLabel, testDataLabel, generalDataLabel, inputNeuronCounter, lastLayerNumber, chartLabel, matrixLabel;
    public LineChart<Integer, Double> chart;
    public NumberAxis chartXAxis;
    public NumberAxis chartYAxis;
    public ChoiceBox<String> inputsChoiceBox, lastColumnChoiceBox, statClassChoiceBox, matrixDataChoiceBox;
    public TextArea simulatorOutput;
    public Pane chartParent, matrixParent, dataPartitionPane;
    public FontIcon datasetInfo, autoPartitionInfo;
    public StackPane menuStackPane;
    public Separator menuSeparator;


    public NeuralNetwork.NetworkArguments lastArguments;
    public Parser.inputTypes[] lastInputTypes;

    public UIDataController dataController;
    public UIStructureController structureController;
    public UINetworkController networkController;
    public UIOutputController outputController;

    protected boolean sideMenuOpen;

    //Initialize components
    public void initialize(){
        // Set invisible for fade in animation
        rootPane.setOpacity(0);

        dataController = new UIDataController(trainDataPath, testDataPath, trainDataLabel, testDataLabel, generalDataLabel, trainDataTable, testDataTable, generalDataTable,
                trainingPartField, testingPartField);
        /*structureController = new UIStructureController(this, inputVBox, architectureHBox, inputsChoiceBox, lastColumnChoiceBox, inputNeuronCounter, lastLayerNumber, inputNeuronRemoveButton, inputNeuronButton, inputNeuronAutoButton, graphicOutput);
        networkController = new UINetworkController(this, hyperparametersVBox, updateResultsEpoch);
        outputController = new UIOutputController(this, simulatorOutput, startSimulatorButton, simulatorHBox, statVBox, chart, chartLabel, matrixLabel, statClassChoiceBox, matrixDataChoiceBox);

        networkController.setControllerReferences(dataController, structureController, outputController);

        inputsChoiceBox.getItems().addAll(bundle.getString("tab.architecture.trainingDatabase"), bundle.getString("tab.architecture.testingDatabase"));
        inputsChoiceBox.setValue(bundle.getString("tab.architecture.selectDatabase"));

        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.setValue("...");

        graphicOutput.getGraphicsContext2D().fillText("...", graphicOutput.getWidth() / 2, graphicOutput.getHeight() / 2);

        //Create hyperparameters table
        networkController.initializeHyperparameters();

        //Clear simulator
        outputController.initializeSimulator(false);*/

        // Set correct order for side menu animation
        menuStackPane.setViewOrder(1);

        //Setup all hints
        setupHints();

        // Wait for initialization to fade in
        Platform.runLater(() -> fadeNode(rootPane, 200, false));
    }

    public void applyTestData() {
        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.getItems().clear();

        //Reset inputs choice box
        inputsChoiceBox.setValue(bundle.getString("tab.architecture.selectDatabase"));
        lastColumnChoiceBox.setDisable(false);
        lastColumnChoiceBox.setValue("...");
        lastColumnChoiceBox.getItems().addAll(dataController.rawTestSet.get(0));
        structureController.testInputSettings = null;
        updateInputTable();
    }

    public void applyTrainData() {
        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.getItems().clear();

        //Reset inputs choice box
        inputsChoiceBox.setValue(bundle.getString("tab.architecture.selectDatabase"));
        lastColumnChoiceBox.setDisable(false);
        lastColumnChoiceBox.setValue("...");
        lastColumnChoiceBox.getItems().addAll(dataController.rawTrainSet.get(0));
        structureController.trainInputSettings = null;
        updateInputTable();
    }

    public void loadArchitecture() {
    }

    public void loadWeights() {
    }

    public void loadHyperparameters() {
    }

    public void loadNeuralNetwork() {
    }

    public void saveArchitecture() {
    }

    public void saveWeights() {
    }

    public void saveHyperparameters() {
    }

    public void saveNeuralNetwork() {
    }

    public void updateInputTable() {
        structureController.updateInputTable();
    }
    public void addInputNeuron() {
        structureController.addInputNeuron();
    }
    public void removeInputNeuron() {
        structureController.removeInputNeuron();
    }
    public void autoInputNeuron() {
        structureController.autoInputNeurons();
    }
    public void addLayer() {
        structureController.addLayer();
    }
    public void removeLayer() {
        structureController.removeLayer();
    }
    public void startTraining() {
        networkController.startTraining();
    }
    public void prepareSimulation() {
        outputController.prepareSimulation();
    }

    //Open new window
    public void newWindowMatrix() {
        outputController.openMatrixInNewWindow(newWindowMatrixButton, matrixParent);
    }
    public void newWindowChart() {
        outputController.openChartInNewWindow(newWindowChartButton, chartParent);
    }

    //Change output selectors
    public void changeChartLeft() {
        outputController.switchSelector(0, true);
    }
    public void changeChartRight() {
        outputController.switchSelector(0, false);
    }
    public void setChartForceYZero(boolean value){
        chartYAxis.setForceZeroInRange(value);
    }

    //Interface methods implementations
    @Override
    public void clearResults(int chartXLength) {
        outputController.clearCharts();
        outputController.setChartXLength(chartXLength);
    }

    @Override
    public void updateResults(boolean clear, int epoch, List<DataTypes.Evaluation> trainEvaluation, List<DataTypes.Evaluation> testEvaluation) {
        outputController.lastTrainEvaluation = trainEvaluation;
        outputController.lastTestEvaluation = testEvaluation;
        outputController.updateChart(clear, epoch + 1);
        outputController.updateStatistics(clear);
        outputController.updateSingleClassMatrix(clear);
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
        dataController.setMain(application);
        //networkController.setMain(application);
        //outputController.setMain(application);
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
        Tooltip.install(autoPartitionInfo, autoPartition);
    }

    // Change data loading mode on checkbox state change
    public void autoDatasetCheck() {
        if (autoDatasetCheckBox.isSelected()) {
            dataPartitionPane.setVisible(true);
            dataPartitionPane.setManaged(true);
            previewAutoDatasetCheckBox.setSelected(false);
        }

        fadeNode(dataPartitionPane, 300, !autoDatasetCheckBox.isSelected()).setOnFinished(event ->  {
            if(!autoDatasetCheckBox.isSelected()) {
                dataPartitionPane.setManaged(false);
                dataPartitionPane.setVisible(false);
            }
        });
        cycleFadeNode(dataLoaderPane, 300, () -> {
            loadTrainDataButton.setDisable(false);
            loadTestDataButton.setDisable(false);
            dataLoaderPane.getSelectionModel().select(autoDatasetCheckBox.isSelected() ? 0 : 1);

            // Clear tables
            dataController.clearTables();
        });
    }

    public void previewAutoDataset() {
        // Re-split dataset and show it. Probably can cause some performance issues.
        if (previewAutoDatasetCheckBox.isSelected())
            dataController.splitDataset();

        cycleFadeNode(dataLoaderPane, 300, () -> {
            loadTrainDataButton.setDisable(previewAutoDatasetCheckBox.isSelected());
            loadTestDataButton.setDisable(previewAutoDatasetCheckBox.isSelected());
            dataLoaderPane.getSelectionModel().select(previewAutoDatasetCheckBox.isSelected() ? 1 : 0);
        });
    }

    public void loadGeneralDataset() {
        dataController.loadGeneralDataset();
    }
    public void loadTrainData() {
        dataController.loadTrainingDataset();
    }
    public void loadTestData() {
        dataController.loadTestingDataset();
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
