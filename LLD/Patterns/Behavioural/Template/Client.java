package Behavioural.Template;

public class Client {
    public static void main(String[] args) {
        DataProcessor csvProcessor = new CsvDataProcessor();
        csvProcessor.process();

        System.out.println();

        DataProcessor jsonProcessor = new JsonDataProcessor();
        jsonProcessor.process();

    }
}
