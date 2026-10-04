package Structural.Bridge;

public class Square extends Shape {
    protected Square(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.println("Drawing Square in " + color.applyColor());
    }
}
