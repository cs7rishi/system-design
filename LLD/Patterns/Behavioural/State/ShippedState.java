package Behavioural.State;

public class ShippedState implements OrderState {
    @Override
    public void pay(Order order) {
        System.out.println("Order already paid and shipped");
    }

    @Override
    public void ship(Order order) {
        System.out.println("Order already shipped");
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Cannot cancelled a shipped Order");
    }
}
