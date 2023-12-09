package anna;

import anna.ui.Parser;
import anna.ui.PopupController;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataMaster {
    // Class for working with data and serialization

    protected RawDataset trainingSet, testingSet, generalSet;

    protected static ProgramData lastData;
    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault());
    protected static final Logger LOGGER = Logger.getLogger(DataMaster.class.getName());

    // Load general dataset and parse it
    public boolean loadGeneralDataset() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return false;
        File generalSetFile = Parser.getFileFromPath(path, "csv");

        if (!generalSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetDataFile"));
            return false;
        }

        //Parse data and labels
        List<List<String>> rawGeneralSet = Parser.parseData(generalSetFile);
        if(rawGeneralSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the database. Failed to read file.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToReadDataFile"));
            return false;
        }

        List<String> labels = rawGeneralSet.get(0);
        rawGeneralSet.remove(0); // Remove row with labels from dataset

        generalSet = new RawDataset(generalSetFile, rawGeneralSet, labels);

        return true;
    }

    // Load training dataset and parse it
    public boolean loadTrainingDataset() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return false;
        File trainSetFile = Parser.getFileFromPath(path, "csv");

        if (!trainSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetTestingDataFile"));
            return false;
        }
        //Parse data and add it to the table
        List<List<String>> rawTrainSet = Parser.parseData(trainSetFile);
        if(rawTrainSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. Failed to read file.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToReadTrainingDataFile"));
            return false;
        }

        List<String> labels = rawTrainSet.get(0);
        rawTrainSet.remove(0); // Remove row with labels from dataset

        trainingSet = new RawDataset(trainSetFile, rawTrainSet, labels);

        return true;
    }

    // Load general dataset and parse it
    public boolean loadTestingDataset() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return false;
        File testSetFile = Parser.getFileFromPath(path, "csv");

        if (!testSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the testing database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetTestingDataFile"));
            return false;
        }

        //Parse data and add it to the table
        List<List<String>> rawTestSet = Parser.parseData(testSetFile);
        if(rawTestSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the testing database. Failed to read file.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToReadTestingDataFile"));
            return false;
        }

        List<String> labels = rawTestSet.get(0);
        rawTestSet.remove(0); // Remove row with labels from dataset

        testingSet = new RawDataset(testSetFile, rawTestSet, labels);

        return true;
    }

    public boolean splitGeneralDataset(double trainingPart) {
        if (generalSet == null)
            return false;

        // Randomise dataset
        Collections.shuffle(generalSet.data());

        int splitIndex = (int) Math.round(generalSet.data().size() * trainingPart);
        List<List<String>> rawTrainSet = generalSet.data().subList(0, splitIndex);
        List<List<String>> rawTestSet = generalSet.data().subList(splitIndex, generalSet.data().size());

        trainingSet = new RawDataset(null, rawTrainSet, generalSet.labels());
        testingSet = new RawDataset(null, rawTestSet, generalSet.labels());
        return true;
    }

    public void clearData() {
        trainingSet = null;
        testingSet = null;
        generalSet = null;
    }

    public static void saveProgramData(ProgramData data) {
        try {
            // Create new directory if it doesn't exist
            File fileCheck = new File("data/programdata.dat");
            if (!fileCheck.exists())
                if (!fileCheck.getParentFile().mkdirs())
                    throw new IOException();

            FileOutputStream fileOutputStream = new FileOutputStream("data/programdata.dat");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.dataWriteError"));
            LOGGER.warning("An error occurred while saving program data. Failed to save settings.\n" + e);
        }
    }

    public static ProgramData loadProgramData() {
        try {
            FileInputStream fileInputStream = new FileInputStream("data/programdata.dat");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            ProgramData data = (ProgramData) objectInputStream.readObject();
            objectInputStream.close();

            // Apply settings
            Locale.setDefault(data.locale());
            lastData = data;

            return data;
        } catch (FileNotFoundException ignored) {
            return createDefaultProgramData();
        } catch (IOException | ClassNotFoundException e) {
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.dataReadError"));
            LOGGER.warning("An error occurred while reading the program settings file. The settings will be reset.\n" + e);
            return createDefaultProgramData();
        }
    }

    private static ProgramData createDefaultProgramData() {
        return new ProgramData(Locale.getDefault());
    }

    public boolean areDatasetsNotValid() {
        return trainingSet == null || trainingSet.data() == null || trainingSet.data().isEmpty() || trainingSet.labels() == null || trainingSet.labels().isEmpty() ||
                testingSet == null || testingSet.data() == null || testingSet.data().isEmpty() || testingSet.labels() == null || testingSet.labels().isEmpty();
    }

    // Getters for private fields
    public static ProgramData getLastProgramData() {
        return lastData;
    }

    // Get all unique values in selected column
    public List<String> getDatasetUniqueClasses(String parameter) {
        if(areDatasetsNotValid() || !trainingSet.labels().contains(parameter))
            return null;

        return trainingSet.data().stream().map(row -> row.get(trainingSet.labels().indexOf(parameter))).distinct().toList();
    }

    public RawDataset getTrainingSet() {
        return trainingSet;
    }
    public RawDataset getTestingSet() {
        return testingSet;
    }
    public RawDataset getGeneralSet() {
        return generalSet;
    }

    public record RawDataset(File file, List<List<String>> data, List<String> labels) {}
    public record ProgramData(Locale locale) implements Serializable {}
}


