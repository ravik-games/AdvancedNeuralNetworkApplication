package ANNA.Network.neurons;

import ANNA.Functions.ActivationFunctions;
import ANNA.Network.Hyperparameters;
import ANNA.Network.NetworkStructure;

public class BasicNeuron extends  AbstractNeuron{
    public BasicNeuron(int positionX, int positionY, int id, ActivationFunctions.types activationFunction, NetworkStructure.neuronTypes type) {
        super(positionX, positionY, id, activationFunction, type);
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
