package ANNA;

import java.util.ArrayList;

public class NetworkStructure {
    //Class, containing data about neurons and network structure

    //Table of weights by neurons IDs
    private final ArrayList<ArrayList<WeightData>> weightsTable = new ArrayList<>();

    //Structure of neural network
    //First column contains input neurons, last contains output and between all is a hidden neurons
    private final ArrayList<ArrayList<Neuron>> structure = new ArrayList<>();

    //Neurons by IDs
    private final ArrayList<Neuron> neuronsArray = new ArrayList<>();

    //Initialize structure
    public NetworkStructure(int[] structure, double[][] weights){
        setStructure(structure, weights);
    }

    /**
     * @param newStructure Array of Integers. Represents number of neurons in each layer.
     */
    public void setStructure(int[] newStructure, double[][] weights){
        //Clear previous values
        weightsTable.clear();
        structure.clear();
        neuronsArray.clear();

        //Initialize new neurons
        for(int i = 0; i < newStructure.length; i++) {

            ArrayList<Neuron> layer = new ArrayList<Neuron>(newStructure.length);
            //Create new neurons in layer
            for (int j = 0; j < newStructure[i]; j++) {
                layer.add(initializeNeuron(i, j));
            }
            structure.add(layer);
        }

        //Create empty 2D ArrayList for weights
        for (int i = 0; i < neuronsArray.size(); i++) {
            ArrayList<WeightData> weightsRow = new ArrayList<>(neuronsArray.size());
            for (int j = 0; j < neuronsArray.size(); j++) {
                weightsRow.add(new WeightData(0, 0));
            }
            weightsTable.add(weightsRow);
        }

        //Create synapses
        for (int i = 0; i < structure.size() - 1; i++) {
            for (int j = 0; j < structure.get(i).size(); j++) {
                //Getting next rows neurons
                for (int k = 0; k < structure.get(i + 1).size(); k++) {
                    double weight;
                    int firstID = getIDByPosition(i, j);
                    int secondID =getIDByPosition(i + 1, k);
                    //Using pre-defined weights if we can, else random
                    if(weights != null)
                        weight = weights[firstID][secondID];
                    else
                        weight = Math.random() * 2 - 1;
                    createSynapse(firstID, secondID, weight);
                }
            }
        }
    }

    public void createSynapse(int firstID, int secondID, double initialWeight){
        weightsTable.get(firstID).set(secondID, new WeightData(initialWeight, 0));
        weightsTable.get(secondID).set(firstID, new WeightData(initialWeight, 0));
    }

    //Initialize single neuron and add it to the arrays
    private Neuron initializeNeuron(int positionY, int positionX){
        //Create new neuron and set it ID
        Neuron value = new Neuron(neuronsArray.size(), positionY, positionX);
        //Add to the array
        neuronsArray.add(value);
        return value;
    }

    //Getters
    public int getIDByPosition(int x, int y){
        return getNeuronByPosition(x, y).getId();
    }
    public int[] getPositionByID(int id){
        return new int[]{getNeuronByID(id).getPositionY(), getNeuronByID(id).getPositionX()};
    }

    public Neuron getNeuronByID(int id){
        return neuronsArray.get(id);
    }
    public Neuron getNeuronByPosition(int x, int y){
        return structure.get(x).get(y);
    }

    public double getWeightByID(int firstID, int secondID){
        return weightsTable.get(firstID).get(secondID).getWeight();
    }
    public double getWeightByPosition(int firstX, int firstY, int secondX, int secondY){
        return weightsTable.get(getNeuronByPosition(firstX, firstY).getId()).get(getNeuronByPosition(secondX, secondY).getId()).getWeight();
    }
    public void setWeightByID(int firstID, int secondID, double weight){
        weightsTable.get(firstID).get(secondID).setWeight(weight);
        weightsTable.get(secondID).get(firstID).setWeight(weight);
    }
    public void setWeightByPosition(int firstX, int firstY, int secondX, int secondY, double weight){
        weightsTable.get(getNeuronByPosition(firstX, firstY).getId()).get(getNeuronByPosition(secondX, secondY).getId()).setWeight(weight);
        weightsTable.get(getNeuronByPosition(secondX, secondY).getId()).get(getNeuronByPosition(firstX, firstY).getId()).setWeight(weight);
    }

    public double getDeltaWeightByID(int firstID, int secondID){
        return weightsTable.get(firstID).get(secondID).getDeltaWeight();
    }
    public double getDeltaByPosition(int firstX, int firstY, int secondX, int secondY){
        return weightsTable.get(getNeuronByPosition(firstX, firstY).getId()).get(getNeuronByPosition(secondX, secondY).getId()).getDeltaWeight();
    }
    public void setDeltaWeightByID(int firstID, int secondID, double deltaWeight){
         weightsTable.get(firstID).get(secondID).setDeltaWeight(deltaWeight);
        weightsTable.get(secondID).get(firstID).setDeltaWeight(deltaWeight);
    }
    public void setDeltaByPosition(int firstX, int firstY, int secondX, int secondY, double deltaWeight){
        weightsTable.get(getNeuronByPosition(firstX, firstY).getId()).get(getNeuronByPosition(secondX, secondY).getId()).setDeltaWeight(deltaWeight);
        weightsTable.get(getNeuronByPosition(secondX, secondY).getId()).get(getNeuronByPosition(firstX, firstY).getId()).setDeltaWeight(deltaWeight);
    }

    public int getLayersAmount(){
        return structure.size();
    }
    public int getNeuronsAmountInLayer(int layer){
        return structure.get(layer).size();
    }

    public int getLastID(){
        return neuronsArray.size() - 1;
    }

    public void printWeights(boolean delta){
        StringBuilder str = new StringBuilder();
        for (ArrayList<WeightData> i: weightsTable) {
            for (WeightData j: i) {
                if (delta)
                    str.append(j.getDeltaWeight());
                else
                    str.append(j.getWeight());
                str.append("\t\t");
            }
            str.append("\n");
        }
        System.out.println(str);
    }

    //Class with 2 variables for weights table
    private static class WeightData{
        private double weight, deltaWeight;

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

        public WeightData(double weight, double deltaWeight){
            this.deltaWeight = deltaWeight;
            this.weight = weight;
        }
    }
}
