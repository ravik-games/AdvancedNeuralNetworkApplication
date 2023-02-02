package ANNA;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UIController {
    @FXML
    private Button inputNeuronButton;
    @FXML
    private VBox inputVBox;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField trainDataPath, testDataPath;
    @FXML
    private TableView<List<String>> trainDataTable, testDataTable;
    @FXML
    private Label trainDataLabel, testDataLabel;
    @FXML
    private LineChart<Integer, Double> trainSetGraph, testSetGraph;
    @FXML
    private ChoiceBox<String> trainSeparator, testSeparator , inputsChoiceBox;

    private Main main;
    private File trainSetFile, testSetFile;
    private List<List<String>> rawTrainSet, rawTestSet;
    private ObservableList<Node> trainInputSettings, testInputSettings;
    private NeuralNetwork.NetworkArguments trainArguments = new NeuralNetwork.NetworkArguments(
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

        inputsChoiceBox.getItems().addAll("Тренировочная база данных", "Проверочная база данных");
        inputsChoiceBox.setValue("Выберите базу данных");
    }

    //Start neural network
    public void startTraining(ActionEvent actionEvent) {
        tabPane.getSelectionModel().select(3);
        main.train(trainArguments);
    }

    //Select train set data
    public void browseForTrainData(ActionEvent actionEvent) {
        String path =  PopupController.openExplorer();
        if(path == null)
            return;
        trainDataPath.setText(path);
    }

    //Select test set data
    public void browseForTestData(ActionEvent actionEvent) {
        String path =  PopupController.openExplorer();
        if(path == null)
            return;
        testDataPath.setText(path);
    }

    //Read and apply train data
    public void applyTrainData(ActionEvent actionEvent) {
        trainSetFile = getFileFromPath(trainDataPath.getText());
        if (!trainSetFile.exists()) {
            PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании тренировочной базы данных.");
            return;
        }

        trainDataLabel.setText(trainSetFile.getName());
        //Parse data and add it to the table
        rawTrainSet = parseData(trainSetFile, trainSeparator.getValue());
        loadTable(trainDataTable, rawTrainSet, 100);

        //Reset inputs choice box
        inputsChoiceBox.setValue("Выберите базу данных");
        trainInputSettings = null;
        updateInputTable();
    }

    //Read and apply test data
    public void applyTestData(ActionEvent actionEvent) {
        testSetFile = getFileFromPath(testDataPath.getText());
        if (!testSetFile.exists()) {
            PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании тестовой базы данных.");
            return;
        }

        testDataLabel.setText(testSetFile.getName());
        //Parse data and add it to the table
        rawTestSet = parseData(testSetFile, testSeparator.getValue());
        loadTable(testDataTable, rawTestSet, 100);

        //Reset inputs choice box
        inputsChoiceBox.setValue("Выберите базу данных");
        testInputSettings = null;
        updateInputTable();
    }

    public void updateInputTable(){
        inputVBox.getChildren().remove(2, inputVBox.getChildren().size() - 1);

        //Switch on selected data set
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            if(trainSetFile == null || !trainSetFile.exists()){
                inputsChoiceBox.setValue("Выберите базу данных");
                inputNeuronButton.setDisable(true);
                PopupController.errorMessage("WARNING", "Ошибка", "", "Отсутствует тренировочная база данных.");
                return;
            }
            //If settings is not valid, create new.
            if(trainInputSettings == null){
                trainInputSettings = FXCollections.observableArrayList(addInputValue(rawTrainSet, 0));
            }
            inputVBox.getChildren().addAll(2, trainInputSettings);
            inputNeuronButton.setDisable(false);
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            if(testSetFile == null || !testSetFile.exists()){
                inputsChoiceBox.setValue("Выберите базу данных");
                inputNeuronButton.setDisable(true);
                PopupController.errorMessage("WARNING", "Ошибка", "", "Отсутствует проверочная база данных.");
                return;
            }
            //If settings is not valid, create new.
            if(testInputSettings == null){
                testInputSettings = FXCollections.observableArrayList(addInputValue(rawTestSet, 0));
            }
            inputVBox.getChildren().addAll(2, testInputSettings);
            inputNeuronButton.setDisable(false);
        }else{
            inputNeuronButton.setDisable(true);
        }
    }

    //Add new input neuron value
    public void addInputNeuron(ActionEvent actionEvent) {
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
        columnSelector.setPrefHeight(20);
        columnSelector.setPrefWidth(130);

        ChoiceBox<inputTypes> typeSelector = new ChoiceBox<>(FXCollections.observableArrayList(inputTypes.values()));
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
        trainSetGraph.getData().get(trainSetGraph.getData().size() - 1).getData().add(new XYChart.Data<Integer, Double>(epoch, error));
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
    private void loadTable(TableView<List<String>> table, List<List<String>> rawData, int columnWidth){
        table.getColumns().clear();
        table.getItems().clear();

        //Create columns and add them to the table
        for (int i = 0; i < rawData.get(0).size(); i++) {
            TableColumn<List<String>, String> column = new TableColumn<>(rawData.get(0).get(i));
            int finalI = i;
            //Create cell value factory to show data in table cells
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().get(finalI));
                }
            });
            column.setMinWidth(columnWidth >> 1);
            column.setPrefWidth(columnWidth);
            column.setMaxWidth(columnWidth * 2);
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

    private enum inputTypes{
        INT, DOUBLE, BOOLEAN;
    }
}
