package Behavioural.Strategy;

public final class PaymentFactory {
    private PaymentFactory(){};
    public static PaymentStrategy getStrategy(PaymentType paymentType){
        switch (paymentType) {

            case CREDIT_CARD:
                return new CreditCardPaymentStrategy();

            case UPI:
                return new UpiPaymentStrategy();

            case PAYPAL:
                return new PaypalPaymentStrategy();

            default:
                throw new IllegalArgumentException(
                        "Unsupported payment type: " + paymentType
                );
        }
    }
}
