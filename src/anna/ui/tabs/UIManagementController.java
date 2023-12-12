package anna.ui.tabs;

import anna.Application;
import anna.network.data.DataMaster;
import anna.math.ActivationFunctions;
import anna.math.ErrorFunctions;
import anna.network.data.DataTypes;
import anna.network.data.Hyperparameters;
import anna.network.NetworkStructure;
import anna.network.NeuralNetwork;
import anna.ui.DefaultUIController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

public class UIManagementController {
    //Class for working with third tab (Network control)

    public VBox hyperparametersVBox;
    public TextField updateResultsEpoch;
    public CheckBox autoOpenResultsCheckBox;
    public FontIcon startNetworkStatus, saveNetworkInfo, hyperparametersHint;

    protected Application application;
    protected DefaultUIController masterController;
    protected DataMaster dataMaster;
    private UIStructureController structureController;

    protected List<HyperparametersRow> hyperparametersRowList;

    protected static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    public void initialize() {
        hyperparametersRowList = new ArrayList<>();
        initializeHyperparameters();
    }

    public void setReferences(Application application, DataMaster dataMaster, DefaultUIController masterController, UIStructureController structureController){
        this.application = application;
        this.dataMaster = dataMaster;
        this.masterController = masterController;
        this.structureController = structureController;

        setupHints();
    }

    protected void setupHints() {
        masterController.setupHint(saveNetworkInfo, bundle.getString("tab.management.hints.networkSaveLoad"));
        masterController.setupHint(startNetworkStatus, bundle.getString("tab.management.hints.networkStart"));
        masterController.setupHint(hyperparametersHint, bundle.getString("tab.management.hints.hyperparameters"));
    }

    public void startNeuralNetwork(){
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments();

        if(arguments == null)
            return;

        if(autoOpenResultsCheckBox.isSelected())
            masterController.openResultsTab();
        application.runNeuralNetwork(arguments);

        //Enable simulator
        masterController.outputController.initializeSimulator(true);
    }

    // Create or update hyperparameters list
    public void initializeHyperparameters(){
        hyperparametersRowList.clear();
        hyperparametersVBox.getChildren().clear();

        for(Hyperparameters.Identificator hyperparameter: Hyperparameters.Identificator.values()){
            HyperparametersRow row = new HyperparametersRow(hyperparameter);
            hyperparametersRowList.add(row);
            hyperparametersVBox.getChildren().add(row.getRow());
            VBox.setVgrow(row.getRow(), Priority.ALWAYS);
        }
    }

    //Collect data from UI and create arguments for NN
    public NeuralNetwork.NetworkArguments collectDataToArguments(){
        //Change mode DEPRECATED, USE ONLY FOR CLASSIFICATION (FALSE)
        boolean isPrediction = false;

        //Log preparation time
        long startTime = System.nanoTime();
        LOGGER.info("--- Starting preparation ---");

        int logEpoch = Integer.parseInt(updateResultsEpoch.getText());


        DataTypes.NetworkData networkData = new DataTypes.NetworkData(new ArrayList<>(2 + structureController.getArchitectureLayers().size()), new ArrayList<>());
        List<DataTypes.InputParameterData> inputParameters = structureController.getInputParameters();
        DataTypes.Dataset trainData = dataMaster.prepareDataset(true, isPrediction,
                inputParameters, structureController.getTargetParameter());
        DataTypes.Dataset testData =  dataMaster.prepareDataset(false, isPrediction,
                inputParameters, structureController.getTargetParameter());

        //Collect architecture data
        networkData.getStructure().add(new DataTypes.NetworkData.LayerData(structureController.getInputParameters().size(), NetworkStructure.LayerTypes.INPUT,
                ActivationFunctions.Types.LINEAR)); // Input layer
        networkData.getStructure().addAll(structureController.getArchitectureLayers().stream()
                .map(layer -> new DataTypes.NetworkData.LayerData(layer.getNeuronNumber(), layer.getType(), layer.getActivationFunction()))
                .toList());
        networkData.getStructure().add(new DataTypes.NetworkData.LayerData(trainData.allOutputTypes().length, NetworkStructure.LayerTypes.OUTPUT,
                structureController.lastLayerActivationFunction.getValue())); // Output layer //TODO For classification only

        masterController.lastArguments = new NeuralNetwork.NetworkArguments(networkData, trainData, testData, isPrediction, masterController, logEpoch);

        //Log end time
        long elapsedTime = System.nanoTime() - startTime;
        LOGGER.info("--- Preparation finished ---");
        LOGGER.info("Elapsed time: " + (elapsedTime / 1000000000) + "s\t" + (elapsedTime / 1000000) + "ms\t" + elapsedTime + "ns\n");
        //Print time in seconds, milliseconds and nanoseconds

        return masterController.lastArguments;
    }

    public static class HyperparametersRow {
        protected HBox row;
        protected Label name, description;
        protected Control value;
        protected Hyperparameters.Identificator hyperparameter;

