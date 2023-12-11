package anna.ui.tabs;

import anna.Application;
import anna.network.data.DataMaster;
import anna.ui.DefaultUIController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class UIDataController {
    //Class for working with first tab (Data loading)

    public TextField trainingPartField, testingPartField;
    public Label trainDataLabel, testDataLabel, generalDataLabel;
    public TableView<List<String>> trainDataTable, testDataTable, generalDataTable;
    public Pane dataPartitionPane;
    public CheckBox autoDatasetCheckBox, previewAutoDatasetCheckBox;
    public TabPane dataLoaderPane;
    public Button loadTrainDataButton, loadTestDataButton;

    protected Application application;
    protected DefaultUIController masterController;
    protected DataMaster dataMaster;
    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    // Initialize fields on Data tab
    public void initialize() {
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

    public void setReferences(Application application, DataMaster dataMaster, DefaultUIController uiController) {
        this.application = application;
        this.dataMaster = dataMaster;
        masterController = uiController;
    }

    // Load general dataset and parse it
    public boolean loadGeneralDataset() {
        if (!dataMaster.loadGeneralDataset())
            return false;

        generalDataLabel.setText(dataMaster.getGeneralSet().file().getName());
        loadTable(generalDataTable, dataMaster.getGeneralSet().data(), dataMaster.getGeneralSet().labels());

        splitDataset();

        masterController.structureController.resetTab();

        return true;
    }

    public void splitDataset() {

        // Split dataset in parts
        if  (!dataMaster.splitGeneralDataset(Double.parseDouble(trainingPartField.getText()) / 100))
            return;

        // Load new datasets in tables
        trainDataLabel.setText("train");
        testDataLabel.setText("test");
        loadTable(trainDataTable, dataMaster.getTrainingSet().data(), dataMaster.getTrainingSet().labels());
        loadTable(testDataTable, dataMaster.getTestingSet().data(), dataMaster.getTestingSet().labels());
    }

    // Change data loading mode on checkbox state change
    public void autoDatasetCheck() {
        if (autoDatasetCheckBox.isSelected()) {
            dataPartitionPane.setVisible(true);
            dataPartitionPane.setManaged(true);
            previewAutoDatasetCheckBox.setSelected(false);
        }

        masterController.fadeNode(dataPartitionPane, 300, !autoDatasetCheckBox.isSelected()).setOnFinished(event ->  {
            if(!autoDatasetCheckBox.isSelected()) {
                dataPartitionPane.setManaged(false);
                dataPartitionPane.setVisible(false);
            }
        });
        masterController.cycleFadeNode(dataLoaderPane, 300, () -> {
            loadTrainDataButton.setDisable(false);
            loadTestDataButton.setDisable(false);
            dataLoaderPane.getSelectionModel().select(autoDatasetCheckBox.isSelected() ? 0 : 1);

            // Clear tables
            clearTables();
        });
    }

    public void previewAutoDataset() {
        // Re-split dataset and show it. Probably can cause some performance issues.
        if (previewAutoDatasetCheckBox.isSelected())
            splitDataset();

        masterController.cycleFadeNode(dataLoaderPane, 300, () -> {
            loadTrainDataButton.setDisable(previewAutoDatasetCheckBox.isSelected());
            loadTestDataButton.setDisable(previewAutoDatasetCheckBox.isSelected());
            dataLoaderPane.getSelectionModel().select(previewAutoDatasetCheckBox.isSelected() ? 1 : 0);
        });
    }

    // Load training dataset and parse it
    public boolean loadTrainData() {
        if (!dataMaster.loadTrainingDataset())
            return false;

        trainDataLabel.setText(dataMaster.getTrainingSet().file().getName());
        loadTable(trainDataTable, dataMaster.getTrainingSet().data(), dataMaster.getTrainingSet().labels());

        masterController.structureController.resetTab();

        return true;
    }

    // Load general dataset and parse it
    public boolean loadTestData() {
        if (!dataMaster.loadTestingDataset())
            return false;

        testDataLabel.setText(dataMaster.getTestingSet().file().getName());
        loadTable(testDataTable, dataMaster.getTestingSet().data(), dataMaster.getTestingSet().labels());

        masterController.structureController.resetTab();

        return true;
    }

    // Method to limit user if data tab is not configured
    public boolean checkDataStatus() {
        return !dataMaster.areDatasetsNotValid();
    }

    public void clearTables() {
        generalDataTable.getColumns().clear();
        testDataTable.getColumns().clear();
        trainDataTable.getColumns().clear();
        generalDataLabel.setText("---");
        trainDataLabel.setText("---");
        testDataLabel.setText("---");

        dataMaster.clearData();
        masterController.structureController.resetTab();
    }

    //Load table to TableView
    private void loadTable(TableView<List<String>> table, List<List<String>> rawData, List<String> labels){
        table.getColumns().clear();
        table.getItems().clear();

        // Create first column for row numbers
        TableColumn<List<String>, String> numberColumn = getTableColumn();
        table.getColumns().add(numberColumn);

        //Create columns and add them to the table
        for (int i = 0; i < labels.size(); i++) {
            TableColumn<List<String>, String> column = new TableColumn<>(labels.get(i));
            int finalI = i + 1;
            //Create cell value factory to show data in table cells
            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(finalI)));
            column.setMinWidth(50);
            column.setPrefWidth(100);
            column.setMaxWidth(200);
            table.getColumns().add(column);
        }

        //Add data to table
        for (int i = 0; i < rawData.size(); i++) {
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
        numberColumn.setMinWidth(50);
        numberColumn.setPrefWidth(50);
        numberColumn.setMaxWidth(75);
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
