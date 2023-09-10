package ANNA.math;

import ANNA.network.Hyperparameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LearningFunctionsTest {

    private final double DELTA = 0.00001;

    @Test
    void outputDelta() {
    }

    @Test
    void hiddenDelta() {
    }

    @Test
    void gradient() {
    }
    
    @Test
    void testReLU(){
        double[] inputs = new double[]{-5, -1, 0, 1, 5};
        double[] correctOutputs = new double[]{0, 0, 0, 1, 1};

        for (int i = 0; i < inputs.length; i++) {
            double actualOutput = LearningFunctions.derivative(ActivationFunctions.types.RELU, inputs[i]);
            assertEquals(correctOutputs[i], actualOutput, DELTA);
            System.out.println("ReLU with X = " + inputs[i] + " passed   (" + actualOutput + ")");
        }
    }
    @Test
    void testLReLU(){
        double[] inputs = new double[]{-5, -1, 0, 1, 5};
        double[] correctOutputs = new double[]{Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS,
                Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, 1, 1};

        for (int i = 0; i < inputs.length; i++) {
            double actualOutput = LearningFunctions.derivative(ActivationFunctions.types.LRELU, inputs[i]);
            assertEquals(correctOutputs[i], actualOutput, DELTA);
            System.out.println("LeakyReLU with X = " + inputs[i] + " passed   (" + actualOutput + ")");
        }
    }
    @Test
    void testELU(){
        double[] inputs = new double[]{-5, -1, 0, 1, 5};
        double[] correctOutputs = new double[]{6.737946999085467E-3 * Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, 0.36787944117144233 * Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, 1, 1};

        for (int i = 0; i < inputs.length; i++) {
            double actualOutput = LearningFunctions.derivative(ActivationFunctions.types.ELU, inputs[i]);
            assertEquals(correctOutputs[i], actualOutput, DELTA);
            System.out.println("ELU with X = " + inputs[i] + " passed   (" + actualOutput + ")");
        }
    }
    @Test
    void testSigmoid(){
        double[] inputs = new double[]{-5, -1, 0, 1, 5};
        double[] correctOutputs = new double[]{0.006648056670790155, 0.19661193324148185, 0.25, 0.19661193324148185, 0.006648056670790155};

        for (int i = 0; i < inputs.length; i++) {
            double actualOutput = LearningFunctions.derivative(ActivationFunctions.types.SIGMOID, inputs[i]);
            assertEquals(correctOutputs[i], actualOutput, DELTA);
            System.out.println("Sigmoid with X = " + inputs[i] + " passed   (" + actualOutput + ")");
        }
    }
    @Test
    void testTanh(){
        double[] inputs = new double[]{-5, -1, 0, 1, 5};
        double[] correctOutputs = new double[]{0.0001815831906220126, 0.41997434161402614, 1, 0.41997434161402614, 0.0001815831906220126};

        for (int i = 0; i < inputs.length; i++) {
            double actualOutput = LearningFunctions.derivative(ActivationFunctions.types.TANH, inputs[i]);
            assertEquals(correctOutputs[i], actualOutput, DELTA);
            System.out.println("Tanh with X = " + inputs[i] + " passed   (" + actualOutput + ")");
        }
    }
    @Test
    void testLinear(){
        double[] inputs = new double[]{-5, -1, 0, 1, 5};
        double[] correctOutputs = new double[]{1, 1, 1, 1, 1};

        for (int i = 0; i < inputs.length; i++) {
            double actualOutput = LearningFunctions.derivative(ActivationFunctions.types.LINEAR, inputs[i]);
            assertEquals(correctOutputs[i], actualOutput, DELTA);
            System.out.println("Linear with X = " + inputs[i] + " passed   (" + actualOutput + ")");
        }
    }

    @Test
    void deltaWeight() {
    }
}