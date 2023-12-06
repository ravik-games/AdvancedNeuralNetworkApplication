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

    protected File trainSetFile, testSetFile, generalSetFile;
    protected List<List<String>> rawTrainSet, rawTestSet, rawGeneralSet;

    protected static ProgramData lastData;
    protected static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault());
    protected static final Logger LOGGER = Logger.getLogger(DataMaster.class.getName());

    // Load general dataset and parse it
    public boolean loadGeneralDataset() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return false;
        generalSetFile = Parser.getFileFromPath(path, "csv");

        if (!generalSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetDataFile"));
            return false;
        }

        //Parse data and add it to the table
        rawGeneralSet = Parser.parseData(generalSetFile);
        if(rawGeneralSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the database. Failed to read file.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToReadDataFile"));
            return false;
        }
        return true;
    }

    // Load training dataset and parse it
    public boolean loadTrainingDataset() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return false;
        trainSetFile = Parser.getFileFromPath(path, "csv");

        if (!trainSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetTestingDataFile"));
            return false;
        }
        //Parse data and add it to the table
        rawTrainSet = Parser.parseData(trainSetFile);
        if(rawTrainSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the training database. Failed to read file.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToReadTrainingDataFile"));
            return false;
        }
        return true;
    }

    // Load general dataset and parse it
    public boolean loadTestingDataset() {
        String path =  PopupController.openExplorer();
        if(path == null)
            return false;
        testSetFile = Parser.getFileFromPath(path, "csv");

        if (!testSetFile.exists()) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the testing database. The file was not found.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToGetTestingDataFile"));
            return false;
        }

        //Parse data and add it to the table
        rawTestSet = Parser.parseData(testSetFile);
        if(rawTestSet == null){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred while reading the testing database. Failed to read file.");
            PopupController.errorMessage("WARNING", "", bundle.getString("logger.warning.failedToReadTestingDataFile"));
            return false;
        }
        return true;
    }

    public void splitGeneralDataset(double trainingPart) {
        // Remove row with labels
        rawGeneralSet.remove(0);
        // Randomise dataset
        Collections.shuffle(rawGeneralSet);

        int splitIndex = (int) Math.round(rawGeneralSet.size() * trainingPart);
        rawTrainSet = rawGeneralSet.subList(0, splitIndex);
        rawTestSet = rawGeneralSet.subList(splitIndex, rawGeneralSet.size());
    }

    public void clearData() {
        trainSetFile = null;
        testSetFile = null;
        generalSetFile = null;
        rawTrainSet = null;
        rawTestSet = null;
        rawGeneralSet = null;
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

    // Getters for private fields
    public static ProgramData getLastProgramData() {
        return lastData;
    }
    public File getTrainSetFile() {
        return trainSetFile;
    }
    public File getTestSetFile() {
        return testSetFile;
    }
    public File getGeneralSetFile() {
        return generalSetFile;
    }
    public List<List<String>> getRawTrainSet() {
        return rawTrainSet;
    }
    public List<List<String>> getRawTestSet() {
        return rawTestSet;
    }
    public List<List<String>> getRawGeneralSet() {
        return rawGeneralSet;
    }

    public record ProgramData(Locale locale) implements Serializable {}
}


