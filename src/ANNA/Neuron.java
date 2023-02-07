package ANNA;

public class Neuron {
    //Simple class, containing values and logic of one neuron

    //Where neuron is located in NN structure (X is for layers, Y is for place in layer)
    private final int positionX;
    private final int positionY;

    //Index of neuron
    private final int id;

    //Deltas, used in backpropagation
    private double delta;

    private double lastOutput;

    public final NetworkStructure.neuronTypes type;

    public Neuron(int id, int positionY, int positionX, NetworkStructure.neuronTypes type) {
        this.type = type;
        this.positionX = positionX;
        this.positionY = positionY;
        this.id = id;
    }

    //Logic for calculating output of neuron
    public void calculateOutput(double[] inputs, double[] weights){
        double output = 0;

        //Throw error and stop program when length of inputs and length of weights doesn't match.
        if(inputs.length != weights.length - Boolean.compare(Hyperparameters.USE_BIAS_NEURONS, false)){
            //TODO Test conditions
            System.err.println("CRITICAL ERROR: length of inputs and length of weights in neuron doesn't match");
            PopupController.errorMessage("ERROR", "Критическая ошибка", "", "Произошла критическая ошибка при работе нейронной сети. Количество входных данных и весов нейрона не совпадает.");
            System.exit(1);
        }

        //Multiply by weights and add all inputs together.
        for (int i = 0; i < inputs.length; i++) {
            output += inputs[i] * weights[i];
        }

        //Bias neron influence
        if (Hyperparameters.USE_BIAS_NEURONS)
            output += weights[weights.length - 1];

        //Return processed output using activation function.
        lastOutput = ActivationFunctions.sigmoid(output);
    }

    //Getters
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
}
