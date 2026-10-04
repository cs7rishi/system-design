package Structural.Adapter;

public class Client {
    public static void main(String[] args) {
        System.out.println("AdapterMain");

    LegacyPaymentGateway gateway = new LegacyPaymentGateway();
    PaymentProcessor processor = new LegacyPaymentAdapter(gateway, "CUSTOMER-123");

    processor.pay(1000);
    }
}
