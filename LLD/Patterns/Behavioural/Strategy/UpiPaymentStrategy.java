package Behavioural.Strategy;

public class UpiPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Processing UPI payment of " + amount);
    }
}
