package Behavioural.Template;

public class JsonDataProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("Reading JSON data");
    }

    @Override
    protected void processData() {
        System.out.println("Processing JSON data");
    }
}
