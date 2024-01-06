package anna.network;

import anna.math.ErrorFunctions;
import anna.math.LearningFunctions;
import anna.network.data.DataTypes;
import anna.network.data.Hyperparameters;
import anna.network.neurons.Neuron;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeuralNetwork implements Runnable{

    private Thread thread;
    private static final Logger LOGGER = Logger.getLogger(NeuralNetwork.class.getName());
    private final ExecutorService neuronExecutor = Executors.newWorkStealingPool();

    NetworkStructure structure;
    NetworkArguments lastArguments;

    List<DataTypes.Evaluation> lastTrainEvaluation = new ArrayList<>();
    List<DataTypes.Evaluation> lastTestEvaluation = new ArrayList<>();

    public void start(NetworkArguments arguments){
        LOGGER.info("Network thread is starting...");
        lastArguments = arguments;
        if(thread == null || !thread.isAlive()){
            thread = new Thread(this, "NetworkThread");
            thread.setPriority(10);
            thread.start();
        }
    }

    @Override
    public void run() {
        runNetwork(lastArguments);
    }

    //Main method of NN
    private void runNetwork(NetworkArguments arguments){
        //Remember arguments and clear last evaluations data
        lastArguments = arguments;
        lastTrainEvaluation.clear();
        lastTestEvaluation.clear();
        //Create and set network structure
        structure = new NetworkStructure(arguments.networkData());
        if(structure.abortRun){
            abortNetworkMessage();
            return;
        }

        //Log start time
        long startTime = System.nanoTime();
        LOGGER.info("--- Starting neural network ---\n");

        // Call start event in listener
        if(arguments.listener() != null)
            arguments.listener().networkTrainingStarted();

        //Main loop
        int batchSize = Hyperparameters.BATCH_SIZE <= 0? arguments.trainSet().inputs().length : Hyperparameters.BATCH_SIZE;
        for (int i = 0; i < Hyperparameters.NUMBER_OF_EPOCHS; i++) {

            //Log new epoch header
            if(i == 0 || i == Hyperparameters.NUMBER_OF_EPOCHS - 1 || (arguments.logEpoch() != 0 && (i + 1) % arguments.logEpoch() == 0))
                LOGGER.info("---------- " + (i + 1) + "/" + Hyperparameters.NUMBER_OF_EPOCHS + " Epoch ----------");

            //long calculationStartTime = System.nanoTime();

            //Epoch
            DataTypes.Evaluation trainEvaluation = new DataTypes.Evaluation(lastArguments.trainSet().allOutputTypes().length, lastArguments.trainSet().inputs().length);
            DataTypes.Evaluation testEvaluation = new DataTypes.Evaluation(lastArguments.testSet().allOutputTypes().length, lastArguments.testSet().inputs().length);
            //Training
            for (int j = 0; j < batchSize; j++) {
                //Prepare variables
                double[] actualValues;
                if(arguments.isPrediction){
                    actualValues = new double[1];
                    actualValues[0] = Double.parseDouble(arguments.trainSet().expectedOutput()[j]);
                }
                else {
                    actualValues = getActualValuesArray(arguments.trainSet().expectedOutput()[j], arguments.trainSet().allOutputTypes());
                }
                //Iteration
                double[] outputValues = iteration(arguments.trainSet().inputs()[j]);
                if (outputValues == null){
                    abortNetworkMessage();
                    return;
                }
                //Learning
                backpropagation(actualValues);
                //Iteration evaluation
                if(iterationEvaluation(trainEvaluation, outputValues, actualValues) != 0){
                    abortNetworkMessage();
                    return;
                }

                //printProgressBar("Training " + i, j, batchSize - 1, calculationStartTime);
            }

            //calculationStartTime = System.nanoTime();

            //Testing
            for (int j = 0; j < arguments.testSet().inputs().length; j++) {
                //Prepare variables
                double[] actualValues;
                if(arguments.isPrediction){
                    actualValues = new double[1];
                    actualValues[0] = Double.parseDouble(arguments.testSet().expectedOutput()[j]);
                }
                else {
                    actualValues = getActualValuesArray(arguments.testSet().expectedOutput()[j], arguments.testSet().allOutputTypes());
                }
                //Iteration
                double[] outputValues = iteration(arguments.testSet().inputs()[j]);
                if (outputValues == null){
                    abortNetworkMessage();
                    return;
                }

                //Iteration evaluation
                if(iterationEvaluation(testEvaluation, outputValues, actualValues) != 0){
                    abortNetworkMessage();
                    return;
                }

                //printProgressBar("Testing", j, arguments.testSet().inputs().length - 1, calculationStartTime);
            }
            //Calculation of mean error
            trainEvaluation.setMeanError(trainEvaluation.getMeanError() / batchSize);
            testEvaluation.setMeanError(testEvaluation.getMeanError() / arguments.testSet().inputs().length);
            //Store evaluations
            lastTrainEvaluation.add(trainEvaluation);
            lastTestEvaluation.add(testEvaluation);

            // Call epoch event in listener
            if(arguments.listener() != null)
                arguments.listener().epochEnded(i);

            //Log data
            if(i == 0 || i == Hyperparameters.NUMBER_OF_EPOCHS - 1 || (arguments.logEpoch() != 0 && (i + 1) % arguments.logEpoch() == 0)){
                //Log epoch info
                LOGGER.info("Mean train error of epoch:\t" + trainEvaluation.getMeanError());
                LOGGER.info("Mean test error of epoch:\t" + testEvaluation.getMeanError() + "\n");
                LOGGER.info("Mean train accuracy of epoch:\t" + trainEvaluation.getMeanAccuracy());
                LOGGER.info("Mean test accuracy of epoch:\t" + testEvaluation.getMeanAccuracy() + "\n");
                LOGGER.info("Mean train precision of epoch:\t" + trainEvaluation.getMeanPrecision());
                LOGGER.info("Mean test precision of epoch:\t" + testEvaluation.getMeanPrecision() + "\n");
                LOGGER.info("Mean train recall of epoch:\t" + trainEvaluation.getMeanRecall());
                LOGGER.info("Mean test recall of epoch:\t" + testEvaluation.getMeanRecall() + "\n");
                LOGGER.info("Mean train F-score of epoch:\t" + trainEvaluation.getMeanFScore());
                LOGGER.info("Mean test F-score of epoch: \t" + testEvaluation.getMeanFScore() + "\n");

                // Call log event in listener
                if(arguments.listener() != null)
                    arguments.listener().logEpoch(i, lastTrainEvaluation, lastTestEvaluation);
            }
        }

        // Log end time
        long elapsedTime = System.nanoTime() - startTime;
        LOGGER.info("--- Neural network finished ---");
        LOGGER.info("Elapsed time: " + (elapsedTime / 1000000000) + "s\t" + (elapsedTime / 1000000) + "ms\t" + elapsedTime + "ns\n");
        // Print time in seconds, milliseconds and nanoseconds

        // Call finish event in listener
        if(arguments.listener() != null)
            arguments.listener().networkTrainingFinished();

        // Ask garbage collector to clean up after training TODO: Is it really efficient?
        System.gc();
    }

    //Collect data about iteration
    private int iterationEvaluation(DataTypes.Evaluation evaluation, double[] outputValues, double[] actualValues){
        //Error calculation
        double lastError = calculateError(outputValues, actualValues);
        if (lastError == -1){
            return -1;
        }
        evaluation.addMeanError(lastError);

        int predictedClassID = getOutputIDFromRawOutput(outputValues);
        int actualClassID = getOutputIDFromRawOutput(actualValues);

        //Count predictions
        evaluation.addClassInfo(1, predictedClassID, actualClassID);


        if(Double.compare(evaluation.getMeanError(), Double.NaN) == 0 || Double.compare(evaluation.getMeanError(), Double.POSITIVE_INFINITY) == 0 ||
                Double.compare(evaluation.getMeanError(), Double.NEGATIVE_INFINITY) == 0){
            Logger.getLogger(getClass().getName()).log(Level.WARNING,"Unexpected calculation error");
            return -1;
        }
        return 0;
    }

    public boolean simulation(double[] inputs){
        //Validate arguments and inputs
        if(lastArguments == null || inputs == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred when starting the simulator. The required values are missing.");
            return false;
        }
        //Run neural network
        double[] outputValues = iteration(inputs);
        if (outputValues == null){
            abortNetworkMessage();
            return false;
        }
        //Update UI
        if(lastArguments.isPrediction())
            lastArguments.listener().simulationPredictionResult(outputValues[0]);
        else
            lastArguments.listener().simulationClassificationResult(outputValues, lastArguments.trainSet().allOutputTypes()[getOutputIDFromRawOutput(outputValues)]);
        return true;
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
    private int getOutputIDFromRawOutput(double[] rawOutput){
        int idOfValue = 0;
        for (int i = 1; i < rawOutput.length; i++) {
            if(rawOutput[i] > rawOutput[idOfValue]){
                idOfValue = i;
            }
        }
        return idOfValue;
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
                ArrayList<DataTypes.Synapse> inputConnections = structure.getInputConnections(currentID);
                double[] neuronInputs = new double[inputConnections.size()];
                double[] neuronWeights = new double[inputConnections.size()];

                for (int k = 0; k < inputConnections.size(); k++) {
                    neuronInputs[k] = structure.getNeuronByID(inputConnections.get(k).getNeuronID()).getLastOutput(); //Get output of neuron
                    neuronWeights[k] = inputConnections.get(k).getWeight(); //Get weight of synapse
                }

                //Calculate output
                if (!structure.getNeuronByPosition(i, j).calculateOutput(neuronInputs, neuronWeights))
                    return null;
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
        //Calculate error
        return switch (Hyperparameters.ERROR_FUNCTION) {
            case MSE -> ErrorFunctions.MeanSquaredError(ideal, actual);
            case RMSE -> ErrorFunctions.RootMeanSquaredError(ideal, actual);
            case ARCTAN -> ErrorFunctions.Arctan(ideal, actual);
        };
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
                ArrayList<DataTypes.Synapse> outputConnections = structure.getOutputConnections(currentID);

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
            ArrayList<DataTypes.Synapse> outputConnections = structure.getOutputConnections(currentID);

            //Set delta weights
            for (int k = 0; k < outputConnections.size(); k++) {
                updateWeights(outputConnections, currentNeuron, k);
            }
        }
    }

    private void updateWeights(ArrayList<DataTypes.Synapse> outputConnections, Neuron currentNeuron, int connectionID){
        //Set new delta weight
        double deltaWeight = LearningFunctions.deltaWeight(structure.getNeuronByID(outputConnections.get(connectionID).getNeuronID()).getDelta(),
                currentNeuron.getLastOutput(), outputConnections.get(connectionID).getDeltaWeight());
        outputConnections.get(connectionID).setDeltaWeight(deltaWeight);

        //Set new weight
        double weight = outputConnections.get(connectionID).getWeight() + outputConnections.get(connectionID).getDeltaWeight();
        outputConnections.get(connectionID).setWeight(weight);

        //Update weights for other neurons
        for (DataTypes.Synapse synapse : structure.getInputConnections(outputConnections.get(connectionID).getNeuronID())) {
            if(synapse.getNeuronID() == currentNeuron.getId()){
                synapse.setDeltaWeight(deltaWeight);
                synapse.setWeight(weight);
            }
        }
    }

    // Prints progress bar
    private static void printProgressBar(String name, long currentOperations, long allOperations, long startNanoseconds) {
        int percentage = Math.round((float) currentOperations / allOperations * 100);
        String progressBar = "=".repeat(percentage / 10) + " ".repeat(10 - percentage / 10);
        long elapsedNanoseconds = System.nanoTime() - startNanoseconds;
        long estimatedSeconds = currentOperations == 0 ? 0 : elapsedNanoseconds / currentOperations * allOperations / 1000000000;
        String elapsedTime = String.format("%02d:%02d", elapsedNanoseconds / 1000000000 / 60, elapsedNanoseconds / 1000000000 % 60);
        String estimatedTime = String.format("%02d:%02d", estimatedSeconds / 60, estimatedSeconds % 60);

        if(currentOperations == 0)
            System.out.println();
        System.out.printf("%1$s %2$s <%3$s> (%4$s / %5$s)%6$s", name, percentage + "%", progressBar, elapsedTime, estimatedTime, currentOperations == allOperations ? "\n" : "\r");
    }

    // Failed attempt to increase performance. Use only in case of giant network, otherwise don't use at all.
    @Deprecated
    private double[] multithreadingIteration(double[] inputs) {
        //Set input neurons values
        for (int i = 0; i < structure.getNeuronsAmountInLayer(0); i++) {
            structure.getNeuronByPosition(0, i).setLastOutput(inputs[i]);
        }

        //Setup phaser for multithreading
        Phaser phaser = new Phaser(1);

        //Process through all hidden and output neurons
        for (int i = 1; i < structure.getLayersAmount(); i++) {
            phaser.bulkRegister(structure.getNeuronsAmountInLayer(i)); // Register all current tasks
            //System.out.println(phaser.getRegisteredParties() + " registration   " + i);
            for (int j = 0; j < structure.getNeuronsAmountInLayer(i); j++) {
                int finalI = i;
                int finalJ = j;
                try {
                    neuronExecutor.submit(() -> {
                        // Prepare input values
                        int currentID = structure.getIDByPosition(finalI, finalJ);
                        ArrayList<DataTypes.Synapse> inputConnections = structure.getInputConnections(currentID);
                        double[] neuronInputs = new double[inputConnections.size()];
                        double[] neuronWeights = new double[inputConnections.size()];

                        for (int k = 0; k < inputConnections.size(); k++) {
                            neuronInputs[k] = structure.getNeuronByID(inputConnections.get(k).getNeuronID()).getLastOutput(); // Get output of neuron
                            neuronWeights[k] = inputConnections.get(k).getWeight(); // Get weight of synapse
                        }

                        //Calculate output
                        if (!structure.getNeuronByPosition(finalI, finalJ).calculateOutput(neuronInputs, neuronWeights))
                            throw new ArithmeticException();
                        phaser.arriveAndDeregister();
                        //System.out.println(phaser.getRegisteredParties() + " done");
                    });
                }
                catch (Exception e){ // Stop network if error is occurred
                    return null;
                }
            }
            phaser.arriveAndAwaitAdvance(); // Main thread is waiting for all tasks to finish
        }

        phaser.forceTermination(); // Release resources

        // Get output values
        double[] outputLayer = new double[structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1)];
        for (int i = 0; i < structure.getNeuronsAmountInLayer(structure.getLayersAmount() - 1); i++) {
            Neuron currentNeuron = structure.getNeuronByPosition(structure.getLayersAmount() - 1, i);
            outputLayer[i] = currentNeuron.getLastOutput();
        }
        return outputLayer;
    }

    private void abortNetworkMessage(){
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected network error, aborting");

        // Call error event in listener
        if (lastArguments.listener() != null)
            lastArguments.listener().networkError();
    }

    //Structure class for network start arguments
    public record NetworkArguments(DataTypes.NetworkData networkData, DataTypes.Dataset trainSet, DataTypes.Dataset testSet,
                                   boolean isPrediction, NetworkListener listener, int logEpoch) { }
}
