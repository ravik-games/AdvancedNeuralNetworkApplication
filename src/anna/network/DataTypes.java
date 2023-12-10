package anna.network;

import anna.math.ActivationFunctions;
import anna.network.neurons.Neuron;
import anna.ui.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataTypes {

    //Information about network in short form (for save/load)
    public static class NetworkData {
        private final List<LayerData> structure;
        private final List<NeuronWeightData> weights;

        public NetworkData(List<LayerData> structure, List<NeuronData> neuronData){
            this.structure = structure;
            weights = new ArrayList<>();
            for (NeuronData singleNeuronData : neuronData) {
                weights.add(new NeuronWeightData(singleNeuronData.getInputConnections(), singleNeuronData.getOutputConnections()));
            }
        }

        public List<NeuronWeightData> getWeightsList(){
            return weights;
        }

        public NeuronWeightData getWeights(int id){
            return weights.get(id);
        }

        public List<LayerData> getStructure() {
            return structure;
        }

        public record NeuronWeightData(ArrayList<Synapse> inputConnections, ArrayList<Synapse> outputConnections) {}

        public record LayerData(int neuronNumber, NetworkStructure.LayerTypes type, ActivationFunctions.Types activationFunction) {}
    }

    //Information about one neuron
    public static class NeuronData {
        private Neuron neuron;
        private ArrayList<DataTypes.Synapse> inputConnections = new ArrayList<>();
        private ArrayList<DataTypes.Synapse> outputConnections = new ArrayList<>();

        public NeuronData(Neuron neuron){
            setNeuron(neuron);
        }

        public Neuron getNeuron() {
            return neuron;
        }
        public void setNeuron(Neuron neuron) {
            this.neuron = neuron;
        }

        public ArrayList<DataTypes.Synapse> getInputConnections() {
            return inputConnections;
        }
        public void setInputConnections(ArrayList<DataTypes.Synapse> inputConnections) {
            this.inputConnections = inputConnections;
        }

        public ArrayList<DataTypes.Synapse> getOutputConnections() {
            return outputConnections;
        }
        public void setOutputConnections(ArrayList<DataTypes.Synapse> outputConnections) {
            this.outputConnections = outputConnections;
        }
    }

    //Information about one synapse (connection) between neurons
    public static class Synapse{
        private double weight, deltaWeight;

        private final int neuronID;

        public int getNeuronID() {
            return neuronID;
        }

        public double getWeight() {
            return weight;
        }
        public void setWeight(double weight) {
            this.weight = weight;
        }

        public double getDeltaWeight() {
            return deltaWeight;
        }
        public void setDeltaWeight(double deltaWeight) {
            this.deltaWeight = deltaWeight;
        }

        public Synapse(int neuronID, double weight, double deltaWeight){
            this.deltaWeight = deltaWeight;
            this.weight = weight;
            this.neuronID = neuronID;
        }
    }

    //Information about dataset for network run arguments
    public record Dataset(double[][] inputs, String[] expectedOutput, String[] allOutputTypes){ }

    public record InputParameterData(String parameter, Parser.InputTypes type) { }

    public static class Evaluation {
        private final long classSize;
        private double meanError;
        private final double[][] classesInfoTable;

        public Evaluation(int classCount, long datasetSize){
            classSize = datasetSize;
            classesInfoTable = new double[classCount][classCount];
        }

        public void addClassInfo(double value, int firstID, int secondID){
            classesInfoTable[firstID][secondID] += value;
        }
        public long getSize() {
            return classSize * classesInfoTable.length;
        }
        public long getClassSize(){
            return classSize;
        }
        public double getMeanError() {
            return meanError;
        }
        public void setMeanError(double meanError) {
            this.meanError = meanError;
        }
        public void addMeanError(double value) {
            meanError += value;
        }
        public double getClassAccuracy(int classID) {
            return getClassHits(classID) / getClassSize();
        }
        public double getMeanAccuracy(){
            return getHits() / getSize();
        }
        public double getClassHits(int classID){
            return getClassTrueNegative(classID) + getClassTruePositive(classID);
        }
        public double getHits(){
            return getTruePositive() + getTrueNegative();
        }
        public double getClassPrecision(int classID){
            if(classesInfoTable[classID][classID] == 0)
                return 0;
            return classesInfoTable[classID][classID] / Arrays.stream(classesInfoTable[classID]).sum();
        }
        public double getClassRecall(int classID){
            if(classesInfoTable[classID][classID] == 0)
                return 0;
            double sum = 0;
            for (double[] i : classesInfoTable) {
                sum += i[classID];
            }
            return classesInfoTable[classID][classID] / sum;
        }
        public double getClassFScore(int classID, double beta){
            double precision = getClassPrecision(classID);
            double recall = getClassRecall(classID);
            if((precision + recall) == 0)
                return 0;
            return ((beta * beta + 1) * precision * recall) / (beta * beta * precision + recall);
        }
        public double getClassFScore(int classID){
            return getClassFScore(classID, 1);
        }
        public double getMeanPrecision(){
            double sum = 0;
            for (int i = 0; i < classesInfoTable.length; i++) {
                sum += getClassPrecision(i);
            }
            return sum / classesInfoTable.length;
        }
        public double getMeanRecall(){
            double sum = 0;
            for (int i = 0; i < classesInfoTable[0].length; i++) {
                sum += getClassRecall(i);
            }
            return sum / classesInfoTable[0].length;
        }
        public double getMeanFScore(double beta){
            double sum = 0;
            for (int i = 0; i < classesInfoTable.length; i++) {
                sum += getClassFScore(i, beta);
            }
            return sum / classesInfoTable.length;
        }
        public double getMeanFScore(){
            return getMeanFScore(1);
        }

        public double getClassNegative(int classID) {
            return getClassSize() - getClassPositive(classID);
        }
        public double getClassPositive(int classID) {
            return Arrays.stream(classesInfoTable[classID]).sum();
        }
        public double getClassTruePositive(int classID) {
            return classesInfoTable[classID][classID];
        }
        public double getClassFalsePositive(int classID) {
            return getClassPositive(classID) - classesInfoTable[classID][classID];
        }
        public double getClassTrueNegative(int classID) {
            return getClassNegative(classID) - getClassFalseNegative(classID);
        }
        public double getClassFalseNegative(int classID) {
            double sum = 0;
            for (double[] i : classesInfoTable) {
                sum += i[classID];
            }
            return sum - getClassTruePositive(classID);
        }

        public double getTruePositive() {
            double sum = 0;
            for (int i = 0; i < classesInfoTable.length; i++) {
                sum += getClassTruePositive(i);
            }
            return sum;
        }
        public double getTrueNegative() {
            double sum = 0;
            for (int i = 0; i < classesInfoTable.length; i++) {
                sum += getClassTrueNegative(i);
            }
            return sum;
        }
        public double getFalsePositive() {
            double sum = 0;
            for (int i = 0; i < classesInfoTable.length; i++) {
                sum += getClassFalsePositive(i);
            }
            return sum;
        }
        public double getFalseNegative() {
            double sum = 0;
            for (int i = 0; i < classesInfoTable.length; i++) {
                sum += getClassFalseNegative(i);
            }
            return sum;
        }

        public double[][] getClassesInfoTable() {
            return classesInfoTable;
        }

        public String getClassInfoTableDebug(){
            StringBuilder sb = new StringBuilder();
            for (double[] doubles : classesInfoTable) {
                for (int j = 0; j < classesInfoTable[0].length; j++) {
                    sb.append(doubles[j]).append("\t\t");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        public enum Metrics{
            LOSS, SIZE, HITS, ACCURACY, PRECISION, RECALL, F_SCORE
        }
    }
}
