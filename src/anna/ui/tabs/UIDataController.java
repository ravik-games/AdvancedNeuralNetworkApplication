package anna.ui.tabs;

import anna.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class UIDataController {
    //Class for working with first tab (Data loading)

    public File trainSetFile, testSetFile, generalSetFile;
    protected final TextField trainDataPath, testDataPath, trainingPartField, testingPartField;
    protected final Label trainDataLabel, testDataLabel, generalDataLabel;
    protected final TableView<List<String>> trainDataTable, testDataTable, generalDataTable;

    public List<List<String>> rawTrainSet, rawTestSet, generalSet;

    protected Application application;
    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    // Initialize fields on Data tab
    public UIDataController(TextField trainDataPath, TextField testDataPath, Label trainDataLabel, Label testDataLabel, Label generalDataLabel,
                            TableView<List<String>> trainDataTable, TableView<List<String>> testDataTable, TableView<List<String>> generalDataTable,
                            TextField trainingPartField, TextField testingPartField){
        this.trainDataPath = trainDataPath;
        this.testDataPath = testDataPath;
        this.trainDataLabel = trainDataLabel;
        this.testDataLabel = testDataLabel;
        this.generalDataLabel = generalDataLabel;
        this.trainDataTable = trainDataTable;
        this.testDataTable = testDataTable;
        this.generalDataTable = generalDataTable;
        this.trainingPartField = trainingPartField;
        this.testingPartField = testingPartField;

        // Strict part fields to digits only
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([1-9][0-9]?)?")) {
                return change;
            }
            return null;
        };
        trainingPartField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        testingPartField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        // Set number to change if other field is changed
        trainingPartField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty())
                testingPartField.setText(String.valueOf(100 - Integer.parseInt(newValue)));
        }));
        testingPartField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty())
                trainingPartField.setText(String.valueOf(100 - Integer.parseInt(newValue)));
        }));
        // Set default value
        trainingPartField.setText("75");
    }

    public void setMain(Application application) {
        this.application = application;
    }

    // Load general dataset and parse it
    public boolean loadGeneralDataset() {
        if (!application.getDataMaster().loadGeneralDataset())
            return false;

        generalDataLabel.setText(application.getDataMaster().getGeneralSetFile().getName());
        loadTable(generalDataTable, application.getDataMaster().getRawGeneralSet());

        splitDataset();

        return true;
    }

    public void splitDataset() {
        // Split dataset in parts
        application.getDataMaster().splitGeneralDataset(Double.parseDouble(trainingPartField.getText()) / 100);

        // Load new datasets in tables
        trainDataLabel.setText("train");
        testDataLabel.setText("test");
        loadTable(trainDataTable, application.getDataMaster().getRawTrainSet());
        loadTable(testDataTable, application.getDataMaster().getRawTestSet());
    }

    // Load training dataset and parse it
    public boolean loadTrainingDataset() {
        if (!application.getDataMaster().loadTrainingDataset())
            return false;

        trainDataLabel.setText(application.getDataMaster().getTrainSetFile().getName());
        loadTable(trainDataTable, application.getDataMaster().getRawTrainSet());
        return true;
    }

    // Load general dataset and parse it
    public boolean loadTestingDataset() {
        if (!application.getDataMaster().loadTestingDataset())
            return false;

        testDataLabel.setText(application.getDataMaster().getTestSetFile().getName());
        loadTable(testDataTable, application.getDataMaster().getRawTestSet());
        return true;
    }

    public void clearTables() {
        generalDataTable.getColumns().clear();
        testDataTable.getColumns().clear();
        trainDataTable.getColumns().clear();
        generalDataLabel.setText("---");
        trainDataLabel.setText("---");
        testDataLabel.setText("---");

        application.getDataMaster().clearData();
    }

    //Load table to TableView
    private void loadTable(TableView<java.util.List<String>> table, java.util.List<java.util.List<String>> rawData){
        table.getColumns().clear();
        table.getItems().clear();

        // Create first column for row numbers
        TableColumn<List<String>, String> numberColumn = getTableColumn();
        table.getColumns().add(numberColumn);

        //Create columns and add them to the table
        for (int i = 0; i < rawData.get(0).size(); i++) {
            TableColumn<List<String>, String> column = new TableColumn<>(rawData.get(0).get(i));
            int finalI = i + 1;
            //Create cell value factory to show data in table cells
            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(finalI)));
            column.setMinWidth(50);
            column.setPrefWidth(100);
            column.setMaxWidth(200);
            table.getColumns().add(column);
        }

        //Add data to table
        for (int i = 1; i < rawData.size(); i++) {
            ArrayList<String> list = new ArrayList<>(rawData.get(i));
            list.add(0, Integer.toString(i));
            //list.set(0, Integer.toString(i));
            table.getItems().add(list);
        }
    }

    private static TableColumn<List<String>, String> getTableColumn() {
        TableColumn<List<String>, String> numberColumn = new TableColumn<>("#");
        numberColumn.setStyle("-fx-font-weight: bold;" +
                            "-fx-font-style: italic");
        numberColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
        numberColumn.setMinWidth(20);
        numberColumn.setPrefWidth(50);
        numberColumn.setMaxWidth(200);
        //Set valid integer comparator
        numberColumn.setComparator((o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;

            Integer i1=null;
            try{ i1=Integer.valueOf(o1); } catch(NumberFormatException ignored){}
            Integer i2=null;
            try{ i2=Integer.valueOf(o2); } catch(NumberFormatException ignored){}

            if(i1==null && i2==null) return o1.compareTo(o2);
            if(i1==null) return -1;
            if(i2==null) return 1;

            return i1-i2;
        });
        return numberColumn;
    }
}
