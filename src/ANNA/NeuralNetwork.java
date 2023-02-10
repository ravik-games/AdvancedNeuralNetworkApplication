package ANNA;

public class NeuralNetwork {

    NetworkStructure structure;

    //Main method of NN
    public void run(NetworkArguments arguments){
        //Create and set network structure
        structure = new NetworkStructure(arguments.architecture(), arguments.initialWeights());

        //Main loop
        int bathSize = Hyperparameters.BATCH_SIZE <= 0? arguments.inputs().length : Hyperparameters.BATCH_SIZE;
        for (int i = 0; i < Hyperparameters.NUMBER_OF_EPOCHS; i++) {

            double meanError = 0;
            for (int j = 0; j < bathSize; j++) {

                OutputValue output = iteration(arguments.inputs()[j], arguments.expectedOutputs()[j]);
                if(arguments.training)
                    backpropagation(arguments.expectedOutputs()[j]);
                meanError += output.error();
            }
            meanError = meanError / bathSize;

            if(i == 0 || i == Hyperparameters.NUMBER_OF_EPOCHS - 1 || (arguments.logEpoch() != 0 && (i + 1) % arguments.logEpoch() == 0)){
                boolean justStarted = i == 0;

                System.out.println("\n---------- " + (i + 1) + " Epoch ----------");
                System.out.println("Mean error of epoch:\t" + meanError);

                if(arguments.uiController() != null){
                    arguments.uiController().updateTrainGraph(false, justStarted, meanError, i + 1);
                }
            }
        }
    }

    //Contains main structure of NN.
    private OutputValue iteration(double[] inputs, double[] ideal){
        //Set input neurons values
        for (int i = 0; i < structure.getNeuronsAmountInLayer(0); i++) {
            structure.getNeuronByPosition(0, i).setLastOutput(inputs[i]);
        }

        //Process through all hidden and output neurons
        for (int i = 1; i < structure.getLayersAmount(); i++) {
            for (int j = 0; j < structure.getNeuronsAmountInLayer(i); j++) {
                //Prepare input values
                int currentID = structure.getIDByPosition(i, j);
                double[] neuronInputs = new double[currentID];
                double[] neuronWeights = new double[currentID];
                for (int k = 0; k < currentID; k++) {
                    if(structure.getWeightByID(currentID, k) != 0) {
                        neuronWeights[k] = structure.getWeightByID(currentID, k);
                        neuronInputs[k] = structure.getNeuronByID(k).getLastOutput();
                    }
                }
                //Calculate output
                structure.getNeuronByPosition(i, j).calculateOutput(neuronInputs, neuronWeights);
            }
        }
        //Calculate error
        double[] outputLayer = new double[structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1)];
        for (int i = 0; i < structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(structure.getLayersAmount() - 1, i);
            outputLayer[i] = currentNeuron.getLastOutput();
        }
        double error = ErrorFunctions.MeanSquaredError(ideal, outputLayer);
        return new OutputValue(outputLayer, error);
    }

    public void backpropagation(double[] idealValues){
        //Calculate output delta
        for (int i = 0; i < structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(structure.getLayersAmount() - 1, i);
            currentNeuron.setDelta(LearningFunctions.outputDelta(idealValues[i], currentNeuron.getLastOutput(), ActivationFunctions.types.SIGMOID));
        }

        //Hidden neurons delta
        for (int i = structure.getLayersAmount() - 2; i > 0; i--) {
            for (int j = 0; j < structure.getNeuronsAmountInLayer(i); j++) {
                //Prepare values
                Neuron currentNeuron = structure.getNeuronByPosition(i, j);
                int currentID = structure.getIDByPosition(i, j);
                double[] neuronWeights = new double[structure.getLastID() + 1];
                double[] neuronDeltas = new double[structure.getLastID() + 1];
                //Get deltas and set delta of weights
                for (int k = currentID + 1; k <= structure.getLastID(); k++) {
                    if(structure.getWeightByID(currentID, k) != 0) {
                        neuronDeltas[k] = structure.getNeuronByID(k).getDelta();
                        neuronWeights[k] = structure.getWeightByID(currentID, k);
                        structure.setDeltaWeightByID(currentID, k,
                                LearningFunctions.deltaWeight(structure.getNeuronByID(k).getDelta(), currentNeuron.getLastOutput(), structure.getDeltaWeightByID(currentID, k)));
                        structure.setWeightByID(currentID, k, structure.getWeightByID(currentID, k) + structure.getDeltaWeightByID(currentID, k));
                    }
                }
                //Calculate delta
                currentNeuron.setDelta(LearningFunctions.hiddenDelta(neuronWeights, neuronDeltas, currentNeuron.getLastOutput(), ActivationFunctions.types.SIGMOID));
            }
        }

        //Update weights from input neurons
        for (int i = 0; i < structure.getNeuronsAmountInLayer(0); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(0, i);
            int currentID = structure.getIDByPosition(0, i);
            //Set deltas of weights
            for (int k = currentID + 1; k <= structure.getLastID(); k++) {
                if(structure.getWeightByID(currentID, k) != 0) {
                    structure.setDeltaWeightByID(currentID, k,
                            LearningFunctions.deltaWeight(structure.getNeuronByID(k).getDelta(), currentNeuron.getLastOutput(), structure.getDeltaWeightByID(currentID, k)));
                    structure.setWeightByID(currentID, k, structure.getWeightByID(currentID, k) + structure.getDeltaWeightByID(currentID, k));
                }
            }
        }
    }

    //Structure class for output
    private record OutputValue(double[] output, double error) { }
    //Structure class for network start arguments
    public record NetworkArguments(int[] architecture, double[][] initialWeights, double[][] inputs, double[][] expectedOutputs, boolean training, UIController uiController, int logEpoch) { }
}
