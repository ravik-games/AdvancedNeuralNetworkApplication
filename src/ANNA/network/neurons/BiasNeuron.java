package ANNA.network.neurons;

import ANNA.math.ActivationFunctions;
import ANNA.network.NetworkStructure;

public class BiasNeuron extends Neuron{

    public BiasNeuron(int id, ActivationFunctions.types activationFunction, NetworkStructure.neuronTypes type) {
        super(id, activationFunction, type);
    }
    @Override
    protected double calculateRawOutput(double[] inputs, double[] weights) {
        return 1;
    }
}
