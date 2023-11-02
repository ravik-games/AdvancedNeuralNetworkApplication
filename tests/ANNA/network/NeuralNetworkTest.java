package ANNA.network;

import ANNA.UI.DefaultUIController;
import ANNA.UI.Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class NeuralNetworkTest {

    //@Test
    void Sum() { //DEPRECATED
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

    //@Test
    void SingleHidden(){ //DEPRECATED
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


    @Test
    void PerformanceTest() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Hyperparameters.NUMBER_OF_EPOCHS = 5000;

        int[] architecture = new int[]{5, 8, 16, 32, 2};
        DataTypes.NetworkData networkData = new DataTypes.NetworkData(architecture, new ArrayList<>());

        List<List<String>> rawDataset = Parser.parseData(new File(System.getProperty("user.dir") + "\\src\\resources\\datasets\\5-parameters\\5-parameters_test.csv"));
        double[][] inputs = new double[rawDataset.size() - 1][rawDataset.get(0).size()];
        String[] expectedOutput = new String[rawDataset.size() - 1];
        List<String> allOutputTypes = new ArrayList<>();

        for(int i = 1; i < rawDataset.size(); i++) {
            for (int j = 0; j < rawDataset.get(0).size(); j++) {
                inputs[i - 1][j] = Parser.parseRawValue(rawDataset.get(i).get(j), Parser.inputTypes.NUMBER);
            }
            //Set expected outputs for training set
            String idealValue = rawDataset.get(i).get(5);
            expectedOutput[i - 1] = idealValue;
            if(!allOutputTypes.contains(idealValue)){
                allOutputTypes.add(idealValue);
            }
        }
        DataTypes.Dataset dataset = new DataTypes.Dataset(inputs, expectedOutput, allOutputTypes.toArray(new String[0]));

        NeuralNetwork network = new NeuralNetwork();
        network.start(new NeuralNetwork.NetworkArguments(networkData, dataset, dataset, false, null, 10));
        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}