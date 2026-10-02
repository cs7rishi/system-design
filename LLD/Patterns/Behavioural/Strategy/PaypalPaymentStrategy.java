package Behavioural.Strategy;

public class PaypalPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Processing paypal payment of " + amount);
    }
}
