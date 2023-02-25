package ANNA;

public enum Hyperparameters {
    //Class with all hyperparameters of neural network

    /**
     * Number of epochs
     * <p>
     * Affects: TODO Test what does number of epochs affect
     */
    NUMBER_OF_EPOCHS("1000",
            Type.POSITIVE_INT,
            "Количество эпох", """
            Сколько раз должна нейронная сеть обучиться на входном файле.
            Напрямую влияет на длительность и точность обучения.
            Слишком большие значения могут привести и переобучению, а слишком маленькие к недостаточному обучению."""
    ),

    /**
     * The number of training samples used in one iteration. If equal or below 0, then will be used number of input arguments.
     * <p>
     * Affects: TODO Test what does batch size affect
     */
    BATCH_SIZE("0",
            Type.POSITIVE_INT,
            "Размер входных данных в эпохе", """
            Размер подаваемых нейронной сети данных в строках.
            0 -- считывать весь файл."""
    ),

    /**
     * Use bias neurons
     * <p>
     * Affects: TODO Test what does bias neurons affect
     */
    USE_BIAS_NEURONS("false",
            Type.BOOLEAN,
            "[WIP] Использовать нейроны смещения", """
            Добавить к структуре нейронной сети нейроны смещения (смещение).
            Смещение добавляют к каждому слою (исключая входной) нейрон с значением 1.
            Это помогает сдвигать функцию активации по оси X и потенциально увеличить захватываемую область значений."""
    ),

    /**
     * How weights will be initialized on different layers
     * <p>
     * Affects: TODO Add options for initializing weights
     */
    NETWORK_WEIGHT_INITIALIZATION("0",
            Type.POSITIVE_INT,
            "[WIP] Тип инициализации первоначальных весов", """
            Способ вычисления первых весов в нейронной сети"""
    ),

    /**
     * How quickly a network updates its parameters.
     * <p>
     * Affects: TODO Test what does learning rate affect
     */
    LEARNING_RATE("0.1",
            Type.POSITIVE_DOUBLE,
            "Скорость обучения", """
            Параметр при вычислении градиентного спуска.
            Прямо пропорционально влияет на значение градиентного спуска при обучении нейронной сети."""
    ),

    /**
     * Helps to prevent oscillations and getting stuck in local minimums.
     * <p>
     * Affects: TODO Test what does momentum affect
     */
    MOMENTUM("0.3",
            Type.POSITIVE_DOUBLE,
            "Момент", """
            "Ускорение" при использовании градиентного спуска.
            Слишком большие значения могут "перескочить" глобальный минимум/максимум, а слишком маленькие "застрянут" в локальных минимумах/максимумах."""
    ),

    /**
     * Needed for ELU and LReLU. Default is 0.01.
     * <p>
     * Affects: TODO Test what does slope affect
     */
    SLOPE_IN_ACTIVATION_FUNCTIONS("0.01",
            Type.POSITIVE_DOUBLE,
            "Наклон в функциях активации", """
            Определяет градиент отрицательных значений в функции активации LeakyReLU и ELU."""
    );

    private String value;
    private final Type type;
    private final String name;
    private final String description;
    Hyperparameters(String value, Type type, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Type getType() {
        return type;
    }

    //Parse value as Number
    public Number getValue() {
        if(value == null || type == null || value.equals(""))
            return 0;

        switch (type){
            case BOOLEAN -> {
                value = value.toLowerCase();
                return value.equals("true") ? 1: 0;
            }
            case POSITIVE_DOUBLE, DOUBLE -> {
                return Double.valueOf(value);
            }
            case POSITIVE_INT -> {
                return Integer.valueOf(value);
            }
        }
        return 0;
    }

    public String getRawValue(){
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum Type {
        BOOLEAN, POSITIVE_DOUBLE, POSITIVE_INT, DOUBLE
    }
}
