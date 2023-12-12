package anna.ui.tabs;

import anna.Application;
import anna.math.ErrorFunctions;
import anna.network.data.DataMaster;
import anna.network.data.DataTypes;
import anna.network.data.Hyperparameters;
import anna.ui.DefaultUIController;
import anna.ui.Parser;
import anna.ui.PopupController;
import anna.ui.tabs.modules.UIClassMatrixController;
import anna.ui.tabs.modules.UIFullMatrixController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UIOutputController {
    //Class for working with fourth tab (Results)

    public Label chartLabel;
    public HBox simulatorHBox, statPane;
    public Button startSimulatorButton, newWindowChartButton, newWindowBinaryMatrixButton, newWindowFullMatrixButton;
    public Label simulatorOutput, overallRatingLabel;
    public LineChart<Integer, Double> chart;
    public NumberAxis chartXAxis, chartYAxis;
    public Parent confusionMatrixFull, confusionMatrixSingle;
    public Pane chartPane, binaryMatrixPane, fullMatrixPane;
    public ChoiceBox<String> matrixDataChoiceBox, statClassChoiceBox;
    public FontIcon binaryMatrixInfo, fullMatrixInfo, statisticsInfo, ratingInfo, simulatorInfo, simulatorResultInfo;

    protected DefaultUIController masterController;
    protected UIClassMatrixController binaryMatrixController;
    protected UIFullMatrixController fullMatrixController;
    protected DataMaster dataMaster;
    protected Application application;

    protected int chartSelectionID = 0;
    protected int fullMatrixClassCount = 0;
    protected boolean fullMatrixInNewWindow;
    protected final List<StatisticsUI> statisticsUI = new ArrayList<>();
    protected List<SimulatorColumn> simulatorColumns = new ArrayList<>();

    public List<DataTypes.Evaluation> lastTrainEvaluation;
    public List<DataTypes.Evaluation> lastTestEvaluation;


    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    public void initialize() {
        chart.setCreateSymbols(false);
        //Load matrix
        try {
            //Single class matrix
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfusionMatrixSingle.fxml"), bundle);
            confusionMatrixSingle = loader.load();
            binaryMatrixPane.getChildren().add(confusionMatrixSingle);
            AnchorPane.setTopAnchor(confusionMatrixSingle, 0D);
            AnchorPane.setBottomAnchor(confusionMatrixSingle, 0D);
            AnchorPane.setLeftAnchor(confusionMatrixSingle, 0D);
            AnchorPane.setRightAnchor(confusionMatrixSingle, 0D);
            binaryMatrixController = loader.getController();

            //Full matrix
            loader = new FXMLLoader(getClass().getResource("/fxml/ConfusionMatrixFull.fxml"), bundle);
            confusionMatrixFull = loader.load();
            fullMatrixPane.getChildren().add(confusionMatrixFull);
            AnchorPane.setTopAnchor(confusionMatrixFull, 0D);
            AnchorPane.setBottomAnchor(confusionMatrixFull, 0D);
            AnchorPane.setLeftAnchor(confusionMatrixFull, 0D);
            AnchorPane.setRightAnchor(confusionMatrixFull, 0D);
            fullMatrixController = loader.getController();

            //Set default to class matrix
            //changeMatrix(MatrixSelector.CLASS_MATRIX);
        } catch (IOException e) {
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.fileLoadingError"), true);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE ,"Couldn't load .fxml files. See stack trace:\n" + e.getMessage());
        }

        //Add listener to choice boxes
        statClassChoiceBox.setValue(bundle.getString("tab.results.allClasses"));
        statClassChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> UIOutputController.this.updateStatistics(false, t1.intValue()));

        binaryMatrixController.matrixClassSelector.setValue(bundle.getString("tab.results.allClasses"));
        binaryMatrixController.matrixClassSelector.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> updateSingleClassMatrix(t1.intValue(),
                matrixDataChoiceBox.getItems().indexOf(matrixDataChoiceBox.getValue()) == 0));

        //Add items to data selector choice box
        matrixDataChoiceBox.getItems().add(bundle.getString("general.training"));
        matrixDataChoiceBox.getItems().add(bundle.getString("general.testing"));
        matrixDataChoiceBox.setValue(bundle.getString("general.training"));
        matrixDataChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> updateMatrix(t1.intValue() == 0));

        // Clear and prepare simulator
        initializeSimulator(false);
    }


    public void setReferences(Application application, DataMaster dataMaster, DefaultUIController masterController){
        this.application = application;
        this.dataMaster = dataMaster;
        this.masterController = masterController;

        setupHints();
    }

    protected void setupHints() {
        masterController.setupHint(binaryMatrixInfo, bundle.getString("tab.results.hints.binaryMatrix"));
        masterController.setupHint(fullMatrixInfo, bundle.getString("tab.results.hints.fullMatrix"));
        masterController.setupHint(statisticsInfo, bundle.getString("tab.results.hints.statistics"));
        masterController.setupHint(ratingInfo, bundle.getString("tab.results.hints.rating"));
        masterController.setupHint(simulatorInfo, bundle.getString("tab.results.hints.simulator"));
        masterController.setupHint(simulatorResultInfo, bundle.getString("tab.results.hints.simulatorResult"));
    }

    // Create/update simulator table
    public void initializeSimulator(boolean enable){
        simulatorHBox.getChildren().clear();
        simulatorColumns.clear();
        simulatorHBox.setDisable(!enable);
        startSimulatorButton.setDisable(!enable);
        if(!enable || masterController.lastArguments == null) {
            simulatorHBox.setDisable(true);
            startSimulatorButton.setDisable(true);
            return;
        }

        for (int i = 0; i < masterController.lastArguments.networkData().getStructure().get(0).neuronNumber(); i++) {
            SimulatorColumn column = new SimulatorColumn(masterController.structureController.getInputParameters().get(i), dataMaster);
            simulatorColumns.add(column);
            simulatorHBox.getChildren().addAll(column.getColumn());
        }
    }

    //Prepare and run simulator
    public void prepareSimulationValues(){
        double[] inputs = simulatorColumns.stream().mapToDouble(simulatorColumn -> switch (simulatorColumn.getType()) {
            case NUMBER -> simulatorColumn.getNumberValue();
            case CATEGORICAL -> simulatorColumn.getCategoricalValue();
        }).toArray();

        application.runSimulation(inputs);
    }

    //Add simulation output to UI
    public void simulationClassificationResult(double[] outputValues, String outputCategory){
        String text = bundle.getString("tab.results.simulator.classificationCategory") + outputCategory;
        simulatorOutput.setText(text);
    }
    public void simulationPredictionResult(double outputValue){
        String text = bundle.getString("tab.results.simulator.predictionValue") + outputValue;
        simulatorOutput.setText(text);
    }

    //Show data on chart
    public void updateChart(boolean newSeries, int epoch){
        DataTypes.Evaluation trainData = lastTrainEvaluation.get(lastTrainEvaluation.size() - 1);
        DataTypes.Evaluation testData = lastTestEvaluation.get(lastTestEvaluation.size() - 1);

        //Create series if not exist
        if(chart.getData().isEmpty() || chart.getData().get(chart.getData().size() - 1) == null || newSeries) {
            XYChart.Series<Integer, Double> trainSeries = new XYChart.Series<>();
            XYChart.Series<Integer, Double> testSeries = new XYChart.Series<>();
            trainSeries.setName(bundle.getString("tab.data.trainDataset"));
            testSeries.setName(bundle.getString("tab.data.testDataset"));
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
        if(lastTrainEvaluation == null || lastTrainEvaluation.isEmpty() || lastTestEvaluation == null || lastTestEvaluation.isEmpty())
            return;
        clearCharts();
        //Prepare chart
        XYChart.Series<Integer, Double> trainSeries = new XYChart.Series<>();
        XYChart.Series<Integer, Double> testSeries = new XYChart.Series<>();
        trainSeries.setName(bundle.getString("tab.data.trainDataset"));
        testSeries.setName(bundle.getString("tab.data.testDataset"));
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
        chartXAxis.setAutoRanging(number < 0);
        chartXAxis.setLowerBound(0);
        chartXAxis.setUpperBound(number * 1.1);
        chartXAxis.setTickUnit(number / 10);
    }

    //Update values in matrix
    public void updateSingleClassMatrix(){
        updateSingleClassMatrix(binaryMatrixController.matrixClassSelector.getItems().indexOf(binaryMatrixController.matrixClassSelector.getValue()),
                matrixDataChoiceBox.getItems().indexOf(matrixDataChoiceBox.getValue()) == 0);
    }
    public void updateSingleClassMatrix(boolean trainData){
        updateSingleClassMatrix(binaryMatrixController.matrixClassSelector.getItems().indexOf(binaryMatrixController.matrixClassSelector.getValue()), trainData);
    }
    public void updateSingleClassMatrix(int newClass, boolean trainData){
        if(lastTrainEvaluation == null || lastTrainEvaluation.isEmpty() || lastTestEvaluation == null || lastTestEvaluation.isEmpty())
            return;

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

        binaryMatrixController.truePositiveLabel.setText(truePositiveText);
        binaryMatrixController.trueNegativeLabel.setText(trueNegativeText);
        binaryMatrixController.falsePositiveLabel.setText(falsePositiveText);
        binaryMatrixController.falseNegativeLabel.setText(falseNegativeText);
    }

    public void updateFullMatrix(boolean clear){
        updateFullMatrix(matrixDataChoiceBox.getItems().indexOf(matrixDataChoiceBox.getValue()) == 0, clear);
    }
    public void updateFullMatrix(boolean trainData, boolean clear){
        if(lastTrainEvaluation == null || lastTrainEvaluation.isEmpty() || lastTestEvaluation == null || lastTestEvaluation.isEmpty())
            return;
        if(clear)
            clearMatrix();

        //Switch on data
        DataTypes.Evaluation evaluation = trainData ? lastTrainEvaluation.get(lastTrainEvaluation.size() - 1) : lastTestEvaluation.get(lastTestEvaluation.size() - 1);
        fullMatrixClassCount = evaluation.getClassesInfoTable().length;

        //Limiting class count
        double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        if (fullMatrixClassCount > Math.floor(height / 30.8) && fullMatrixInNewWindow){
            fullMatrixController.matrixGrid.add(createNewCellWithLabel(bundle.getString("tab.results.matrix.errorMatrix.classLimitFull") + Math.floor(height / 30.8) + ")",
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
        else if (fullMatrixClassCount > 6 && !fullMatrixInNewWindow){
            fullMatrixController.matrixGrid.add(createNewCellWithLabel(bundle.getString("tab.results.matrix.errorMatrix.classLimitSmall"),
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
                    pane.setStyle("-fx-border-width: 1; -fx-border-color: rgb(128,128,128);");
                    fullMatrixController.matrixGrid.add(pane, j + 1, i + 1);
                    continue;
                }

                //Set color of pane
                Color color = Color.hsb((i == j ? currentCount / expectedClassPositive : (1 - currentCount / expectedClassPositive)) * 100, 0.8, 0.9);
                pane.setStyle("-fx-border-width: 1;" +
                        "-fx-border-color: rgb(128,128,128);" +
                        "-fx-background-color: #" + colorFormat(color.getRed()) + colorFormat(color.getGreen()) + colorFormat(color.getBlue()) + colorFormat(color.getOpacity()));

                fullMatrixController.matrixGrid.add(pane, j + 1, i + 1);
            }

            //Add column and row class labels
            Pane rowLabelCell = createNewCellWithLabel(masterController.lastArguments.trainSet().allOutputTypes()[i], 14);
            Pane columnLabelCell = createNewCellWithLabel(masterController.lastArguments.trainSet().allOutputTypes()[i], 14);
            fullMatrixController.matrixGrid.add(rowLabelCell, 0, i + 1);
            fullMatrixController.matrixGrid.add(columnLabelCell, i + 1, 0);
        }

        //Add empty cell in top right corner for design reasons
        Pane pane = new Pane();
        pane.setStyle("-fx-border-width: 1;-fx-border-color: rgb(128,128,128);");
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
        fullMatrixController.clear();
    }

    public void initializeMatrixClasses() {
        binaryMatrixController.matrixClassSelector.getItems().clear();
        binaryMatrixController.matrixClassSelector.getItems().add(bundle.getString("tab.results.allClasses"));
        binaryMatrixController.matrixClassSelector.getItems().addAll(masterController.lastArguments.trainSet().allOutputTypes());
        binaryMatrixController.matrixClassSelector.getSelectionModel().select(0);
    }

    private String colorFormat(double value){
        String str = Integer.toHexString((int) Math.round(value * 255));
        return str.length() == 1 ? "0" + str : str;
    }

    private Pane createNewCellWithLabel(String text, int textSize){
        AnchorPane pane = new AnchorPane();
        Label label = new Label(text);
        label.setFont(Font.font("Inter Light", textSize));
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setWrapText(true);

        //Set label in center
        AnchorPane.setLeftAnchor(label, 0d);
        AnchorPane.setRightAnchor(label, 0d);
        AnchorPane.setTopAnchor(label, 0d);
        AnchorPane.setBottomAnchor(label, 0d);

        pane.setStyle("-fx-border-width: 1;-fx-border-color: rgb(128,128,128);");
        pane.getChildren().add(label);
        return pane;
    }

    //Switch between different charts
    private void changeChart(boolean left){
        chartSelectionID = (chartSelectionID + (left ? -1 : 1)) % ChartSelector.values().length;
        chartSelectionID = chartSelectionID < 0 ? ChartSelector.values().length - 1 : chartSelectionID;

        switch (ChartSelector.values()[chartSelectionID]){
            case LOSS_CHART -> {
                chart.getYAxis().setLabel(bundle.getString("tab.results.chart.meanError"));
                chartLabel.setText(bundle.getString("tab.results.chart.meanErrorChart"));
                chartYAxis.setForceZeroInRange(true);
            }
            case ACCURACY_CHART -> {
                chart.getYAxis().setLabel(bundle.getString("tab.results.chart.accuracy"));
                chartLabel.setText(bundle.getString("tab.results.chart.accuracyChart"));
                chartYAxis.setForceZeroInRange(false);
            }
            case F_SCORE_CHART -> {
                chart.getYAxis().setLabel(bundle.getString("tab.results.chart.meanFScore"));
                chartLabel.setText(bundle.getString("tab.results.chart.meanFScoreChart"));
                chartYAxis.setForceZeroInRange(false);
            }
        }
        updateFullChart();
    }

    private void updateMatrix(boolean trainData){
        updateSingleClassMatrix(trainData);
        updateFullMatrix(trainData, true);
    }

    private void updateMatrix(){
        updateSingleClassMatrix(false);
        updateFullMatrix(true);
    }

    //Open chart in new window
    public void openElementInNewWindow(String title, Pane parent, Parent element, Button openButton, double minWidth, double minHeight, Runnable onClose){
        parent.getChildren().remove(element);
        Label label = new Label(bundle.getString("tab.results.inNewWindow"));
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
            parent.getChildren().clear();
            parent.getChildren().add(element);
            element.setStyle("-fx-background-color: white"); //Fix background color
            openButton.setDisable(false);
            onClose.run();
        });

    }

    public void newWindowChart() {
        openElementInNewWindow(bundle.getString("tab.results.chart"), chartPane, chart, newWindowChartButton, 500, 500, () -> {});
    }

    public void newWindowBinaryMatrix() {
        openMatrixInNewWindow(false);
    }
    public void newWindowFullMatrix() {
        openMatrixInNewWindow(true);
    }

    public void openMatrixInNewWindow(boolean fullMatrix){
        if(fullMatrix)
            fullMatrixInNewWindow = true;

        Button button = fullMatrix ? newWindowFullMatrixButton : newWindowBinaryMatrixButton;
        Pane parent = fullMatrix ? fullMatrixPane : binaryMatrixPane;
        Parent matrix = fullMatrix ? confusionMatrixFull : confusionMatrixSingle;

        double minHeight = fullMatrix ? Math.max(290, Math.min(fullMatrixClassCount * 30.6, Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 50)) : 300;

        openElementInNewWindow(bundle.getString("tab.results.matrix"), parent, matrix, button, Math.floor(minHeight * 2), minHeight, () -> {
            if(fullMatrix)
                fullMatrixInNewWindow = false;

            updateMatrix();
        });

        updateMatrix();
    }

    public void changeChartLeft() {
        changeChart(true);
    }
    public void changeChartRight() {
        changeChart(false);
    }

    //Show and update statistics
    public void updateStatistics(boolean clear){
        updateStatistics(clear, statClassChoiceBox.getItems().indexOf(statClassChoiceBox.getValue()));
    }
    public void updateStatistics(boolean clear, int currentClass){
        //Clear
        if (clear) {
            statPane.getChildren().clear();
            statisticsUI.clear();
            statClassChoiceBox.getItems().clear();
            statClassChoiceBox.setValue(bundle.getString("tab.results.allClasses"));
            overallRatingLabel.setText("...");
            overallRatingLabel.setStyle("-fx-background-radius: 2.5; -fx-background-color: #ffffff;");
        }
        if(lastTrainEvaluation == null || lastTrainEvaluation.isEmpty() || lastTestEvaluation == null || lastTestEvaluation.isEmpty())
            return;

        //Create rows
        if (statisticsUI.isEmpty()) {
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

        long rating = calculateRating(lastTrainData, lastTestData);
        overallRatingLabel.setText(String.valueOf(rating));

        Color color = Color.hsb(rating, 0.8, 0.9);
        overallRatingLabel.setStyle("-fx-background-radius: 2.5;" +
                        "-fx-background-color: #" + colorFormat(color.getRed()) + colorFormat(color.getGreen()) + colorFormat(color.getBlue()) + colorFormat(color.getOpacity()));
    }

    protected void createStatisticsUI(){
        String style = "-fx-border-color: #4769ff;" +
                "-fx-border-width: 1 0 0 0;";

        for (int i = 0; i < DataTypes.Evaluation.Metrics.values().length; i++) {
            //Set name
            Label nameLabel = getStatisticsLabel(i);

            Label trainLabel = new Label("...");
            trainLabel.setFont(new Font("Inter", 14));
            trainLabel.setPadding(new Insets(10));
            trainLabel.setMaxWidth(Double.MAX_VALUE);
            trainLabel.setMaxHeight(Double.MAX_VALUE);
            trainLabel.setWrapText(true);
            trainLabel.setStyle(style);

            Label testLabel = new Label("...");
            testLabel.setFont(new Font("Inter", 14));
            testLabel.setPadding(new Insets(10));
            testLabel.setMaxWidth(Double.MAX_VALUE);
            testLabel.setMaxHeight(Double.MAX_VALUE);
            testLabel.setWrapText(true);
            testLabel.setStyle(style);

            //Add all to HBox and to VBox
            VBox vBox = new VBox();
            vBox.getChildren().add(nameLabel);
            vBox.getChildren().add(trainLabel);
            vBox.getChildren().add(testLabel);

            vBox.setStyle("-fx-border-color: #4769ff; -fx-border-width: 0 1 0 0;");
            vBox.setPrefWidth(190);

            VBox.setVgrow(nameLabel, Priority.ALWAYS);
            VBox.setVgrow(trainLabel, Priority.ALWAYS);
            VBox.setVgrow(testLabel, Priority.ALWAYS);

            statisticsUI.add(new StatisticsUI(DataTypes.Evaluation.Metrics.values()[i], trainLabel, testLabel));
            statPane.getChildren().add(vBox);
        }
        statClassChoiceBox.getItems().add(bundle.getString("tab.results.allClasses"));
        statClassChoiceBox.getItems().addAll(masterController.lastArguments.trainSet().allOutputTypes());
    }

    protected static Label getStatisticsLabel(int currentMetric) {
        String name = switch (DataTypes.Evaluation.Metrics.values()[currentMetric]){
            case SIZE -> bundle.getString("tab.results.statistics.metric.dataSize");
            case LOSS -> bundle.getString("tab.results.statistics.metric.error") + String.format(" (%s)", Hyperparameters.ERROR_FUNCTION);
            case HITS -> bundle.getString("tab.results.statistics.metric.hits");
            case ACCURACY -> bundle.getString("tab.results.statistics.metric.accuracy");
            case PRECISION -> bundle.getString("tab.results.statistics.metric.precision");
            case RECALL -> bundle.getString("tab.results.statistics.metric.recall");
            case F_SCORE -> bundle.getString("tab.results.statistics.metric.fScore");
        };

        //Create and config labels
        Label nameLabel = new Label(name);
        nameLabel.setFont(new Font("Inter SemiBold", 14));
        nameLabel.setPadding(new Insets(10));
        nameLabel.setWrapText(true);
        nameLabel.setPrefHeight(70);
        nameLabel.setMinHeight(70);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        return nameLabel;
    }

    protected long calculateRating(DataTypes.Evaluation lastTrainData, DataTypes.Evaluation lastTestData) {
        double meanFScore = (lastTrainData.getMeanFScore() + lastTestData.getMeanFScore()) / 2;
        double meanError = (lastTrainData.getMeanError() + lastTestData.getMeanError()) / 2;
        double meanAccuracy = (lastTrainData.getMeanAccuracy() + lastTestData.getMeanAccuracy()) / 2;

        meanError = 1 - switch (Hyperparameters.ERROR_FUNCTION) { // Calculate the worst error for every case
            case MSE, RMSE ->  meanError;
            case ARCTAN -> meanError / ErrorFunctions.Arctan(new double[]{0}, new double[]{1});
        };

        double rating = (meanFScore + meanError + meanAccuracy) / 3;

        return Math.round(rating * 100);
    }

    protected record StatisticsUI(DataTypes.Evaluation.Metrics metric, Label trainLabel, Label testLabel) { }

    //Lists of items for selector panel
    protected enum ChartSelector{
        LOSS_CHART, ACCURACY_CHART, F_SCORE_CHART
    }

    public static class SimulatorColumn {
        protected VBox column;
        protected Label parameterLabel;
        protected Control value;
        protected Parser.InputTypes type;
        protected List<String> categories;

        public SimulatorColumn(DataTypes.InputParameterData parameterData, DataMaster dataMaster) {
            this.type = parameterData.type();

            // Initialize components
            parameterLabel = new Label(parameterData.parameter());
            parameterLabel.setPadding(new Insets(25, 15, 25, 15));
            parameterLabel.setFont(Font.font("Inter", 14));
            parameterLabel.setPrefHeight(71);
            parameterLabel.setMinHeight(71);
            parameterLabel.setMaxWidth(Double.MAX_VALUE);
            parameterLabel.setWrapText(true);
            parameterLabel.setAlignment(Pos.CENTER_LEFT);
            parameterLabel.setStyle("-fx-border-color: #4769ff; -fx-border-width: 0 0 1 0;");

            // Define value field in this row
            switch (type){
                case NUMBER -> {
                    // Create text field and limit it to only double values
                    TextField textField = new TextField();
                    UnaryOperator<TextFormatter.Change> filter = change -> {
                        String newText = change.getControlNewText();
                        if (newText.matches("([+-]?\\d*[.,]?\\d*)?"))
                            return change;
                        return null;
                    };
                    textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0D, filter));

                    value = textField;
                }
                case CATEGORICAL -> {
                    ChoiceBox<String> choiceBox = new ChoiceBox<>();
                    categories = dataMaster.getDatasetUniqueValues(parameterData.parameter());
                    choiceBox.getItems().addAll(categories);
                    choiceBox.getSelectionModel().select(0);

                    value = choiceBox;
                }
            }

            value.setMaxWidth(Double.MAX_VALUE);
            BorderPane valuePane = new BorderPane(value);
            valuePane.setMaxHeight(Double.MAX_VALUE);
            valuePane.setMaxWidth(Double.MAX_VALUE);
            valuePane.setPadding(new Insets(25, 15, 25, 15));
            BorderPane.setAlignment(valuePane, Pos.CENTER);

            column = new VBox(parameterLabel, valuePane);
            column.setPrefWidth(150);
            column.setMaxHeight(Double.MAX_VALUE);
            column.setStyle("-fx-border-color: #4769ff;" +
                    "-fx-border-width: 0 1 0 0;");

            // Set VBox constrains for children
            column.setFillWidth(true);
            VBox.setVgrow(parameterLabel, Priority.NEVER);
            VBox.setVgrow(valuePane, Priority.ALWAYS);
        }

        public VBox getColumn() {
            return column;
        }

        public Parser.InputTypes getType() {
            return type;
        }

        // Return specified value
        public double getNumberValue() {
            if(type != Parser.InputTypes.NUMBER)
                return 0;

            TextField field = (TextField) value;
            return Parser.parseNumberValue(field.getText()).doubleValue();
        }
        public double getCategoricalValue() {
            if(type != Parser.InputTypes.CATEGORICAL)
                return 0;

            // Suppress warning because at this point value is guaranteed a ChoiceBox
            @SuppressWarnings("unchecked") ChoiceBox<String> choiceBox = (ChoiceBox<String>) value;
            return Parser.parseCategoricalValue(choiceBox.getValue(), categories);
        }
    }
}
