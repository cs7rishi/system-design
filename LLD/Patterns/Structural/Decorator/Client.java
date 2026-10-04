package Structural.Decorator;

public class Client {
    public static void main(String[] args) {
        Coffee coffee =
                new SimpleCoffee();

        coffee =
                new MilkDecorator(coffee);

        coffee =
                new SugarDecorator(coffee);

        System.out.println(
                coffee.getDescription()
        );

        System.out.println(
                coffee.getCost()
        );
    }
}
