package anna.network.neurons;

import anna.math.ActivationFunctions;
import anna.network.NetworkStructure;
import anna.ui.PopupController;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Neuron {

    protected int id;

    protected double delta;
    protected double lastRawOutput; //Last output without activation function
    protected double lastOutput;

    protected NetworkStructure.LayerTypes layerType;
    protected ActivationFunctions.Types activationFunction;

    public Neuron(int id, ActivationFunctions.Types activationFunction, NetworkStructure.LayerTypes layerTypes) {
        this.id = id;
        this.activationFunction = activationFunction;
        this.layerType = layerTypes;
    }

    public boolean calculateOutput(double[] inputs, double[] weights){
        //Throw error and stop program when length of inputs and length of weights doesn't match.
        if(inputs.length != weights.length){
            ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "CRITICAL ERROR: length of inputs and length of weights in neuron doesn't match");
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.neuronInputsMismatch"));
            return false;
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
        return true;
    }

    protected abstract double calculateRawOutput(double[] inputs, double[] weights);

    public boolean isBias() {
        return false;
    }

    public NetworkStructure.LayerTypes getType() {
        return layerType;
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

    public ActivationFunctions.Types getActivationFunction() {
        return activationFunction;
    }
}
