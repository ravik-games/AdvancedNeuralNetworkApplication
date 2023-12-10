package anna.network.neurons;

import anna.math.ActivationFunctions;
import anna.network.NetworkStructure;

public class BiasNeuron extends Neuron{

    public BiasNeuron(int id, ActivationFunctions.Types activationFunction, NetworkStructure.LayerTypes type) {
        super(id, activationFunction, type);
    }
    @Override
    protected double calculateRawOutput(double[] inputs, double[] weights) {
        return 1;
    }
}
