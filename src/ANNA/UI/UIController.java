package ANNA.UI;

import ANNA.*;
import ANNA.Network.NeuralNetwork;
import ANNA.UI.Tabs.UIDataController;
import ANNA.UI.Tabs.UINetworkController;
import ANNA.UI.Tabs.UIOutputController;
import ANNA.UI.Tabs.UIStructureController;
import javafx.collections.ObservableList;
import javafx.scene.Node;
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
import java.util.ArrayList;
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

    private Main main;
    public NeuralNetwork.NetworkArguments lastArguments;
    public Parser.inputTypes[] lastInputTypes;

    public UIDataController dataController;
    public UIStructureController structureController;
    public UINetworkController networkController;
    public UIOutputController outputController;
    public void setMain(Main main){
        this.main = main;
        networkController.setMain(main);
        outputController.setMain(main);
    }

    //Initialize components
    public void initialize(){
        dataController = new UIDataController(this, trainDataPath, testDataPath, trainDataLabel, testDataLabel, trainDataTable, testDataTable);
        structureController = new UIStructureController(this, inputVBox, architectureHBox, inputsChoiceBox, lastColumnChoiceBox, inputNeuronCounter, lastLayerNumber, inputNeuronRemoveButton, inputNeuronButton);
        networkController = new UINetworkController(this, hyperparametersVBox);
        System.out.println(main);
        outputController = new UIOutputController(this, simulatorOutput, startSimulatorButton, simulatorHBox, trainSetGraph, testSetGraph);

        dataController.setControllerReferences(structureController, networkController, outputController);
        structureController.setControllerReferences(dataController, networkController, outputController);
        networkController.setControllerReferences(dataController, structureController, outputController);
        outputController.setControllerReferences(dataController, structureController, networkController);

        inputsChoiceBox.getItems().addAll("Обучающая база данных", "Тестовая база данных");
        inputsChoiceBox.setValue("Выберите базу данных");

        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.setValue("...");

        graphicOutput.getGraphicsContext2D().fillText("В разработке...", 400, 150);

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
        dataController.applyTrainData();

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
    //Collect data from UI and create arguments for NN
    public NeuralNetwork.NetworkArguments collectDataToArguments(boolean trainData){
        if(trainData && (structureController.trainInputSettings == null || structureController.trainInputSettings.size() < 1)){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроены входные нейроны для обучающих данных");
            return null;
        }
        else if (!trainData && (structureController.testInputSettings == null || structureController.testInputSettings.size() < 1)){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроены входные нейроны для тестовых данных");
            return null;
        }
        if(trainData && (lastColumnChoiceBox.getValue().equals("...") || lastColumnChoiceBox.getValue() == null)){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не выбран проверочный столбец для обучающих данных");
            return null;
        }
        if(structureController.architectureSettings == null || structureController.architectureSettings.size() < 1){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроена структура нейронной сети");
            return null;
        }

        //Local variables
        ObservableList<Node> currentInputNeuronSet = trainData ? structureController.trainInputSettings : structureController.testInputSettings;
        List<List<String>> currentRawDataSet = trainData ? dataController.rawTrainSet : dataController.rawTestSet;

        int[] architecture = new int[2 + structureController.architectureSettings.size() / 2];
        double[][] inputs = new double[currentRawDataSet.size() - 1][currentInputNeuronSet.size() / 2];
        lastInputTypes = new Parser.inputTypes[currentInputNeuronSet.size() / 2];
        String[] expectedOutput = new String[currentRawDataSet.size() - 1];
        List<String> allOutputTypes = new ArrayList<>();
        int logEpoch = Integer.parseInt(updateResultsEpoch.getText()); //TODO Check for only digits in field

        //Collect architecture data
        architecture[0] = currentInputNeuronSet.size() / 2;
        for(int i = 0; i < structureController.architectureSettings.size(); i+=2) {
            VBox vBox = (VBox) structureController.architectureSettings.get(i);
            TextField textField = (TextField) vBox.getChildren().get(2);
            architecture[i / 2 + 1] = Integer.parseInt(textField.getText()); //TODO Check for only digits in field
        }

        //Create inputs from existing data
        int[] reassign = new int[currentInputNeuronSet.size() / 2]; //Each value corresponds to column number in raw data
        Parser.inputTypes[] types = new Parser.inputTypes[currentInputNeuronSet.size() / 2]; //Each value will be parsed according to set type
        int expectedColumn = currentRawDataSet.get(0).indexOf(lastColumnChoiceBox.getValue());
        for(int i = 0; i < currentInputNeuronSet.size(); i+=2) {
            HBox hBox = (HBox) currentInputNeuronSet.get(i);
            ChoiceBox<String> column = (ChoiceBox<String>) hBox.getChildren().get(2);
            ChoiceBox<Parser.inputTypes> type = (ChoiceBox<Parser.inputTypes>) hBox.getChildren().get(4);
            lastInputTypes[i / 2] = type.getValue();
            int position = currentRawDataSet.get(0).indexOf(column.getValue());
            types[i / 2] = type.getValue();
            reassign[i / 2] = position;
        }

        //Parse raw data file
        for(int i = 1; i < currentRawDataSet.size(); i++) {
            for (int j = 0; j < reassign.length; j++) {
                inputs[i - 1][j] = Parser.parseRawValue(currentRawDataSet.get(i).get(reassign[j]), types[j]);
            }
            //Set expected outputs for training set
            String idealValue = currentRawDataSet.get(i).get(expectedColumn);
            expectedOutput[i - 1] = idealValue;
            if(!allOutputTypes.contains(idealValue)){
                allOutputTypes.add(idealValue);
            }
        }

        //Set output layer neuron counter
        architecture[architecture.length - 1] = allOutputTypes.size();

        lastArguments = new NeuralNetwork.NetworkArguments(architecture, null, inputs, expectedOutput, allOutputTypes.toArray(new String[0]), trainData, this, logEpoch);
        return lastArguments;
    }
}
