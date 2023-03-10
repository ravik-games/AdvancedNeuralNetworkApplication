package ANNA.UI;

import ANNA.*;
import ANNA.Network.NeuralNetwork;
import ANNA.UI.Tabs.UIDataController;
import ANNA.UI.Tabs.UINetworkController;
import ANNA.UI.Tabs.UIOutputController;
import ANNA.UI.Tabs.UIStructureController;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class UIController {
    public CheckBox autoOpenResults;
    public Button inputNeuronButton, layerAddButton, inputNeuronRemoveButton, layerRemoveButton, startSimulatorButton;
    public VBox inputVBox, hyperparametersVBox;
    public HBox architectureHBox, simulatorHBox;
    public TabPane tabPane;
    public Canvas graphicOutput;
    public TextField updateResultsEpoch, trainDataPath, testDataPath, loadArchitecturePath, loadWeightsPath, loadHyperparametersPath,
            loadNeuralNetworkPath, saveArchitecturePath, saveWeightsPath, saveHyperparametersPath, saveNeuralNetworkPath;
    public TableView<List<String>> trainDataTable, testDataTable;
    public Label trainDataLabel, testDataLabel, inputNeuronCounter, lastLayerNumber;
    public LineChart<Integer, Double> trainSetGraph, testSetGraph;
    public ChoiceBox<String> inputsChoiceBox, lastColumnChoiceBox;
    public TextArea simulatorOutput;

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
        dataController = new UIDataController(this, trainDataPath, testDataPath, trainDataLabel, testDataLabel, trainDataTable, testDataTable);
        structureController = new UIStructureController(this, inputVBox, architectureHBox, inputsChoiceBox, lastColumnChoiceBox, inputNeuronCounter, lastLayerNumber, inputNeuronRemoveButton, inputNeuronButton, graphicOutput);
        networkController = new UINetworkController(this, hyperparametersVBox, updateResultsEpoch);
        outputController = new UIOutputController(this, simulatorOutput, startSimulatorButton, simulatorHBox, trainSetGraph, testSetGraph);

        dataController.setControllerReferences(structureController, networkController, outputController);
        structureController.setControllerReferences(dataController, networkController, outputController);
        networkController.setControllerReferences(dataController, structureController, outputController);
        outputController.setControllerReferences(dataController, structureController, networkController);

        inputsChoiceBox.getItems().addAll("?????????????????? ???????? ????????????", "???????????????? ???????? ????????????");
        inputsChoiceBox.setValue("???????????????? ???????? ????????????");

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
        dataController.applyTestData();

        //Reset inputs choice box
        inputsChoiceBox.setValue("???????????????? ???????? ????????????");
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
        dataController.applyTrainData();

        //Reset inputs choice box
        inputsChoiceBox.setValue("???????????????? ???????? ????????????");
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

    public void addLayer() {
        structureController.addLayer();
    }

    public void removeLayer() {
        structureController.removeLayer();
    }

    public void startTraining() {
        networkController.startTraining();
    }

    public void startTesting() {
        networkController.startTesting();
    }

    public void prepareSimulation() {
        outputController.prepareSimulation();
    }
}
