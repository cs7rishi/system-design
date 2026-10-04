package Structural.Adapter;

public class LegacyPaymentGateway {
    public void makePayment(String customerId, double amount) {
        System.out.println(
                "Legacy gateway payment: "
                        + amount
                        + " for customer "
                        + customerId
        );
    }
}
