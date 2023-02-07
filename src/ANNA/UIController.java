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
    public ChoiceBox<String> trainSeparator, testSeparator , inputsChoiceBox;

    private Main main;
    private File trainSetFile, testSetFile;
    private List<List<String>> rawTrainSet, rawTestSet;
    private ObservableList<Node> trainInputSettings, testInputSettings, architectureSettings;
    private final NeuralNetwork.NetworkArguments trainArguments = new NeuralNetwork.NetworkArguments(
            new int[]{3, 6, 6, 1},
            null,
            new double[][]{{0, 0, 0}, {0, 0, 1}, {0, 1, 0}, {0, 1, 1}, {1, 0, 0}, {1, 0, 1}, {1, 1, 0}, {1, 1, 1}},
            new double[][]{{0}, {1}, {1}, {0}, {1}, {0}, {0}, {1}},
            this,
            Hyperparameters.NUMBER_OF_EPOCHS / 100);

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
    }

    //Start neural network on train data
    public void startTraining() {
        if(autoOpenResults.isSelected())
            tabPane.getSelectionModel().select(3);
        main.train(trainArguments);
    }

    //Start neural network on test data
    public void startTesting() {
        if(autoOpenResults.isSelected())
            tabPane.getSelectionModel().select(3);
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
        testInputSettings = null;
        updateInputTable();
    }

    public void updateInputTable(){
        inputVBox.getChildren().remove(2, inputVBox.getChildren().size() - 1);
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
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
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
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
        }else{
            inputNeuronRemoveButton.setDisable(true);
            inputNeuronButton.setDisable(true);
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

        TextField neuronNumber = new TextField(Integer.toString(3));
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

        if(architectureSettings == null || architectureSettings.size() < 1){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Не настроена структура нейронной сети");
            return null;
        }

        //Local variables
        ObservableList<Node> currentInputNeuronSet = trainData ? trainInputSettings : testInputSettings;
        int[] architecture = new int[2 + architectureSettings.size() / 2];
        double[][] inputs = new double[0][];
        double[][] expectedOutputs = new double[0][];
        int logEpoch = Integer.parseInt(updateResultsEpoch.getText()); //TODO Check for only digits in field

        //Collect architecture data
        architecture[0] = currentInputNeuronSet.size() / 2;
        for(int i = 0; i < architectureSettings.size(); i+=2) {
            VBox vBox = (VBox) architectureSettings.get(i);
            TextField textField = (TextField) vBox.getChildren().get(2);
            architecture[i / 2] = Integer.parseInt(textField.getText()); //TODO Check for only digits in field
        }
        architecture[architecture.length - 1] = 1;

        NeuralNetwork.NetworkArguments arguments = new NeuralNetwork.NetworkArguments(architecture, null, inputs, expectedOutputs, this, logEpoch);
        System.out.println(arguments);
        return arguments;
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
