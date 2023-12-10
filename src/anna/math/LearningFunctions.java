package anna.math;

import anna.network.Hyperparameters;

public class LearningFunctions {
    //Class with functions, needed to learn neural network

    //  --- Backpropagation functions ---

    //Calculates delta of output neuron
    public static double outputDelta(double rawOutput, double ideal, double actual, ActivationFunctions.Types typeOfActivation){
        return (ideal - actual) * derivative(typeOfActivation, rawOutput);
    }

    //Calculates delta of hidden neuron
    public static double hiddenDelta(double[] weights, double[] deltas, double rawOutput, ActivationFunctions.Types typeOfActivation){
        double sum = 0;

        //Throw error when lengths mismatch
        if(weights.length != deltas.length){
            System.out.println("CRITICAL ERROR: length of weights and length of deltas in delta of hidden neuron doesn't match");
            System.exit(1);
        }

        //Calculate sum of output weights and deltas of connected neurons
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i] * deltas[i];
        }

        //Return delta of this hidden neuron
        return sum * derivative(typeOfActivation, rawOutput);
    }

    //Calculates gradient
    public static double gradient(double delta, double actual){
        return delta * actual;
    }

    //Calculates derivative using simplified functions
    public static double derivative(ActivationFunctions.Types typeOfActivation, double rawOutput){
        switch(typeOfActivation){
            case RELU -> {
                return rawOutput > 0 ? 1 : 0;
                //Special case: when rawOutput == 0, derivative will count as 0.
            }
            case LRELU -> {
                return rawOutput > 0 ? 1 : Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS;
                //Special case: when rawOutput == 0, derivative will count as slope value.
            }
            case SIGMOID -> {
                return ActivationFunctions.sigmoid(rawOutput) * (1 - ActivationFunctions.sigmoid(rawOutput));
            }
            case TANH -> {
                return  1 - ActivationFunctions.tanh(rawOutput) * ActivationFunctions.tanh(rawOutput);
            }
            case ELU -> {
                return rawOutput > 0 ? 1 : Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS * Math.exp(rawOutput);
                //Special case: when rawOutput == 0, derivative will count as slope value * e^rawOutput.
            }
            case LINEAR -> {
                return 1;
            }
            default -> {
                System.out.println("CRITICAL ERROR: Unexpected value in derivative -- " + typeOfActivation);
                System.exit(1);
            }
        }
        return 0;
    }

    public static double deltaWeight(double nextDelta, double currentOutput, double lastDeltaWeight){
        //Calculate delta weight
        return Hyperparameters.LEARNING_RATE * gradient(nextDelta, currentOutput) + Hyperparameters.MOMENTUM * lastDeltaWeight;
    }
}
