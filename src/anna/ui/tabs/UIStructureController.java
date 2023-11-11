package anna.ui.tabs;

import anna.ui.DefaultUIController;
import anna.ui.Parser;
import anna.ui.PopupController;
import anna.network.NetworkStructure;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UIStructureController {
    //Class for working with second tab of UI (Structure creation)
    private final VBox inputVBox;
    private final HBox architectureHBox;
    public final ChoiceBox<String> lastColumnChoiceBox, inputsChoiceBox;
    private final Label inputNeuronCounter, lastLayerNumber;
    private final Button inputNeuronButton, inputNeuronRemoveButton, inputNeuronAutoButton;
    private final Canvas graphicOutput;

    private final DefaultUIController mainController;
    private List<List<String>> rawTestSet, rawTrainSet;

    public ObservableList<Node> trainInputSettings, testInputSettings, architectureSettings;
    public int trainCheckColumn = 0, testCheckColumn = 0;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    public UIStructureController(DefaultUIController mainController, VBox inputVBox, HBox architectureHBox, ChoiceBox<String> inputsChoiceBox,
                                 ChoiceBox<String> lastColumnChoiceBox, Label inputNeuronCounter, Label lastLayerNumber, Button inputNeuronRemoveButton,
                                 Button inputNeuronButton, Button inputNeuronAutoButton, Canvas graphicOutput){
        this.mainController = mainController;
        this.inputVBox = inputVBox;
        this.architectureHBox = architectureHBox;
        this.lastColumnChoiceBox = lastColumnChoiceBox;
        this.inputsChoiceBox = inputsChoiceBox;
        this.inputNeuronCounter = inputNeuronCounter;
        this.lastLayerNumber = lastLayerNumber;
        this.inputNeuronButton = inputNeuronButton;
        this.inputNeuronRemoveButton = inputNeuronRemoveButton;
        this.graphicOutput = graphicOutput;
        this.inputNeuronAutoButton = inputNeuronAutoButton;

        //Update check column selections
        lastColumnChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            if (inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
                trainCheckColumn = t1.intValue() < 0 ? trainCheckColumn : t1.intValue();
            }
            else if (inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
                testCheckColumn = t1.intValue() < 0 ? testCheckColumn : t1.intValue();
            }
            else {
                trainCheckColumn = 0;
                testCheckColumn = 0;
            }
        });
    }

    public void updateInputTable(){
        inputVBox.getChildren().remove(2, inputVBox.getChildren().size() - 1);
        lastColumnChoiceBox.getItems().clear();
        lastColumnChoiceBox.setValue("...");
        inputNeuronCounter.setText("...");
        
        rawTrainSet = mainController.dataController.rawTrainSet;
        rawTestSet = mainController.dataController.rawTestSet;

        //Switch on selected data set
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            if(mainController.dataController.trainSetFile == null || !mainController.dataController.trainSetFile.exists()){
                inputsChoiceBox.setValue(bundle.getString("tab.architecture.selectDatabase"));
                inputNeuronButton.setDisable(true);
                inputNeuronRemoveButton.setDisable(true);
                inputNeuronAutoButton.setDisable(true);
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "There is no training database.");
                PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.noTrainingDatabase"));
                return;
            }
            //If settings is not valid, create new.
            if(trainInputSettings == null){
                trainInputSettings = FXCollections.observableArrayList(addInputValue(rawTrainSet, 0));
                trainCheckColumn = 0;
            }
            inputVBox.getChildren().addAll(2, trainInputSettings);
            inputNeuronCounter.setText(String.valueOf(trainInputSettings.size() / 2));
            lastColumnChoiceBox.getItems().addAll(rawTrainSet.get(0));

            lastColumnChoiceBox.setValue(lastColumnChoiceBox.getItems().get(trainCheckColumn));
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
            inputNeuronAutoButton.setDisable(false);
            lastColumnChoiceBox.setDisable(false);
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            if(mainController.dataController.testSetFile == null || !mainController.dataController.testSetFile.exists()){
                inputsChoiceBox.setValue(bundle.getString("tab.architecture.selectDatabase"));
                inputNeuronButton.setDisable(true);
                inputNeuronRemoveButton.setDisable(true);
                inputNeuronAutoButton.setDisable(true);
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "There is no test database.");
                PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.noTestDatabase"));
                return;
            }
            //If settings is not valid, create new.
            if(testInputSettings == null){
                testInputSettings = FXCollections.observableArrayList(addInputValue(rawTestSet, 0));
                testCheckColumn = 0;
            }
            inputVBox.getChildren().addAll(2, testInputSettings);
            inputNeuronCounter.setText(String.valueOf(testInputSettings.size() / 2));
            lastColumnChoiceBox.getItems().addAll(rawTestSet.get(0));
            lastColumnChoiceBox.setValue(lastColumnChoiceBox.getItems().get(testCheckColumn));
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
            inputNeuronAutoButton.setDisable(false);
            lastColumnChoiceBox.setDisable(false);
        }else{
            inputNeuronRemoveButton.setDisable(true);
            inputNeuronButton.setDisable(true);
            inputNeuronAutoButton.setDisable(true);
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

    //Auto add input and set output neurons. Adds input neurons from 2nd to size - 1.
    public void autoInputNeurons(){
        //Cleat input neurons
        inputVBox.getChildren().remove(2, inputVBox.getChildren().size() - 1);

        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            trainInputSettings.clear();
            for (int i = 0; i < rawTrainSet.get(0).size() - 1; i++) {
                trainInputSettings.addAll(addInputValue(rawTrainSet, i));
            }
            trainCheckColumn = lastColumnChoiceBox.getItems().size() - 1;
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            testInputSettings.clear();
            for (int i = 0; i < rawTestSet.get(0).size() - 1; i++) {
                testInputSettings.addAll(addInputValue(rawTestSet, i));
            }
            testCheckColumn = lastColumnChoiceBox.getItems().size() - 1;
        }
        updateInputTable();
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
        num.setFont(new Font("Segoe UI SemiBold", 14));
        num.setPrefHeight(20);
        num.setPrefWidth(25);

        ChoiceBox<String> columnSelector = new ChoiceBox<>(columnNames);
        if(number >= columnNames.size())
            columnSelector.setValue(columnNames.get(0));
        else
            columnSelector.setValue(columnNames.get(number));
        columnSelector.setPrefHeight(20);
        columnSelector.setPrefWidth(130);

        ChoiceBox<Parser.inputTypes> typeSelector = new ChoiceBox<>(FXCollections.observableArrayList(Parser.inputTypes.values()));
        typeSelector.setValue(Parser.inputTypes.NUMBER);
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
    }

    private void updateGraphicOutput(){
        GraphicsContext graphicsContext = graphicOutput.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, graphicOutput.getWidth(), graphicOutput.getHeight());

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
    }
}
