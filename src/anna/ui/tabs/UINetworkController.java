package anna.ui.tabs;

import anna.Main;
import anna.network.DataTypes;
import anna.network.Hyperparameters;
import anna.network.NeuralNetwork;
import anna.ui.Parser;
import anna.ui.PopupController;
import anna.ui.DefaultUIController;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UINetworkController {
    //Class for working with third tab (Network control)

    private final VBox hyperparametersVBox;
    private final TextField updateResultsEpoch;

    private final DefaultUIController mainController;
    private UIDataController dataController;
    private UIStructureController structureController;
    private UIOutputController outputController;
    private Main main;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    public UINetworkController(DefaultUIController controller, VBox hyperparametersVBox, TextField updateResultsEpoch){
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
        NeuralNetwork.NetworkArguments arguments = collectDataToArguments();
        if(arguments == null)
            return;
        if(mainController.autoOpenResults.isSelected())
            mainController.tabPane.getSelectionModel().select(3);
        main.runNeuralNetwork(arguments);
        //Enable simulator
        outputController.initializeSimulator(true);
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
        hBox.setPadding(new Insets(0, 2, 0, 2));

        //Name of hyperparameters
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI SemiBold", 12));
        nameLabel.setPrefWidth(140);
        nameLabel.setWrapText(true);

        TextField valueField = new TextField(defaultValue);
        valueField.setPromptText(bundle.getString("general.inputValue"));
        valueField.setPrefWidth(150);

        Text descriptionLabel = new Text(description);
        descriptionLabel.setFont(Font.font("Segoe UI SemiLight", 12));
        descriptionLabel.setWrappingWidth(280);
        HBox.setMargin(descriptionLabel, new Insets(10, 0, 10, 0));

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
    public NeuralNetwork.NetworkArguments collectDataToArguments(){
        //Handling errors
        boolean trainToTest = false;
        if (structureController.trainInputSettings == null || structureController.trainInputSettings.isEmpty()){
            LOGGER.log(Level.WARNING, "Input neurons for training data are not configured.");
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.trainingInputNeuronsNotConfigured"));
            return null;
        }
        if (structureController.architectureSettings == null){
            LOGGER.log(Level.WARNING, "The structure of the neural network is not set up.");
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.structureNotConfigured"));
            return null;
        }
        if (structureController.testInputSettings == null || structureController.testInputSettings.isEmpty()){
            LOGGER.log(Level.WARNING, "The input neurons for the test data are not configured. The training data will be used for testing.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.error.testingInputNeuronsNotConfigured"));
            trainToTest = true;
        }
        else if (structureController.trainInputSettings.size() != structureController.testInputSettings.size()){
            LOGGER.log(Level.WARNING, "The structure of the neural network is not the same for training and test data. The number of input neurons is different.");
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.structureMisconfigured"));
            return null;
        }


        //Change mode DEPRECATED, USE ONLY FOR CLASSIFICATION (FALSE)
        boolean isPrediction = false;

        //Log preparation time
        long startTime = System.nanoTime();
        LOGGER.info("--- Starting preparation ---");

        int logEpoch = (int) Parser.parseRawValue(updateResultsEpoch.getText(), Parser.inputTypes.NUMBER);
        DataTypes.NetworkData networkData = new DataTypes.NetworkData(new int[2 + structureController.architectureSettings.size() / 2], new ArrayList<>());
        DataTypes.Dataset trainData = collectDataset(true, isPrediction);
        DataTypes.Dataset testData = collectDataset(trainToTest, isPrediction);
        if(trainData == null || testData == null)
            return null;
        //Collect architecture data
        networkData.getStructure()[0] = structureController.trainInputSettings.size() / 2;
        for(int i = 0; i < structureController.architectureSettings.size(); i+=2) {
            VBox vBox = (VBox) structureController.architectureSettings.get(i);
            TextField textField = (TextField) vBox.getChildren().get(2);
            networkData.getStructure()[i / 2 + 1] = (int) Parser.parseRawValue(textField.getText(), Parser.inputTypes.NUMBER);
        }

        //Set output layer neuron counter
        networkData.getStructure()[networkData.getStructure().length - 1] = trainData.allOutputTypes().length; //TODO For classification only

        mainController.lastArguments = new NeuralNetwork.NetworkArguments(networkData, trainData, testData, isPrediction, mainController, logEpoch);

        //Log end time
        long elapsedTime = System.nanoTime() - startTime;
        LOGGER.info("--- Preparation finished ---");
        LOGGER.info("Elapsed time: " + (elapsedTime / 1000000000) + "s\t" + (elapsedTime / 1000000) + "ms\t" + elapsedTime + "ns\n");
        //Print time in seconds, milliseconds and nanoseconds

        return mainController.lastArguments;
    }

    private DataTypes.Dataset collectDataset(boolean trainData, boolean isPrediction){

        //Initialize local variables
        ObservableList<Node> currentInputNeuronSet = trainData ? structureController.trainInputSettings : structureController.testInputSettings;
        List<List<String>> currentRawDataSet = trainData ? dataController.rawTrainSet : dataController.rawTestSet;

        double[][] inputs = new double[currentRawDataSet.size() - 1][currentInputNeuronSet.size() / 2];
        mainController.lastInputTypes = new Parser.inputTypes[currentInputNeuronSet.size() / 2];
        String[] expectedOutput = new String[currentRawDataSet.size() - 1];
        List<String> allOutputTypes = new ArrayList<>();

        //Create inputs from existing data
        int[] reassign = new int[currentInputNeuronSet.size() / 2]; //Each value corresponds to column number in raw data
        Parser.inputTypes[] types = new Parser.inputTypes[currentInputNeuronSet.size() / 2]; //Each value will be parsed according to set type
        int expectedColumn = currentRawDataSet.get(0).indexOf(structureController.lastColumnChoiceBox.getValue());
        if(expectedColumn < 0){
            LOGGER.log(Level.WARNING, "An error occurred when reading the dataset. The selected data of the training and test samples do not match.");
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.dataMismatch"));
            return null;
        }
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

        //Parse and update hyperparameters
        for (int i = 2; i < hyperparametersVBox.getChildren().size(); i+=2) {
            HBox hBox = (HBox) hyperparametersVBox.getChildren().get(i);
            TextField textField = (TextField) hBox.getChildren().get(2);
            Hyperparameters.setValueByID(Hyperparameters.Identificator.values()[i / 2 - 1], textField.getText());
        }

        return new DataTypes.Dataset(inputs, expectedOutput, allOutputTypes.toArray(new String[0]));
    }
}
