package ANNA.network.neurons;

import ANNA.math.ActivationFunctions;
import ANNA.network.Hyperparameters;
import ANNA.network.NetworkStructure;

public class BasicNeuron extends Neuron {
    public BasicNeuron(int id, ActivationFunctions.types activationFunction, NetworkStructure.neuronTypes type) {
        super(id, activationFunction, type);
    }

    @Override
    protected double calculateRawOutput(double[] inputs, double[] weights) {
        double rawOutput = 0;

        //Multiply by weights and add all inputs together.
        for (int i = 0; i < inputs.length; i++) {
            rawOutput += inputs[i] * weights[i];
        }

        //Bias neron influence
        if (Hyperparameters.USE_BIAS_NEURONS)
            rawOutput += weights[weights.length - 1];

        return rawOutput;
    }
}
