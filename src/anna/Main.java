package anna;

public class Main {
    // Workaround for javafx bug, where it cant run main class extended from javafx.Application
    public static void main(String[] args) {
        Application.main(args);
    }
}
