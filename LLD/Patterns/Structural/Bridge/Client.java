package Structural.Bridge;

public class Client {
    public static void main(String[] args) {
        Shape redCircle = new Circle(new RedColor());
        Shape blueCircle = new Circle(new BlueColor());
        Shape redSquare = new Square(new RedColor());

        redCircle.draw();
        blueCircle.draw();
        redSquare.draw();
    }
}
