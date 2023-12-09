package anna.ui.tabs;

import anna.Application;
import anna.DataMaster;
import anna.math.ActivationFunctions;
import anna.network.NetworkStructure;
import anna.ui.DefaultUIController;
import anna.ui.Parser;
import anna.ui.PopupController;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class UIStructureController {
    //Class for working with second tab of UI (Structure creation)
    public HBox inputLayerBox, architectureLayerInfoPane, architectureLayerManagementPane;
    public ChoiceBox<String> classParameterChoiceBox, networkTaskChoiceBox;
    public ChoiceBox<ActivationFunctions.types> lastLayerActivationFunction;
    public CheckBox autoConfigureCheckBox;
    public Label lastLayerNumber;
    public Canvas architectureCanvas;
    public Pane inputLayerPane, inputPaneAnimationPane, canvasPane;

    protected DefaultUIController masterController;
    protected Application application;
    protected DataMaster dataMaster;

    protected List<InputLayerColumn> inputLayerColumns;
    private List<ArchitectureLayer> architectureLayers;

    public ObservableList<Node> trainInputSettings, testInputSettings, architectureSettings;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    public void initialize() {
        networkTaskChoiceBox.getSelectionModel().select(bundle.getString("tab.architecture.networkTask.classification"));
        networkTaskChoiceBox.setDisable(true);

        inputLayerColumns = new ArrayList<>();
        architectureLayers = new ArrayList<>();

        lastLayerActivationFunction.getSelectionModel().select(ActivationFunctions.types.SIGMOID);

        // Set canvas dynamic resize
        canvasPane.heightProperty().addListener((observable, oldValue, newValue) -> architectureCanvas.setHeight(newValue.doubleValue() - 1));
        //canvasPane.widthProperty().addListener((observable, oldValue, newValue) -> architectureCanvas.setWidth(newValue.doubleValue() - 1));

        // Set initial height for correct animation
        Platform.runLater(() -> {
            inputPaneAnimationPane.setPrefHeight(inputLayerPane.getHeight());
            inputPaneAnimationPane.setScaleY(0);

            // Set default values for animation
            inputLayerPane.setManaged(false);
            inputLayerPane.setVisible(false);
        });
    }

    public void setReferences(Application application, DataMaster dataMaster, DefaultUIController masterController) {
        this.application = application;
        this.dataMaster = dataMaster;
        this.masterController = masterController;
    }

    // Hide and show configuration for input layer
    public void autoConfigureInputLayer() {
        if (!autoConfigureCheckBox.isSelected()){
            inputLayerPane.setOpacity(0);
            inputLayerPane.setVisible(true);
        }
        else {
            inputLayerPane.setManaged(false);
            inputPaneAnimationPane.setPrefHeight(inputLayerPane.getHeight());
        }

        SequentialTransition transition = createInputLayerAnimation();

        transition.setOnFinished(event -> {
            inputLayerPane.setManaged(!autoConfigureCheckBox.isSelected());
            if (autoConfigureCheckBox.isSelected()){
                inputLayerPane.setVisible(false);
            }
        });
    }

    private SequentialTransition createInputLayerAnimation() {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), inputPaneAnimationPane);
        scaleTransition.setFromY(autoConfigureCheckBox.isSelected() ? 1 : 0);
        scaleTransition.setToY(autoConfigureCheckBox.isSelected() ? 0 : 1);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), inputLayerPane);
        fadeTransition.setFromValue(autoConfigureCheckBox.isSelected() ? 1 : 0);
        fadeTransition.setToValue(autoConfigureCheckBox.isSelected() ? 0 : 1);

        SequentialTransition transition = autoConfigureCheckBox.isSelected() ?
                new SequentialTransition(fadeTransition, scaleTransition) : new SequentialTransition(scaleTransition, fadeTransition);
        transition.play();
        return transition;
    }

    public void updateInputList() {
        if (!dataMaster.areDatasetsValid()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "There is no database found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.noDatabase"));
            return;
        }

        // Update list
        inputLayerBox.getChildren().clear();
        inputLayerColumns.clear();
        classParameterChoiceBox.getItems().clear();
        // Add default parameters
        List<String> labels = dataMaster.getTrainingSet().labels();
        for (int i = 0; i < labels.size() - 1; i++) {
            addNewInputParameter(i);
        }
        classParameterChoiceBox.getItems().addAll(labels);
        classParameterChoiceBox.getSelectionModel().select(labels.size() - 1);

    }

    // Methods for input list control
    public void addNewInputParameter(int i) {
        InputLayerColumn column = new InputLayerColumn(i, dataMaster.getTrainingSet().labels(), dataMaster.getTrainingSet().labels().get(0), Parser.inputTypes.NUMBER);

        inputLayerColumns.add(i, column);
        inputLayerBox.getChildren().add(i, column.getColumn());
        IntStream.range(i + 1, inputLayerColumns.size()).forEach(j -> inputLayerColumns.get(j).setNumber(j));


        masterController.fadeNode(column.getColumn(), 400, false);

        column.getAddNewColumnButton().setOnAction(event -> addNewInputParameter(column.getNumber()));
        column.getRemoveColumnButton().setOnAction(event -> removeInputParameter(column.getNumber()));
    }
    public void removeInputParameter(int i) {
        if(inputLayerColumns.size() <= 1)
            return;

        // Fade out and delete
        masterController.fadeNode(inputLayerColumns.get(i).getColumn(), 400, true).setOnFinished(event -> {
            inputLayerColumns.remove(i);
            inputLayerBox.getChildren().remove(i);
            IntStream.range(i, inputLayerColumns.size()).forEach(j -> inputLayerColumns.get(j).setNumber(j));
        });
    }

    public void updateArchitectureTable() {
        if (!dataMaster.areDatasetsValid()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "There is no database found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.noDatabase"));
            return;
        }

        // Update numbers in all layers
        for (int j = 0; j < architectureLayers.size(); j++) {
            architectureLayers.get(j).setNumber(j + 1);
        }
        lastLayerNumber.setText(String.valueOf(architectureLayers.size() + 1));
    }

    // Add layer to architecture table
    public void addNewLayer() {
        addNewLayer(0);
    }
    public void addNewLayer(int i) {
        if (!dataMaster.areDatasetsValid()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "There is no database found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.noDatabase"));
            return;
        }

        i++;
        ArchitectureLayer layer = new ArchitectureLayer(i, NetworkStructure.neuronTypes.values(), NetworkStructure.neuronTypes.HIDDEN,
                (dataMaster.getTestingSet().labels().size() - 1) * 2, ActivationFunctions.types.SIGMOID);

        architectureLayers.add(i - 1, layer);
        architectureLayerInfoPane.getChildren().add(i, layer.getColumn());
        architectureLayerManagementPane.getChildren().add(i, layer.getManagement());
        masterController.fadeNode(layer.getColumn(), 400, false);
        masterController.fadeNode(layer.getManagement(), 400, false);

        layer.getRemoveLayerButton().setOnAction(event -> removeLayer(layer));
        layer.getAddNewLayerButton().setOnAction(event -> addNewLayer(layer.getNumber() - 1));

        updateArchitectureTable();
    }

    // Remove layer from architecture table
    public void removeLayer(ArchitectureLayer layer) {
        masterController.fadeNode(layer.getColumn(), 400, true).setOnFinished(event -> {
            architectureLayers.remove(layer);
            architectureLayerInfoPane.getChildren().remove(layer.getColumn());
            architectureLayerManagementPane.getChildren().remove(layer.getManagement());

            updateArchitectureTable();
        });
        masterController.fadeNode(layer.getManagement(), 400, true);
    }

    /*
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
        if(architectureSettings != null && architectureSettings.size() > 1){
            architectureSettings.remove(architectureSettings.size() - 2, architectureSettings.size());
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

        updateGraphicOutput();
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
        num.setFont(new Font("Segoe UI SemiBold", 14));
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
        neuronNumber.textProperty().addListener(
                (observable -> updateGraphicOutput())
        );

        ChoiceBox<NetworkStructure.neuronTypes> typeSelector = new ChoiceBox<>(FXCollections.observableArrayList(NetworkStructure.neuronTypes.values()));
        typeSelector.setDisable(true);
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
    }*/

    /*
    private void updateVisualisation(){
        GraphicsContext graphicsContext = architectureCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, architectureCanvas.getWidth(), architectureCanvas.getHeight());

        double radius = 20;
        int maxNeurons = 10;
        int maxLayers = 10;

        //Parse data
        int[] data = new int[Math.min(maxLayers, architectureSettings.size() / 2 + 2)];
        NetworkStructure.neuronTypes[] types = new NetworkStructure.neuronTypes[Math.min(maxLayers, architectureSettings.size() / 2 + 2)];
        for (int i = 0; i < architectureSettings.size(); i+=2) {
            if(i / 2 >= data.length - 1){
                break;
            }
            VBox vBox = (VBox) architectureSettings.get(i);
            TextField textField = (TextField) vBox.getChildren().get(2);
            ChoiceBox<NetworkStructure.neuronTypes> choiceBox = (ChoiceBox<NetworkStructure.neuronTypes>) vBox.getChildren().get(4);
            if(textField.getText().isEmpty()) {
                return;
            }
            data[i / 2 + 1] = Integer.parseInt(textField.getText());
            types[i / 2 + 1] = NetworkStructure.neuronTypes.valueOf(String.valueOf(choiceBox.getValue()));
        }
        if(inputNeuronCounter.getText().equals("..."))
            return;
        data[0] = Integer.parseInt(inputNeuronCounter.getText());
        data[data.length - 1] = 1;

        //Calculate position
        double xSpacing = ((graphicOutput.getWidth() - radius) - radius * data.length) / (data.length + 1);
        double lastX = 0;
        double lastYSpacing = 0;

        //Draw network graph
        for (int i = 0; i < data.length; i++) {
            //Calculate position x
            double x = xSpacing * (i + 1) + radius * i;
            double ySpacing = ((graphicOutput.getHeight() - radius) - radius * Math.min(maxNeurons, data[i])) / (Math.min(maxNeurons, data[i]) + 1);

            if(maxLayers == data.length && i == maxLayers - 2){
                graphicsContext.fillText("...", x, graphicOutput.getHeight() / 2);
                continue;
            }

            for (int j = 0; j < data[i]; j++) {
                //Calculate position y
                double y = ySpacing * (j + 1) + radius * j;
                if(j >= maxNeurons){
                    graphicsContext.fillText("...", x, y);
                    break;
                }

                //Switch colors
                if(i == 0){
                    graphicsContext.setFill(Color.web("#0be881"));
                }
                else if(i < data.length - 1){
                    switch(types[i]){
                        case HIDDEN -> graphicsContext.setFill(Color.web("#4769ff"));
                    }
                }
                else{
                    graphicsContext.setFill(Color.web("#e15f41"));
                }

                //Draw neurons
                graphicsContext.strokeOval(x, y, radius, radius);
                graphicsContext.fillOval(x, y, radius, radius);

                if(i == 0 || i == maxLayers - 1)
                    continue;

                //Draw synapses
                for (int k = 0; k < data[i - 1]; k++) {
                    if(k >= maxNeurons){
                        break;
                    }
                    double y2 = lastYSpacing * (k + 1) + radius * k;
                    graphicsContext.strokeLine(lastX + radius, y2 + radius / 2, x, y + radius / 2);
                }
            }
            lastX = x;
            lastYSpacing = ySpacing;
        }
    }*/

    // Inner classes for convenient management of input lists
    public static class InputLayerColumn {
        protected VBox column;
        protected ChoiceBox<String> parameter;
        protected ChoiceBox<Parser.inputTypes> type;
        protected Button removeColumn, addNewColumn;
        protected Label numberLabel;
        protected int number;

        public InputLayerColumn(int number, List<String> parameters, String currentParameter, Parser.inputTypes currentType) {
            this.number = number;
            String style = "-fx-border-color: #4769ff;" +
                    "-fx-border-width: 1 0 0 0;";

            // Initialize components
            numberLabel = new Label(String.valueOf(number));
            numberLabel.setPadding(new Insets(15));
            numberLabel.setFont(Font.font("Inter", 16));
            numberLabel.setPrefHeight(50);
            numberLabel.setMaxWidth(Double.MAX_VALUE);
            numberLabel.setAlignment(Pos.CENTER);

            parameter = new ChoiceBox<>();
            parameter.getItems().addAll(parameters);
            parameter.getSelectionModel().select(currentParameter);
            parameter.setMaxWidth(Double.MAX_VALUE);
            BorderPane parameterPane = new BorderPane(parameter);
            parameterPane.setStyle(style);
            parameterPane.setMaxWidth(Double.MAX_VALUE);
            parameterPane.setMaxHeight(Double.MAX_VALUE);
            parameterPane.setMinHeight(71);
            BorderPane.setMargin(parameter, new Insets(14, 15, 14, 15));

            type = new ChoiceBox<>();
            type.getItems().addAll(Parser.inputTypes.values());
            type.getSelectionModel().select(currentType);
            type.setMaxWidth(Double.MAX_VALUE);
            BorderPane typePane = new BorderPane(type);
            typePane.setStyle(style);
            typePane.setMaxWidth(Double.MAX_VALUE);
            typePane.setMaxHeight(Double.MAX_VALUE);
            typePane.setMinHeight(71);
            BorderPane.setMargin(type, new Insets(14, 15, 14,15));

            removeColumn = new Button();
            addNewColumn = new Button();

            FontIcon addIcon = new FontIcon("zondi-add-outline");
            addIcon.setIconSize(24);
            addIcon.setIconColor(Paint.valueOf("#4769ff"));
            FontIcon removeIcon = new FontIcon("zondi-minus-outline");
            removeIcon.setIconSize(24);
            removeIcon.setIconColor(Paint.valueOf("#4769ff"));

            HBox hBox = new HBox(15, removeColumn, addNewColumn);
            hBox.setAlignment(Pos.CENTER);
            hBox.setPrefHeight(51);
            hBox.setStyle(style);

            removeColumn.setGraphic(removeIcon);
            removeColumn.setStyle("-fx-background-radius: 15;" +
                    "-fx-border-width: 0");
            addNewColumn.setGraphic(addIcon);
            addNewColumn.setStyle("-fx-background-radius: 15;" +
                    "-fx-border-width: 0");

            column = new VBox(numberLabel, parameterPane, typePane, hBox);
            column.setMinWidth(140);
            column.setStyle("-fx-border-color: #4769ff;" +
                    "-fx-border-width: 0 1 0 0;");

            // Set VBox constrains for children
            column.setFillWidth(true);
            VBox.setVgrow(numberLabel, Priority.NEVER);
            VBox.setVgrow(parameterPane, Priority.ALWAYS);
            VBox.setVgrow(typePane, Priority.ALWAYS);
            VBox.setVgrow(hBox, Priority.ALWAYS);
        }

        public VBox getColumn() {
            return column;
        }
        public int getNumber() {
            return number;
        }

        public Button getRemoveColumnButton() {
            return removeColumn;
        }
        public Button getAddNewColumnButton() {
            return addNewColumn;
        }

        public void setNumber(int number) {
            this.number = number;
            numberLabel.setText(String.valueOf(number));
        }
    }

    public static class ArchitectureLayer {
        protected VBox column;
        protected Label layerNumber;
        protected int number;
        protected ChoiceBox<NetworkStructure.neuronTypes> type;
        protected TextField neuronNumberField;
        protected ChoiceBox<ActivationFunctions.types> activationFunction;
        protected Button addNewLayer, removeLayer;
        protected HBox managementPane;

        public ArchitectureLayer(int number, NetworkStructure.neuronTypes[] types, NetworkStructure.neuronTypes currentType, int currentNeuronsCount,
                                 ActivationFunctions.types currentFunction) {
            this.number = number;
            String style = "-fx-border-color: #4769ff;" +
                    "-fx-border-width: 1 0 0 0;";

            // Initialize components
            layerNumber = new Label(String.valueOf(number));
            layerNumber.setPadding(new Insets(15));
            layerNumber.setFont(Font.font("Inter", 16));
            layerNumber.setMaxWidth(Double.MAX_VALUE);
            layerNumber.setAlignment(Pos.CENTER);

            type = new ChoiceBox<>();
            type.getItems().addAll(types);
            type.getSelectionModel().select(currentType);
            type.setMaxWidth(Double.MAX_VALUE);
            type.setDisable(true); // Temporary disabled
            BorderPane typePane = new BorderPane(type);
            typePane.setStyle(style);
            typePane.setMaxWidth(Double.MAX_VALUE);
            typePane.setPrefHeight(62);
            typePane.setMinHeight(62);
            typePane.setPadding(new Insets(15));

            neuronNumberField = new TextField(String.valueOf(currentNeuronsCount));
            neuronNumberField.setMaxWidth(Double.MAX_VALUE);
            BorderPane neuronNumberPane = new BorderPane(neuronNumberField);
            neuronNumberPane.setStyle(style);
            neuronNumberPane.setMaxWidth(Double.MAX_VALUE);
            neuronNumberPane.setPrefHeight(62);
            neuronNumberPane.setMinHeight(62);
            neuronNumberPane.setPadding(new Insets(15));

            activationFunction = new ChoiceBox<>();
            activationFunction.getItems().addAll(ActivationFunctions.types.values());
            activationFunction.getSelectionModel().select(currentFunction);
            activationFunction.setMaxWidth(Double.MAX_VALUE);
            BorderPane activationFunctionPane = new BorderPane(activationFunction);
            activationFunctionPane.setStyle(style);
            activationFunctionPane.setMaxWidth(Double.MAX_VALUE);
            activationFunctionPane.setPrefHeight(62);
            activationFunctionPane.setMinHeight(62);
            activationFunctionPane.setPadding(new Insets(15));

            removeLayer = new Button();
            addNewLayer = new Button();

            FontIcon addIcon = new FontIcon("zondi-add-outline");
            addIcon.setIconSize(24);
            addIcon.setIconColor(Paint.valueOf("#4769ff"));
            FontIcon removeIcon = new FontIcon("zondi-minus-outline");
            removeIcon.setIconSize(24);
            removeIcon.setIconColor(Paint.valueOf("#4769ff"));

            managementPane = new HBox(15, removeLayer, addNewLayer);
            managementPane.setAlignment(Pos.CENTER);
            managementPane.setPrefHeight(51);
            managementPane.setMinHeight(51);
            managementPane.setPrefWidth(150);
            managementPane.setMinWidth(150);
            managementPane.setPadding(new Insets(15));
            managementPane.setMaxWidth(Double.MAX_VALUE);
            managementPane.setStyle("-fx-border-color: #4769ff;" +
                    "-fx-border-width: 1 1 0 0;");

            removeLayer.setGraphic(removeIcon);
            removeLayer.setStyle("-fx-background-radius: 15;" +
                    "-fx-border-width: 0");
            addNewLayer.setGraphic(addIcon);
            addNewLayer.setStyle("-fx-background-radius: 15;" +
                    "-fx-border-width: 0");

            column = new VBox(layerNumber, typePane, neuronNumberPane, activationFunctionPane);
            column.setMinWidth(150);
            column.setPrefWidth(150);
            column.setStyle("-fx-border-color: #4769ff;" +
                    "-fx-border-width: 0 1 0 0;");

            // Set VBox constrains for children
            column.setFillWidth(true);
            VBox.setVgrow(layerNumber, Priority.NEVER);
            VBox.setVgrow(typePane, Priority.ALWAYS);
            VBox.setVgrow(neuronNumberPane, Priority.ALWAYS);
            VBox.setVgrow(activationFunctionPane, Priority.ALWAYS);
        }

        public VBox getColumn() {
            return column;
        }
        public HBox getManagement() {
            return managementPane;
        }
        public int getNumber() {
            return number;
        }
        public void setNumber(int number) {
            this.number = number;
            layerNumber.setText(String.valueOf(number));
        }

        public Button getAddNewLayerButton() {
            return addNewLayer;
        }
        public Button getRemoveLayerButton() {
            return removeLayer;
        }
    }
}
