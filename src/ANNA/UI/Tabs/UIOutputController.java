package ANNA.UI.Tabs;

import ANNA.Main;
import ANNA.UI.Parser;
import ANNA.UI.UIController;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UIOutputController {
    //Class for working with fourth tab (Results)

    private HBox simulatorHBox;
    private Button startSimulatorButton;
    private TextArea simulatorOutput;
    private LineChart<Integer, Double> trainSetGraph, testSetGraph;

    private final UIController mainController;
    private UIDataController dataController;
    private UIStructureController structureController;
    private UINetworkController networkController;
    private Main main;


    public UIOutputController(UIController controller, TextArea simulatorOutput, Button startSimulatorButton, HBox simulatorHBox,
                              LineChart<Integer, Double> trainSetGraph, LineChart<Integer, Double> testSetGraph){
        this.simulatorHBox = simulatorHBox;
        this.startSimulatorButton = startSimulatorButton;
        this.simulatorOutput = simulatorOutput;
        this.mainController = controller;
        this.trainSetGraph = trainSetGraph;
        this.testSetGraph = testSetGraph;
    }

    public void setMain(Main main){
        this.main = main;
    }

    public void setControllerReferences(UIDataController dataController, UIStructureController structureController, UINetworkController networkController){
        this.dataController = dataController;
        this.networkController = networkController;
        this.structureController = structureController;
    }

    //Create/update simulator table
    public void initializeSimulator(boolean enable){
        simulatorHBox.getChildren().remove(2, simulatorHBox.getChildren().size());
        simulatorHBox.setDisable(!enable);
        startSimulatorButton.setDisable(!enable);
        if(mainController.lastArguments == null || mainController.lastInputTypes == null) {
            simulatorHBox.setDisable(true);
            startSimulatorButton.setDisable(true);
            return;
        }

        for (int i = 0; i < mainController.lastArguments.architecture()[0]; i++) {
            simulatorHBox.getChildren().addAll(createSimulatorColumn(i));
        }
    }

    //Create input column for simulator
    private List<Node> createSimulatorColumn(int numberOfNeuron){
        //Create column
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPrefHeight(100);
        vBox.setPrefWidth(90);
        vBox.setMinWidth(90);

        //Create label and field
        Label name = new Label(String.valueOf(numberOfNeuron));
        name.setFont(new Font("Segoe UI SemiBold", 14));
        name.setAlignment(Pos.CENTER);
        name.setPrefWidth(90);
        name.setPrefHeight(50);

        TextField field = new TextField();
        field.setFont(new Font("Segoe UI SemiLight", 12));
        field.setAlignment(Pos.CENTER);
        field.prefWidth(90);
        field.setPrefHeight(50);

        //Add children nto VBox
        vBox.getChildren().add(name);
        vBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        vBox.getChildren().add(field);

        //Create output list
        List<Node> output = new ArrayList<>(2);
        output.add(vBox);
        output.add(new Separator(Orientation.VERTICAL));

        return output;
    }

    //Prepare and run simulator
    public void prepareSimulation(){
        double[] inputs = new double[(simulatorHBox.getChildren().size() - 2) / 2];

        for(int i = 2; i < simulatorHBox.getChildren().size(); i+=2) {
            VBox vBox = (VBox) simulatorHBox.getChildren().get(i);
            TextField field = (TextField) vBox.getChildren().get(2);
            inputs[i / 2 - 1] = Parser.parseRawValue(field.getText(), mainController.lastInputTypes[i / 2 - 1]);
        }

        main.runSimulation(inputs);
    }

    //Add simulation output to UI
    public void simulationResult(double[] outputValues, String outputCategory){
        String text = "Наиболее вероятная категория:\n" + outputCategory + "\nВыходные значения нейронной сети:\n" + Arrays.toString(outputValues);
        simulatorOutput.setText(text);
    }
    public void simulationResult(double outputValue){
        String text = "Прогнозируемое значение:\n" + outputValue;
        simulatorOutput.setText(text);
    }

    //Show data on train graph
    public void updateTrainGraph(boolean clear, boolean newSeries, double error, int epoch){
        if(clear)
            trainSetGraph.getData().clear();

        //Create series if not exist
        if(trainSetGraph.getData().isEmpty() || trainSetGraph.getData().get(trainSetGraph.getData().size() - 1) == null || newSeries) {
            XYChart.Series<Integer, Double> series = new XYChart.Series<>();
            series.setName("Средняя ошибка эпохи");
            trainSetGraph.getData().add(series);
        }

        if(trainSetGraph.getData().size() > 5){
            trainSetGraph.getData().remove(0);
        }
        //Add values to the chart
        trainSetGraph.getData().get(trainSetGraph.getData().size() - 1).getData().add(new XYChart.Data<>(epoch, error));
    }
}
