package Behavioural.Strategy;

public class StrategyMain {
    public static void main(String[] args) {
        PaymentStrategy paymentStrategy = new CreditCardPaymentStrategy();
        paymentStrategy.pay(100);

        paymentStrategy = new UpiPaymentStrategy();
        paymentStrategy.pay(100);

        paymentStrategy = new PaypalPaymentStrategy();
        paymentStrategy.pay(100);
    }
}
