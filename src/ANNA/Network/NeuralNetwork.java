package ANNA.Network;

import ANNA.Functions.ErrorFunctions;
import ANNA.Functions.LearningFunctions;
import ANNA.Network.neurons.Neuron;
import ANNA.UI.PopupController;
import ANNA.UI.UIController;

import java.util.ArrayList;

public class NeuralNetwork {

    NetworkStructure structure;
    NetworkArguments lastArguments;

    //Main method of NN
    public void run(NetworkArguments arguments){
        //Remember arguments
        lastArguments = arguments;
        //Create and set network structure
        structure = new NetworkStructure(arguments.architecture(), arguments.initialWeights());

        //Log start time
        long startTime = System.nanoTime();
        System.out.println("\n--- Starting neural network ---");

        //Main loop
        int batchSize = Hyperparameters.BATCH_SIZE <= 0? arguments.inputs().length : Hyperparameters.BATCH_SIZE;
        for (int i = 0; i < Hyperparameters.NUMBER_OF_EPOCHS; i++) {

            //Epoch
            double meanError = 0;
            for (int j = 0; j < batchSize; j++) {
                //Prepare variables
                double[] actualValues;
                if(arguments.isPrediction){
                    actualValues = new double[1];
                    actualValues[0] = Double.parseDouble(arguments.expectedOutput()[j]);
                }
                else {
                    actualValues = getActualValuesArray(arguments.expectedOutput()[j], arguments.allOutputTypes());
                }

                //Iteration
                double[] outputValues = iteration(arguments.inputs()[j]);
                //Learning
                if(arguments.training)
                    backpropagation(actualValues);
                //Error calculation
                meanError += calculateError(outputValues, actualValues);
                if(Double.compare(meanError, Double.NaN) == 0 || Double.compare(meanError, Double.POSITIVE_INFINITY) == 0 || Double.compare(meanError, Double.NEGATIVE_INFINITY) == 0){
                    System.err.println("Unexpected calculation error, aborting");
                    return;
                }
            }
            //Calculation mean error
            meanError = meanError / batchSize;

            //Log data
            if(i == 0 || i == Hyperparameters.NUMBER_OF_EPOCHS - 1 || (arguments.logEpoch() != 0 && (i + 1) % arguments.logEpoch() == 0)){
                boolean justStarted = i == 0;

                System.out.println("\n---------- " + (i + 1) + " Epoch ----------");
                System.out.println("Mean error of epoch:\t" + meanError);

                if(arguments.uiController() != null){
                    arguments.uiController().outputController.updateTrainGraph(false, justStarted, meanError, i + 1);
                }
            }
        }

        //Log end time
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("\n--- Neural network finished ---");
        System.out.println("\nElapsed time: " + (elapsedTime / 1000000000) + "s\t" + (elapsedTime / 1000000) + "ms\t" + elapsedTime + "ns\n");
        //Print time in seconds, milliseconds and nanoseconds
    }

    public void simulation(double[] inputs){
        //Validate arguments and inputs
        if(lastArguments == null || inputs == null){
            PopupController.errorMessage("ERROR", "Ошибка симулятора", "", "Произошла ошибка при запуске симулятора. Отсутствуют необходимые значения");
            return;
        }
        //Run neural network
        double[] outputValues = iteration(inputs);
        //Update UI
        if(lastArguments.isPrediction())
            lastArguments.uiController().outputController.simulationResult(outputValues[0]);
        else
            lastArguments.uiController().outputController.simulationResult(outputValues, getOutputValueFromRawOutput(outputValues, lastArguments.allOutputTypes()));
    }

    //Convert ideal value to array of ideal values
    private double[] getActualValuesArray(String value, String[] allOutputTypes){
        double[] array = new double[allOutputTypes.length];
        int idOfValue = 0;
        //Find id of current output value
        for (int i = 0; i < allOutputTypes.length; i++) {
            if(allOutputTypes[i].equals(value)) {
                idOfValue = i;
                break;
            }
        }
        //Create array
        for (int i = 0; i < allOutputTypes.length; i++) {
            if(i == idOfValue)
                array[i] = 1;
            else
                array[i] = 0;
        }

        return array;
    }

    //Inverting getActualValuesArray()
    private String getOutputValueFromRawOutput(double[] rawOutput, String[] allOutputTypes){
        int idOfValue = 0;
        for (int i = 0; i < rawOutput.length; i++) {
            if(rawOutput[i] > rawOutput[idOfValue]) {
                idOfValue = i;
            }
        }
        return allOutputTypes[idOfValue];
    }

