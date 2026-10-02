package Behavioural.State;

public interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void cancel(Order order);
}
