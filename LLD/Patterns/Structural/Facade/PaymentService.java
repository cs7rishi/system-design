package Structural.Facade;

public class PaymentService {

    public void processPayment(
            String customerId,
            double amount
    ) {
        System.out.println(
                "Payment processed: " + amount
        );
    }
}
