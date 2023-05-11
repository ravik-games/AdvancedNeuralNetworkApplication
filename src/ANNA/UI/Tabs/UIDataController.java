package ANNA.UI.Tabs;

import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UIDataController {
    //Class for working with first tab (Data loading)

    public File trainSetFile, testSetFile;
    private final TextField trainDataPath, testDataPath;
    private final Label trainDataLabel, testDataLabel;
    private final TableView<List<String>> trainDataTable, testDataTable;

    public List<List<String>> rawTrainSet, rawTestSet;

    public UIDataController(TextField trainDataPath, TextField testDataPath, Label trainDataLabel, Label testDataLabel,
                            TableView<List<String>> trainDataTable, TableView<List<String>> testDataTable){
        this.trainDataPath = trainDataPath;
        this.testDataPath = testDataPath;
        this.trainDataLabel = trainDataLabel;
        this.testDataLabel = testDataLabel;
        this.trainDataTable = trainDataTable;
        this.testDataTable = testDataTable;
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
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. The file was not found.");
            PopupController.errorMessage("WARNING", "Ошибка при считывании базы данных", "", "Произошла ошибка при считывании тренировочной базы данных. Файл не найден.");
            return;
        }

        trainDataLabel.setText(trainSetFile.getName());
        //Parse data and add it to the table
        rawTrainSet = Parser.parseData(trainSetFile);
        if(rawTrainSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. Failed to read file.");
            PopupController.errorMessage("WARNING", "Ошибка при считывании базы данных", "", "Произошла ошибка при считывании тренировочной базы данных. Не удалось прочитать файл.");
            return;
        }
        loadTable(trainDataTable, rawTrainSet);
    }

    //Read and apply test data
    public void applyTestData() {
        testSetFile = getFileFromPath(testDataPath.getText());
        if (!testSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the testing database. The file was not found.");
            PopupController.errorMessage("WARNING", "Ошибка при считывании базы данных", "", "Произошла ошибка при считывании тестовой базы данных. Файл не найден.");
            return;
        }

        testDataLabel.setText(testSetFile.getName());
        //Parse data and add it to the table
        rawTestSet = Parser.parseData(testSetFile);
        if(rawTestSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the testing database. Failed to read file.");
            PopupController.errorMessage("WARNING", "Ошибка при считывании базы данных", "", "Произошла ошибка при считывании тестовой базы данных. Не удалось прочитать файл.");
            return;
        }
        loadTable(testDataTable, rawTestSet);
    }

    //Load table to TableView
    private void loadTable(TableView<java.util.List<String>> table, java.util.List<java.util.List<String>> rawData){
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
            Logger.getLogger(getClass().getName()).log(Level.WARNING ,"Selected file is not valid");
            PopupController.errorMessage("WARNING", "Ошибка", "", "Выбранный файл не действителен.");
        }
        return result;
    }
}
