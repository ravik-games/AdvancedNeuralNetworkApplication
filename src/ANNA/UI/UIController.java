package ANNA.UI;

import ANNA.Main;
import ANNA.Network.DataTypes;
import ANNA.Network.NeuralNetwork;
import ANNA.UI.Tabs.UIDataController;
import ANNA.UI.Tabs.UINetworkController;
import ANNA.UI.Tabs.UIOutputController;
import ANNA.UI.Tabs.UIStructureController;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

public class UIController{
    //Master class for all UI stuff

    public CheckBox autoOpenResults;
    public Button inputNeuronButton, layerAddButton, inputNeuronRemoveButton, inputNeuronAutoButton, layerRemoveButton, startSimulatorButton, newWindowChartButton, newWindowMatrixButton;
    public VBox inputVBox, hyperparametersVBox, statVBox;
    public HBox architectureHBox, simulatorHBox;
    public TabPane tabPane;
    public Canvas graphicOutput;
    public TextField updateResultsEpoch, trainDataPath, testDataPath, loadArchitecturePath, loadWeightsPath, loadHyperparametersPath,
            loadNeuralNetworkPath, saveArchitecturePath, saveWeightsPath, saveHyperparametersPath, saveNeuralNetworkPath;
    public TableView<List<String>> trainDataTable, testDataTable;
    public Label trainDataLabel, testDataLabel, inputNeuronCounter, lastLayerNumber, chartLabel, matrixLabel;
    public LineChart<Integer, Double> chart;
    public NumberAxis chartXAxis;
    public NumberAxis chartYAxis;
    public ChoiceBox<String> inputsChoiceBox, lastColumnChoiceBox, statClassChoiceBox, matrixDataChoiceBox;
    public TextArea simulatorOutput;
    public Pane chartParent, matrixParent;

    public NeuralNetwork.NetworkArguments lastArguments;
    public Parser.inputTypes[] lastInputTypes;

    public UIDataController dataController;
    public UIStructureController structureController;
    public UINetworkController networkController;
    public UIOutputController outputController;
    public void setMain(Main main){
        networkController.setMain(main);
        outputController.setMain(main);
    }

    //Initialize components
    public void initialize(){
        dataController = new UIDataController(trainDataPath, testDataPath, trainDataLabel, testDataLabel, trainDataTable, testDataTable);
        structureController = new UIStructureController(inputVBox, architectureHBox, inputsChoiceBox, lastColumnChoiceBox, inputNeuronCounter, lastLayerNumber, inputNeuronRemoveButton, inputNeuronButton, inputNeuronAutoButton, graphicOutput);
        networkController = new UINetworkController(this, hyperparametersVBox, updateResultsEpoch);
        outputController = new UIOutputController(this, simulatorOutput, startSimulatorButton, simulatorHBox, statVBox, chart, chartLabel, matrixLabel, statClassChoiceBox, matrixDataChoiceBox);

        structureController.setControllerReferences(dataController);
        networkController.setControllerReferences(dataController, structureController, outputController);

        inputsChoiceBox.getItems().addAll("Обучающая база данных", "Тестовая база данных");
        inputsChoiceBox.setValue("Выберите базу данных");

        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.setValue("...");

        graphicOutput.getGraphicsContext2D().fillText("...", graphicOutput.getWidth() / 2, graphicOutput.getHeight() / 2);

        //Create hyperparameters table
        networkController.initializeHyperparameters();

        //Clear simulator
        outputController.initializeSimulator(false);
    }

    public void browseForTestData() {
        dataController.browseForTestData();
    }

    public void applyTestData() {
        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.getItems().clear();
        if(!dataController.applyTestData())
            return;

        //Reset inputs choice box
        inputsChoiceBox.setValue("Выберите базу данных");
        lastColumnChoiceBox.setDisable(false);
        lastColumnChoiceBox.setValue("...");
        lastColumnChoiceBox.getItems().addAll(dataController.rawTestSet.get(0));
        structureController.testInputSettings = null;
        updateInputTable();
    }

    public void browseForTrainData() {
        dataController.browseForTrainData();
    }

    public void applyTrainData() {
        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.getItems().clear();
        if(!dataController.applyTrainData())
            return;

        //Reset inputs choice box
        inputsChoiceBox.setValue("Выберите базу данных");
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

    public void browseForArchitecture() {
    }

    public void browseForWeights() {
    }

    public void browseForHyperparameters() {
    }

    public void browseForNeuralNetwork() {
    }

    public void saveArchitecture() {
    }

    public void saveWeights() {
    }

    public void saveHyperparameters() {
    }

    public void saveNeuralNetwork() {
    }

    public void browseSaveArchitecture() {
    }

    public void browseSaveWeights() {
    }

    public void browseSaveNeuralNetwork() {
    }

    public void browseSaveHyperparameters() {
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
    public void changeMatrixLeft() {
        outputController.switchSelector(1, true);
    }
    public void changeMatrixRight() {
        outputController.switchSelector(1, true);
    }
    public void setChartForceYZero(boolean value){
        chartYAxis.setForceZeroInRange(value);
    }

    public void updateResults(boolean clear, int epoch, List<DataTypes.Evaluation> trainEvaluation, List<DataTypes.Evaluation> testEvaluation) {
        outputController.lastTrainEvaluation = trainEvaluation;
        outputController.lastTestEvaluation = testEvaluation;
        outputController.updateChart(clear, epoch + 1);
        outputController.updateStatistics(clear);
        outputController.updateSingleClassMatrix(clear);
        outputController.updateFullMatrix(true);
    }
}
