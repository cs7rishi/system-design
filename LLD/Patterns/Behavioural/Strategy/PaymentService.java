package Behavioural.Strategy;

public class PaymentService {
    private final PaymentStrategy paymentStrategy;
    PaymentService(PaymentStrategy paymentStrategy){
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(double amount){
        this.paymentStrategy.pay(amount);
    }
}
