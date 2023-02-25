package ANNA.UI.Tabs;

import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import ANNA.UI.UIController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.io.File;
import java.util.List;

public class UIDataController {
    //Class for working with first tab (Data loading)

    public File trainSetFile, testSetFile;
    private TextField trainDataPath, testDataPath;
    private Label trainDataLabel, testDataLabel;
    private TableView<List<String>> trainDataTable, testDataTable;

    private UIController mainController;
    private UIStructureController structureController;
    private UINetworkController networkController;
    private UIOutputController outputController;

    public List<List<String>> rawTrainSet, rawTestSet;

    public UIDataController(UIController controller, TextField trainDataPath, TextField testDataPath, Label trainDataLabel, Label testDataLabel,
                            TableView<List<String>> trainDataTable, TableView<List<String>> testDataTable){
        mainController = controller;
        this.trainDataPath = trainDataPath;
        this.testDataPath = testDataPath;
        this.trainDataLabel = trainDataLabel;
        this.testDataLabel = testDataLabel;
        this.trainDataTable = trainDataTable;
        this.testDataTable = testDataTable;
    }

    public void setControllerReferences(UIStructureController structureController, UINetworkController networkController, UIOutputController outputController){
        this.structureController = structureController;
        this.networkController = networkController;
        this.outputController = outputController;
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
        rawTrainSet = Parser.parseData(trainSetFile);
        loadTable(trainDataTable, rawTrainSet);
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
        rawTestSet = Parser.parseData(testSetFile);
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
            System.err.println("ERROR: Selected file is not valid");
            PopupController.errorMessage("WARNING", "Ошибка", "", "Выбранный файл не действителен.");
        }
        return result;
    }
}
