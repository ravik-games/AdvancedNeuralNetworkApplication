package ANNA;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UIController {
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField trainDataPath;
    @FXML
    private TableView<List<String>> trainDataTable;
    @FXML
    private Label trainDataLabel;
    @FXML
    private LineChart<Integer, Double> trainSetGraph, testSetGraph;
    @FXML
    private ChoiceBox<String> trainSeparator, inputsChoiceBox;

    private Main main;
    private File trainSet, testSet;
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
        String path = openExplorer();
        if(path == null)
            return;
        trainDataPath.setText(path);
    }

    //Read and apply train data
    public void applyTrainData(ActionEvent actionEvent) {
        trainSet = getFileFromPath(trainDataPath.getText());
        if (!trainSet.exists()) {
            errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании тренировочной базы данных.");
            return;
        }
        //Update table
        loadTable(trainDataTable, trainSeparator.getValue(), 110);
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

    //Load table to TableView
    private void loadTable(TableView<List<String>> table, String separator, int columnWidth){
        trainDataLabel.setText(trainSet.getName());
        table.getColumns().clear();
        table.getItems().clear();

        try {
            BufferedReader bReader = new BufferedReader(new FileReader(trainSet));
            String line = bReader.readLine();

            //Create columns and add them to the table
            String[] split = line.split(separator);
            for (int i = 0; i < split.length; i++) {
                TableColumn<List<String>, String> column = new TableColumn<>(split[i]);
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

            //Read next line
            line = bReader.readLine();

            //Read all lines in file
            while(line != null){
                List<String> vals = new ArrayList<>(List.of(line.split(separator)));
                table.getItems().add(vals);
                line = bReader.readLine();
            }

        }catch (IOException e){
            e.printStackTrace();
            errorMessage("ERROR", "Ошибка", "", e.toString());
        }
    }

    //Open window and select file
    private String openExplorer(){
        FileChooser open = new FileChooser();
        File file;
        open.setTitle("Выберите данные");
        String currentDir = System.getProperty("user.dir") + "//src//resources";
        open.setInitialDirectory(new File(currentDir));
        open.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".csv files", "*.csv"));
        file = open.showOpenDialog(null);
        if(file == null) {
            System.err.println("ERROR: Selected file is not valid");
            errorMessage("WARNING", "Ошибка", "", "Выбранный файл не действителен.");
            return null;
        }
        return file.getAbsolutePath();
    }

    //Get file from path
    private File getFileFromPath(String path){
        File result;
        result = new File(path);
        int index = result.getName().lastIndexOf(".");
        if(index <= 0 || !result.getName().substring(index + 1).equals("csv")) {
            System.err.println("ERROR: Selected file is not valid");
            errorMessage("WARNING", "Ошибка", "", "Выбранный файл не действителен.");
        }
        return result;
    }

    //Method for showing errors to user
    public static void errorMessage(String rawType, String title, String headerMessage, String infoMessage){
        Alert.AlertType type = switch (rawType) {
            case "ERROR" -> Alert.AlertType.ERROR;
            case "INFORMATION" -> Alert.AlertType.INFORMATION;
            default -> Alert.AlertType.WARNING;
        };
        Alert alert= new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }
}
