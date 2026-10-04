package Structural.Composite;

public class File implements FileSystemComponent{
    private final String name;
    private final long size;

    File(String name, long size){
        this.name = name;
        this.size = size;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void print() {
        System.out.println("File: " + name);
    }

}
