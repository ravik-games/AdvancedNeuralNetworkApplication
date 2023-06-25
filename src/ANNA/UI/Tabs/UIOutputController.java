package ANNA.UI.Tabs;

import ANNA.Main;
import ANNA.Network.DataTypes;
import ANNA.UI.Parser;
import ANNA.UI.PopupController;
import ANNA.UI.Tabs.modules.UIClassMatrixController;
import ANNA.UI.Tabs.modules.UIFullMatrixController;
import ANNA.UI.UIController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.awt.*;
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
    private UIFullMatrixController fullMatrixController;
    private Main main;

    private int chartSelectionID = 0;
    private int matrixSelectionID = 0;
    private int fullMatrixClassCount = 0;
    private boolean matrixInNewWindow;
    private final List<StatisticsUI> statisticsUI = new ArrayList<>();

    public List<DataTypes.Evaluation> lastTrainEvaluation;
    public List<DataTypes.Evaluation> lastTestEvaluation;


    public UIOutputController(UIController controller, TextArea simulatorOutput, Button startSimulatorButton, HBox simulatorHBox, VBox statVBox,
                              LineChart<Integer, Double> chart, Label chartLabel, Label matrixLabel, ChoiceBox<String> statClassChoiceBox, ChoiceBox<String> matrixDataChoiceBox){
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
            //Single class matrix
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfusionMatrixSingle.fxml"));
            confusionMatrixSingle = loader.load();
            classMatrixController = loader.getController();

            //Full matrix
            loader = new FXMLLoader(getClass().getResource("/fxml/ConfusionMatrixFull.fxml"));
            confusionMatrixFull = loader.load();
            fullMatrixController = loader.getController();

            //Set default to class matrix
            changeMatrix(MatrixSelector.CLASS_MATRIX);
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
        matrixDataChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> updateMatrix(t1.intValue() == 0));

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
    public void updateChart(boolean newSeries, int epoch){
        DataTypes.Evaluation trainData = lastTrainEvaluation.get(lastTrainEvaluation.size() - 1);
        DataTypes.Evaluation testData = lastTestEvaluation.get(lastTestEvaluation.size() - 1);

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

        //Collect and add data to chart
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

    public void setChartXLength(double number){
        mainController.chartXAxis.setAutoRanging(number < 0);
        mainController.chartXAxis.setLowerBound(0);
        mainController.chartXAxis.setUpperBound(number * 1.1);
        mainController.chartXAxis.setTickUnit(number / 10);
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
        if(clear)
            clearMatrix();

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

    public void updateFullMatrix(boolean clear){
        updateFullMatrix(mainController.matrixDataChoiceBox.getItems().indexOf(mainController.matrixDataChoiceBox.getValue()) == 0, clear);
    }
    public void updateFullMatrix(boolean trainData, boolean clear){
        if(lastTrainEvaluation == null || lastTrainEvaluation.size() < 1 || lastTestEvaluation == null || lastTestEvaluation.size() < 1)
            return;
        if(clear)
            clearMatrix();

        //Switch on data
        DataTypes.Evaluation evaluation = trainData ? lastTrainEvaluation.get(lastTrainEvaluation.size() - 1) : lastTestEvaluation.get(lastTestEvaluation.size() - 1);
        fullMatrixClassCount = evaluation.getClassesInfoTable().length;

        //Limiting class count
        double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        if (fullMatrixClassCount > Math.floor(height / 30.8) && matrixInNewWindow){
            fullMatrixController.matrixGrid.add(createNewCellWithLabel("Превышен лимит классов для отображения (" + Math.floor(height / 30.8) + ")",
                    32), 0, 0);
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100);
            row.setValignment(VPos.CENTER);

            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100);
            column.setHalignment(HPos.CENTER);

            fullMatrixController.matrixGrid.getRowConstraints().add(row);
            fullMatrixController.matrixGrid.getColumnConstraints().add(column);
            return;
        }
        else if (fullMatrixClassCount > 6 && !matrixInNewWindow){
            fullMatrixController.matrixGrid.add(createNewCellWithLabel("Превышен лимит классов для отображения (6), откройте матрицу в новом окне для просмотра",
                    14), 0, 0);
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100);
            row.setValignment(VPos.CENTER);

            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100);
            column.setHalignment(HPos.CENTER);

            fullMatrixController.matrixGrid.getRowConstraints().add(row);
            fullMatrixController.matrixGrid.getColumnConstraints().add(column);
            return;
        }

        //Create and add data to matrix
        for (int i = 0; i < fullMatrixClassCount; i++) {
            for (int j = 0; j < fullMatrixClassCount; j++) {
                double currentCount = evaluation.getClassesInfoTable()[i][j];
                double expectedClassPositive = 0;
                for (double[] k : evaluation.getClassesInfoTable()) {
                    expectedClassPositive += k[j];
                }
                Pane pane = createNewCellWithLabel(currentCount == 0 ? "" : String.format("%.0f", currentCount), 14);

                //Skip, if count == 0 and it's not diagonal
                if(i != j && Double.compare(currentCount, 0) == 0) {
                    pane.setStyle("-fx-border-width: 1; -fx-border-color: gray;");
                    fullMatrixController.matrixGrid.add(pane, j + 1, i + 1);
                    continue;
                }

                //Set color of pane
                Color color = Color.hsb((i == j ? currentCount / expectedClassPositive : (1 - currentCount / expectedClassPositive)) * 100, 0.8, 0.9);
                pane.setStyle("-fx-border-width: 1;" +
                        "-fx-border-color: gray;" +
                        "-fx-background-color: #" + colorFormat(color.getRed()) + colorFormat(color.getGreen()) + colorFormat(color.getBlue()) + colorFormat(color.getOpacity()));

                fullMatrixController.matrixGrid.add(pane, j + 1, i + 1);
            }

            //Add column and row class labels
            Pane rowLabelCell = createNewCellWithLabel(mainController.lastArguments.trainSet().allOutputTypes()[i], 14);
            Pane columnLabelCell = createNewCellWithLabel(mainController.lastArguments.trainSet().allOutputTypes()[i], 14);
            fullMatrixController.matrixGrid.add(rowLabelCell, 0, i + 1);
            fullMatrixController.matrixGrid.add(columnLabelCell, i + 1, 0);
        }

        //Add empty cell in top right corner for design reasons
        Pane pane = new Pane();
        pane.setStyle("-fx-border-width: 1;-fx-border-color: gray;");
        fullMatrixController.matrixGrid.add(pane, 0, 0);

        //Configure rows and columns
        double percent = 100f / (fullMatrixClassCount + 1);
        for (int i = 0; i < fullMatrixClassCount + 1; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(percent);
            row.setValignment(VPos.CENTER);

            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(percent);
            column.setHalignment(HPos.CENTER);

            fullMatrixController.matrixGrid.getRowConstraints().add(row);
            fullMatrixController.matrixGrid.getColumnConstraints().add(column);
        }
    }

    public void clearMatrix(){
        switch (MatrixSelector.values()[matrixSelectionID]){
            case CLASS_MATRIX -> {
                classMatrixController.clear();
                classMatrixController.matrixClassSelector.getItems().add("Все классы");
                classMatrixController.matrixClassSelector.getItems().addAll(mainController.lastArguments.trainSet().allOutputTypes());
                classMatrixController.matrixClassSelector.setValue("Все классы");
            }
            case FULL_MATRIX -> fullMatrixController.clear();
        }
    }

    private String colorFormat(double value){
        String str = Integer.toHexString((int) Math.round(value * 255));
        return str.length() == 1 ? "0" + str : str;
    }

    private Pane createNewCellWithLabel(String text, int textSize){
        AnchorPane pane = new AnchorPane();
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI SemiBold", textSize));
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setWrapText(true);

        //Set label in center
        AnchorPane.setLeftAnchor(label, 0d);
        AnchorPane.setRightAnchor(label, 0d);
        AnchorPane.setTopAnchor(label, 0d);
        AnchorPane.setBottomAnchor(label, 0d);

        pane.setStyle("-fx-border-width: 1;-fx-border-color: gray;");
        pane.getChildren().add(label);
        return pane;
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
        if(matrixInNewWindow)
            return;
        switch (selection){
            case CLASS_MATRIX -> {
                currentMatrix = confusionMatrixSingle;
                matrixLabel.setText("Матрица ошибок №1");
                updateSingleClassMatrix(false);
            }
            case FULL_MATRIX -> {
                currentMatrix = confusionMatrixFull;
                matrixLabel.setText("Матрица ошибок №2");
                updateFullMatrix(true);
            }
        }
        mainController.matrixParent.getChildren().clear();
        mainController.matrixParent.getChildren().add(currentMatrix);
    }

    private void updateMatrix(boolean trainData){
        switch (MatrixSelector.values()[matrixSelectionID]){
            case CLASS_MATRIX -> updateSingleClassMatrix(trainData, false);
            case FULL_MATRIX -> updateFullMatrix(trainData, true);
        }
    }

    private void updateMatrix(){
        switch (MatrixSelector.values()[matrixSelectionID]){
            case CLASS_MATRIX -> updateSingleClassMatrix(false);
            case FULL_MATRIX -> updateFullMatrix(true);
        }
    }

    //Open chart in new window
    public Stage openElementInNewWindow(String title, Pane parent, Parent element, Button openButton, double minWidth, double minHeight){
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
        stage.setWidth(minWidth);
        stage.setHeight(minHeight);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            scene.setRoot(new Pane());
            parent.getChildren().remove(label);
            parent.getChildren().add(element);
            element.setStyle("-fx-background-color: white"); //Fix background color
            openButton.setDisable(false);
        });

        return stage;
    }
    
    public void openChartInNewWindow(Button button, Pane parent){
        openElementInNewWindow("График", parent, chart, button, 500, 500).setOnCloseRequest(windowEvent -> {
            //Override default close event
            chart.getScene().setRoot(new Pane());
            parent.getChildren().clear();
            parent.getChildren().add(chart);
            chart.setStyle("-fx-background-color: white"); //Fix background color
            button.setDisable(false);
        });
    }
    public void openMatrixInNewWindow(Button button, Pane parent){
        matrixInNewWindow = true;
        double minHeight = Math.max(290, Math.min(fullMatrixClassCount * 30.6, Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 50));

        openElementInNewWindow("Матрица", parent, currentMatrix, button, Math.floor(minHeight * 2), minHeight).setOnCloseRequest(windowEvent -> {
            //Override default close event
            currentMatrix.getScene().setRoot(new Pane());
            parent.getChildren().clear();
            parent.getChildren().add(currentMatrix);
            currentMatrix.setStyle("-fx-background-color: white"); //Fix background color
            button.setDisable(false);

            matrixInNewWindow = false;
            updateMatrix();
        });

        updateMatrix();
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
