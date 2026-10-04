package Structural.Decorator;

public abstract class CoffeeDecorator implements Coffee{
    protected final Coffee coffee;

    protected CoffeeDecorator(Coffee coffee){
        this.coffee = coffee;
    }
}
