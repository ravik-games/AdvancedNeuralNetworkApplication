package ANNA.UI.Tabs;

import ANNA.Network.NetworkStructure;
import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import ANNA.UI.UIController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UIStructureController {
    //Class for working with second tab of UI (Structure creation)
    private final VBox inputVBox;
    private final HBox architectureHBox;
    private final ChoiceBox<String> lastColumnChoiceBox, inputsChoiceBox;
    private final Label inputNeuronCounter, lastLayerNumber;
    private final Button inputNeuronButton, inputNeuronRemoveButton;

    private UIController mainController;
    private UIDataController dataController;
    private UINetworkController networkController;
    private UIOutputController outputController;

    public ObservableList<Node> trainInputSettings, testInputSettings, architectureSettings;

    public UIStructureController(UIController controller, VBox inputVBox, HBox architectureHBox, ChoiceBox<String> inputsChoiceBox, ChoiceBox<String> lastColumnChoiceBox,
                                 Label inputNeuronCounter, Label lastLayerNumber, Button inputNeuronRemoveButton, Button inputNeuronButton){
        this.inputVBox = inputVBox;
        this.architectureHBox = architectureHBox;
        this.lastColumnChoiceBox = lastColumnChoiceBox;
        this.inputsChoiceBox = inputsChoiceBox;
        this.inputNeuronCounter = inputNeuronCounter;
        this.lastLayerNumber = lastLayerNumber;
        this.inputNeuronButton = inputNeuronButton;
        this.inputNeuronRemoveButton = inputNeuronRemoveButton;
        this.mainController = controller;
    }

    public void setControllerReferences(UIDataController dataController, UINetworkController networkController, UIOutputController outputController){
        this.dataController = dataController;
        this.networkController = networkController;
        this.outputController = outputController;
    }

    public void updateInputTable(){
        inputVBox.getChildren().remove(2, inputVBox.getChildren().size() - 1);
        lastColumnChoiceBox.getItems().clear();
        lastColumnChoiceBox.setValue("...");
        inputNeuronCounter.setText("...");

        //Switch on selected data set
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            if(dataController.trainSetFile == null || !dataController.trainSetFile.exists()){
                inputsChoiceBox.setValue("Выберите базу данных");
                inputNeuronButton.setDisable(true);
                inputNeuronRemoveButton.setDisable(true);
                PopupController.errorMessage("WARNING", "Ошибка", "", "Отсутствует обучающая база данных.");
                return;
            }
            //If settings is not valid, create new.
            if(trainInputSettings == null){
                trainInputSettings = FXCollections.observableArrayList(addInputValue(dataController.rawTrainSet, 0));
            }
            inputVBox.getChildren().addAll(2, trainInputSettings);
            inputNeuronCounter.setText(String.valueOf(trainInputSettings.size() / 2));
            lastColumnChoiceBox.getItems().addAll(dataController.rawTrainSet.get(0));
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
            lastColumnChoiceBox.setDisable(false);
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            if(dataController.testSetFile == null || !dataController.testSetFile.exists()){
                inputsChoiceBox.setValue("Выберите базу данных");
                inputNeuronButton.setDisable(true);
                inputNeuronRemoveButton.setDisable(true);
                PopupController.errorMessage("WARNING", "Ошибка", "", "Отсутствует тестовая база данных.");
                return;
            }
            //If settings is not valid, create new.
            if(testInputSettings == null){
                testInputSettings = FXCollections.observableArrayList(addInputValue(dataController.rawTestSet, 0));
            }
            inputVBox.getChildren().addAll(2, testInputSettings);
            inputNeuronCounter.setText(String.valueOf(testInputSettings.size() / 2));
            lastColumnChoiceBox.getItems().addAll(dataController.rawTestSet.get(0));
            inputNeuronButton.setDisable(false);
            inputNeuronRemoveButton.setDisable(false);
            lastColumnChoiceBox.setDisable(false);
        }else{
            inputNeuronRemoveButton.setDisable(true);
            inputNeuronButton.setDisable(true);
            lastColumnChoiceBox.setDisable(true);
        }
    }

    //Add new input neuron value
    public void addInputNeuron() {
        if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(0))){
            trainInputSettings.addAll(addInputValue(dataController.rawTrainSet, trainInputSettings.size() / 2));
            updateInputTable();
        }
        else if(inputsChoiceBox.getValue().equals(inputsChoiceBox.getItems().get(1))){
            testInputSettings.addAll(addInputValue(dataController.rawTestSet, testInputSettings.size() / 2));
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
}
