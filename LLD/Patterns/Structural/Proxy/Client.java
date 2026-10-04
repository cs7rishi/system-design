package Structural.Proxy;

public class Client {

    public static void main(String[] args) {

        Image image =
                new ImageProxy(
                        "photo.jpg"
                );

        System.out.println(
                "Proxy created"
        );

        image.display();

        System.out.println(
                "Displaying again"
        );

        image.display();
    }
}