        public HyperparametersRow(Hyperparameters.Identificator hyperparameter) {
            this.hyperparameter = hyperparameter;
            String style = "-fx-border-color: #4769ff;" +
                    "-fx-border-width: 0 1 0 0;";

            // Initialize components
            name = new Label(hyperparameter.getName());
            name.setPadding(new Insets(15));
            name.setFont(Font.font("Inter", 14));
            name.setMaxHeight(Double.MAX_VALUE);
            name.setWrapText(true);
            name.setPrefWidth(200);
            name.setMinWidth(200);
            name.setAlignment(Pos.CENTER_LEFT);
            name.setStyle(style);

            // Define value field in this row
            switch (hyperparameter.getType()){
                case BOOLEAN -> {
                    CheckBox checkBox = new CheckBox();
                    if(hyperparameter == Hyperparameters.Identificator.USE_BIAS_NEURONS) {
                        checkBox.setSelected(Hyperparameters.USE_BIAS_NEURONS);
                        // Set hyperparameter value
                        checkBox.setOnAction(event -> Hyperparameters.USE_BIAS_NEURONS = checkBox.isSelected());
                    }

                    value = checkBox;
                }
                case POSITIVE_DOUBLE, POSITIVE_INT, DOUBLE -> value = getTextField(hyperparameter);
                case ENUM -> {
                    if (hyperparameter == Hyperparameters.Identificator.NETWORK_WEIGHT_INITIALIZATION){
                        ChoiceBox<Hyperparameters.WeightsInitializationType> choiceBox = new ChoiceBox<>();
                        choiceBox.getItems().addAll(Hyperparameters.WeightsInitializationType.values());
                        choiceBox.getSelectionModel().select(Hyperparameters.NETWORK_WEIGHT_INITIALIZATION);
                        // Set hyperparameter value
                        choiceBox.setOnAction(event -> Hyperparameters.NETWORK_WEIGHT_INITIALIZATION = choiceBox.getValue());

                        value = choiceBox;

                    } else if (hyperparameter == Hyperparameters.Identificator.ERROR_FUNCTION) {
                        ChoiceBox<ErrorFunctions.Types> choiceBox = new ChoiceBox<>();
                        choiceBox.getItems().addAll(ErrorFunctions.Types.values());
                        choiceBox.getSelectionModel().select(Hyperparameters.ERROR_FUNCTION);
                        // Set hyperparameter value
                        choiceBox.setOnAction(event -> Hyperparameters.ERROR_FUNCTION = choiceBox.getValue());

                        value = choiceBox;
                    }
                }
            }
            value.setMaxWidth(Double.MAX_VALUE);
            BorderPane valuePane = new BorderPane(value);
            valuePane.setStyle(style);
            valuePane.setMaxHeight(Double.MAX_VALUE);
            valuePane.setPrefWidth(250);
            valuePane.setMinWidth(250);
            valuePane.setPadding(new Insets(15));
            BorderPane.setAlignment(valuePane, Pos.CENTER_LEFT);

            description = new Label(hyperparameter.getDescription());
            description.setPadding(new Insets(15));
            description.setFont(Font.font("Inter", 14));
            description.setWrapText(true);
            description.setMaxHeight(Double.MAX_VALUE);
            description.setMaxWidth(Double.MAX_VALUE);
            description.setMinWidth(300);
            description.setAlignment(Pos.CENTER_LEFT);

            row = new HBox(name, valuePane, description);
            row.setMaxHeight(Double.MAX_VALUE);
            row.setStyle("-fx-border-color: #4769ff;" +
                    "-fx-border-width: 0 0 1 0;");

            // Set HBox constrains for children
            row.setFillHeight(true);
            HBox.setHgrow(name, Priority.NEVER);
            HBox.setHgrow(valuePane, Priority.NEVER);
            HBox.setHgrow(description, Priority.ALWAYS);
        }

        private static TextField getTextField(Hyperparameters.Identificator hyperparameter) {
            TextField textField = new TextField();

            UnaryOperator<TextFormatter.Change> filter = getFilter(hyperparameter);
            switch (hyperparameter) {
                case NUMBER_OF_EPOCHS -> textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), Hyperparameters.NUMBER_OF_EPOCHS, filter));
                case BATCH_SIZE -> textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), Hyperparameters.BATCH_SIZE, filter));
                case LEARNING_RATE -> textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), Hyperparameters.LEARNING_RATE, filter));
                case MOMENTUM -> textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), Hyperparameters.MOMENTUM, filter));
                case SLOPE_IN_ACTIVATION_FUNCTIONS -> textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, filter));
            }

            // Set hyperparameter value
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.isEmpty())
                    return;
                switch (hyperparameter) {
                    case NUMBER_OF_EPOCHS -> Hyperparameters.NUMBER_OF_EPOCHS = Integer.parseInt(newValue);
                    case BATCH_SIZE -> Hyperparameters.BATCH_SIZE = Integer.parseInt(newValue);
                    case LEARNING_RATE -> Hyperparameters.LEARNING_RATE = Double.parseDouble(newValue);
                    case MOMENTUM -> Hyperparameters.MOMENTUM = Double.parseDouble(newValue);
                    case SLOPE_IN_ACTIVATION_FUNCTIONS -> Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS = Double.parseDouble(newValue);
                }
            });

            return textField;
        }

        private static UnaryOperator<TextFormatter.Change> getFilter(Hyperparameters.Identificator hyperparameter) {
            String regex = switch (hyperparameter.getType()) {
                case DOUBLE -> "([+-]?\\d*[.,]?\\d*)?";
                case POSITIVE_INT -> "([1-9]\\d*)?";
                case POSITIVE_DOUBLE -> "(\\d*[.,]?\\d*)?";
                default -> "";
            };

            // Strict field to digits only
            return change -> {
                String newText = change.getControlNewText();
                if (newText.matches(regex))
                    return change;
                return null;
            };
        }

        public HBox getRow() {
            return row;
        }
    }
}
