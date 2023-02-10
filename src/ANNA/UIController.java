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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UIController {
    public CheckBox autoOpenResults;
    public Button inputNeuronButton, layerAddButton, inputNeuronRemoveButton, layerRemoveButton;
    public VBox inputVBox, hyperparametersVBox;
    public HBox architectureHBox;
    public TabPane tabPane;
    public Canvas graphicOutput;
    public TextField updateResultsEpoch, trainDataPath, testDataPath, loadArchitecturePath, loadWeightsPath, loadHyperparametersPath, loadNeuralNetworkPath, saveArchitecturePath, saveWeightsPath, saveHyperparametersPath, saveNeuralNetworkPath;
    public TableView<List<String>> trainDataTable, testDataTable;
    public Label trainDataLabel, testDataLabel, inputNeuronCounter, lastLayerNumber;
    public LineChart<Integer, Double> trainSetGraph, testSetGraph;
    public ChoiceBox<String> trainSeparator, testSeparator, inputsChoiceBox, lastColumnChoiceBox;

    private Main main;
    private String trainLastColumn, testLastColumn; //TODO Replace single choice box with same system, as with input neurons
    private File trainSetFile, testSetFile;
    private List<List<String>> rawTrainSet, rawTestSet;
    private ObservableList<Node> trainInputSettings, testInputSettings, architectureSettings;

    public void setMain(Main main){
        this.main = main;
    }

    //Initialize components
    public void initialize(){
        trainSeparator.getItems().addAll(",", ";");
        trainSeparator.setValue(";");
        testSeparator.getItems().addAll(",", ";");
        testSeparator.setValue(";");

        inputsChoiceBox.getItems().addAll("Обучающая база данных", "Тестовая база данных");
        inputsChoiceBox.setValue("Выберите базу данных");

        lastColumnChoiceBox.setDisable(true);
        lastColumnChoiceBox.setValue("...");

        graphicOutput.getGraphicsContext2D().fillText("В разработке...", 400, 150);

        //Create hyperparameters table
        initializeHyperparameters();
    }

    //Start neural network on train data
    public void startTraining() {
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments(true);
        if(arguments == null)
            return;
        if(autoOpenResults.isSelected())
            tabPane.getSelectionModel().select(3);
        main.train(arguments);
    }

    //Start neural network on test data
    public void startTesting() {
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments(false);
        if(arguments == null)
            return;
        if(autoOpenResults.isSelected())
            tabPane.getSelectionModel().select(3);
        main.train(arguments);
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
        rawTrainSet = parseData(trainSetFile, trainSeparator.getValue());
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
        rawTestSet = parseData(testSetFile, testSeparator.getValue());
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

        ChoiceBox<inputTypes> typeSelector = new ChoiceBox<>(FXCollections.observableArrayList(inputTypes.values()));
        typeSelector.setValue(inputTypes.NUMBER);
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

    //Parse data from file to 2D list
    public List<List<String>> parseData(File file, String separator){
        List<List<String>> result = new ArrayList<>();
        try {
            BufferedReader bReader = new BufferedReader(new FileReader(file));
            String line = bReader.readLine();
            //Read all lines in file
            do{
                result.add(List.of(line.split(separator)));
                line = bReader.readLine();
            }while(line != null);

        }catch (IOException e){
            e.printStackTrace();
            PopupController.errorMessage("ERROR", "Ошибка", "", e.toString());
            System.exit(1);
        }
        return result;
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
            column.setMinWidth(100 >> 1);
            column.setPrefWidth(100);
            column.setMaxWidth(100 * 2);
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
        double[][] expectedOutputs = new double[currentRawDataSet.size() - 1][1];
        int logEpoch = Integer.parseInt(updateResultsEpoch.getText()); //TODO Check for only digits in field

        //Collect architecture data
        architecture[0] = currentInputNeuronSet.size() / 2;
        for(int i = 0; i < architectureSettings.size(); i+=2) {
            VBox vBox = (VBox) architectureSettings.get(i);
            TextField textField = (TextField) vBox.getChildren().get(2);
            architecture[i / 2 + 1] = Integer.parseInt(textField.getText()); //TODO Check for only digits in field
        }
        architecture[architecture.length - 1] = 1;

        //Create inputs from existing data
        int[] reassign = new int[currentInputNeuronSet.size() / 2]; //Each value corresponds to column number in raw data
        inputTypes[] types = new inputTypes[currentInputNeuronSet.size() / 2]; //Each value will be parsed according to set type
        int expectedColumn = currentRawDataSet.get(0).indexOf(lastColumnChoiceBox.getValue());
        for(int i = 0; i < currentInputNeuronSet.size(); i+=2) {
            HBox hBox = (HBox) currentInputNeuronSet.get(i);
            ChoiceBox<String> column = (ChoiceBox<String>) hBox.getChildren().get(2);
            ChoiceBox<inputTypes> type = (ChoiceBox<inputTypes>) hBox.getChildren().get(4);
            int position = currentRawDataSet.get(0).indexOf(column.getValue());
            types[i / 2] = type.getValue();
            reassign[i / 2] = position;
        }
        for(int i = 1; i < currentRawDataSet.size(); i++) {
            for (int j = 0; j < reassign.length; j++) {
                inputs[i - 1][j] = parseRawValue(currentRawDataSet.get(i).get(reassign[j]), types[j]);
            }
            //Set expected outputs for training set
            expectedOutputs[i - 1][0] = Double.parseDouble(currentRawDataSet.get(i).get(expectedColumn));
        }

        return new NeuralNetwork.NetworkArguments(architecture, null, inputs, expectedOutputs, trainData, this, logEpoch);
    }

    //Method for parsing raw set values
    private double parseRawValue(String value, inputTypes type){
        switch(type){
            case NUMBER -> {
                return Double.parseDouble(value);
            }
            case BOOLEAN -> {
                value = value.toLowerCase();
                if(value.equals("true") || value.equals("1"))
                    return 1;
                else
                    return 0;
            }
            default -> {
                PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании базы данных. Ошибочные входные значения будут заменены нулями");
                return 0;
            }
        }
    }

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

    private enum inputTypes{
        NUMBER, BOOLEAN
    }
}