    //Contains main structure of NN.
    private double[] iteration(double[] inputs){
        //Set input neurons values
        for (int i = 0; i < structure.getNeuronsAmountInLayer(0); i++) {
            structure.getNeuronByPosition(0, i).setLastOutput(inputs[i]);
        }

        //Process through all hidden and output neurons
        for (int i = 1; i < structure.getLayersAmount(); i++) {
            for (int j = 0; j < structure.getNeuronsAmountInLayer(i); j++) {
                //Prepare input values
                int currentID = structure.getIDByPosition(i, j);
                ArrayList<NetworkStructure.Synapse> inputConnections = structure.getInputConnections(currentID);
                double[] neuronInputs = new double[inputConnections.size()];
                double[] neuronWeights = new double[inputConnections.size()];

                for (int k = 0; k < inputConnections.size(); k++) {
                    neuronInputs[k] = structure.getNeuronByID(inputConnections.get(k).getNeuronID()).getLastOutput(); //Get output of neuron
                    neuronWeights[k] = inputConnections.get(k).getWeight(); //Get weight of synapse
                }

                //Calculate output
                structure.getNeuronByPosition(i, j).calculateOutput(neuronInputs, neuronWeights);
            }
        }
        //Get output values
        double[] outputLayer = new double[structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1)];
        for (int i = 0; i < structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(structure.getLayersAmount() - 1, i);
            outputLayer[i] = currentNeuron.getLastOutput();
        }
        return outputLayer;
    }

    private double calculateError(double[] actual, double[] ideal){
        //Calculate error. Change error function here
        return ErrorFunctions.MeanSquaredError(ideal, actual);
    }

    //Learning method
    private void backpropagation(double[] idealValues){
        //Calculate output delta
        for (int i = 0; i < structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(structure.getLayersAmount() - 1, i);
            currentNeuron.setDelta(LearningFunctions.outputDelta(currentNeuron.getLastRawOutput(), idealValues[i], currentNeuron.getLastOutput(), currentNeuron.getActivationFunction()));
        }

        //Hidden neurons delta
        for (int i = structure.getLayersAmount() - 2; i > 0; i--) {
            for (int j = 0; j < structure.getNeuronsAmountInLayer(i); j++) {
                //Prepare values
                Neuron currentNeuron = structure.getNeuronByPosition(i, j);
                int currentID = structure.getIDByPosition(i, j);
                ArrayList<NetworkStructure.Synapse> outputConnections = structure.getOutputConnections(currentID);

                double[] neuronWeights = new double[outputConnections.size()];
                double[] neuronDeltas = new double[outputConnections.size()];

                //Get deltas and set delta of weights
                for (int k = 0; k < outputConnections.size(); k++) {
                    neuronWeights[k] = outputConnections.get(k).getWeight();
                    neuronDeltas[k] = structure.getNeuronByID(outputConnections.get(k).getNeuronID()).getDelta();

                    updateWeights(outputConnections, currentNeuron, k);
                }

                //Calculate delta
                currentNeuron.setDelta(LearningFunctions.hiddenDelta(neuronWeights, neuronDeltas, currentNeuron.getLastRawOutput(), currentNeuron.getActivationFunction()));
            }
        }

        //Update weights from input neurons
        for (int i = 0; i < structure.getNeuronsAmountInLayer(0); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(0, i);
            int currentID = structure.getIDByPosition(0, i);
            ArrayList<NetworkStructure.Synapse> outputConnections = structure.getOutputConnections(currentID);

            //Set delta weights
            for (int k = 0; k < outputConnections.size(); k++) {
                updateWeights(outputConnections, currentNeuron, k);
            }
        }
    }

    private void updateWeights(ArrayList<NetworkStructure.Synapse> outputConnections, Neuron currentNeuron, int connectionID){
        //Set new delta weight
        double deltaWeight = LearningFunctions.deltaWeight(structure.getNeuronByID(outputConnections.get(connectionID).getNeuronID()).getDelta(),
                currentNeuron.getLastOutput(), outputConnections.get(connectionID).getDeltaWeight());
        outputConnections.get(connectionID).setDeltaWeight(deltaWeight);

        //Set new weight
        double weight = outputConnections.get(connectionID).getWeight() + outputConnections.get(connectionID).getDeltaWeight();
        outputConnections.get(connectionID).setWeight(weight);

        //Update weights for other neurons
        for (NetworkStructure.Synapse synapse : structure.getInputConnections(outputConnections.get(connectionID).getNeuronID())) {
            if(synapse.getNeuronID() == currentNeuron.getId()){
                synapse.setDeltaWeight(deltaWeight);
                synapse.setWeight(weight);
            }
        }
    }

    //Structure class for network start arguments
    public record NetworkArguments(int[] architecture, double[][] initialWeights, double[][] inputs, String[] expectedOutput, String[] allOutputTypes, boolean training,
            boolean isPrediction, UIController uiController, int logEpoch) { }
}
