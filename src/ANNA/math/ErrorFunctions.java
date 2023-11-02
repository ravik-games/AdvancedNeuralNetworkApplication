package ANNA.math;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorFunctions {

    /**
     * Calculates mead squared error. Almost always useful option.
     * @param ideal double array of ideal values
     * @param actual double array of actual values (outputs)
     * @return double Error
     */
    public static double MeanSquaredError(double[] ideal, double[] actual){
        double output = 0;

        //Throw error when lengths mismatch
        if(ideal.length != actual.length){
            Logger.getLogger(ErrorFunctions.class.getName()).log(Level.WARNING, "Length of ideal values and length of actual values in error function doesn't match");
            return -1;
        }

        //MSE function
        for (int i = 0; i < ideal.length; i++) {
            output += (ideal[i] - actual[i]) * (ideal[i] - actual[i]);
        }

        //Return error
        return output / ideal.length;
    }

    /**
     * Calculates square root of mead squared error. Usually returns lower value, than normal MSE.
     * @param ideal double array of ideal values
     * @param actual double array of actual values (outputs)
     * @return double Error
     */
    public static double RootMeanSquaredError(double[] ideal, double[] actual){
        double output = 0;

        //Throw error when lengths mismatch
        if(ideal.length != actual.length){
            Logger.getLogger(ErrorFunctions.class.getName()).log(Level.WARNING, "Length of ideal values and length of actual values in error function doesn't match");
            return -1;
        }

        //MSE numerator function
        for (int i = 0; i < ideal.length; i++) {
            output += (ideal[i] - actual[i]) * (ideal[i] - actual[i]);
        }

        //Get square root
        return Math.sqrt(output / ideal.length);
    }

    /**
     * Calculates error using arc tangent. Usually returns higher value, than normal MSE.
     * @param ideal double array of ideal values
     * @param actual double array of actual values (outputs)
     * @return double Error
     */
    public static double Arctan(double[] ideal, double[] actual){
        double output = 0;

        //Throw error when lengths mismatch
        if(ideal.length != actual.length){
            Logger.getLogger(ErrorFunctions.class.getName()).log(Level.WARNING, "Length of ideal values and length of actual values in error function doesn't match");
            return -1;
        }

        //Arc tangent numerator function
        for (int i = 0; i < ideal.length; i++) {
            output += (Math.atan(ideal[i] - actual[i])) * (Math.atan(ideal[i] - actual[i]));
        }
        //Return error
        return output / ideal.length;
    }
}
