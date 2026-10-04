package Structural.Decorator;

public class MilkDecorator extends CoffeeDecorator {
    protected MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription()
                + ", Milk";
    }

    @Override
    public double getCost() {
        return coffee.getCost()
                + 20.0;
    }
}
