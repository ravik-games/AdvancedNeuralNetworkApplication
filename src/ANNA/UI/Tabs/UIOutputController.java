package ANNA.UI.Tabs;

import ANNA.Main;
import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import ANNA.UI.UIController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UIOutputController {
    //Class for working with fourth tab (Results)

    public Label truePositiveLabel, trueNegativeLabel, falsePositiveLabel, falseNegativeLabel;

    private HBox simulatorHBox;
    private Button startSimulatorButton;
    private TextArea simulatorOutput;
    private LineChart<Integer, Double> errorChart;
    private Parent currentMatrix, confusionMatrixFull, confusionMatrixSingle;

    private UIController mainController;
    private UIDataController dataController;
    private UIStructureController structureController;
    private UINetworkController networkController;
    private Main main;


    public UIOutputController(UIController controller, TextArea simulatorOutput, Button startSimulatorButton, HBox simulatorHBox,
                              LineChart<Integer, Double> errorChart, Pane matrixParent){
        this.simulatorHBox = simulatorHBox;
        this.startSimulatorButton = startSimulatorButton;
        this.simulatorOutput = simulatorOutput;
        this.mainController = controller;
        this.errorChart = errorChart;

        errorChart.setCreateSymbols(false);

        //Load matrix
        try {
            //FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfusionMatrixSingle.fxml"));
            confusionMatrixSingle = loader.load();
            currentMatrix = confusionMatrixSingle;
            matrixParent.getChildren().add(currentMatrix);
        } catch (IOException e) {
            PopupController.errorMessage("ERROR", "Ошибка загрузки", "", "Произошла ошибка при загрузке дополнительных файлов.\n" + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE ,"Couldn't load .fxml files");
            System.exit(1);
        }
    }

    public UIOutputController(){
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

        for (int i = 0; i < mainController.lastArguments.networkData().getStructure()[0]; i++) {
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

        //Add children to VBox
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
    public void updateErrorChart(boolean newSeries, double trainError, double testError, int epoch){

        //Create series if not exist
        if(errorChart.getData().isEmpty() || errorChart.getData().get(errorChart.getData().size() - 1) == null || newSeries) {
            XYChart.Series<Integer, Double> trainSeries = new XYChart.Series<>();
            XYChart.Series<Integer, Double> testSeries = new XYChart.Series<>();
            trainSeries.setName("Обучающая выборка");
            testSeries.setName("Тестовая выборка");
            errorChart.getData().add(trainSeries);
            errorChart.getData().add(testSeries);
        }

        if(errorChart.getData().size() > 2){
            errorChart.getData().remove(0);
        }

        //Add values to the chart
        errorChart.getData().get(errorChart.getData().size() - 2).getData().add(new XYChart.Data<>(epoch, trainError));
        errorChart.getData().get(errorChart.getData().size() - 1).getData().add(new XYChart.Data<>(epoch, testError));
    }

    public void clearCharts(){
        errorChart.getData().clear();
    }

    //Open element in new window
    public void openElementInNewWindow(String title, Pane parent, Parent element, Button openButton, double minWidth, double minHeight){
        parent.getChildren().remove(element);
        Label label = new Label("Открыто в другом окне");
        label.setFont(new Font("Segoe UI SemiLight", 14));
        label.setAlignment(Pos.CENTER);
        label.setPrefHeight(parent.getHeight());
        label.setPrefWidth(parent.getWidth());

        parent.getChildren().add(label);
        Scene scene = new Scene(element);
        openButton.setDisable(true);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            scene.setRoot(new Pane());
            parent.getChildren().remove(label);
            parent.getChildren().add(element);
            element.setStyle("-fx-background-color: white"); //Fix background color
            openButton.setDisable(false);
        });
    }
    
    public void openChartInNewWindow(Button button, Pane parent){
        openElementInNewWindow("График средней ошибки", parent, errorChart, button, 250, 250);
    }
    public void openMatrixInNewWindow(Button button, Pane parent){
        openElementInNewWindow("Матрица", parent, currentMatrix, button, 616, 290);
    }
}
