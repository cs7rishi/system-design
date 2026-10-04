package Structural.Composite;

public class Client {
    public static void main(String[] args) {
        FileSystemComponent resume = new File("resume.pdf", 100);
        FileSystemComponent photo = new File("photo.jpg", 200);

        FileSystemComponent notes = new File("notes.txt", 50);
        Directory documents = new Directory("documents");
        documents.add(notes);

        Directory root = new Directory("root");
        root.add(resume);
        root.add(photo);
        root.add(documents);

        System.out.println(root.getSize());
        root.print();
    }
}
