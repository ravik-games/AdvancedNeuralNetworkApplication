package ANNA.Network;

import ANNA.UI.UIController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeuralNetworkTest {

    @Test
    void run() {
        Hyperparameters.NUMBER_OF_EPOCHS = 10;
        Hyperparameters.USE_BIAS_NEURONS = false;

        int[] architecture = new int[]{2, 2, 1};
        double[][] initialWeights = null;
        double[][] inputs = new double[][]{{1, 2}, {2, 3}, {3, 4}};
        String[] expectedOutput = new String[]{"3", "5", "7"};
        String[] allOutputTypes = null;
        boolean training = true;
        boolean isPrediction = true;
        UIController uiController = null;
        int logEpoch = 1;
        NeuralNetwork.NetworkArguments arguments = new NeuralNetwork.NetworkArguments(architecture, initialWeights, inputs, expectedOutput, allOutputTypes, training, isPrediction,
                uiController, logEpoch);

        NeuralNetwork network = new NeuralNetwork();
        network.run(arguments);
        network.structure.printWeights(false);
    }
}