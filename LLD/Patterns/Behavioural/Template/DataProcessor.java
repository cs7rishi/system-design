package Behavioural.Template;

public abstract class DataProcessor {
    public final void process() {

        openFile();

        readData();

        processData();

        saveData();

        closeFile();
    }

    private void openFile() {
        System.out.println("Opening file");
    }

    protected abstract void readData();

    protected abstract void processData();

    private void saveData() {
        System.out.println("Saving data");
    }

    private void closeFile() {
        System.out.println("Closing file");
    }
}
