package ANNA.Network;

import ANNA.Network.neurons.Neuron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataTypes {

    //Information about network in short form (for save/load)
    public static class NetworkData {
        private final int[] structure;
        private final List<NeuronWeightData> weights;

        public NetworkData(int[] structure, List<NeuronData> neuronData){
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

        public int[] getStructure() {
            return structure;
        }

        public record NeuronWeightData(ArrayList<Synapse> inputConnections, ArrayList<Synapse> outputConnections){}
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

    public static class Evaluation {
        private final long size;
        private double positive;
        private double meanError;
        private final double[][] classesInfo;

        public Evaluation(int classCount, long datasetSize){
            size = datasetSize;
            classesInfo = new double[classCount][classCount];
        }

        public long getSize() {
            return size;
        }
        public double getPositive() {
            return positive;
        }
        public void addPositive(long value){
            positive += value;
        }
        public double[] getClassInfo(int id) {
            return classesInfo[id];
        }
        public void addClassInfo(double value, int firstID, int secondID){
            classesInfo[firstID][secondID] += value;
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
        public double getAccuracy(){
            return positive / size;
        }
        public double getClassPrecision(int classID){
            if(classesInfo[classID][classID] == 0)
                return 0;
            return classesInfo[classID][classID] / Arrays.stream(classesInfo[classID]).sum();
        }
        public double getClassRecall(int classID){
            if(classesInfo[classID][classID] == 0)
                return 0;
            double sum = 0;
            for (double[] i : classesInfo) {
                sum += i[classID];
            }
            return classesInfo[classID][classID] / sum;
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
            for (int i = 0; i < classesInfo.length; i++) {
                sum += getClassPrecision(i);
            }
            return sum / classesInfo.length;
        }
        public double getMeanRecall(){
            double sum = 0;
            for (int i = 0; i < classesInfo[0].length; i++) {
                sum += getClassRecall(i);
            }
            return sum / classesInfo[0].length;
        }
        public double getMeanFScore(double beta){
            double sum = 0;
            for (int i = 0; i < classesInfo.length; i++) {
                sum += getClassFScore(i, beta);
            }
            return sum / classesInfo.length;
        }
        public double getMeanFScore(){
            return getMeanFScore(1);
        }
    }
}
