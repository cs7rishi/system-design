package Behavioural.State;

public class PaidState implements OrderState {
    @Override
    public void pay(Order order) {
        System.out.println("Order is already paid");
    }

    @Override
    public void ship(Order order) {
        System.out.println("Order shipped");
        order.setState(new ShippedState());
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Order cancelled");
        order.setState(new CancelledState());

    }
}
