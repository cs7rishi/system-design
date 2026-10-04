package Structural.Bridge;

public class Circle extends Shape {
    protected Circle(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.println("Drawing Circle in " + color.applyColor());
    }
}
