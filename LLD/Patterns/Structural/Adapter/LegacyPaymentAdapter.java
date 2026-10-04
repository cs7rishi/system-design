package Structural.Adapter;

public class LegacyPaymentAdapter implements PaymentProcessor {
    private final LegacyPaymentGateway legacyPaymentGateway;

    private final String customerId;

    LegacyPaymentAdapter(LegacyPaymentGateway legacyPaymentGateway, String customerId){
        this.legacyPaymentGateway = legacyPaymentGateway;
        this.customerId = customerId;
    }
    @Override
    public void pay(double amount) {
        legacyPaymentGateway.makePayment(customerId, amount);
    }
}
