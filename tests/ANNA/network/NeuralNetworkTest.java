package ANNA.network;

import ANNA.UI.DefaultUIController;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class NeuralNetworkTest {

    @Test
    void Sum() {
        Hyperparameters.NUMBER_OF_EPOCHS = 10;
        Hyperparameters.USE_BIAS_NEURONS = true;

        int[] architecture = new int[]{2, 2, 1};
        DataTypes.NetworkData initialWeights = null;
        double[][] inputs = new double[][]{{1, 2}, {2, 3}, {3, 4}};
        String[] expectedOutput = new String[]{"3", "5", "7"};
        String[] allOutputTypes = null;
        boolean training = true;
        boolean isPrediction = true;
        DefaultUIController defaultUiController = null;
        int logEpoch = 1;

        NeuralNetwork network = new NeuralNetwork();
        //network.run(arguments);
        network.structure.printWeights(false);
    }

    @Test
    void SingleHidden(){
        Hyperparameters.NUMBER_OF_EPOCHS = 2;
        Hyperparameters.LEARNING_RATE = 0.7;
        Hyperparameters.MOMENTUM = 0.3;

        int[] architecture = new int[]{2, 2, 1};
        //double[][] initialWeights = new double[][]{{0, 0, 0.45, 0.78, 0}, {0, 0, -0.12, 0.13, 0}, {0.45, -0.12, 0, 0, 1.5}, {0.78, 0.13, 0, 0, -2.3}, {0, 0, 1.5, -2.3, 0}};
        DataTypes.NetworkData initialWeights = null;
        double[][] inputs = new double[][]{{1, 0}};
        String[] expectedOutput = new String[]{"1"};
        List<String> allOutputTypes = new ArrayList<>();
        allOutputTypes.add("1");
        boolean training = true;
        boolean isPrediction = false;
        DefaultUIController defaultUiController = null;
        int logEpoch = 1;

        NeuralNetwork network = new NeuralNetwork();
        //network.run(arguments);
        network.structure.printWeights(false);
    }
}