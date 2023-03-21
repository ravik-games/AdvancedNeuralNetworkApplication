package ANNA.Network.neurons;

import ANNA.Functions.ActivationFunctions;
import ANNA.Network.Hyperparameters;
import ANNA.Network.NetworkStructure;
import ANNA.UI.PopupController;

public abstract class AbstractNeuron {

    protected int positionX;
    protected int positionY;

    protected int id;

    protected double delta;
    protected double lastRawOutput; //Last output without activation function
    protected double lastOutput;

    protected NetworkStructure.neuronTypes type;

    protected ActivationFunctions.types activationFunction;

    public AbstractNeuron(int positionX, int positionY, int id, ActivationFunctions.types activationFunction, NetworkStructure.neuronTypes type) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.id = id;
        this.activationFunction = activationFunction;
        this.type = type;
    }

    public void calculateOutput(double[] inputs, double[] weights){
        //Throw error and stop program when length of inputs and length of weights doesn't match.
        if(inputs.length != weights.length - Boolean.compare(Hyperparameters.USE_BIAS_NEURONS, false)){
            //TODO Test conditions
            System.err.println("CRITICAL ERROR: length of inputs and length of weights in neuron doesn't match");
            PopupController.errorMessage("ERROR", "Критическая ошибка", "", "Произошла критическая ошибка при работе нейронной сети. Количество входных данных и весов нейрона не совпадает.");
            System.exit(1);
        }

        //Calculate output in child class
        double output = calculateRawOutput(inputs, weights);

        //Save raw output for later learning
        lastRawOutput = output;
        //Return processed output using activation function.
        lastOutput = switch(activationFunction){
            case RELU -> ActivationFunctions.ReLU(output);
            case LRELU -> ActivationFunctions.LeakyReLU(output);
            case SIGMOID -> ActivationFunctions.sigmoid(output);
            case TANH -> ActivationFunctions.tanh(output);
            case ELU -> ActivationFunctions.ExponentialLU(output);
            case LINEAR -> ActivationFunctions.Linear(output);
        };
    }

    protected abstract double calculateRawOutput(double[] inputs, double[] weights);

    //Getters and setters
    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getId() {
        return id;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public double getLastOutput() {
        return lastOutput;
    }

    public void setLastOutput(double lastOutput) {
        this.lastOutput = lastOutput;
    }

    public double getLastRawOutput() {
        return lastRawOutput;
    }

    public ActivationFunctions.types getActivationFunction() {
        return activationFunction;
    }
}
