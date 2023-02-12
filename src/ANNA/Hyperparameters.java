package ANNA;

public class Hyperparameters {
    //Class with all hyperparameters of neural network

    /**
     * Number of epochs
     * <p>
     * Affects: TODO Test what does number of epochs affect
     */
    public static final int NUMBER_OF_EPOCHS = 1000;

    /**
     * The number of training samples used in one iteration. If equal or below 0, then will be used number of input arguments.
     * <p>
     * Affects: TODO Test what does batch size affect
     */
    public static final int BATCH_SIZE = 0;

    /**
     * Use bias neurons
     * <p>
     * Affects: TODO Test what does bias neurons affect
     */
    public static final boolean USE_BIAS_NEURONS = false; //FIXME Doesn't work

    /**
     * How weights will be initialized on different layers
     * <p>
     * Affects: TODO Add options for initializing weights
     */
    public static final int NETWORK_WEIGHT_INITIALIZATION = 0; //TODO Not implemented

    /**
     * How quickly a network updates its parameters.
     * <p>
     * Affects: TODO Test what does learning rate affect
     */
    public static final double LEARNING_RATE = 0.1;

    /**
     * Helps to prevent oscillations and getting stuck in local minimums.
     * <p>
     * Affects: TODO Test what does momentum affect
     */
    public static final double MOMENTUM = 0.3;

    /**
     * Needed for ELU and LReLU. Default is 0.01.
     * <p>
     * Affects: TODO Test what does slope affect
     */
    public static final double SLOPE_IN_ACTIVATION_FUNCTIONS = 0.01;


    //Method to get name and description of hyperparameter
    public static String getData(identificator id, boolean name){
        switch(id){
            default ->{
                return null;
            }
            case NUMBER_OF_EPOCHS -> {
                if(name)
                    return "Количество эпох";
                else {
                    return "Сколько раз должна нейронная сеть обучиться на входном файле.\n" +
                            "Напрямую влияет на длительность и точность обучения. " +
                            "Слишком большие значения могут привести и переобучению, а слишком маленькие к недостаточному обучению.";
                }
            }
            case BATCH_SIZE -> {
                if(name)
                    return "Размер входных данных в эпохе";
                else{
                    return "Размер подаваемых нейронной сети данных в строках.\n" +
                            "0 -- считывать весь файл.";
                }
            }
            case USE_BIAS_NEURONS -> {
                if(name)
                    return "[WIP] Использовать нейроны смещения";
                else{
                    return "Добавить к структуре нейронной сети нейроны смещения (смещение).\n" +
                            "Смещение добавляют к каждому слою (исключая входной) нейрон с значением 1. " +
                            "Это помогает сдвигать функцию активации по оси X и потенциально увеличить захватываемую область значений.";
                }
            }
            case NETWORK_WEIGHT_INITIALIZATION -> {
                if(name)
                    return "[WIP] Тип инициализации первоначальных весов";
                else{
                    return "Способ вычисления первых весов в нейронной сети.\n";
                }
            }
            case LEARNING_RATE -> {
                if(name)
                    return "Скорость обучения";
                else{
                    return "Параметр при вычислении градиентного спуска.\n" +
                            "Прямо пропорционально влияет на значение градиентного спуска при обучении нейронной сети.";
                }
            }
            case MOMENTUM -> {
                if(name)
                    return "Момент";
                else{
                    return "\"Ускорение\" при использовании градиентного спуска.\n" +
                            "Слишком большие значения могут \"перескочить\" глобальный минимум/максимум, а слишком маленькие \"застрянут\" в локальных минимумах/максимумах.";
                }
            }
            case SLOPE_IN_ACTIVATION_FUNCTIONS -> {
                if(name)
                    return "Наклон в функциях активации";
                else{
                    return "Определяет градиент отрицательных значений в функции активации LeakyReLU и ELU.\n";
                }
            }
        }
    }

    //Method to get type and range of values of hyperparameter
    public static type getType(identificator id){
        switch(id){
            default -> {
                return null;
            }
            case NUMBER_OF_EPOCHS, BATCH_SIZE, NETWORK_WEIGHT_INITIALIZATION -> {
                return type.POSITIVE_INT;
            }
            case USE_BIAS_NEURONS -> {
                return type.BOOLEAN;
            }
            case LEARNING_RATE, SLOPE_IN_ACTIVATION_FUNCTIONS, MOMENTUM -> {
                return type.POSITIVE_DOUBLE;
            }
        }
    }
    public static String getValueByID(identificator id){
        switch(id){
            default -> {
                return null;
            }
            case NUMBER_OF_EPOCHS -> {
                return String.valueOf(NUMBER_OF_EPOCHS);
            }
            case BATCH_SIZE -> {
                return String.valueOf(BATCH_SIZE);
            }
            case USE_BIAS_NEURONS -> {
                return String.valueOf(USE_BIAS_NEURONS);
            }
            case NETWORK_WEIGHT_INITIALIZATION -> {
                return String.valueOf(NETWORK_WEIGHT_INITIALIZATION);
            }
            case LEARNING_RATE -> {
                return String.valueOf(LEARNING_RATE);
            }
            case MOMENTUM -> {
                return String.valueOf(MOMENTUM);
            }
            case SLOPE_IN_ACTIVATION_FUNCTIONS -> {
                return String.valueOf(SLOPE_IN_ACTIVATION_FUNCTIONS);
            }
        }
    }
    public enum identificator{
        NUMBER_OF_EPOCHS, BATCH_SIZE, USE_BIAS_NEURONS, NETWORK_WEIGHT_INITIALIZATION, LEARNING_RATE, MOMENTUM, SLOPE_IN_ACTIVATION_FUNCTIONS
    }
    public enum type {
        BOOLEAN, POSITIVE_DOUBLE, POSITIVE_INT, DOUBLE
    }
}
