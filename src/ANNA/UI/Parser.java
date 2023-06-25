package ANNA.UI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser {

    //Method for parsing raw set values
    public static double parseRawValue(String value, inputTypes type){
        if(value.equals(""))
            return 0;
        switch(type){
            case NUMBER -> {
                if(checkValueForOnlyNumbers(value))
                    return Double.parseDouble(value);
                else
                    return 0;
            }
            case BOOLEAN -> {
                value = value.toLowerCase();
                if(value.equals("true") || value.equals("1"))
                    return 1;
                else
                    return 0;
            }
            default -> {
                PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании значения. Ошибочное значение будет заменено нулем.");
                Logger.getLogger(Parser.class.getName()).log(Level.WARNING, "An error has occurred while reading the value. The wrong value will be replaced by zero.");
                return 0;
            }
        }
    }

    //Parse data from file to 2D list
    public static List<List<String>> parseData(File file){
        List<List<String>> result = new ArrayList<>();
        try {
            BufferedReader bReader = new BufferedReader(new FileReader(file));
            int maxLength = 0;
            String line = bReader.readLine();
            //Read all lines in file
            do{
                //Parse one line
                List<String> parsedLine = new ArrayList<>(List.of(line.split("[,;|](?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))")));

                //set max length of parsed line
                if(parsedLine.size() > maxLength)
                    maxLength = parsedLine.size();

                //Check for skips in last columns
                if(parsedLine.size() < maxLength){
                    int repeats = maxLength - parsedLine.size();
                    for(int i = 0; i < repeats; i++){
                        parsedLine.add("");
                    }
                }

                //Read next line
                result.add(parsedLine);
                line = bReader.readLine();
            }while(line != null);

        }catch (IOException e){
            PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании базы данных.\n" + e.getMessage());
            Logger.getLogger(Parser.class.getName()).log(Level.WARNING, "An error occurred while reading the database. See stack trace.\n" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static boolean checkValueForOnlyNumbers(String value){
        if(value.matches("^-?\\d*([.,]\\d+)?$") && !value.isEmpty())
            return true;
        else {
            PopupController.errorMessage("WARNING", "Ошибка", "", "Произошла ошибка при считывании пользовательских полей. Некорректные данные будут заменены нулями.\n");
            Logger.getLogger(Parser.class.getName()).log(Level.WARNING, "An error occurred while reading user fields. Incorrect data will be replaced with zeros.");
            return false;
        }
    }

    //Field value types
    public enum inputTypes{
        NUMBER, BOOLEAN
    }
}
