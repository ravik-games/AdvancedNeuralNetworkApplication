package ANNA.UI;

import ANNA.Main;
import ANNA.network.DataTypes;

import java.util.List;

//Interface for different UI controller realisations
public interface UIController {
    void clearResults(int chartXLength);

    void updateResults(boolean clear, int epoch, List<DataTypes.Evaluation> trainEvaluation, List<DataTypes.Evaluation> testEvaluation);

    void simulationClassificationResult(double[] outputValues, String outputCategory);

    void simulationPredictionResult(double outputValue);

    void setMain(Main main);
}
