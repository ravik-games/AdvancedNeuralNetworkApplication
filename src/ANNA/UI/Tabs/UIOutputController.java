package ANNA.UI.Tabs;

import ANNA.Main;
import ANNA.Network.DataTypes;
import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import ANNA.UI.Tabs.modules.UIClassMatrixController;
import ANNA.UI.UIController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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

    public Label chartLabel, matrixLabel;

    private final HBox simulatorHBox;
    private final VBox statVBox;
    private final Button startSimulatorButton;
    private final TextArea simulatorOutput;
    private final LineChart<Integer, Double> chart;
    private Parent currentMatrix, confusionMatrixFull, confusionMatrixSingle;

    private final UIController mainController;
    private UIClassMatrixController classMatrixController;
    private Main main;

    private int chartSelectionID = 0;
    private int matrixSelectionID = 0;
    private final List<StatisticsUI> statisticsUI = new ArrayList<>();

    public List<DataTypes.Evaluation> lastTrainEvaluation;
    public List<DataTypes.Evaluation> lastTestEvaluation;


    public UIOutputController(UIController controller, TextArea simulatorOutput, Button startSimulatorButton, HBox simulatorHBox, VBox statVBox,
                              LineChart<Integer, Double> chart, Pane matrixParent, Label chartLabel, Label matrixLabel, ChoiceBox<String> statClassChoiceBox, ChoiceBox<String> matrixDataChoiceBox){
        this.simulatorHBox = simulatorHBox;
        this.statVBox = statVBox;
        this.startSimulatorButton = startSimulatorButton;
        this.simulatorOutput = simulatorOutput;
        this.mainController = controller;
        this.chart = chart;
        this.chartLabel = chartLabel;
        this.matrixLabel = matrixLabel;

        chart.setCreateSymbols(false);

        //Load matrix
        try {
            //FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfusionMatrixSingle.fxml"));
            confusionMatrixSingle = loader.load();
            classMatrixController = loader.getController();

            changeMatrix(MatrixSelector.CLASS_MATRIX);
            matrixParent.getChildren().add(currentMatrix);
        } catch (IOException e) {
            PopupController.errorMessage("ERROR", "Ошибка загрузки", "", "Произошла ошибка при загрузке дополнительных файлов.\n" + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE ,"Couldn't load .fxml files");
            System.exit(1);
        }

        //Add listener to choice boxes
        statClassChoiceBox.setValue("Все классы");
        statClassChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> UIOutputController.this.updateStatistics(false, t1.intValue()));

        classMatrixController.matrixClassSelector.setValue("Все классы");
        classMatrixController.matrixClassSelector.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> updateSingleClassMatrix(t1.intValue(),
                mainController.matrixDataChoiceBox.getItems().indexOf(mainController.matrixDataChoiceBox.getValue()) == 0, false));

        //Add items to data selector choice box
        matrixDataChoiceBox.getItems().add("Обучение");
        matrixDataChoiceBox.getItems().add("Тестирование");
        matrixDataChoiceBox.setValue("Обучение");
        matrixDataChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> updateSingleClassMatrix(t1.intValue() == 0,
                false));

    }

    public void setMain(Main main){
        this.main = main;
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
    public void updateChart(boolean newSeries, DataTypes.Evaluation trainData, DataTypes.Evaluation testData, int epoch){

        //Create series if not exist
        if(chart.getData().isEmpty() || chart.getData().get(chart.getData().size() - 1) == null || newSeries) {
            XYChart.Series<Integer, Double> trainSeries = new XYChart.Series<>();
            XYChart.Series<Integer, Double> testSeries = new XYChart.Series<>();
            trainSeries.setName("Обучающая выборка");
            testSeries.setName("Тестовая выборка");
            chart.getData().add(trainSeries);
            chart.getData().add(testSeries);
        }

        if(chart.getData().size() > 2){
            chart.getData().remove(0);
        }

        double trainValue = 0, testValue = 0;
        switch (ChartSelector.values()[chartSelectionID]){
            case LOSS_CHART -> {
                trainValue = trainData.getMeanError();
                testValue = testData.getMeanError();
            }
            case ACCURACY_CHART -> {
                trainValue = trainData.getMeanAccuracy();
                testValue = testData.getMeanAccuracy();
            }
            case F_SCORE_CHART -> {
                trainValue = trainData.getMeanFScore();
                testValue = testData.getMeanFScore();
            }
        }
        //Add values to the chart
        chart.getData().get(chart.getData().size() - 2).getData().add(new XYChart.Data<>(epoch, trainValue));
        chart.getData().get(chart.getData().size() - 1).getData().add(new XYChart.Data<>(epoch, testValue));
    }

    //Show full data on chart
    public void updateFullChart(){
        if(lastTrainEvaluation == null || lastTrainEvaluation.size() < 1 || lastTestEvaluation == null || lastTestEvaluation.size() < 1)
            return;
        clearCharts();
        //Prepare chart
        XYChart.Series<Integer, Double> trainSeries = new XYChart.Series<>();
        XYChart.Series<Integer, Double> testSeries = new XYChart.Series<>();
        trainSeries.setName("Обучающая выборка");
        testSeries.setName("Тестовая выборка");
        chart.getData().add(trainSeries);
        chart.getData().add(testSeries);
        List<XYChart.Data<Integer, Double>> trainValues = new ArrayList<>();
        List<XYChart.Data<Integer, Double>> testValues = new ArrayList<>();

        //Collect data
        switch (ChartSelector.values()[chartSelectionID]){
            case LOSS_CHART -> {
                for (int i = 0; i < lastTrainEvaluation.size(); i++) {
                    trainValues.add(new XYChart.Data<>(i + 1, lastTrainEvaluation.get(i).getMeanError()));
                }
                for (int i = 0; i < lastTestEvaluation.size(); i++) {
                    testValues.add(new XYChart.Data<>(i + 1, lastTestEvaluation.get(i).getMeanError()));
                }
            }
            case ACCURACY_CHART -> {
                for (int i = 0; i < lastTrainEvaluation.size(); i++) {
                    trainValues.add(new XYChart.Data<>(i + 1, lastTrainEvaluation.get(i).getMeanAccuracy()));
                }
                for (int i = 0; i < lastTestEvaluation.size(); i++) {
                    testValues.add(new XYChart.Data<>(i + 1, lastTestEvaluation.get(i).getMeanAccuracy()));
                }
            }
            case F_SCORE_CHART -> {
                for (int i = 0; i < lastTrainEvaluation.size(); i++) {
                    trainValues.add(new XYChart.Data<>(i + 1, lastTrainEvaluation.get(i).getMeanFScore()));
                }
                for (int i = 0; i < lastTestEvaluation.size(); i++) {
                    testValues.add(new XYChart.Data<>(i + 1, lastTestEvaluation.get(i).getMeanFScore()));
                }
            }
        }
        //Add values to the chart
        chart.getData().get(chart.getData().size() - 2).getData().addAll(trainValues);
        chart.getData().get(chart.getData().size() - 1).getData().addAll(testValues);
    }

    public void clearCharts(){
        chart.getData().clear();
    }

    //Update values in matrix
    public void updateSingleClassMatrix(boolean clear){
        updateSingleClassMatrix(classMatrixController.matrixClassSelector.getItems().indexOf(classMatrixController.matrixClassSelector.getValue()),
                mainController.matrixDataChoiceBox.getItems().indexOf(mainController.matrixDataChoiceBox.getValue()) == 0, clear);
    }
    public void updateSingleClassMatrix(boolean trainData, boolean clear){
        updateSingleClassMatrix(classMatrixController.matrixClassSelector.getItems().indexOf(classMatrixController.matrixClassSelector.getValue()), trainData, clear);
    }
    public void updateSingleClassMatrix(int newClass, boolean trainData, boolean clear){
        if(lastTrainEvaluation == null || lastTrainEvaluation.size() < 1 || lastTestEvaluation == null || lastTestEvaluation.size() < 1)
            return;
        if(clear) {
            clearMatrix();
            classMatrixController.matrixClassSelector.getItems().clear();
            classMatrixController.matrixClassSelector.getItems().add("Все классы");
            classMatrixController.matrixClassSelector.getItems().addAll(mainController.lastArguments.trainSet().allOutputTypes());
            classMatrixController.matrixClassSelector.setValue("Все классы");
        }

        int selectedClass = Math.max(0, newClass) - 1;
        DataTypes.Evaluation evaluation;
        //Switch on data
        if(trainData)
            evaluation = lastTrainEvaluation.get(lastTrainEvaluation.size() - 1);
        else
            evaluation = lastTestEvaluation.get(lastTestEvaluation.size() - 1);

        //Create string from values
        String truePositiveText = selectedClass == -1 ? String.format("%.0f", (evaluation.getTruePositive())) : String.format("%.0f", evaluation.getClassTruePositive(selectedClass));

        String trueNegativeText = selectedClass == -1 ?  String.format("%.0f", evaluation.getTrueNegative()) : String.format("%.0f", evaluation.getClassTrueNegative(selectedClass));

        String falsePositiveText = selectedClass == -1 ?  String.format("%.0f", evaluation.getFalsePositive()) : String.format("%.0f", evaluation.getClassFalsePositive(selectedClass));

        String falseNegativeText = selectedClass == -1 ?  String.format("%.0f", evaluation.getFalseNegative()) : String.format("%.0f", evaluation.getClassFalseNegative(selectedClass));

        classMatrixController.truePositiveLabel.setText(truePositiveText);
        classMatrixController.trueNegativeLabel.setText(trueNegativeText);
        classMatrixController.falsePositiveLabel.setText(falsePositiveText);
        classMatrixController.falseNegativeLabel.setText(falseNegativeText);
    }

    public void clearMatrix(){
        switch (MatrixSelector.values()[matrixSelectionID]){
            case CLASS_MATRIX -> {
                classMatrixController.truePositiveLabel.setText("...");
                classMatrixController.trueNegativeLabel.setText("...");
                classMatrixController.falsePositiveLabel.setText("...");
                classMatrixController.falseNegativeLabel.setText("...");
            }
        }
    }

    //Switch between different output panes
    public void switchSelector(int selectorID, boolean left){
        switch (selectorID){
            case 0 -> {
                chartSelectionID = (chartSelectionID + (left ? -1 : 1)) % ChartSelector.values().length;
                chartSelectionID = chartSelectionID < 0 ? ChartSelector.values().length - 1 : chartSelectionID;

                changeChart(ChartSelector.values()[chartSelectionID]);
                updateFullChart();
            }
            case 1 -> {
                matrixSelectionID = (matrixSelectionID + (left ? -1 : 1)) % MatrixSelector.values().length;
                matrixSelectionID = matrixSelectionID < 0 ? MatrixSelector.values().length - 1 : matrixSelectionID;

                changeMatrix(MatrixSelector.values()[matrixSelectionID]);
            }
        }
    }

    private void changeChart(ChartSelector selection){
        switch (selection){
            case LOSS_CHART -> {
                chart.getYAxis().setLabel("Средняя ошибка");
                chartLabel.setText("График средней ошибки");
                mainController.setChartForceYZero(true);
            }
            case ACCURACY_CHART -> {
                chart.getYAxis().setLabel("Точность (Accuracy)");
                chartLabel.setText("График средней точности");
                mainController.setChartForceYZero(false);
            }
            case F_SCORE_CHART -> {
                chart.getYAxis().setLabel("Средняя F-мера");
                chartLabel.setText("График средней F-меры");
                mainController.setChartForceYZero(false);
            }
        }
    }

    private void changeMatrix(MatrixSelector selection){
        switch (selection){
            case CLASS_MATRIX -> {
                currentMatrix = confusionMatrixSingle;
                matrixLabel.setText("Матрица ошибок №1");
                updateSingleClassMatrix(false);
            }
        }
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
        openElementInNewWindow("График", parent, chart, button, 250, 250);
    }
    public void openMatrixInNewWindow(Button button, Pane parent){
        openElementInNewWindow("Матрица", parent, currentMatrix, button, 616, 290);
    }

    //Show and update statistics
    public void updateStatistics(boolean clear){
        updateStatistics(clear, mainController.statClassChoiceBox.getItems().indexOf(mainController.statClassChoiceBox.getValue()));
    }
    public void updateStatistics(boolean clear, int currentClass){
        //Clear
        if (clear) {
            statVBox.getChildren().remove(1, statVBox.getChildren().size() - 1);
            statisticsUI.clear();
            mainController.statClassChoiceBox.getItems().clear();
            mainController.statClassChoiceBox.setValue("Все классы");
        }
        if(lastTrainEvaluation == null || lastTrainEvaluation.size() < 1 || lastTestEvaluation == null || lastTestEvaluation.size() < 1)
            return;

        //Create rows
        if (statisticsUI.size() == 0) {
            createStatisticsUI();
        }

        DataTypes.Evaluation lastTrainData = lastTrainEvaluation.get(lastTrainEvaluation.size() - 1);
        DataTypes.Evaluation lastTestData = lastTestEvaluation.get(lastTestEvaluation.size() - 1);
        currentClass = Math.max(0, currentClass)  - 1;
        //Update statistics values
        for (StatisticsUI ui : statisticsUI) {
            double trainValue = 0, testValue = 0;
            switch (ui.metric){
                case LOSS -> {
                    trainValue = lastTrainData.getMeanError();
                    testValue = lastTestData.getMeanError();
                }
                case SIZE -> {
                    trainValue = currentClass == -1 ? lastTrainData.getSize() : lastTrainData.getClassSize();
                    testValue = currentClass == -1 ? lastTestData.getSize() : lastTestData.getClassSize();
                }
                case HITS -> {
                    trainValue = currentClass == -1 ? lastTrainData.getHits() : lastTrainData.getClassHits(currentClass);
                    testValue = currentClass == -1 ? lastTestData.getHits() : lastTestData.getClassHits(currentClass);
                }
                case ACCURACY -> {
                    trainValue = currentClass == -1 ? lastTrainData.getMeanAccuracy() : lastTrainData.getClassAccuracy(currentClass);
                    testValue = currentClass == -1 ? lastTestData.getMeanAccuracy() : lastTestData.getClassAccuracy(currentClass);
                }
                case PRECISION -> {
                    trainValue = currentClass == -1 ? lastTrainData.getMeanPrecision() : lastTrainData.getClassPrecision(currentClass);
                    testValue = currentClass == -1 ? lastTestData.getMeanPrecision() : lastTestData.getClassPrecision(currentClass);
                }
                case RECALL -> {
                    trainValue = currentClass == -1 ? lastTrainData.getMeanRecall() : lastTrainData.getClassRecall(currentClass);
                    testValue = currentClass == -1 ? lastTestData.getMeanRecall() : lastTestData.getClassRecall(currentClass);
                }
                case F_SCORE -> {
                    trainValue = currentClass == -1 ? lastTrainData.getMeanFScore() : lastTrainData.getClassFScore(currentClass);
                    testValue = currentClass == -1 ? lastTestData.getMeanFScore() : lastTestData.getClassFScore(currentClass);
                }
            }
            ui.trainLabel.setText(String.valueOf(trainValue));
            ui.testLabel.setText(String.valueOf(testValue));
        }
    }

    private void createStatisticsUI(){
        for (int i = 0; i < DataTypes.Evaluation.Metrics.values().length; i++) {
            //Set name
            String name = switch (DataTypes.Evaluation.Metrics.values()[i]){
                case SIZE -> "Размер данных";
                case LOSS -> "Ошибка последней эпохи (MSE)";
                case HITS -> "Верно определено";
                case ACCURACY -> "Точность (Accuracy)";
                case PRECISION -> "Точность (Precision)";
                case RECALL -> "Полнота (Recall)";
                case F_SCORE -> "F-мера";
            };

            //Create and config labels
            Label nameLabel = new Label(name);
            nameLabel.setFont(new Font("Segoe UI SemiLight", 14));
            nameLabel.setPadding(new Insets(5));
            nameLabel.setPrefWidth(225);
            nameLabel.setPrefHeight(35);

            Label trainLabel = new Label("...");
            trainLabel.setFont(new Font("Segoe UI SemiLight", 14));
            trainLabel.setPadding(new Insets(5));
            trainLabel.setPrefWidth(188);
            trainLabel.setPrefHeight(35);

            Label testLabel = new Label("...");
            testLabel.setFont(new Font("Segoe UI SemiLight", 14));
            testLabel.setPadding(new Insets(5));
            testLabel.setPrefWidth(188);
            testLabel.setPrefHeight(35);

            //Add all to HBox and to VBox
            HBox hBox = new HBox();
            hBox.getChildren().add(nameLabel);
            hBox.getChildren().add(new Separator(Orientation.VERTICAL));
            hBox.getChildren().add(trainLabel);
            hBox.getChildren().add(new Separator(Orientation.VERTICAL));
            hBox.getChildren().add(testLabel);

            statisticsUI.add(new StatisticsUI(DataTypes.Evaluation.Metrics.values()[i], trainLabel, testLabel));
            statVBox.getChildren().add(hBox);
            statVBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        }
        mainController.statClassChoiceBox.getItems().add("Все классы");
        mainController.statClassChoiceBox.getItems().addAll(mainController.lastArguments.trainSet().allOutputTypes());
    }

    private record StatisticsUI(DataTypes.Evaluation.Metrics metric, Label trainLabel, Label testLabel) { }

    //Lists of items for selector panels in UI
    private enum ChartSelector{ //ID 0
        LOSS_CHART, ACCURACY_CHART, F_SCORE_CHART
    }
    private enum MatrixSelector{ //ID 1
        CLASS_MATRIX, FULL_MATRIX
    }
}
