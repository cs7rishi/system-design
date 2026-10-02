package Behavioural.State;

public class CreatedState implements OrderState {
    @Override
    public void pay(Order order) {
        System.out.println("Payment successful");
        order.setState(new PaidState());
    }

    @Override
    public void ship(Order order) {
        System.out.println("Cannot ship before payment");
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Order cancelled");
        order.setState(new CancelledState());
    }
}
