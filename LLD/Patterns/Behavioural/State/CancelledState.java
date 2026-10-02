package Behavioural.State;

public class CancelledState implements OrderState {
    @Override
    public void pay(Order order) {
        System.out.println("Cannot pay a cancelled order");
    }

    @Override
    public void ship(Order order) {
        System.out.println("Cannot ship a cancelled order");
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Order is already cancelled");
    }
}
