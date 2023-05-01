package ANNA.Network.neurons;

import ANNA.Functions.ActivationFunctions;
import ANNA.Network.NetworkStructure;

public class BiasNeuron extends Neuron{

    public BiasNeuron(int positionX, int positionY, int id, ActivationFunctions.types activationFunction, NetworkStructure.neuronTypes type) {
        super(positionX, positionY, id, activationFunction, type);
    }
    @Override
    protected double calculateRawOutput(double[] inputs, double[] weights) {
        return 1;
    }
}