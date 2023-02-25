package ANNA.UI.Tabs;

import ANNA.Main;
import ANNA.Network.Hyperparameters;
import ANNA.Network.NeuralNetwork;
import ANNA.UI.UIController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class UINetworkController {
    //Class for working with third tab (Network control)

    private UIController mainController;
    private UIDataController dataController;
    private UIStructureController structureController;
    private UIOutputController outputController;
    private Main main;
    private VBox hyperparametersVBox;

    public UINetworkController(UIController controller, VBox hyperparametersVBox){
        this.mainController = controller;
        this.hyperparametersVBox = hyperparametersVBox;
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
        NeuralNetwork.NetworkArguments arguments = mainController.collectDataToArguments(true);
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
        NeuralNetwork.NetworkArguments arguments = mainController.collectDataToArguments(false);
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
        hBox.setMinHeight(80);
        hBox.setPadding(new Insets(2));

        //Name of hyperparameters
        Label nameLabel = new Label(name);
        nameLabel.setFont(new Font("Segoe UI SemiBold", 12));
        nameLabel.setPrefWidth(140);
        nameLabel.setWrapText(true);

        TextField valueField = new TextField(defaultValue);
        valueField.setPromptText("Введите значение");
        valueField.setPrefWidth(150);

        Label descriptionLabel = new Label(description);
        descriptionLabel.setFont(new Font("Segoe UI SemiLight", 12));
        descriptionLabel.setPrefWidth(280);
        descriptionLabel.setWrapText(true);

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
}
