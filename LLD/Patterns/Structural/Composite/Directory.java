package Structural.Composite;

import java.util.ArrayList;
import java.util.List;

public class Directory implements FileSystemComponent{
    private final String name;
    private final List<FileSystemComponent> children
            = new ArrayList<>();

    Directory(String name){
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {

        long totalSize = 0;
        for(FileSystemComponent child : children){
            totalSize += child.getSize();
        }
        return totalSize;
    }

    @Override
    public void print() {
        System.out.println("Directory: " + name);
        for (FileSystemComponent child: children){
            child.print();
        }
    }

    public void add(FileSystemComponent component){
        this.children.add(component);
    }

    public void remove(FileSystemComponent component){
        this.children.remove(component);
    }
}
