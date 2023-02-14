package ANNA;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    private File trainSetFile, testSetFile;
    private List<List<String>> rawTrainSet, rawTestSet;
    private ObservableList<Node> trainInputSettings, testInputSettings, architectureSettings;
    private NeuralNetwork.NetworkArguments lastArguments;
    private Parser.inputTypes[] lastInputTypes;

    public void setMain(Main main){
        this.main = main;
    }

    //Initialize components
    public void initialize(){

        inputsChoiceBox.getItems().addAll("Обучающая база данных", "Тестовая база данных");
        inputsChoiceBox.setValue("Выберите базу данных");

        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.setValue("...");

        graphicOutput.getGraphicsContext2D().fillText("В разработке...", 400, 150);

        //Create hyperparameters table
        initializeHyperparameters();

        //Clear simulator
        initializeSimulator(false);
    }

    //Start neural network on train data
    public void startTraining() {
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments(true);
        if(arguments == null)
            return;
        if(autoOpenResults.isSelected())
            tabPane.getSelectionModel().select(3);
        main.runNeuralNetwork(arguments);
        //Enable simulator
        initializeSimulator(true);
    }

    //Start neural network on test data
    public void startTesting() {
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments(false);
        if(arguments == null)
            return;
        if(autoOpenResults.isSelected())
            tabPane.getSelectionModel().select(3);
        main.runNeuralNetwork(arguments);
    }

    //Select train set data
    public void browseForTrainData() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return;
        trainDataPath.setText(path);
    }

    //Select test set data
    public void browseForTestData() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return;
        testDataPath.setText(path);
    }

    //Read and apply train data
    public void applyTrainData() {
        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.getItems().clear();
        trainSetFile = getFileFromPath(trainDataPath.getText());
        if (!trainSetFile.exists()) {
            PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании тренировочной базы данных.");
            return;
        }

        trainDataLabel.setText(trainSetFile.getName());
        //Parse data and add it to the table
        rawTrainSet = Parser.parseData(trainSetFile);
        loadTable(trainDataTable, rawTrainSet);

        //Reset inputs choice box
        inputsChoiceBox.setValue("Выберите базу данных");
        trainInputSettings = null;
        updateInputTable();
    }

    //Read and apply test data
    public void applyTestData() {
        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.getItems().clear();
        testSetFile = getFileFromPath(testDataPath.getText());
        if (!testSetFile.exists()) {
            PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании тестовой базы данных.");
            return;
        }

        testDataLabel.setText(testSetFile.getName());
        //Parse data and add it to the table
        rawTestSet = Parser.parseData(testSetFile);
        loadTable(testDataTable, rawTestSet);

        //Reset inputs choice box
        inputsChoiceBox.setValue("Выберите базу данных");
        lastColumnChoiceBox.setDisable(false);
        lastColumnChoiceBox.setValue("...");
        lastColumnChoiceBox.getItems().addAll(rawTestSet.get(0));
        testInputSettings = null;
        updateInputTable();
    }

    public void updateInputTable(){
        inputVBox.getChildren().remove(2, inputVBox.getChildren().size() - 1);
        lastColumnChoiceBox.getItems().clear();
        lastColumnChoiceBox.setValue("...");
        inputNeuronCounter.setText("...");

        //Switch on selected data set
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            if(trainSetFile == null || !trainSetFile.exists()){
                inputsChoiceBox.setValue("Выберите базу данных");
                inputNeuronButton.setDisable(true);
                inputNeuronRemoveButton.setDisable(true);
                PopupController.errorMessage("WARNING", "Ошибка", "", "Отсутствует обучающая база данных.");
                return;
            }
            //If settings is not valid, create new.
            if(trainInputSettings == null){
                trainInputSettings = FXCollections.observableArrayList(addInputValue(rawTrainSet, 0));
            }
            inputVBox.getChildren().addAll(2, trainInputSettings);
            inputNeuronCounter.setText(String.valueOf(trainInputSettings.size() / 2));
            lastColumnChoiceBox.getItems().addAll(rawTrainSet.get(0));
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
            lastColumnChoiceBox.setDisable(false);
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            if(testSetFile == null || !testSetFile.exists()){
                inputsChoiceBox.setValue("Выберите базу данных");
                inputNeuronButton.setDisable(true);
                inputNeuronRemoveButton.setDisable(true);
                PopupController.errorMessage("WARNING", "Ошибка", "", "Отсутствует тестовая база данных.");
                return;
            }
            //If settings is not valid, create new.
            if(testInputSettings == null){
                testInputSettings = FXCollections.observableArrayList(addInputValue(rawTestSet, 0));
            }
            inputVBox.getChildren().addAll(2, testInputSettings);
            inputNeuronCounter.setText(String.valueOf(testInputSettings.size() / 2));
            lastColumnChoiceBox.getItems().addAll(rawTestSet.get(0));
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
            lastColumnChoiceBox.setDisable(false);
        }else{
            inputNeuronRemoveButton.setDisable(true);
            inputNeuronButton.setDisable(true);
            lastColumnChoiceBox.setDisable(true);
        }
    }

    //Add new input neuron value
    public void addInputNeuron() {
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            trainInputSettings.addAll(addInputValue(rawTrainSet, trainInputSettings.size() / 2));
            updateInputTable();
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            testInputSettings.addAll(addInputValue(rawTestSet, testInputSettings.size() / 2));
            updateInputTable();
        }
    }

    //Create input neuron value
    private List<Node> addInputValue(List<List<String>> data, int number){
        List<Node> result = new ArrayList<>();
        result.add(createInputRow(number, FXCollections.observableArrayList(data.get(0))));
        result.add(new Separator(Orientation.HORIZONTAL));
        return result;
    }

    //Remove last neuron input field
    public void removeInputNeuron() {
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            if(trainInputSettings.size() < 3)
                return;
            trainInputSettings.remove(trainInputSettings.size() - 3 , trainInputSettings.size() - 1);
            updateInputTable();
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            if(testInputSettings.size() < 3)
                return;
            testInputSettings.remove(testInputSettings.size() - 3 , testInputSettings.size() - 1);
            updateInputTable();
        }
    }

    //Create row for table with input neurons
    private HBox createInputRow(int number, ObservableList<String> columnNames){
        HBox result = new HBox();
        result.setPrefHeight(24);
        result.setPrefWidth(100);
        result.setPadding(new Insets(2));

        List<Node> children = new ArrayList<>(5);

        Label num = new Label(Integer.toString(number));
        num.setFont(new Font("Segoe UI Semibold", 14));
        num.setPrefHeight(20);
        num.setPrefWidth(25);

        ChoiceBox<String> columnSelector = new ChoiceBox<>(columnNames);
        if(number >= columnNames.size())
            columnSelector.setValue(columnNames.get(0));
        else
            columnSelector.setValue(columnNames.get(number));
        columnSelector.setPrefHeight(20);
        columnSelector.setPrefWidth(130);

        ChoiceBox<Parser.inputTypes> typeSelector = new ChoiceBox<>(FXCollections.observableArrayList(Parser.inputTypes.values()));
        typeSelector.setValue(Parser.inputTypes.NUMBER);
        typeSelector.setPrefHeight(20);
        typeSelector.setPrefWidth(130);

        //Add them to array
        children.add(num);
        children.add(new Separator(Orientation.VERTICAL));
        children.add(columnSelector);
        children.add(new Separator(Orientation.VERTICAL));
        children.add(typeSelector);

        result.getChildren().addAll(children);
        return result;
    }

    //Add new layer to neural network architecture
    public void addLayer(){
        if(architectureSettings == null){
            architectureSettings = FXCollections.observableArrayList(addLayerValue(1));
        }
        else{
            architectureSettings.addAll(addLayerValue(architectureSettings.size() / 2 + 1));
        }
        updateArchitectureTable();
    }

    //Remove last layer field
    public void removeLayer() {
        if(architectureSettings != null && architectureSettings.size() > 3){
            architectureSettings.remove(architectureSettings.size() - 3, architectureSettings.size() - 1);
        }
        updateArchitectureTable();
    }

    //Refresh architecture table
    private void updateArchitectureTable(){
        architectureHBox.getChildren().remove(4, architectureHBox.getChildren().size() - 4);
        if(architectureSettings == null){
            architectureSettings = FXCollections.observableArrayList(addLayerValue(1));
        }
        architectureHBox.getChildren().addAll(4, architectureSettings);
        lastLayerNumber.setText(String.valueOf(architectureSettings.size() / 2 + 1));
    }

    //Create layer value
    private List<Node> addLayerValue(int number){
        List<Node> result = new ArrayList<>();
        result.add(createLayerColumn(number));
        result.add(new Separator((Orientation.VERTICAL)));
        return result;
    }

    //Create column for table with architecture
    private VBox createLayerColumn(int number){
        VBox result = new VBox();
        result.setPrefHeight(185);
        result.setPrefWidth(90);
        result.setPadding(new Insets(2, 5, 2, 2));

        List<Node> children = new ArrayList<>(5);

        Label num = new Label(Integer.toString(number));
        num.setFont(new Font("Segoe UI Semibold", 14));
        num.setAlignment(Pos.CENTER);
        num.setPrefHeight(30);
        num.setPrefWidth(100);
        num.setMinWidth(100);
        num.setMinHeight(30);

        TextField neuronNumber = new TextField();
        if(inputNeuronCounter.getText() != null && !inputNeuronCounter.getText().equals("..."))
            neuronNumber.setText(String.valueOf(Integer.parseInt(inputNeuronCounter.getText()) * 2));
        else
            neuronNumber.setText(String.valueOf(3));
        neuronNumber.setAlignment(Pos.CENTER);
        neuronNumber.setPrefWidth(100);
        neuronNumber.setPrefHeight(70);
        neuronNumber.setMinWidth(100);
        neuronNumber.setMinHeight(70);

        ChoiceBox<NetworkStructure.neuronTypes> typeSelector = new ChoiceBox<>(FXCollections.observableArrayList(NetworkStructure.neuronTypes.values()));
        typeSelector.setValue(NetworkStructure.neuronTypes.HIDDEN);
        typeSelector.setPrefHeight(70);
        typeSelector.setPrefWidth(100);
        typeSelector.setMinWidth(100);
        typeSelector.setMinHeight(70);

        //Add them to array
        children.add(num);
        children.add(new Separator(Orientation.HORIZONTAL));
        children.add(neuronNumber);
        children.add(new Separator(Orientation.HORIZONTAL));
        children.add(typeSelector);

        result.getChildren().addAll(children);
        return result;
    }

    //Show data on train graph
    public void updateTrainGraph(boolean clear, boolean newSeries, double error, int epoch){
        if(clear)
            trainSetGraph.getData().clear();

        //Create series if not exist
        if(trainSetGraph.getData().isEmpty() || trainSetGraph.getData().get(trainSetGraph.getData().size() - 1) == null || newSeries) {
            XYChart.Series<Integer, Double> series = new XYChart.Series<>();
            series.setName("Средняя ошибка эпохи");
            trainSetGraph.getData().add(series);
        }

        if(trainSetGraph.getData().size() > 5){
            trainSetGraph.getData().remove(0);
        }
        //Add values to the chart
        trainSetGraph.getData().get(trainSetGraph.getData().size() - 1).getData().add(new XYChart.Data<>(epoch, error));
    }

    //Load table to TableView
    private void loadTable(TableView<List<String>> table, List<List<String>> rawData){
        table.getColumns().clear();
        table.getItems().clear();

        //Create columns and add them to the table
        for (int i = 0; i < rawData.get(0).size(); i++) {
            TableColumn<List<String>, String> column = new TableColumn<>(rawData.get(0).get(i));
            int finalI = i;
            //Create cell value factory to show data in table cells
            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(finalI)));
            column.setMinWidth(50);
            column.setPrefWidth(100);
            column.setMaxWidth(200);
            table.getColumns().add(column);
        }

        for (int i = 1; i < rawData.size(); i++) {
            table.getItems().add(rawData.get(i));
        }
    }

    //Get file from path
    private File getFileFromPath(String path){
        File result;
        result = new File(path);
        int index = result.getName().lastIndexOf(".");
        if(index <= 0 || !result.getName().substring(index + 1).equals("csv")) {
            System.err.println("ERROR: Selected file is not valid");
            PopupController.errorMessage("WARNING", "Ошибка", "", "Выбранный файл не действителен.");
        }
        return result;
    }

    //Collect data from UI and create arguments for NN
    private NeuralNetwork.NetworkArguments collectDataToArguments(boolean trainData){
        if(trainData && (trainInputSettings == null || trainInputSettings.size() < 1)){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроены входные нейроны для обучающих данных");
            return null;
        }
        else if (!trainData && (testInputSettings == null || testInputSettings.size() < 1)){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроены входные нейроны для тестовых данных");
            return null;
        }
        if(trainData && (lastColumnChoiceBox.getValue().equals("...") || lastColumnChoiceBox.getValue() == null)){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не выбран проверочный столбец для обучающих данных");
            return null;
        }
        if(architectureSettings == null || architectureSettings.size() < 1){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроена структура нейронной сети");
            return null;
        }

        //Local variables
        ObservableList<Node> currentInputNeuronSet = trainData ? trainInputSettings : testInputSettings;
        List<List<String>> currentRawDataSet = trainData ? rawTrainSet : rawTestSet;

        int[] architecture = new int[2 + architectureSettings.size() / 2];
        double[][] inputs = new double[currentRawDataSet.size() - 1][currentInputNeuronSet.size() / 2];
        lastInputTypes = new Parser.inputTypes[currentInputNeuronSet.size() / 2];
        String[] expectedOutput = new String[currentRawDataSet.size() - 1];
        List<String> allOutputTypes = new ArrayList<>();
        int logEpoch = Integer.parseInt(updateResultsEpoch.getText()); //TODO Check for only digits in field

        //Collect architecture data
        architecture[0] = currentInputNeuronSet.size() / 2;
        for(int i = 0; i < architectureSettings.size(); i+=2) {
            VBox vBox = (VBox) architectureSettings.get(i);
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

    //Create/update hyperparameters UI
    private void initializeHyperparameters(){
        hyperparametersVBox.getChildren().remove(2, hyperparametersVBox.getChildren().size());

        for(Hyperparameters.identificator i: Hyperparameters.identificator.values()){
            hyperparametersVBox.getChildren().addAll(createHyperparametersRow(Hyperparameters.getData(i, true),
                    Hyperparameters.getValueByID(i),
                    Hyperparameters.getData(i, false)));
        }
    }

    private List<Node> createHyperparametersRow(String name, String defaultValue, String description){
        //Create row
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMinHeight(80);
        hBox.setPadding(new Insets(2));

        //Name of hyperparameters
        Label nameLabel = new Label(name);
        nameLabel.setFont(new Font("Segoe UI SemiBold", 12));
        nameLabel.setPrefWidth(140);
        nameLabel.setWrapText(true);

        TextField valueField = new TextField(defaultValue);
        valueField.setPromptText("Введите значение");
        valueField.setPrefWidth(150);

        Label descriptionLabel = new Label(description);
        descriptionLabel.setFont(new Font("Segoe UI SemiLight", 12));
        descriptionLabel.setPrefWidth(280);
        descriptionLabel.setWrapText(true);

        hBox.getChildren().add(nameLabel);
        hBox.getChildren().add(new Separator(Orientation.VERTICAL));
        hBox.getChildren().add(valueField);
        hBox.getChildren().add(new Separator(Orientation.VERTICAL));
        hBox.getChildren().add(descriptionLabel);

        List<Node> result = new ArrayList<>(2);
        result.add(hBox);
        result.add(new Separator(Orientation.HORIZONTAL));

        return  result;
    }

    //Create/update simulator table
    private void initializeSimulator(boolean enable){
        simulatorHBox.getChildren().remove(2, simulatorHBox.getChildren().size());
        simulatorHBox.setDisable(!enable);
        startSimulatorButton.setDisable(!enable);
        if(lastArguments == null || lastInputTypes == null) {
            simulatorHBox.setDisable(true);
            startSimulatorButton.setDisable(true);
            return;
        }

        for (int i = 0; i < lastArguments.architecture()[0]; i++) {
            simulatorHBox.getChildren().addAll(createSimulatorColumn(i));
        }
    }

    //Create input column for simulator
    private List<Node> createSimulatorColumn(int numberOfNeuron){
        //Create column
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPrefHeight(100);
        vBox.setPrefWidth(90);
        vBox.setMinWidth(90);

        //Create label and field
        Label name = new Label(String.valueOf(numberOfNeuron));
        name.setFont(new Font("Segoe UI SemiBold", 14));
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(90);
        name.setPrefHeight(50);

        TextField field = new TextField();
        field.setFont(new Font("Segoe UI SemiLight", 12));
        field.setAlignment(Pos.CENTER);
        field.prefWidth(90);
        field.setPrefHeight(50);

        //Add children nto VBox
        vBox.getChildren().add(name);
        vBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        vBox.getChildren().add(field);

        //Create output list
        List<Node> output = new ArrayList<>(2);
        output.add(vBox);
        output.add(new Separator(Orientation.VERTICAL));

        return output;
    }

    //Prepare and run simulator
    public void prepareSimulation(){
        double[] inputs = new double[(simulatorHBox.getChildren().size() - 2) / 2];

        for(int i = 2; i < simulatorHBox.getChildren().size(); i+=2) {
            VBox vBox = (VBox) simulatorHBox.getChildren().get(i);
            TextField field = (TextField) vBox.getChildren().get(2);
            inputs[i / 2 - 1] = Parser.parseRawValue(field.getText(), lastInputTypes[i / 2 - 1]);
        }

        main.runSimulation(inputs);
    }

    //Add simulation output to UI
    public void simulationResult(double[] outputValues, String outputCategory){
        String text = "Наиболее вероятная категория:\n" + outputCategory + "\nВыходные значения нейронной сети:\n" + Arrays.toString(outputValues);
        simulatorOutput.setText(text);
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
}
