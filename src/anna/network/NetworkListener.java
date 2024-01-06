package anna.network;

import anna.network.data.DataTypes;

import java.util.List;

public interface NetworkListener {

    void networkTrainingStarted();
    void logEpoch(long number, List<DataTypes.Evaluation> trainEvaluation, List<DataTypes.Evaluation> testEvaluation);
    void epochEnded(long number);
    void networkTrainingFinished();
    void simulationPredictionResult(double outputValue);
    void simulationClassificationResult(double[] outputValues, String outputCategory);
    void networkError();

}
