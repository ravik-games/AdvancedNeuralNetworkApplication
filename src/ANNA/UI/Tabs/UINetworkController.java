package ANNA.UI.Tabs;

import ANNA.Main;
import ANNA.Network.Hyperparameters;
import ANNA.Network.NeuralNetwork;
import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import ANNA.UI.UIController;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class UINetworkController {
    //Class for working with third tab (Network control)

    private VBox hyperparametersVBox;
    private TextField updateResultsEpoch;

    private UIController mainController;
    private UIDataController dataController;
    private UIStructureController structureController;
    private UIOutputController outputController;
    private Main main;

    public UINetworkController(UIController controller, VBox hyperparametersVBox, TextField updateResultsEpoch){
        this.mainController = controller;
        this.hyperparametersVBox = hyperparametersVBox;
        this.updateResultsEpoch = updateResultsEpoch;
    }

    public void setMain(Main main){
        this.main = main;
    }

    public void setControllerReferences(UIDataController dataController, UIStructureController structureController, UIOutputController outputController){
        this.dataController = dataController;
        this.structureController = structureController;
        this.outputController = outputController;
    }

    //Start neural network on train data
    public void startTraining() {
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments(true);
        if(arguments == null)
            return;
        if(mainController.autoOpenResults.isSelected())
            mainController.tabPane.getSelectionModel().select(3);
        main.runNeuralNetwork(arguments);
        //Enable simulator
        outputController.initializeSimulator(true);
    }

    //Start neural network on test data
    public void startTesting() {
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments(false);
        if(arguments == null)
            return;
        if(mainController.autoOpenResults.isSelected())
            mainController.tabPane.getSelectionModel().select(3);
        main.runNeuralNetwork(arguments);
    }

    //Create/update hyperparameters UI
    public void initializeHyperparameters(){
        hyperparametersVBox.getChildren().remove(2, hyperparametersVBox.getChildren().size());

        for(Hyperparameters.Identificator i: Hyperparameters.Identificator.values()){
            hyperparametersVBox.getChildren().addAll(createHyperparametersRow(Hyperparameters.getValueByID(i), i.getName(), i.getDescription()));
        }
    }

    private List<Node> createHyperparametersRow(String defaultValue, String name, String description){
        //Create row
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(2));

        //Name of hyperparameters
        Label nameLabel = new Label(name);
        nameLabel.setFont(new Font("Segoe UI SemiBold", 12));
        nameLabel.setPrefWidth(140);
        nameLabel.setWrapText(true);

        TextField valueField = new TextField(defaultValue);
        valueField.setPromptText("?????????????? ????????????????");
        valueField.setPrefWidth(150);

        Text descriptionLabel = new Text(description);
        descriptionLabel.setFont(new Font("Segoe UI SemiLight", 12));
        descriptionLabel.setWrappingWidth(280);

        hBox.getChildren().add(nameLabel);
        hBox.getChildren().add(new Separator(Orientation.VERTICAL));
        hBox.getChildren().add(valueField);
        hBox.getChildren().add(new Separator(Orientation.VERTICAL));
        hBox.getChildren().add(descriptionLabel);

        List<Node> result = new ArrayList<>(2);
        result.add(hBox);
        result.add(new Separator(Orientation.HORIZONTAL));

        return  result;
    }

    //Collect data from UI and create arguments for NN
    public NeuralNetwork.NetworkArguments collectDataToArguments(boolean trainData){
        if(trainData && (structureController.trainInputSettings == null || structureController.trainInputSettings.size() < 1)){
            PopupController.errorMessage("WARNING", "????????????", "", "???? ?????????????????? ?????????????? ?????????????? ?????? ?????????????????? ????????????");
            return null;
        }
        else if (!trainData && (structureController.testInputSettings == null || structureController.testInputSettings.size() < 1)){
            PopupController.errorMessage("WARNING", "????????????", "", "???? ?????????????????? ?????????????? ?????????????? ?????? ???????????????? ????????????");
            return null;
        }
        if(trainData && (structureController.lastColumnChoiceBox.getValue().equals("...") || structureController.lastColumnChoiceBox.getValue() == null)){
            PopupController.errorMessage("WARNING", "????????????", "", "???? ???????????? ?????????????????????? ?????????????? ?????? ?????????????????? ????????????");
            return null;
        }
        if(structureController.architectureSettings == null){
            PopupController.errorMessage("WARNING", "????????????", "", "???? ?????????????????? ?????????????????? ?????????????????? ????????");
            return null;
        }

        boolean isPrediction = true;

        //Log preparation time
        long startTime = System.nanoTime();
        System.out.println("\n--- Starting preparation ---");

        //Local variables
        ObservableList<Node> currentInputNeuronSet = trainData ? structureController.trainInputSettings : structureController.testInputSettings;
        List<List<String>> currentRawDataSet = trainData ? dataController.rawTrainSet : dataController.rawTestSet;

        int[] architecture = new int[2 + structureController.architectureSettings.size() / 2];
        double[][] inputs = new double[currentRawDataSet.size() - 1][currentInputNeuronSet.size() / 2];
        mainController.lastInputTypes = new Parser.inputTypes[currentInputNeuronSet.size() / 2];
        String[] expectedOutput = new String[currentRawDataSet.size() - 1];
        List<String> allOutputTypes = new ArrayList<>();
        int logEpoch = Integer.parseInt(updateResultsEpoch.getText()); //TODO Check for only digits in field

        //Collect architecture data
        architecture[0] = currentInputNeuronSet.size() / 2;
        for(int i = 0; i < structureController.architectureSettings.size(); i+=2) {
            VBox vBox = (VBox) structureController.architectureSettings.get(i);
            TextField textField = (TextField) vBox.getChildren().get(2);
            architecture[i / 2 + 1] = Integer.parseInt(textField.getText()); //TODO Check for only digits in field
        }

        //Create inputs from existing data
        int[] reassign = new int[currentInputNeuronSet.size() / 2]; //Each value corresponds to column number in raw data
        Parser.inputTypes[] types = new Parser.inputTypes[currentInputNeuronSet.size() / 2]; //Each value will be parsed according to set type
        int expectedColumn = currentRawDataSet.get(0).indexOf(structureController.lastColumnChoiceBox.getValue());
        for(int i = 0; i < currentInputNeuronSet.size(); i+=2) {
            HBox hBox = (HBox) currentInputNeuronSet.get(i);
            ChoiceBox<String> column = (ChoiceBox<String>) hBox.getChildren().get(2);
            ChoiceBox<Parser.inputTypes> type = (ChoiceBox<Parser.inputTypes>) hBox.getChildren().get(4);
            mainController.lastInputTypes[i / 2] = type.getValue();
            int position = currentRawDataSet.get(0).indexOf(column.getValue());
            types[i / 2] = type.getValue();
            reassign[i / 2] = position;
        }

        //Parse raw data file
        for(int i = 1; i < currentRawDataSet.size(); i++) {
            for (int j = 0; j < reassign.length; j++) {
                inputs[i - 1][j] = Parser.parseRawValue(currentRawDataSet.get(i).get(reassign[j]), types[j]);
            }
            //Set expected outputs for training set
            String idealValue = currentRawDataSet.get(i).get(expectedColumn);
            expectedOutput[i - 1] = idealValue;
            if(!isPrediction && !allOutputTypes.contains(idealValue)){
                allOutputTypes.add(idealValue);
            }
        }

        //Set output layer neuron counter
        if(isPrediction) {
            architecture[architecture.length - 1] = 1;
        }
        else {
            architecture[architecture.length - 1] = allOutputTypes.size();
        }

        //Parse and update hyperparameters
        for (int i = 2; i < hyperparametersVBox.getChildren().size(); i+=2) {
            HBox hBox = (HBox) hyperparametersVBox.getChildren().get(i);
            TextField textField = (TextField) hBox.getChildren().get(2);
            Hyperparameters.setValueByID(Hyperparameters.Identificator.values()[i / 2 - 1], textField.getText());
        }

        mainController.lastArguments = new NeuralNetwork.NetworkArguments(architecture, null, inputs, expectedOutput, allOutputTypes.toArray(new String[0]),
                trainData, isPrediction, mainController, logEpoch);

        //Log end time
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("\n--- Preparation finished ---");
        System.out.println("\nElapsed time: " + (elapsedTime / 1000000000) + "s\t" + (elapsedTime / 1000000) + "ms\t" + elapsedTime + "ns\n");
        //Print time in seconds, milliseconds and nanoseconds

        return mainController.lastArguments;
    }
}
